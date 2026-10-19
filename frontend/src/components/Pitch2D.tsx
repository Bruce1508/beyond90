import { PITCH_LENGTH_M, PITCH_WIDTH_M } from "@/lib/pitch";
import type { BallPosition, PlayerPose, PlayerSummary } from "@/lib/api";

interface Pitch2DProps {
  players: PlayerSummary[];
  homeTeam: string;
  playerPoses: PlayerPose[];
  ball: BallPosition | null;
}

const HOME_COLOR = "#3b82f6";
const AWAY_COLOR = "#ef4444";

export function Pitch2D({ players, homeTeam, playerPoses, ball }: Pitch2DProps) {
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
    </svg>
  );
}
