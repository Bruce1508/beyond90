package com.beyond90.api.simulation;

import com.beyond90.api.fixture.Fixture;
import com.beyond90.api.simulation.SimulationDtos.ActionType;
import com.beyond90.api.simulation.SimulationDtos.Outcome;
import com.beyond90.api.simulation.SimulationDtos.SimulationAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Golden vectors per docs/04-FOOTBALL-ENGINES.md section C test list. */
class BranchSimulatorTest {

	private static final Fixture.Pitch PITCH = new Fixture.Pitch(105, 68);
	private static final String ATTACK_LEFT_TO_RIGHT = "LEFT_TO_RIGHT";

	private static Fixture.Point point(double x, double y) {
		return new Fixture.Point(x, y);
	}

	private static Fixture.PlayerPose pose(String id, double x, double y) {
		return new Fixture.PlayerPose(id, x, y);
	}

	private static Fixture.TrackingSnapshot snapshot(double ballX, double ballY, Fixture.PlayerPose... poses) {
		return new Fixture.TrackingSnapshot("snap-1", 0, new Fixture.BallPosition(ballX, ballY, 0.1), List.of(poses));
	}

	@Test
	void sameSnapshotSeedAction_producesIdenticalTrajectory() {
		var snapshot = snapshot(20, 34, pose("actor", 20, 34));
		var action = new SimulationAction(ActionType.PASS, "actor", point(35, 34));

		var result1 = BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(), action, 42L);
		var result2 = BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(), action, 42L);

		assertThat(result1).isEqualTo(result2);
	}

	@Test
	void passWithDefenderOnLine_intercepted() {
		var snapshot = snapshot(20, 34, pose("actor", 20, 34));
		var defender = pose("def-1", 28, 34); // sits exactly on the straight pass line
		var action = new SimulationAction(ActionType.PASS, "actor", point(35, 34));

		var result = BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(defender), action, 1L);

		assertThat(result.outcome()).isEqualTo(Outcome.INTERCEPTED);
		assertThat(result.reachedTarget()).isFalse();
		assertThat(result.nearestDefenderMeters()).isEqualTo(0.0, org.assertj.core.data.Offset.offset(1e-9));
		assertThat(result.pathLengthMeters()).isEqualTo(8.0, org.assertj.core.data.Offset.offset(1e-9)); // 8/15 of the 15m pass
	}

	@Test
	void passWithDefenderWellClear_completed() {
		var snapshot = snapshot(20, 34, pose("actor", 20, 34));
		var defender = pose("def-1", 28, 50); // 16m off the line, well outside the collision radius
		var action = new SimulationAction(ActionType.PASS, "actor", point(35, 34));

		var result = BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(defender), action, 1L);

		assertThat(result.outcome()).isEqualTo(Outcome.COMPLETED);
		assertThat(result.reachedTarget()).isTrue();
		assertThat(result.pathLengthMeters()).isEqualTo(15.0, org.assertj.core.data.Offset.offset(1e-9));
	}

	@Test
	void shootInsideGoalMouth_onTarget() {
		var snapshot = snapshot(95, 34, pose("shooter", 95, 34));
		var action = new SimulationAction(ActionType.SHOOT, "shooter", point(105, 34));

		var result = BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(), action, 1L);

		assertThat(result.outcome()).isEqualTo(Outcome.ON_TARGET);
		assertThat(result.shotOnTarget()).isTrue();
		assertThat(result.reachedTarget()).isTrue();
	}

	@Test
	void shootOutsideGoalRectangle_miss() {
		var snapshot = snapshot(95, 34, pose("shooter", 95, 34));
		var action = new SimulationAction(ActionType.SHOOT, "shooter", point(105, 50));

		var result = BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(), action, 1L);

		assertThat(result.outcome()).isEqualTo(Outcome.MISS);
		assertThat(result.shotOnTarget()).isFalse();
	}

	@Test
	void shootBlockedByDefenderOnPath_blocked() {
		var snapshot = snapshot(95, 34, pose("shooter", 95, 34));
		var defender = pose("def-1", 100, 34); // sits exactly on the shot line
		var action = new SimulationAction(ActionType.SHOOT, "shooter", point(105, 34));

		var result = BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(defender), action, 1L);

		assertThat(result.outcome()).isEqualTo(Outcome.BLOCKED);
		assertThat(result.shotOnTarget()).isFalse();
		assertThat(result.reachedTarget()).isFalse();
	}

	@Test
	void actorNotNearBall_rejected() {
		var snapshot = snapshot(20, 34, pose("actor", 50, 50));
		var action = new SimulationAction(ActionType.PASS, "actor", point(35, 34));

		assertThatThrownBy(() -> BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(), action, 1L))
				.isInstanceOf(InvalidActionException.class)
				.hasMessageContaining("does not possess the ball");
	}

	@Test
	void passTargetingOwnPosition_rejected() {
		var snapshot = snapshot(20, 34, pose("actor", 20, 34));
		var action = new SimulationAction(ActionType.PASS, "actor", point(21, 34)); // 1m away, within POSSESSION_RADIUS

		assertThatThrownBy(() -> BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(), action, 1L))
				.isInstanceOf(InvalidActionException.class)
				.hasMessageContaining("meaningfully different");
	}

	@Test
	void shootTargetNotOnGoalLine_rejected() {
		var snapshot = snapshot(95, 34, pose("shooter", 95, 34));
		var action = new SimulationAction(ActionType.SHOOT, "shooter", point(100, 34));

		assertThatThrownBy(() -> BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(), action, 1L))
				.isInstanceOf(InvalidActionException.class)
				.hasMessageContaining("goal line");
	}

	@Test
	void destinationOutsidePitch_rejected() {
		var snapshot = snapshot(20, 34, pose("actor", 20, 34));
		var action = new SimulationAction(ActionType.PASS, "actor", point(35, 200));

		assertThatThrownBy(() -> BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(), action, 1L))
				.isInstanceOf(InvalidActionException.class)
				.hasMessageContaining("out of pitch bounds");
	}

	@Test
	void trajectorySampledAtFixedTimestep() {
		var snapshot = snapshot(20, 34, pose("actor", 20, 34));
		var action = new SimulationAction(ActionType.PASS, "actor", point(35, 34));

		var result = BranchSimulator.simulate(PITCH, ATTACK_LEFT_TO_RIGHT, snapshot, List.of(), action, 1L);

		assertThat(result.trajectory().get(0).tMs()).isZero();
		for (int i = 1; i < result.trajectory().size() - 1; i++) {
			long delta = result.trajectory().get(i).tMs() - result.trajectory().get(i - 1).tMs();
			assertThat(delta).isEqualTo(BranchSimulator.DT_MS);
		}
		assertThat(result.engineVersion()).isEqualTo(BranchSimulator.ENGINE_VERSION);
		assertThat(result.seed()).isEqualTo(1L);
	}
}
