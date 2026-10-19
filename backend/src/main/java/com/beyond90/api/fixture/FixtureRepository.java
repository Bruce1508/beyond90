package com.beyond90.api.fixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable in-memory repository of synthetic match fixtures, loaded once from the
 * classpath at startup. Fails fast if a bundled fixture is schema- or
 * reference-invalid: a broken demo fixture must never silently ship.
 */
@Component
public class FixtureRepository {

	private static final List<String> BUNDLED_FIXTURE_CLASSPATHS = List.of("examples/demo-001-mini-fixture.json");

	private final Map<String, Fixture> fixturesByMatchId;

	public FixtureRepository() {
		ObjectMapper objectMapper = new ObjectMapper();
		FixtureSchemaValidator schemaValidator = new FixtureSchemaValidator();
		FixtureIntegrityValidator integrityValidator = new FixtureIntegrityValidator();

		this.fixturesByMatchId = BUNDLED_FIXTURE_CLASSPATHS.stream()
				.map(classpath -> loadAndValidate(classpath, objectMapper, schemaValidator, integrityValidator))
				.collect(java.util.stream.Collectors.toUnmodifiableMap(Fixture::matchId, f -> f));
	}

	public Optional<Fixture> findByMatchId(String matchId) {
		return Optional.ofNullable(fixturesByMatchId.get(matchId));
	}

	/** Events ordered by canonical {@code seq}, independent of authoring order in the source file. */
	public List<Fixture.MatchEvent> orderedEvents(Fixture fixture) {
		return fixture.events().stream()
				.sorted(Comparator.comparingInt(Fixture.MatchEvent::seq))
				.toList();
	}

	private static Fixture loadAndValidate(
			String classpath,
			ObjectMapper objectMapper,
			FixtureSchemaValidator schemaValidator,
			FixtureIntegrityValidator integrityValidator) {
		String json = readClasspathResource(classpath);

		var schemaErrors = schemaValidator.validate(json);
		if (!schemaErrors.isEmpty()) {
			throw new IllegalStateException("Bundled fixture " + classpath + " failed schema validation: " + schemaErrors);
		}

		JsonNode node;
		try {
			node = objectMapper.readTree(json);
		} catch (IOException e) {
			throw new IllegalStateException("Bundled fixture " + classpath + " is not valid JSON", e);
		}
		var integrityErrors = integrityValidator.validate(node);
		if (!integrityErrors.isEmpty()) {
			throw new IllegalStateException("Bundled fixture " + classpath + " failed integrity validation: " + integrityErrors);
		}

		try {
			return objectMapper.readValue(json, Fixture.class);
		} catch (IOException e) {
			throw new IllegalStateException("Bundled fixture " + classpath + " could not be parsed into Fixture", e);
		}
	}

	private static String readClasspathResource(String classpath) {
		try (InputStream stream = FixtureRepository.class.getClassLoader().getResourceAsStream(classpath)) {
			if (stream == null) {
				throw new IllegalStateException("Missing classpath resource: " + classpath);
			}
			return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read classpath resource: " + classpath, e);
		}
	}
}
