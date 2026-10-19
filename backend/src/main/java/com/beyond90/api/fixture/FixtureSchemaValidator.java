package com.beyond90.api.fixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Validates a synthetic match fixture document against the canonical
 * {@code contracts/match-event.schema.json} JSON Schema (structural checks
 * only; cross-field invariants belong to a future fixture-level validator).
 */
public class FixtureSchemaValidator {

	private static final String SCHEMA_CLASSPATH = "contracts/match-event.schema.json";

	private final JsonSchema schema;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public FixtureSchemaValidator() {
		try (InputStream schemaStream = requireResource(SCHEMA_CLASSPATH)) {
			JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
			this.schema = factory.getSchema(schemaStream);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load " + SCHEMA_CLASSPATH, e);
		}
	}

	/** @return validation errors; empty when the fixture is schema-valid. */
	public Set<ValidationMessage> validate(String fixtureJson) {
		JsonNode fixtureNode;
		try {
			fixtureNode = objectMapper.readTree(fixtureJson);
		} catch (IOException e) {
			throw new IllegalArgumentException("Fixture is not valid JSON", e);
		}
		return schema.validate(fixtureNode);
	}

	private static InputStream requireResource(String classpath) {
		InputStream stream = FixtureSchemaValidator.class.getClassLoader().getResourceAsStream(classpath);
		if (stream == null) {
			throw new IllegalStateException("Missing classpath resource: " + classpath);
		}
		return stream;
	}
}
