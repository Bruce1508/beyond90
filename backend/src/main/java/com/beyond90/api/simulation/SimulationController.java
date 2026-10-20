package com.beyond90.api.simulation;

import com.beyond90.api.simulation.SimulationDtos.BranchResultDto;
import com.beyond90.api.simulation.SimulationDtos.SimulateRequestDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Implements contracts/openapi.yaml's POST /api/v1/moments/{momentId}/simulate. */
@RestController
@RequestMapping("/api/v1/moments")
public class SimulationController {

	private final SimulationService simulationService;

	public SimulationController(SimulationService simulationService) {
		this.simulationService = simulationService;
	}

	@PostMapping("/{momentId}/simulate")
	public BranchResultDto simulate(
			@PathVariable String momentId,
			@RequestBody SimulateRequestDto request,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
		return simulationService.simulate(momentId, request, idempotencyKey);
	}
}
