package com.beyond90.api.fixture;

import java.util.List;
import java.util.Map;

/** Immutable in-memory representation of a synthetic match fixture (contracts/match-event.schema.json). */
public record Fixture(
		String schemaVersion,
		String scenarioVersion,
		String matchId,
		long generatorSeed,
		boolean synthetic,
		Pitch pitch,
		List<Team> teams,
		List<Player> players,
		List<Period> periods,
		List<MatchEvent> events,
		List<TrackingSnapshot> snapshots,
		List<Moment> moments) {

	/** Fixture authoring convention (not schema-enforced): teams[0] is home, teams[1] is away. */
	public Team homeTeam() {
		return teams.get(0);
	}

	public Team awayTeam() {
		return teams.get(1);
	}

	public record Pitch(int lengthMeters, int widthMeters) {}

	public record Team(String teamId, String name, String firstHalfAttackDirection) {}

	public record Player(String playerId, String teamId, int shirtNumber, String displayName) {}

	public record Period(String period, long startMs, Map<String, String> attackDirectionByTeam) {}

	public record Point(double x, double y) {}

	public record BallPosition(double x, double y, double z) {}

	public record PlayerPose(String playerId, double x, double y) {}

	public record MatchEvent(
			String eventId,
			int seq,
			String period,
			long matchClockMs,
			long periodClockMs,
			String type,
			String teamId,
			String actorId,
			String targetActorId,
			Point location,
			Point endLocation,
			String outcome,
			String snapshotId,
			String provenance) {}

	public record TrackingSnapshot(String snapshotId, long matchClockMs, BallPosition ball, List<PlayerPose> playerPoses) {}

	public record Moment(
			String momentId,
			long startMs,
			long endMs,
			List<String> eventIds,
			List<String> snapshotIds,
			List<String> availableActions,
			String actorId,
			String baseSnapshotId) {}
}
