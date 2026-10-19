package com.beyond90.api.simulation;

import com.beyond90.api.fixture.Fixture;
import com.beyond90.api.simulation.SimulationDtos.ActionType;
import com.beyond90.api.simulation.SimulationDtos.Outcome;
import com.beyond90.api.simulation.SimulationDtos.SimulationAction;
import com.beyond90.api.simulation.SimulationDtos.SimulationResult;
import com.beyond90.api.simulation.SimulationDtos.TrajectoryPointDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Bounded Multiverse simulation (docs/04-FOOTBALL-ENGINES.md section C): deterministic, timestep
 * kinematics for a PASS or SHOOT branch off an observed base snapshot. Not a physics or ML model —
 * synthetic, disclosed, reproducible geometry only. A counterfactual is never ground truth.
 */
public final class BranchSimulator {

	public static final String ENGINE_VERSION = "SIM.v1";
	public static final long DT_MS = 100;
	public static final long MAX_DURATION_MS = 8000;
	public static final double PASS_SPEED_METERS_PER_SECOND = 18.0;
	public static final double SHOOT_SPEED_METERS_PER_SECOND = 26.0;
	public static final double COLLISION_RADIUS_METERS = 1.0;
	public static final double POSSESSION_RADIUS_METERS = 3.0;
	public static final double GOAL_WIDTH_METERS = 7.32;
	public static final double GOAL_LINE_TOLERANCE_METERS = 0.5;
	// ponytail: visual-only arc height for a shot's z, not a real ballistic model.
	public static final double SHOT_ARC_PEAK_METERS = 1.5;

	private BranchSimulator() {}

	public static SimulationResult simulate(
			Fixture.Pitch pitch,
			String attackDirection,
			Fixture.TrackingSnapshot baseSnapshot,
			List<Fixture.PlayerPose> defenders,
			SimulationAction action,
			long seed) {
		validate(pitch, attackDirection, baseSnapshot, action);

		Fixture.BallPosition ball = baseSnapshot.ball();
		Fixture.Point target = action.target();
		double dx = target.x() - ball.x();
		double dy = target.y() - ball.y();
		double fullDistance = Math.hypot(dx, dy);
		double speed = action.type() == ActionType.PASS ? PASS_SPEED_METERS_PER_SECOND : SHOOT_SPEED_METERS_PER_SECOND;
		long fullDurationMs = fullDistance == 0 ? 0 : Math.round(fullDistance / speed * 1000);
		long cappedFullDurationMs = Math.min(fullDurationMs, MAX_DURATION_MS);

		List<Fixture.PlayerPose> considered = defenders.stream()
				.filter(d -> !d.playerId().equals(action.actorId()))
				.filter(d -> action.targetActorId() == null || !d.playerId().equals(action.targetActorId()))
				.toList();

		double nearestDefenderMeters = Double.POSITIVE_INFINITY;
		double collisionFraction = Double.NaN;
		for (Fixture.PlayerPose defender : considered) {
			double t = fullDistance == 0 ? 0 : ((defender.x() - ball.x()) * dx + (defender.y() - ball.y()) * dy) / (fullDistance * fullDistance);
			t = Math.max(0, Math.min(1, t));
			double closestX = ball.x() + t * dx;
			double closestY = ball.y() + t * dy;
			double distance = Math.hypot(defender.x() - closestX, defender.y() - closestY);
			nearestDefenderMeters = Math.min(nearestDefenderMeters, distance);
			if (distance < COLLISION_RADIUS_METERS && (Double.isNaN(collisionFraction) || t < collisionFraction)) {
				collisionFraction = t;
			}
		}

		double effectiveFraction = Double.isNaN(collisionFraction) ? 1.0 : collisionFraction;
		long effectiveDurationMs = Math.round(effectiveFraction * cappedFullDurationMs);
		double pathLengthMeters = effectiveFraction * fullDistance;
		boolean reachedTarget = Double.isNaN(collisionFraction);

		List<TrajectoryPointDto> trajectory = new ArrayList<>();
		for (long t = 0; t < effectiveDurationMs; t += DT_MS) {
			double frac = cappedFullDurationMs == 0 ? 0 : (double) t / cappedFullDurationMs;
			trajectory.add(point(t, frac, ball, dx, dy, action.type()));
		}
		trajectory.add(point(effectiveDurationMs, effectiveFraction, ball, dx, dy, action.type()));

		boolean shotOnTarget = false;
		Outcome outcome;
		if (action.type() == ActionType.PASS) {
			outcome = reachedTarget ? Outcome.COMPLETED : Outcome.INTERCEPTED;
		} else {
			double goalWidthHalf = GOAL_WIDTH_METERS / 2;
			double pitchMidY = pitch.widthMeters() / 2.0;
			boolean withinGoal = target.y() >= pitchMidY - goalWidthHalf && target.y() <= pitchMidY + goalWidthHalf;
			boolean blocked = !reachedTarget;
			shotOnTarget = !blocked && withinGoal;
			outcome = blocked ? Outcome.BLOCKED : (withinGoal ? Outcome.ON_TARGET : Outcome.MISS);
		}

		return new SimulationResult(
				action.type(),
				seed,
				ENGINE_VERSION,
				List.copyOf(trajectory),
				pathLengthMeters,
				nearestDefenderMeters,
				reachedTarget,
				shotOnTarget,
				outcome);
	}

	private static TrajectoryPointDto point(long tMs, double frac, Fixture.BallPosition ball, double dx, double dy, ActionType type) {
		double x = ball.x() + frac * dx;
		double y = ball.y() + frac * dy;
		double z = type == ActionType.SHOOT ? ball.z() + SHOT_ARC_PEAK_METERS * Math.sin(Math.PI * frac) : ball.z();
		return new TrajectoryPointDto(tMs, x, y, z);
	}

	/** ActionValidator (docs/04-FOOTBALL-ENGINES.md section C, pipeline step 1). */
	private static void validate(Fixture.Pitch pitch, String attackDirection, Fixture.TrackingSnapshot baseSnapshot, SimulationAction action) {
		Fixture.PlayerPose actorPose = findPose(baseSnapshot, action.actorId())
				.orElseThrow(() -> new InvalidActionException("Actor not found in base snapshot: " + action.actorId()));
		double distanceToBall = Math.hypot(actorPose.x() - baseSnapshot.ball().x(), actorPose.y() - baseSnapshot.ball().y());
		if (distanceToBall > POSSESSION_RADIUS_METERS) {
			throw new InvalidActionException("Actor does not possess the ball: " + action.actorId());
		}
		if (action.type() == ActionType.PASS) {
			if (action.targetActorId() == null || action.targetActorId().equals(action.actorId())) {
				throw new InvalidActionException("PASS target must be a different player than the actor");
			}
			findPose(baseSnapshot, action.targetActorId())
					.orElseThrow(() -> new InvalidActionException("PASS target player not found: " + action.targetActorId()));
		}
		Fixture.Point target = action.target();
		if (target.x() < 0 || target.x() > pitch.lengthMeters() || target.y() < 0 || target.y() > pitch.widthMeters()) {
			throw new InvalidActionException("Target destination is out of pitch bounds");
		}
		if (action.type() == ActionType.SHOOT) {
			double goalLineX = "LEFT_TO_RIGHT".equals(attackDirection) ? pitch.lengthMeters() : 0;
			if (Math.abs(target.x() - goalLineX) > GOAL_LINE_TOLERANCE_METERS) {
				throw new InvalidActionException("SHOOT target must be on the attacking goal line");
			}
		}
	}

	private static Optional<Fixture.PlayerPose> findPose(Fixture.TrackingSnapshot snapshot, String playerId) {
		return snapshot.playerPoses().stream().filter(p -> p.playerId().equals(playerId)).findFirst();
	}
}
