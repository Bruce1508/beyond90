package com.beyond90.api.xray;

import com.beyond90.api.analytics.PressureDetector;
import com.beyond90.api.fixture.Fixture;
import com.beyond90.api.fixture.FixtureRepository;
import com.beyond90.api.xray.XRayDtos.GeometryOverlayDto;
import com.beyond90.api.xray.XRayDtos.InsightMetricDto;
import com.beyond90.api.xray.XRayDtos.PointDto;
import com.beyond90.api.xray.XRayDtos.XRayResponseDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * Builds contracts/openapi.yaml's XRayResponse for a moment. Only wires Detector D2 (pressure,
 * docs/04-FOOTBALL-ENGINES.md) so far: D1 (lane clearance) needs a PASS event with a recorded
 * passer/receiver pair, which no currently-authored moment has (M01 is a pressure/interception
 * moment with no pass attempt) — wiring it now would mean fabricating an evidence basis that
 * isn't in the fixture. It lands with the M02/M03 fixtures in P0-11.
 *
 * <p>Confidence is derived deterministically from whether a derivable insight was actually
 * evidenced, per docs/04-FOOTBALL-ENGINES.md §B (never from a model's self-reported probability):
 * {@code SUPPORTED} when a detector fires with full evidence, {@code PARTIAL} when a candidate
 * turnover exists but the detector's own thresholds aren't met, {@code INSUFFICIENT} when no
 * qualifying turnover event exists in the moment at all.
 */
@Service
public class XRayService {

	private final FixtureRepository fixtureRepository;

	public XRayService(FixtureRepository fixtureRepository) {
		this.fixtureRepository = fixtureRepository;
	}

	public XRayResponseDto buildXRay(String momentId) {
		Fixture fixture =
				fixtureRepository.findFixtureContainingMoment(momentId).orElseThrow(() -> new MomentNotFoundException(momentId));
		Fixture.Moment moment = fixture.moments().stream()
				.filter(m -> m.momentId().equals(momentId))
				.findFirst()
				.orElseThrow(() -> new MomentNotFoundException(momentId));

		List<Fixture.MatchEvent> allEvents = fixtureRepository.orderedEvents(fixture);
		List<Fixture.MatchEvent> momentEvents =
				allEvents.stream().filter(e -> moment.eventIds().contains(e.eventId())).toList();

		Optional<Fixture.MatchEvent> turnover =
				momentEvents.stream().filter(e -> "POSSESSION_CHANGE".equals(e.type())).findFirst();

		List<InsightMetricDto> metrics = new ArrayList<>();
		List<GeometryOverlayDto> geometry = new ArrayList<>();
		List<String> caveats = new ArrayList<>();
		String confidence;

		if (turnover.isEmpty()) {
			confidence = "INSUFFICIENT";
			caveats.add("No turnover event in this moment; no pressure-association insight could be derived.");
		} else {
			var pressureResult = PressureDetector.detect(allEvents, turnover.get());
			if (pressureResult.detected()) {
				confidence = "SUPPORTED";
			} else {
				confidence = "PARTIAL";
			}
			metrics.add(new InsightMetricDto(
					pressureResult.metricId(),
					pressureResult.pressureEventCount(),
					"count",
					pressureResult.evidenceIds(),
					pressureResult.algorithmVersion()));
			caveats.add(pressureResult.caveat());

			List<Fixture.MatchEvent> pressureEvents =
					momentEvents.stream().filter(e -> "PRESSURE".equals(e.type())).toList();
			if (!pressureEvents.isEmpty()) {
				geometry.add(new GeometryOverlayDto(
						"LINE",
						pressureEvents.stream().map(e -> new PointDto(e.location().x(), e.location().y())).toList(),
						pressureEvents.stream().map(Fixture.MatchEvent::eventId).toList()));
			}
		}

		List<String> evidenceIds = new LinkedHashSet<>(
				metrics.stream().flatMap(m -> m.evidenceIds().stream()).toList())
				.stream()
				.toList();

		return new XRayResponseDto(
				"insight-" + momentId, momentId, fixture.scenarioVersion(), confidence, metrics, geometry, evidenceIds, caveats);
	}
}
