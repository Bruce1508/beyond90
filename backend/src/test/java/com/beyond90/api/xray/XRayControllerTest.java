package com.beyond90.api.xray;

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
class XRayControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getXRayReturnsSupportedInsightForM01() throws Exception {
		mockMvc.perform(get("/api/v1/moments/m01/xray"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.momentId").value("m01"))
				.andExpect(jsonPath("$.confidence").value("SUPPORTED"))
				.andExpect(jsonPath("$.metrics.length()").value(1))
				.andExpect(jsonPath("$.metrics[0].metricId").value("PRESSURE_ASSOCIATED_TURNOVER"))
				.andExpect(jsonPath("$.metrics[0].algorithmVersion").value("D2.v1"))
				.andExpect(jsonPath("$.metrics[0].evidenceIds.length()").value(4))
				.andExpect(jsonPath("$.geometry[0].type").value("LINE"))
				.andExpect(jsonPath("$.evidenceIds").isArray())
				.andExpect(jsonPath("$.caveats.length()").value(1));
	}

	@Test
	void getXRayReturns404ProblemForUnknownMoment() throws Exception {
		mockMvc.perform(get("/api/v1/moments/does-not-exist/xray"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType("application/problem+json"))
				.andExpect(jsonPath("$.status").value(404));
	}
}
