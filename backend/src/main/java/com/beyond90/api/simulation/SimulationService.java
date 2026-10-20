package com.beyond90.api.simulation;

import com.beyond90.api.fixture.Fixture;
import com.beyond90.api.fixture.FixtureRepository;
import com.beyond90.api.simulation.SimulationDtos.ActionType;
import com.beyond90.api.simulation.SimulationDtos.BranchFrameDto;
import com.beyond90.api.simulation.SimulationDtos.BranchResultDto;
import com.beyond90.api.simulation.SimulationDtos.PointDto;
import com.beyond90.api.simulation.SimulationDtos.SimulateRequestDto;
import com.beyond90.api.simulation.SimulationDtos.SimulationAction;
import com.beyond90.api.simulation.SimulationDtos.SimulationResult;
import com.beyond90.api.xray.MomentNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements contracts/openapi.yaml's POST /api/v1/moments/{momentId}/simulate, wiring the pure
 * {@link BranchSimulator} (P0-13) to a real moment's fixture data.
 */
@Service
public class SimulationService {

	private final FixtureRepository fixtureRepository;
	// ponytail: unbounded in-memory idempotency cache, fine for this single-process synthetic
	// demo; add TTL/eviction if this ever serves real concurrent traffic.
	private final Map<String, BranchResultDto> idempotencyCache = new ConcurrentHashMap<>();

	public SimulationService(FixtureRepository fixtureRepository) {
		this.fixtureRepository = fixtureRepository;
	}

	public BranchResultDto simulate(String momentId, SimulateRequestDto request, String idempotencyKey) {
		String cacheKey = idempotencyKey == null ? null : momentId + "|" + idempotencyKey;
		if (cacheKey != null) {
			BranchResultDto cached = idempotencyCache.get(cacheKey);
			if (cached != null) {
				return cached;
			}
		}

		Fixture fixture =
				fixtureRepository.findFixtureContainingMoment(momentId).orElseThrow(() -> new MomentNotFoundException(momentId));
		Fixture.Moment moment = fixture.moments().stream()
				.filter(m -> m.momentId().equals(momentId))
				.findFirst()
				.orElseThrow(() -> new MomentNotFoundException(momentId));

		if (!request.scenarioVersion().equals(fixture.scenarioVersion())) {
			throw new InvalidActionException("scenarioVersion does not match the current fixture; reload the moment and retry");
		}
		if (moment.actorId() == null || moment.baseSnapshotId() == null) {
			throw new InvalidActionException("Moment " + momentId + " has no actor or base snapshot to branch from");
		}
		if (!moment.availableActions().contains(request.action())) {
			throw new InvalidActionException("Action " + request.action() + " is not available for moment " + momentId);
		}
		ActionType actionType = parseActionType(request.action());
		if (request.target() == null) {
			throw new InvalidActionException(request.action() + " requires a target");
		}

		Fixture.TrackingSnapshot baseSnapshot = fixture.snapshots().stream()
				.filter(s -> s.snapshotId().equals(moment.baseSnapshotId()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Moment " + momentId + " references missing snapshot " + moment.baseSnapshotId()));

		Map<String, String> teamByPlayerId =
				fixture.players().stream().collect(java.util.stream.Collectors.toMap(Fixture.Player::playerId, Fixture.Player::teamId));
		String actorTeamId = teamByPlayerId.get(moment.actorId());

		Fixture.Period period = fixture.periods().stream()
				.filter(p -> p.startMs() <= moment.startMs())
				.max(Comparator.comparingLong(Fixture.Period::startMs))
				.orElseThrow(() -> new IllegalStateException("No period covers moment " + momentId + " at startMs=" + moment.startMs()));
		String attackDirection = period.attackDirectionByTeam().get(actorTeamId);

		List<Fixture.PlayerPose> defenders = baseSnapshot.playerPoses().stream()
				.filter(p -> !actorTeamId.equals(teamByPlayerId.get(p.playerId())))
				.toList();

		SimulationAction action =
				new SimulationAction(actionType, moment.actorId(), new Fixture.Point(request.target().x(), request.target().y()));
		SimulationResult result = BranchSimulator.simulate(fixture.pitch(), attackDirection, baseSnapshot, defenders, action, request.seed());

		BranchResultDto branchResult = toBranchResultDto(momentId, moment.baseSnapshotId(), result);
		if (cacheKey != null) {
			idempotencyCache.put(cacheKey, branchResult);
		}
		return branchResult;
	}

	private static ActionType parseActionType(String rawAction) {
		if ("PASS".equals(rawAction)) {
			return ActionType.PASS;
		}
		if ("SHOOT".equals(rawAction)) {
			return ActionType.SHOOT;
		}
		// CARRY is a declared P1 action in the contract enum; P0 has no engine support for it.
		throw new InvalidActionException("Action " + rawAction + " is not yet supported by the simulator");
	}

	private static BranchResultDto toBranchResultDto(String momentId, String baseSnapshotId, SimulationResult result) {
		List<BranchFrameDto> frames = result.trajectory().stream()
				.map(p -> new BranchFrameDto(p.tMs(), new PointDto(p.ballX(), p.ballY()), p.ballZ()))
				.toList();

		List<String> caveats = new ArrayList<>();
		caveats.add(
				"Simulated branch: synthetic kinematics only, not validated real-world physics or probability. "
						+ "Unmodeled: fatigue, intent, goalkeeper anticipation, teammates' unseen movement, true tracking error.");
		caveats.add(String.format(
				"Outcome: %s. Nearest defender approach: %.1fm. Path length: %.1fm.",
				result.outcome(), result.nearestDefenderMeters(), result.pathLengthMeters()));

		return new BranchResultDto(
				"branch-" + UUID.randomUUID(),
				momentId,
				baseSnapshotId,
				true,
				true,
				result.engineVersion(),
				result.seed(),
				frames,
				List.copyOf(caveats));
	}
}
