package com.beyond90.api.xray;

import com.beyond90.api.fixture.FixtureRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XRayServiceTest {

	private final FixtureRepository fixtureRepository = new FixtureRepository();
	private final XRayService xRayService = new XRayService(fixtureRepository);

	@Test
	void m01IsSupportedWithEvidenceFromTheRealPressureAndTurnoverEvents() {
		var response = xRayService.buildXRay("m01");

		assertThat(response.momentId()).isEqualTo("m01");
		assertThat(response.confidence()).isEqualTo("SUPPORTED");
		assertThat(response.metrics()).hasSize(1);
		assertThat(response.metrics().get(0).metricId()).isEqualTo("PRESSURE_ASSOCIATED_TURNOVER");
		assertThat(response.metrics().get(0).algorithmVersion()).isEqualTo("D2.v1");
		assertThat(response.caveats()).anySatisfy(c -> assertThat(c).containsIgnoringCase("not evidence"));
	}

	@Test
	void allReferencedEvidenceIdsResolveToRealEventsInTheFixture() {
		var fixture = fixtureRepository.findByMatchId("demo-001").orElseThrow();
		Set<String> realEventIds = fixture.events().stream().map(e -> e.eventId()).collect(Collectors.toSet());

		var response = xRayService.buildXRay("m01");

		// AC: "all referenced IDs resolve" -- every evidenceId at every level must be a real event.
		assertThat(response.evidenceIds()).isNotEmpty().allSatisfy(id -> assertThat(realEventIds).contains(id));
		response.metrics()
				.forEach(metric -> metric.evidenceIds().forEach(id -> assertThat(realEventIds).contains(id)));
		response.geometry()
				.forEach(overlay -> overlay.evidenceIds().forEach(id -> assertThat(realEventIds).contains(id)));
	}

	@Test
	void geometryOverlayTracesBackToThePressureEvents() {
		var response = xRayService.buildXRay("m01");

		assertThat(response.geometry()).hasSize(1);
		assertThat(response.geometry().get(0).type()).isEqualTo("LINE");
		assertThat(response.geometry().get(0).evidenceIds()).containsExactly("evt_press_1", "evt_press_2");
	}

	@Test
	void unknownMomentThrowsNotFound() {
		assertThatThrownBy(() -> xRayService.buildXRay("does-not-exist")).isInstanceOf(MomentNotFoundException.class);
	}
}
