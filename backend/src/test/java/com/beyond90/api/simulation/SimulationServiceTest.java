package com.beyond90.api.simulation;

import com.beyond90.api.fixture.FixtureRepository;
import com.beyond90.api.simulation.SimulationDtos.PointDto;
import com.beyond90.api.simulation.SimulationDtos.SimulateRequestDto;
import com.beyond90.api.xray.MomentNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Wiring tests for P0-14: {@link BranchSimulator} geometry itself is covered by BranchSimulatorTest. */
class SimulationServiceTest {

	private static final String SCENARIO_VERSION = "1.0.0";

	private final FixtureRepository fixtureRepository = new FixtureRepository();
	private final SimulationService simulationService = new SimulationService(fixtureRepository);

	private static SimulateRequestDto request(String action, PointDto target, long seed) {
		return new SimulateRequestDto(SCENARIO_VERSION, action, target, seed);
	}

	@Test
	void passOnM03_producesSimulatedBranch() {
		var result = simulationService.simulate("m03", request("PASS", new PointDto(10, 34), 7L), null);

		assertThat(result.momentId()).isEqualTo("m03");
		assertThat(result.baseSnapshotId()).isEqualTo("snap_m03_a");
		assertThat(result.originalUnchanged()).isTrue();
		assertThat(result.simulated()).isTrue();
		assertThat(result.engineVersion()).isEqualTo(BranchSimulator.ENGINE_VERSION);
		assertThat(result.seed()).isEqualTo(7L);
		assertThat(result.branchId()).startsWith("branch-");
		assertThat(result.frames()).isNotEmpty();
		assertThat(result.caveats()).isNotEmpty();
	}

	@Test
	void mismatchedScenarioVersion_rejected() {
		var staleRequest = new SimulateRequestDto("0.0.1-stale", "PASS", new PointDto(10, 34), 1L);

		assertThatThrownBy(() -> simulationService.simulate("m03", staleRequest, null))
				.isInstanceOf(InvalidActionException.class)
				.hasMessageContaining("scenarioVersion");
	}

	@Test
	void actionNotAvailableForMoment_rejected() {
		assertThatThrownBy(() -> simulationService.simulate("m03", request("CARRY", new PointDto(10, 34), 1L), null))
				.isInstanceOf(InvalidActionException.class)
				.hasMessageContaining("not available");
	}

	@Test
	void unknownMoment_rejected() {
		assertThatThrownBy(() -> simulationService.simulate("does-not-exist", request("PASS", new PointDto(10, 34), 1L), null))
				.isInstanceOf(MomentNotFoundException.class);
	}

	@Test
	void momentWithoutActorOrBaseSnapshot_rejected() {
		// m01 is a pressure/interception moment with no decision-fork actor to branch from.
		assertThatThrownBy(() -> simulationService.simulate("m01", request("PASS", new PointDto(10, 34), 1L), null))
				.isInstanceOf(InvalidActionException.class)
				.hasMessageContaining("no actor or base snapshot");
	}

	@Test
	void missingTarget_rejected() {
		assertThatThrownBy(() -> simulationService.simulate("m03", request("PASS", null, 1L), null))
				.isInstanceOf(InvalidActionException.class)
				.hasMessageContaining("requires a target");
	}

	@Test
	void idempotencyKeyReplaysSameBranchId() {
		var key = "test-key-1";
		var first = simulationService.simulate("m03", request("PASS", new PointDto(10, 34), 7L), key);
		var second = simulationService.simulate("m03", request("PASS", new PointDto(99, 1), 999L), key);

		assertThat(second.branchId()).isEqualTo(first.branchId());
		assertThat(second.seed()).isEqualTo(first.seed());
	}

	@Test
	void differentIdempotencyKeys_produceIndependentBranches() {
		var first = simulationService.simulate("m03", request("PASS", new PointDto(10, 34), 7L), "key-a");
		var second = simulationService.simulate("m03", request("PASS", new PointDto(10, 34), 7L), "key-b");

		assertThat(second.branchId()).isNotEqualTo(first.branchId());
	}
}
