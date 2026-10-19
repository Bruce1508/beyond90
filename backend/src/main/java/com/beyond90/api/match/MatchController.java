package com.beyond90.api.match;

import com.beyond90.api.fixture.Fixture;
import com.beyond90.api.fixture.FixtureRepository;
import com.beyond90.api.match.MatchDtos.EventDto;
import com.beyond90.api.match.MatchDtos.MatchSummaryDto;
import com.beyond90.api.match.MatchDtos.MomentDto;
import com.beyond90.api.match.MatchDtos.SnapshotsResponseDto;
import com.beyond90.api.match.MatchDtos.TimelineDto;
import com.beyond90.api.match.MatchDtos.TrackingSnapshotDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;

/** Implements the matches endpoints of contracts/openapi.yaml: canonical fixture + timeline. */
@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

	private final FixtureRepository fixtureRepository;

	public MatchController(FixtureRepository fixtureRepository) {
		this.fixtureRepository = fixtureRepository;
	}

	@GetMapping("/{matchId}")
	public MatchSummaryDto getMatch(@PathVariable String matchId) {
		return MatchSummaryDto.from(requireFixture(matchId));
	}

	@GetMapping("/{matchId}/timeline")
	public TimelineDto getTimeline(@PathVariable String matchId) {
		Fixture fixture = requireFixture(matchId);

		// Canonical order is always by seq, regardless of authoring order in the
		// source file: the same matchId yields the same event order every call.
		var events = fixtureRepository.orderedEvents(fixture).stream().map(EventDto::from).toList();
		var moments = fixture.moments().stream().map(MomentDto::from).toList();

		return new TimelineDto(fixture.matchId(), fixture.scenarioVersion(), events, moments);
	}

	@GetMapping("/{matchId}/snapshots")
	public SnapshotsResponseDto getSnapshots(
			@PathVariable String matchId,
			@RequestParam(required = false) Long startMs,
			@RequestParam(required = false) Long endMs) {
		Fixture fixture = requireFixture(matchId);

		var snapshots = fixture.snapshots().stream()
				.filter(s -> startMs == null || s.matchClockMs() >= startMs)
				.filter(s -> endMs == null || s.matchClockMs() <= endMs)
				.sorted(Comparator.comparingLong(Fixture.TrackingSnapshot::matchClockMs))
				.map(TrackingSnapshotDto::from)
				.toList();

		return new SnapshotsResponseDto(fixture.matchId(), fixture.scenarioVersion(), snapshots);
	}

	private Fixture requireFixture(String matchId) {
		return fixtureRepository.findByMatchId(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
	}
}
