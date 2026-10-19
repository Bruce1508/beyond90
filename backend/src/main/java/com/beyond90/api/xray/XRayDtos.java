package com.beyond90.api.xray;

import java.util.List;

/** Response shapes for contracts/openapi.yaml XRayResponse, InsightMetric, GeometryOverlay, Point schemas. */
public final class XRayDtos {

	private XRayDtos() {}

	public record PointDto(double x, double y) {}

	public record InsightMetricDto(String metricId, double value, String unit, List<String> evidenceIds, String algorithmVersion) {}

	public record GeometryOverlayDto(String type, List<PointDto> points, List<String> evidenceIds) {}

	public record XRayResponseDto(
			String insightId,
			String momentId,
			String scenarioVersion,
			String confidence,
			List<InsightMetricDto> metrics,
			List<GeometryOverlayDto> geometry,
			List<String> evidenceIds,
			List<String> caveats) {}
}
