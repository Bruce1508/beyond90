"use client";

import { Canvas } from "@react-three/fiber";
import { OrbitControls } from "@react-three/drei";
import { PITCH_LENGTH_M, PITCH_WIDTH_M, toWorldPosition } from "@/lib/pitch";
import type { BallPosition, PlayerPose, PlayerSummary } from "@/lib/api";

interface Pitch3DProps {
  players: PlayerSummary[];
  homeTeam: string;
  playerPoses: PlayerPose[];
  ball: BallPosition | null;
}

const HOME_COLOR = "#3b82f6";
const AWAY_COLOR = "#ef4444";

function PlayerMarker({ pose, isHome }: { pose: PlayerPose; isHome: boolean }) {
  const [x, y, z] = toWorldPosition(pose);
  return (
    <mesh position={[x, y + 0.9, z]}>
      <cylinderGeometry args={[0.6, 0.6, 1.8, 12]} />
      <meshStandardMaterial color={isHome ? HOME_COLOR : AWAY_COLOR} />
    </mesh>
  );
}

function BallMarker({ ball }: { ball: BallPosition }) {
  const [x, , z] = toWorldPosition(ball);
  return (
    <mesh position={[x, ball.z + 0.3, z]}>
      <sphereGeometry args={[0.3, 16, 16]} />
      <meshStandardMaterial color="white" />
    </mesh>
  );
}

export function Pitch3D({ players, homeTeam, playerPoses, ball }: Pitch3DProps) {
  const teamByPlayerId = new Map(players.map((p) => [p.playerId, p.teamId]));

  return (
    <div className="h-[420px] w-full max-w-3xl overflow-hidden rounded border border-emerald-700">
      <Canvas camera={{ position: [0, 70, 55], fov: 45 }}>
        <ambientLight intensity={0.7} />
        <directionalLight position={[20, 40, 10]} intensity={0.8} />
        <mesh rotation={[-Math.PI / 2, 0, 0]}>
          <planeGeometry args={[PITCH_LENGTH_M, PITCH_WIDTH_M]} />
          <meshStandardMaterial color="#14532d" />
        </mesh>
        {playerPoses.map((pose) => (
          <PlayerMarker key={pose.playerId} pose={pose} isHome={teamByPlayerId.get(pose.playerId) === homeTeam} />
        ))}
        {ball && <BallMarker ball={ball} />}
        <OrbitControls makeDefault minDistance={20} maxDistance={140} maxPolarAngle={Math.PI / 2.1} />
      </Canvas>
    </div>
  );
}
