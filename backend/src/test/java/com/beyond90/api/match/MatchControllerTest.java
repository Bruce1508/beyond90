package com.beyond90.api.match;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MatchControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getMatchReturnsDemo001Summary() throws Exception {
		mockMvc.perform(get("/api/v1/matches/demo-001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.matchId").value("demo-001"))
				.andExpect(jsonPath("$.synthetic").value(true))
				.andExpect(jsonPath("$.players.length()").value(22))
				.andExpect(jsonPath("$.homeTeam").value("blue"))
				.andExpect(jsonPath("$.awayTeam").value("red"));
	}

	@Test
	void getMatchReturns404ProblemForUnknownMatch() throws Exception {
		mockMvc.perform(get("/api/v1/matches/does-not-exist"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType("application/problem+json"))
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void timelineEventsAreOrderedBySeqAndIncludeM01() throws Exception {
		mockMvc.perform(get("/api/v1/matches/demo-001/timeline"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.events.length()").value(4))
				.andExpect(jsonPath("$.events[0].seq").value(1))
				.andExpect(jsonPath("$.events[1].seq").value(2))
				.andExpect(jsonPath("$.events[2].seq").value(3))
				.andExpect(jsonPath("$.events[3].seq").value(4))
				.andExpect(jsonPath("$.moments[0].momentId").value("m01"));
	}

	@Test
	void timelineIsDeterministicAcrossRepeatedCalls() throws Exception {
		String first = mockMvc.perform(get("/api/v1/matches/demo-001/timeline"))
				.andReturn().getResponse().getContentAsString();
		String second = mockMvc.perform(get("/api/v1/matches/demo-001/timeline"))
				.andReturn().getResponse().getContentAsString();

		org.assertj.core.api.Assertions.assertThat(first).isEqualTo(second);
	}
}
