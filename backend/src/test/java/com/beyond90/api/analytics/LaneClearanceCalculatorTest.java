package com.beyond90.api.analytics;

import com.beyond90.api.fixture.Fixture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Golden vectors per docs/04-FOOTBALL-ENGINES.md Detector D1 test list. */
class LaneClearanceCalculatorTest {

	private static Fixture.Point point(double x, double y) {
		return new Fixture.Point(x, y);
	}

	private static Fixture.PlayerPose defender(String id, double x, double y) {
		return new Fixture.PlayerPose(id, x, y);
	}

	@Test
	void straightHorizontalSegment_perpendicularDefender() {
		var result = LaneClearanceCalculator.clearance(
				point(0, 0), point(10, 0), "snap-1", List.of(defender("d1", 5, 1)), "passer", "receiver");

		assertThat(result.clearanceMeters()).isEqualTo(1.0);
		assertThat(result.contested()).isTrue();
		assertThat(result.defenderCount()).isEqualTo(1);
		assertThat(result.contestingDefenderCount()).isEqualTo(1);
		assertThat(result.evidenceIds()).containsExactly("snap-1", "d1");
		assertThat(result.algorithmVersion()).isEqualTo(LaneClearanceCalculator.ALGORITHM_VERSION);
	}

	@Test
	void defenderBeyondEndpoint_projectionClampedToReceiver() {
		// t = 1.5 unclamped -> clamps to 1 (the receiver), so distance is from (15,0) to (10,0) = 5.
		var result = LaneClearanceCalculator.clearance(
				point(0, 0), point(10, 0), "snap-1", List.of(defender("d1", 15, 0)), "passer", "receiver");

		assertThat(result.clearanceMeters()).isEqualTo(5.0);
		assertThat(result.contested()).isFalse();
	}

	@Test
	void nearZeroLengthPass_fallsBackToDistanceFromPasser() {
		var result = LaneClearanceCalculator.clearance(
				point(5, 5), point(5, 5), "snap-1", List.of(defender("d1", 5, 8)), "passer", "receiver");

		assertThat(result.clearanceMeters()).isEqualTo(3.0);
	}

	@Test
	void noDefenders_clearanceIsUnboundedAndNotContested() {
		var result =
				LaneClearanceCalculator.clearance(point(0, 0), point(10, 0), "snap-1", List.of(), "passer", "receiver");

		assertThat(result.clearanceMeters()).isEqualTo(Double.POSITIVE_INFINITY);
		assertThat(result.contested()).isFalse();
		assertThat(result.defenderCount()).isZero();
		assertThat(result.evidenceIds()).containsExactly("snap-1");
	}

	@Test
	void changingAttackDirection_clearanceInvariantUnderReversedSegment() {
		// The calculator is purely geometric on x/y; reversing passer<->receiver (as happens when
		// attack direction flips between halves) must not change the perpendicular distance.
		var forward = LaneClearanceCalculator.clearance(
				point(0, 0), point(10, 0), "snap-1", List.of(defender("d1", 5, 2)), "passer", "receiver");
		var reversed = LaneClearanceCalculator.clearance(
				point(10, 0), point(0, 0), "snap-1", List.of(defender("d1", 5, 2)), "receiver", "passer");

		assertThat(reversed.clearanceMeters()).isEqualTo(forward.clearanceMeters());
	}

	@Test
	void passerAndReceiverExcludedFromDefenderConsideration() {
		var result = LaneClearanceCalculator.clearance(
				point(0, 0),
				point(10, 0),
				"snap-1",
				List.of(defender("passer", 0, 0), defender("receiver", 10, 0), defender("d1", 5, 2)),
				"passer",
				"receiver");

		assertThat(result.defenderCount()).isEqualTo(1);
		assertThat(result.evidenceIds()).containsExactly("snap-1", "d1");
	}
}
