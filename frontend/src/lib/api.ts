const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export interface PlayerSummary {
  playerId: string;
  teamId: string;
  displayName: string;
  shirtNumber: number;
}

export interface MatchSummary {
  matchId: string;
  scenarioVersion: string;
  synthetic: boolean;
  pitch: { lengthMeters: number; widthMeters: number };
  homeTeam: string;
  awayTeam: string;
  players: PlayerSummary[];
}

export interface PlayerPose {
  playerId: string;
  x: number;
  y: number;
}

export interface BallPosition {
  x: number;
  y: number;
  z: number;
}

export interface TrackingSnapshot {
  snapshotId: string;
  matchClockMs: number;
  ball: BallPosition;
  playerPoses: PlayerPose[];
}

export interface SnapshotsResponse {
  matchId: string;
  scenarioVersion: string;
  snapshots: TrackingSnapshot[];
}

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`);
  if (!response.ok) {
    throw new Error(`BEYOND90 API ${path} failed: HTTP ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export function getMatch(matchId: string): Promise<MatchSummary> {
  return getJson(`/api/v1/matches/${matchId}`);
}

export function getSnapshots(matchId: string): Promise<SnapshotsResponse> {
  return getJson(`/api/v1/matches/${matchId}/snapshots`);
}

export interface InsightMetric {
  metricId: string;
  value: number;
  unit: string;
  evidenceIds: string[];
  algorithmVersion: string;
}

export interface GeometryOverlay {
  type: "LINE" | "CIRCLE" | "POLYGON";
  points: { x: number; y: number }[];
  evidenceIds: string[];
}

export interface XRayResponse {
  insightId: string;
  momentId: string;
  scenarioVersion: string;
  confidence: "SUPPORTED" | "PARTIAL" | "INSUFFICIENT";
  metrics: InsightMetric[];
  geometry: GeometryOverlay[];
  evidenceIds: string[];
  caveats: string[];
}

export function getXRay(momentId: string): Promise<XRayResponse> {
  return getJson(`/api/v1/moments/${momentId}/xray`);
}
