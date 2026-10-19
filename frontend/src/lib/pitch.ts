/**
 * Canonical pitch coordinates (contracts/match-event.schema.json): x in [0,105]m,
 * y in [0,68]m, origin at a corner flag. Single source of truth for both the 2D
 * SVG fallback and the 3D scene so they can never drift apart.
 */
export const PITCH_LENGTH_M = 105;
export const PITCH_WIDTH_M = 68;

export interface MatchPoint {
  x: number;
  y: number;
}

/** 2D fallback uses the canonical meters directly as SVG viewBox units. */
export function toSvgPoint(point: MatchPoint): MatchPoint {
  return point;
}

/**
 * 3D scene: canonical x/y map to world X/Z on a ground plane centered at the
 * origin (so the pitch sits symmetrically under the default camera), world Y
 * is height (unused for ground-plane positions).
 */
export function toWorldPosition(point: MatchPoint): [number, number, number] {
  return [point.x - PITCH_LENGTH_M / 2, 0, point.y - PITCH_WIDTH_M / 2];
}
