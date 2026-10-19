package com.beyond90.api.fixture;

import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FixtureSchemaValidatorTest {

	private final FixtureSchemaValidator validator = new FixtureSchemaValidator();

	@Test
	void demoFixturePassesSchema() throws IOException {
		String fixtureJson = readClasspathResource("examples/demo-001-mini-fixture.json");

		Set<ValidationMessage> errors = validator.validate(fixtureJson);

		assertThat(errors).isEmpty();
	}

	@Test
	void eventWithOutOfBoundsCoordinateIsRejected() {
		String invalidFixture = """
				{
				  "schemaVersion": "1.0.0",
				  "scenarioVersion": "1.0.0",
				  "matchId": "demo-invalid",
				  "generatorSeed": 1,
				  "synthetic": true,
				  "pitch": { "lengthMeters": 105, "widthMeters": 68 },
				  "teams": [
				    { "teamId": "blue", "name": "Blue FC", "firstHalfAttackDirection": "LEFT_TO_RIGHT" },
				    { "teamId": "red", "name": "Red United", "firstHalfAttackDirection": "RIGHT_TO_LEFT" }
				  ],
				  "players": [],
				  "periods": [
				    { "period": "FIRST_HALF", "startMs": 0, "attackDirectionByTeam": { "blue": "LEFT_TO_RIGHT", "red": "RIGHT_TO_LEFT" } },
				    { "period": "SECOND_HALF", "startMs": 2800000, "attackDirectionByTeam": { "blue": "RIGHT_TO_LEFT", "red": "LEFT_TO_RIGHT" } }
				  ],
				  "events": [
				    {
				      "eventId": "evt_1",
				      "seq": 1,
				      "period": "FIRST_HALF",
				      "matchClockMs": 1000,
				      "periodClockMs": 1000,
				      "type": "PASS",
				      "teamId": "blue",
				      "actorId": "blue_01",
				      "location": { "x": 999, "y": 30 },
				      "provenance": "SYNTHETIC"
				    }
				  ],
				  "snapshots": [],
				  "moments": []
				}
				""";

		Set<ValidationMessage> errors = validator.validate(invalidFixture);

		assertThat(errors).isNotEmpty();
		assertThat(errors.toString()).contains("maximum");
	}

	private static String readClasspathResource(String classpath) throws IOException {
		try (InputStream stream = FixtureSchemaValidatorTest.class.getClassLoader().getResourceAsStream(classpath)) {
			if (stream == null) {
				throw new IllegalStateException("Missing classpath resource: " + classpath);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
