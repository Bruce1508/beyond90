package com.beyond90.api.fixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P0-03: validates the authored M01 synthetic key moment in
 * examples/demo-001-mini-fixture.json against the acceptance criteria —
 * 22 player IDs, score-consistent, schema-valid, stable replay digest.
 */
class DemoFixtureM01Test {

	private static final String FIXTURE_PATH = "examples/demo-001-mini-fixture.json";

	// SHA-256 of the fixture bytes at authoring time. Any byte change (including
	// whitespace) must update this constant deliberately, not silently.
	private static final String EXPECTED_DIGEST =
			"4f6b1fe17effbbc983a1762a04045c89759b025523a2990f29a75b0fe66b1aad";

	private final FixtureSchemaValidator schemaValidator = new FixtureSchemaValidator();
	private final FixtureIntegrityValidator integrityValidator = new FixtureIntegrityValidator();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void m01FixtureIsSchemaValid() throws IOException {
		assertThat(schemaValidator.validate(readFixtureJson())).isEmpty();
	}

	@Test
	void m01FixtureHas22UniquePlayersAcrossTwoFullTeams() throws IOException {
		JsonNode fixture = objectMapper.readTree(readFixtureJson());

		Set<String> playerIds = new HashSet<>();
		fixture.path("players").forEach(player -> playerIds.add(player.path("playerId").asText()));

		assertThat(playerIds).hasSize(22);
		assertThat(fixture.path("teams")).hasSize(2);
	}

	@Test
	void m01FixtureIsReferentiallyConsistentAndScoreConsistent() throws IOException {
		JsonNode fixture = objectMapper.readTree(readFixtureJson());

		List<String> violations = integrityValidator.validate(fixture);
		assertThat(violations).isEmpty();

		// M01 contains no GOAL events, so the implied score is 0-0 by construction;
		// score-consistency for a moment with a GOAL event is exercised when M02/M03
		// (P0-11) introduce one.
		long goalEvents = fixture.path("events").findValuesAsText("type").stream()
				.filter("GOAL"::equals)
				.count();
		assertThat(goalEvents).isZero();
	}

	@Test
	void m01FixtureDigestIsStable() throws IOException, NoSuchAlgorithmException {
		byte[] bytes = readFixtureJson().getBytes(StandardCharsets.UTF_8);
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		String digest = HexFormat.of().formatHex(sha256.digest(bytes));

		assertThat(digest).isEqualTo(EXPECTED_DIGEST);
	}

	private static String readFixtureJson() throws IOException {
		try (InputStream stream = DemoFixtureM01Test.class.getClassLoader().getResourceAsStream(FIXTURE_PATH)) {
			if (stream == null) {
				throw new IllegalStateException("Missing classpath resource: " + FIXTURE_PATH);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
