package com.beyond90.api.simulation;

import com.beyond90.api.fixture.Fixture;
import com.beyond90.api.fixture.FixtureRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SimulationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FixtureRepository fixtureRepository;

	@Test
	void simulatePassOnM03ReturnsBranchResult() throws Exception {
		mockMvc.perform(post("/api/v1/moments/m03/simulate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"scenarioVersion":"1.0.0","action":"PASS","target":{"x":10,"y":34},"seed":7}"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.momentId").value("m03"))
				.andExpect(jsonPath("$.baseSnapshotId").value("snap_m03_a"))
				.andExpect(jsonPath("$.originalUnchanged").value(true))
				.andExpect(jsonPath("$.simulated").value(true))
				.andExpect(jsonPath("$.seed").value(7))
				.andExpect(jsonPath("$.frames").isArray())
				.andExpect(jsonPath("$.caveats").isNotEmpty());
	}

	@Test
	void simulateUnsupportedActionReturns422Problem() throws Exception {
		mockMvc.perform(post("/api/v1/moments/m03/simulate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"scenarioVersion":"1.0.0","action":"CARRY","target":{"x":10,"y":34},"seed":1}"""))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().contentType("application/problem+json"))
				.andExpect(jsonPath("$.status").value(422));
	}

	@Test
	void simulateUnknownMomentReturns404Problem() throws Exception {
		mockMvc.perform(post("/api/v1/moments/does-not-exist/simulate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"scenarioVersion":"1.0.0","action":"PASS","target":{"x":10,"y":34},"seed":1}"""))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType("application/problem+json"));
	}

	@Test
	void hundredBranchCallsLeaveTheParentFixtureUntouched() throws Exception {
		Fixture before = fixtureRepository.findByMatchId("demo-001").orElseThrow();

		for (int i = 0; i < 100; i++) {
			mockMvc.perform(post("/api/v1/moments/m03/simulate")
							.contentType(MediaType.APPLICATION_JSON)
							.content(
									"""
									{"scenarioVersion":"1.0.0","action":"%s","target":{"x":%d,"y":34},"seed":%d}"""
											.formatted(i % 2 == 0 ? "PASS" : "SHOOT", i % 2 == 0 ? 10 + (i % 5) : 0, i)))
					.andExpect(status().isOk());
		}

		Fixture after = fixtureRepository.findByMatchId("demo-001").orElseThrow();
		assertThat(after).isEqualTo(before);
	}
}
