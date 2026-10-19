package com.beyond90.api.analytics;

import com.beyond90.api.fixture.Fixture;

import java.util.List;

/**
 * Detector D1 (docs/04-FOOTBALL-ENGINES.md): passing-lane clearance. Perpendicular distance from
 * each defender to the line segment passer-receiver (projection clamped to [0,1]); clearance is
 * the minimum over defenders. {@code k} (contested threshold) is a chosen heuristic, not an
 * empirical intercept probability.
 */
public final class LaneClearanceCalculator {

	public static final String METRIC_ID = "LANE_CLEARANCE";
	public static final String ALGORITHM_VERSION = "D1.v1";
	public static final double DEFAULT_CONTESTED_THRESHOLD_METERS = 2.0;

	private LaneClearanceCalculator() {}

	public static LaneClearanceResult clearance(
			Fixture.Point passer,
			Fixture.Point receiver,
			String snapshotId,
			List<Fixture.PlayerPose> defenders,
			String passerId,
			String receiverId) {
		return clearance(passer, receiver, snapshotId, defenders, passerId, receiverId, DEFAULT_CONTESTED_THRESHOLD_METERS);
	}

	public static LaneClearanceResult clearance(
			Fixture.Point passer,
			Fixture.Point receiver,
			String snapshotId,
			List<Fixture.PlayerPose> defenders,
			String passerId,
			String receiverId,
			double thresholdMeters) {
		List<Fixture.PlayerPose> considered =
				defenders.stream().filter(d -> !d.playerId().equals(passerId) && !d.playerId().equals(receiverId)).toList();

		double clearanceMeters = considered.stream()
				.mapToDouble(d -> distanceToSegment(passer, receiver, d.x(), d.y()))
				.min()
				.orElse(Double.POSITIVE_INFINITY);
		long contestingCount =
				considered.stream().filter(d -> distanceToSegment(passer, receiver, d.x(), d.y()) < thresholdMeters).count();

		List<String> evidenceIds = new java.util.ArrayList<>();
		evidenceIds.add(snapshotId);
		considered.forEach(d -> evidenceIds.add(d.playerId()));

		return new LaneClearanceResult(
				METRIC_ID,
				clearanceMeters,
				"meters",
				List.copyOf(evidenceIds),
				ALGORITHM_VERSION,
				clearanceMeters < thresholdMeters,
				thresholdMeters,
				considered.size(),
				(int) contestingCount);
	}

	/** Perpendicular distance from point (px,py) to segment a-b, with the projection clamped to [0,1]. */
	static double distanceToSegment(Fixture.Point a, Fixture.Point b, double px, double py) {
		double dx = b.x() - a.x();
		double dy = b.y() - a.y();
		double lengthSq = dx * dx + dy * dy;
		double t = lengthSq == 0 ? 0 : ((px - a.x()) * dx + (py - a.y()) * dy) / lengthSq;
		t = Math.max(0, Math.min(1, t));
		double closestX = a.x() + t * dx;
		double closestY = a.y() + t * dy;
		return Math.hypot(px - closestX, py - closestY);
	}

	/** Evidence-carrying result per contracts/openapi.yaml InsightMetric: evidenceIds + algorithmVersion required. */
	public record LaneClearanceResult(
			String metricId,
			double clearanceMeters,
			String unit,
			List<String> evidenceIds,
			String algorithmVersion,
			boolean contested,
			double thresholdMeters,
			int defenderCount,
			int contestingDefenderCount) {}
}
