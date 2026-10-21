import { PITCH_LENGTH_M, PITCH_WIDTH_M } from "@/lib/pitch";
import type { BallPosition, GeometryOverlay, PlayerPose, PlayerSummary } from "@/lib/api";

interface Pitch2DProps {
  players: PlayerSummary[];
  homeTeam: string;
  playerPoses: PlayerPose[];
  ball: BallPosition | null;
  geometry?: GeometryOverlay[];
  branchTrajectory?: { x: number; y: number }[];
}

const XRAY_OVERLAY_COLOR = "#f59e0b";
const BRANCH_TRAJECTORY_COLOR = "#a78bfa";

const HOME_COLOR = "#3b82f6";
const AWAY_COLOR = "#ef4444";

export function Pitch2D({ players, homeTeam, playerPoses, ball, geometry = [], branchTrajectory }: Pitch2DProps) {
  const teamByPlayerId = new Map(players.map((p) => [p.playerId, p.teamId]));

  return (
    <svg
      viewBox={`0 0 ${PITCH_LENGTH_M} ${PITCH_WIDTH_M}`}
      className="h-auto w-full max-w-3xl rounded border border-emerald-700 bg-emerald-900"
      role="img"
      aria-label="2D accessible fallback pitch view with synthetic player and ball positions"
    >
      <rect x={0.5} y={0.5} width={PITCH_LENGTH_M - 1} height={PITCH_WIDTH_M - 1} fill="none" stroke="white" strokeWidth={0.3} />
      <line x1={PITCH_LENGTH_M / 2} y1={0.5} x2={PITCH_LENGTH_M / 2} y2={PITCH_WIDTH_M - 0.5} stroke="white" strokeWidth={0.3} />
      <circle cx={PITCH_LENGTH_M / 2} cy={PITCH_WIDTH_M / 2} r={9.15} fill="none" stroke="white" strokeWidth={0.3} />

      {playerPoses.map((pose) => {
        const isHome = teamByPlayerId.get(pose.playerId) === homeTeam;
        return (
          <circle
            key={pose.playerId}
            cx={pose.x}
            cy={pose.y}
            r={1.4}
            fill={isHome ? HOME_COLOR : AWAY_COLOR}
            stroke="white"
            strokeWidth={0.15}
          >
            <title>{pose.playerId}</title>
          </circle>
        );
      })}

      {ball && <circle cx={ball.x} cy={ball.y} r={0.7} fill="white" stroke="black" strokeWidth={0.1} />}

      {/* X-Ray evidence overlay: every point traces back to overlay.evidenceIds (real event IDs). */}
      {geometry.map((overlay, index) => (
        <polyline
          key={`${overlay.type}-${index}`}
          points={overlay.points.map((p) => `${p.x},${p.y}`).join(" ")}
          fill="none"
          stroke={XRAY_OVERLAY_COLOR}
          strokeWidth={0.4}
          strokeDasharray="1.5,1"
        >
          <title>X-Ray evidence: {overlay.evidenceIds.join(", ")}</title>
        </polyline>
      ))}

      {/* Simulated branch trajectory — synthetic kinematics, never real evidence. Solid, violet, distinct from the amber dashed evidence overlay above. */}
      {branchTrajectory && (
        <polyline
          points={branchTrajectory.map((p) => `${p.x},${p.y}`).join(" ")}
          fill="none"
          stroke={BRANCH_TRAJECTORY_COLOR}
          strokeWidth={0.5}
        >
          <title>Simulated branch (not real): synthetic ball path</title>
        </polyline>
      )}
    </svg>
  );
}
