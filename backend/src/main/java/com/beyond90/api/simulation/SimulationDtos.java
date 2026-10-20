package com.beyond90.api.simulation;

import com.beyond90.api.fixture.Fixture;

import java.util.List;

/** Request/result shapes for {@link BranchSimulator} per docs/04-FOOTBALL-ENGINES.md section C. */
public final class SimulationDtos {

	private SimulationDtos() {}

	public enum ActionType {
		PASS,
		SHOOT
	}

	/** Categorical, simulator-specific outcome — never a claim about the real match. */
	public enum Outcome {
		COMPLETED,
		INTERCEPTED,
		ON_TARGET,
		BLOCKED,
		MISS
	}

	/** {@code target} is a pitch-metres destination only — contracts/openapi.yaml's SimulateRequest has no receiver player ID. */
	public record SimulationAction(ActionType type, String actorId, Fixture.Point target) {}

	public record TrajectoryPointDto(long tMs, double ballX, double ballY, double ballZ) {}

	public record SimulationResult(
			ActionType action,
			long seed,
			String engineVersion,
			List<TrajectoryPointDto> trajectory,
			double pathLengthMeters,
			double nearestDefenderMeters,
			boolean reachedTarget,
			boolean shotOnTarget,
			Outcome outcome) {}

	// --- Wire shapes for contracts/openapi.yaml SimulateRequest/BranchResult ---

	public record PointDto(double x, double y) {}

	/** {@code action} is a raw string so an unsupported value (e.g. CARRY, P1) can be rejected as 422 rather than failing JSON binding. */
	public record SimulateRequestDto(String scenarioVersion, String action, PointDto target, long seed) {}

	public record BranchFrameDto(long tMs, PointDto ball, double ballHeightMeters) {}

	public record BranchResultDto(
			String branchId,
			String momentId,
			String baseSnapshotId,
			boolean originalUnchanged,
			boolean simulated,
			String engineVersion,
			long seed,
			List<BranchFrameDto> frames,
			List<String> caveats) {}
}
