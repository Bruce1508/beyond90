package com.beyond90.api.match;

import com.beyond90.api.fixture.Fixture;

import java.util.List;
import java.util.Map;

/** Response shapes for contracts/openapi.yaml MatchSummary, Timeline, Event, Moment, Point schemas. */
public final class MatchDtos {

	private MatchDtos() {}

	public record PointDto(double x, double y) {
		static PointDto from(Fixture.Point point) {
			return point == null ? null : new PointDto(point.x(), point.y());
		}
	}

	public record PitchDto(int lengthMeters, int widthMeters) {}

	public record PlayerSummaryDto(String playerId, String teamId, String displayName, int shirtNumber) {}

	public record PeriodDto(String period, long startMs, Map<String, String> attackDirectionByTeam) {}

	public record MatchSummaryDto(
			String matchId,
			String scenarioVersion,
			boolean synthetic,
			PitchDto pitch,
			String homeTeam,
			String awayTeam,
			List<PlayerSummaryDto> players,
			List<PeriodDto> periods) {

		public static MatchSummaryDto from(Fixture fixture) {
			return new MatchSummaryDto(
					fixture.matchId(),
					fixture.scenarioVersion(),
					fixture.synthetic(),
					new PitchDto(fixture.pitch().lengthMeters(), fixture.pitch().widthMeters()),
					fixture.homeTeam().teamId(),
					fixture.awayTeam().teamId(),
					fixture.players().stream()
							.map(p -> new PlayerSummaryDto(p.playerId(), p.teamId(), p.displayName(), p.shirtNumber()))
							.toList(),
					fixture.periods().stream()
							.map(p -> new PeriodDto(p.period(), p.startMs(), p.attackDirectionByTeam()))
							.toList());
		}
	}

	public record EventDto(
			String eventId,
			int seq,
			long matchClockMs,
			long periodClockMs,
			String period,
			String type,
			String teamId,
			String actorId,
			PointDto location,
			PointDto endLocation,
			String snapshotId) {

		static EventDto from(Fixture.MatchEvent event) {
			return new EventDto(
					event.eventId(),
					event.seq(),
					event.matchClockMs(),
					event.periodClockMs(),
					event.period(),
					event.type(),
					event.teamId(),
					event.actorId(),
					PointDto.from(event.location()),
					PointDto.from(event.endLocation()),
					event.snapshotId());
		}
	}

	public record MomentDto(String momentId, long startMs, long endMs, List<String> availableActions) {
		static MomentDto from(Fixture.Moment moment) {
			return new MomentDto(moment.momentId(), moment.startMs(), moment.endMs(), moment.availableActions());
		}
	}

	public record TimelineDto(String matchId, String scenarioVersion, List<EventDto> events, List<MomentDto> moments) {}

	public record BallPositionDto(double x, double y, double z) {
		static BallPositionDto from(Fixture.BallPosition ball) {
			return new BallPositionDto(ball.x(), ball.y(), ball.z());
		}
	}

	public record PlayerPoseDto(String playerId, double x, double y) {
		static PlayerPoseDto from(Fixture.PlayerPose pose) {
			return new PlayerPoseDto(pose.playerId(), pose.x(), pose.y());
		}
	}

	public record TrackingSnapshotDto(String snapshotId, long matchClockMs, BallPositionDto ball, List<PlayerPoseDto> playerPoses) {
		static TrackingSnapshotDto from(Fixture.TrackingSnapshot snapshot) {
			return new TrackingSnapshotDto(
					snapshot.snapshotId(),
					snapshot.matchClockMs(),
					BallPositionDto.from(snapshot.ball()),
					snapshot.playerPoses().stream().map(PlayerPoseDto::from).toList());
		}
	}

	public record SnapshotsResponseDto(String matchId, String scenarioVersion, List<TrackingSnapshotDto> snapshots) {}
}
