package com.beyond90.api.fixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P0-11: validates the two new moments authored alongside M01 in
 * examples/demo-001-mini-fixture.json -- M02 (off-ball space creation, Detector D3: a
 * snapshot-only moment with no discrete logged event, the fixture's own "missingness" case)
 * and M03 (a Multiverse decision-fork scene: PASS/SHOOT available, 22-player scene,
 * no committed event yet since the decision is still pending). AC: three demonstrable
 * moments; 22-player scene where required.
 */
class DemoFixtureM02M03Test {

	private static final String FIXTURE_PATH = "examples/demo-001-mini-fixture.json";

	private final FixtureSchemaValidator schemaValidator = new FixtureSchemaValidator();
	private final FixtureIntegrityValidator integrityValidator = new FixtureIntegrityValidator();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void fixtureHasThreeDemonstrableMoments() throws IOException {
		JsonNode fixture = readFixture();

		List<String> momentIds = fixture.path("moments").findValuesAsText("momentId");

		assertThat(momentIds).containsExactly("m01", "m02", "m03");
	}

	@Test
	void m02IsAMissingnessCase_snapshotOnlyWithNoDiscreteEvent() throws IOException {
		JsonNode fixture = readFixture();
		JsonNode m02 = momentById(fixture, "m02");

		// Off-ball space creation (Detector D3) has no logged event type of its own -- it's
		// derived purely from the before/after snapshot pair. An empty eventIds array here is a
		// deliberate, valid "missingness" shape, not an authoring gap.
		assertThat(m02.path("eventIds")).isEmpty();
		assertThat(stringArray(m02.path("snapshotIds"))).containsExactly("snap_m02_a", "snap_m02_b");
		assertThat(m02.path("availableActions")).isEmpty();
	}

	@Test
	void m03IsALabeledDecisionForkWithTwentyTwoPlayerScene() throws IOException {
		JsonNode fixture = readFixture();
		JsonNode m03 = momentById(fixture, "m03");

		assertThat(stringArray(m03.path("availableActions"))).containsExactlyInAnyOrder("PASS", "SHOOT");
		assertThat(m03.path("actorId").asText()).isEqualTo("blue_07");
		assertThat(m03.path("baseSnapshotId").asText()).isEqualTo("snap_m03_a");
		// Decision is still pending (no committed real event yet) -- another valid missingness shape.
		assertThat(m03.path("eventIds")).isEmpty();

		JsonNode snapshot = snapshotById(fixture, "snap_m03_a");
		assertThat(snapshot.path("playerPoses")).hasSize(22);
	}

	@Test
	void m02AndM03AreSchemaValidAndReferentiallyConsistent() throws IOException {
		assertThat(schemaValidator.validate(readFixtureJson())).isEmpty();
		assertThat(integrityValidator.validate(readFixture())).isEmpty();
	}

	@Test
	void fixtureStaysLabeledSyntheticAcrossAllThreeMoments() throws IOException {
		// Scenario labeling (AGENTS.md: "Synthetic-data label always visible") is a single
		// fixture-level flag that covers every moment, not per-moment -- verify it's still there.
		JsonNode fixture = readFixture();
		assertThat(fixture.path("synthetic").asBoolean()).isTrue();
		assertThat(fixture.path("scenarioVersion").asText()).isNotBlank();
	}

	private static List<String> stringArray(JsonNode arrayNode) {
		List<String> values = new ArrayList<>();
		arrayNode.forEach(n -> values.add(n.asText()));
		return values;
	}

	private static JsonNode momentById(JsonNode fixture, String momentId) {
		for (JsonNode moment : fixture.path("moments")) {
			if (momentId.equals(moment.path("momentId").asText())) return moment;
		}
		throw new AssertionError("No moment with momentId=" + momentId);
	}

	private static JsonNode snapshotById(JsonNode fixture, String snapshotId) {
		for (JsonNode snapshot : fixture.path("snapshots")) {
			if (snapshotId.equals(snapshot.path("snapshotId").asText())) return snapshot;
		}
		throw new AssertionError("No snapshot with snapshotId=" + snapshotId);
	}

	private JsonNode readFixture() throws IOException {
		return objectMapper.readTree(readFixtureJson());
	}

	private static String readFixtureJson() throws IOException {
		try (InputStream stream = DemoFixtureM02M03Test.class.getClassLoader().getResourceAsStream(FIXTURE_PATH)) {
			if (stream == null) {
				throw new IllegalStateException("Missing classpath resource: " + FIXTURE_PATH);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
