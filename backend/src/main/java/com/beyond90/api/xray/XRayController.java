package com.beyond90.api.xray;

import com.beyond90.api.xray.XRayDtos.XRayResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Implements contracts/openapi.yaml's GET /api/v1/moments/{momentId}/xray. */
@RestController
@RequestMapping("/api/v1/moments")
public class XRayController {

	private final XRayService xRayService;

	public XRayController(XRayService xRayService) {
		this.xRayService = xRayService;
	}

	@GetMapping("/{momentId}/xray")
	public XRayResponseDto getXRay(@PathVariable String momentId) {
		return xRayService.buildXRay(momentId);
	}
}
