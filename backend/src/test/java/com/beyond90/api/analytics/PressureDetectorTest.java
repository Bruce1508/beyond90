package com.beyond90.api.analytics;

import com.beyond90.api.fixture.Fixture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Golden vectors per docs/04-FOOTBALL-ENGINES.md Detector D2 test list, modeled on the real M01 fixture. */
class PressureDetectorTest {

	private static Fixture.MatchEvent event(String id, int seq, long matchClockMs, String type, String teamId) {
		return new Fixture.MatchEvent(
				id, seq, "FIRST_HALF", matchClockMs, matchClockMs, type, teamId, "actor", null, null, null, null, null, "SYNTHETIC");
	}

	@Test
	void twoPressEventsPlusTurnover_detected() {
		// Mirrors examples/demo-001-mini-fixture.json M01: 2 red PRESSURE events, a red
		// INTERCEPTION, then the red POSSESSION_CHANGE, all within the window.
		var pressure1 = event("evt_press_1", 1, 1_310_000, "PRESSURE", "red");
		var pressure2 = event("evt_press_2", 2, 1_312_000, "PRESSURE", "red");
		var interception = event("evt_interception_1", 3, 1_316_000, "INTERCEPTION", "red");
		var turnover = event("evt_poss_1", 4, 1_316_200, "POSSESSION_CHANGE", "red");
		var events = List.of(pressure1, pressure2, interception, turnover);

		var result = PressureDetector.detect(events, turnover);

		assertThat(result.detected()).isTrue();
		assertThat(result.metricId()).isEqualTo(PressureDetector.METRIC_ID);
		assertThat(result.pressureEventCount()).isEqualTo(2);
		assertThat(result.evidenceIds())
				.containsExactly("evt_press_1", "evt_press_2", "evt_interception_1", "evt_poss_1");
		// The caveat explicitly disclaims causation ("NOT evidence that pressure caused the
		// turnover") rather than asserting it unconditionally.
		assertThat(result.caveat()).containsIgnoringCase("not evidence");
	}

	@Test
	void turnoverWithoutPressure_notDetected() {
		var interception = event("evt_interception_1", 1, 1_316_000, "INTERCEPTION", "red");
		var turnover = event("evt_poss_1", 2, 1_316_200, "POSSESSION_CHANGE", "red");
		var events = List.of(interception, turnover);

		var result = PressureDetector.detect(events, turnover);

		assertThat(result.detected()).isFalse();
		assertThat(result.pressureEventCount()).isZero();
	}

	@Test
	void latePressureAfterTurnover_notDetected() {
		// Pressure events fire AFTER the possession change already happened -> can't explain it.
		var turnover = event("evt_poss_1", 1, 1_316_200, "POSSESSION_CHANGE", "red");
		var lateress1 = event("evt_press_1", 2, 1_318_000, "PRESSURE", "red");
		var lateress2 = event("evt_press_2", 3, 1_320_000, "PRESSURE", "red");
		var events = List.of(turnover, lateress1, lateress2);

		var result = PressureDetector.detect(events, turnover);

		assertThat(result.detected()).isFalse();
		assertThat(result.pressureEventCount()).isZero();
	}

	@Test
	void crossTeamIdMismatch_notDetected() {
		// Pressure came from "blue", but "red" is the team that ends up in possession -> the
		// pressure events don't belong to the team the turnover is attributed to.
		var pressure1 = event("evt_press_1", 1, 1_310_000, "PRESSURE", "blue");
		var pressure2 = event("evt_press_2", 2, 1_312_000, "PRESSURE", "blue");
		var interception = event("evt_interception_1", 3, 1_316_000, "INTERCEPTION", "red");
		var turnover = event("evt_poss_1", 4, 1_316_200, "POSSESSION_CHANGE", "red");
		var events = List.of(pressure1, pressure2, interception, turnover);

		var result = PressureDetector.detect(events, turnover);

		assertThat(result.detected()).isFalse();
		assertThat(result.pressureEventCount()).isZero();
	}

	@Test
	void onlyOnePressureEvent_belowMinimum_notDetected() {
		var pressure1 = event("evt_press_1", 1, 1_310_000, "PRESSURE", "red");
		var interception = event("evt_interception_1", 2, 1_316_000, "INTERCEPTION", "red");
		var turnover = event("evt_poss_1", 3, 1_316_200, "POSSESSION_CHANGE", "red");
		var events = List.of(pressure1, interception, turnover);

		var result = PressureDetector.detect(events, turnover);

		assertThat(result.detected()).isFalse();
		assertThat(result.pressureEventCount()).isEqualTo(1);
	}

	@Test
	void pressureOutsideWindow_notCounted() {
		var pressure1 = event("evt_press_1", 1, 1_310_000, "PRESSURE", "red");
		var pressure2 = event("evt_press_2", 2, 1_312_000, "PRESSURE", "red");
		var turnover = event("evt_poss_1", 3, 1_316_200, "POSSESSION_CHANGE", "red");
		var interception = event("evt_interception_1", 4, 1_316_000, "INTERCEPTION", "red");
		var events = List.of(pressure1, pressure2, interception, turnover);

		// 1316200 - 1310000 = 6200ms gap; the minimum 5000ms window excludes the earliest
		// pressure event (1310000 < 1311200 window start) while the 8000ms default includes it.
		var result = PressureDetector.detect(events, turnover, 5000L);

		assertThat(result.detected()).isFalse();
		assertThat(result.pressureEventCount()).isEqualTo(1);
	}
}
