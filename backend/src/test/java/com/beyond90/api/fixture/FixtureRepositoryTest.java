package com.beyond90.api.fixture;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FixtureRepositoryTest {

	private final FixtureRepository repository = new FixtureRepository();

	@Test
	void findsDemo001ByMatchId() {
		Fixture fixture = repository.findByMatchId("demo-001").orElseThrow();

		assertThat(fixture.matchId()).isEqualTo("demo-001");
		assertThat(fixture.players()).hasSize(22);
	}

	@Test
	void unknownMatchIdIsAbsent() {
		assertThat(repository.findByMatchId("does-not-exist")).isEmpty();
	}

	@Test
	void orderedEventsAreSortedBySeqRegardlessOfSourceOrder() {
		Fixture fixture = repository.findByMatchId("demo-001").orElseThrow();

		var orderedSeqs = repository.orderedEvents(fixture).stream().map(Fixture.MatchEvent::seq).toList();

		assertThat(orderedSeqs).isSorted();
	}
}
