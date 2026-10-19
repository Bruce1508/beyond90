package com.beyond90.api.fixture;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cross-field referential integrity checks that JSON Schema cannot express:
 * duplicate IDs, team roster cardinality, and dangling references between
 * events/snapshots/moments. Schema-level bounds checking is
 * {@link FixtureSchemaValidator}'s job; this runs only after that passes.
 */
public class FixtureIntegrityValidator {

	/** @return human-readable violations; empty when the fixture is internally consistent. */
	public List<String> validate(JsonNode fixture) {
		List<String> violations = new ArrayList<>();

		Set<String> teamIds = new HashSet<>();
		fixture.path("teams").forEach(team -> teamIds.add(team.path("teamId").asText()));

		Set<String> playerIds = new HashSet<>();
		Map<String, Integer> playersPerTeam = new HashMap<>();
		fixture.path("players").forEach(player -> {
			String playerId = player.path("playerId").asText();
			if (!playerIds.add(playerId)) {
				violations.add("Duplicate playerId: " + playerId);
			}
			String teamId = player.path("teamId").asText();
			if (!teamIds.contains(teamId)) {
				violations.add("Player " + playerId + " references unknown teamId: " + teamId);
			}
			playersPerTeam.merge(teamId, 1, Integer::sum);
		});
		for (String teamId : teamIds) {
			int count = playersPerTeam.getOrDefault(teamId, 0);
			if (count != 11) {
				violations.add("Team " + teamId + " has " + count + " players, expected 11");
			}
		}

		Set<String> snapshotIds = new HashSet<>();
		fixture.path("snapshots").forEach(snapshot -> snapshotIds.add(snapshot.path("snapshotId").asText()));

		Set<String> eventIds = new HashSet<>();
		fixture.path("events").forEach(event -> {
			String eventId = event.path("eventId").asText();
			if (!eventIds.add(eventId)) {
				violations.add("Duplicate eventId: " + eventId);
			}
			requireKnownReference(violations, "event " + eventId + " teamId", event.path("teamId").asText(), teamIds);
			requireKnownReference(violations, "event " + eventId + " actorId", event.path("actorId").asText(), playerIds);
			if (event.has("targetActorId")) {
				requireKnownReference(violations, "event " + eventId + " targetActorId", event.path("targetActorId").asText(), playerIds);
			}
			if (event.has("snapshotId")) {
				requireKnownReference(violations, "event " + eventId + " snapshotId", event.path("snapshotId").asText(), snapshotIds);
			}
		});

		fixture.path("moments").forEach(moment -> {
			String momentId = moment.path("momentId").asText();
			moment.path("eventIds").forEach(id ->
					requireKnownReference(violations, "moment " + momentId + " eventId", id.asText(), eventIds));
			moment.path("snapshotIds").forEach(id ->
					requireKnownReference(violations, "moment " + momentId + " snapshotId", id.asText(), snapshotIds));
			if (moment.has("baseSnapshotId")) {
				requireKnownReference(violations, "moment " + momentId + " baseSnapshotId", moment.path("baseSnapshotId").asText(), snapshotIds);
			}
		});

		return violations;
	}

	private static void requireKnownReference(List<String> violations, String label, String value, Set<String> known) {
		if (!known.contains(value)) {
			violations.add(label + " references unknown ID: " + value);
		}
	}
}
