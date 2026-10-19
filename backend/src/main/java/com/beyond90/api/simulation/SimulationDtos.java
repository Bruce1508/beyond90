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

	/** {@code targetActorId} is required for PASS (the intended receiver) and ignored for SHOOT. */
	public record SimulationAction(ActionType type, String actorId, String targetActorId, Fixture.Point target) {}

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
}
