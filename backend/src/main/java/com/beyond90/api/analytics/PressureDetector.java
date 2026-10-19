package com.beyond90.api.analytics;

import com.beyond90.api.fixture.Fixture;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Detector D2 (docs/04-FOOTBALL-ENGINES.md): pressure-associated turnover, a curated-scenario
 * detector. Requires, within a 5-10s window before a {@code POSSESSION_CHANGE}: (1) at least two
 * {@code PRESSURE} events from the team that gains possession, and (2) that turnover be linked to
 * an {@code INTERCEPTION} or a {@code PASS} by that same team within the window. Emits
 * {@code PRESSURE_ASSOCIATED_TURNOVER} — an association, never a causal claim.
 */
public final class PressureDetector {

	public static final String METRIC_ID = "PRESSURE_ASSOCIATED_TURNOVER";
	public static final String ALGORITHM_VERSION = "D2.v1";
	public static final long DEFAULT_WINDOW_MS = 8000; // within the doc's required 5-10s range
	public static final int MIN_PRESSURE_EVENTS = 2;
	public static final String CAVEAT =
			"Association only: pressure events are temporally associated with the turnover window; "
					+ "this is NOT evidence that pressure caused the turnover.";

	private PressureDetector() {}

	public static PressureDetectionResult detect(List<Fixture.MatchEvent> events, Fixture.MatchEvent turnoverEvent) {
		return detect(events, turnoverEvent, DEFAULT_WINDOW_MS);
	}

	public static PressureDetectionResult detect(
			List<Fixture.MatchEvent> events, Fixture.MatchEvent turnoverEvent, long windowMs) {
		String possessingTeamId = turnoverEvent.teamId();
		long windowStartMs = turnoverEvent.matchClockMs() - windowMs;

		List<Fixture.MatchEvent> pressureEventsInWindow = events.stream()
				.filter(e -> "PRESSURE".equals(e.type()))
				.filter(e -> possessingTeamId.equals(e.teamId()))
				.filter(e -> e.matchClockMs() >= windowStartMs && e.matchClockMs() < turnoverEvent.matchClockMs())
				.toList();

		Optional<Fixture.MatchEvent> linkedEvent = events.stream()
				.filter(e -> "INTERCEPTION".equals(e.type()) || "PASS".equals(e.type()))
				.filter(e -> possessingTeamId.equals(e.teamId()))
				.filter(e -> e.matchClockMs() >= windowStartMs && e.matchClockMs() <= turnoverEvent.matchClockMs())
				.max(Comparator.comparingLong(Fixture.MatchEvent::matchClockMs));

		boolean detected = pressureEventsInWindow.size() >= MIN_PRESSURE_EVENTS && linkedEvent.isPresent();

		List<String> evidenceIds = new ArrayList<>();
		pressureEventsInWindow.forEach(e -> evidenceIds.add(e.eventId()));
		linkedEvent.ifPresent(e -> evidenceIds.add(e.eventId()));
		evidenceIds.add(turnoverEvent.eventId());

		return new PressureDetectionResult(
				METRIC_ID, detected, List.copyOf(evidenceIds), ALGORITHM_VERSION, windowMs, pressureEventsInWindow.size(), CAVEAT);
	}

	/** Evidence-carrying result per contracts/openapi.yaml InsightMetric: evidenceIds + algorithmVersion required. */
	public record PressureDetectionResult(
			String metricId,
			boolean detected,
			List<String> evidenceIds,
			String algorithmVersion,
			long windowMs,
			int pressureEventCount,
			String caveat) {}
}
