"use client";

import { useMemo } from "react";
import { Canvas } from "@react-three/fiber";
import { Line, OrbitControls } from "@react-three/drei";
import { PITCH_LENGTH_M, PITCH_WIDTH_M, toWorldPosition } from "@/lib/pitch";
import type { BallPosition, GeometryOverlay, PlayerPose, PlayerSummary } from "@/lib/api";

interface Pitch3DProps {
  players: PlayerSummary[];
  homeTeam: string;
  playerPoses: PlayerPose[];
  ball: BallPosition | null;
  geometry?: GeometryOverlay[];
}

const HOME_COLOR = "#3b82f6";
const AWAY_COLOR = "#ef4444";
const XRAY_OVERLAY_COLOR = "#f59e0b";

/** Centroid of the first overlay's points, in world space -- the "camera X-Ray focus" target. */
function overlayFocusTarget(geometry: GeometryOverlay[]): [number, number, number] | null {
  const overlay = geometry[0];
  if (!overlay || overlay.points.length === 0) return null;
  const centroid = overlay.points.reduce((acc, p) => ({ x: acc.x + p.x / overlay.points.length, y: acc.y + p.y / overlay.points.length }), {
    x: 0,
    y: 0,
  });
  return toWorldPosition(centroid);
}

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

export function Pitch3D({ players, homeTeam, playerPoses, ball, geometry = [] }: Pitch3DProps) {
  const teamByPlayerId = new Map(players.map((p) => [p.playerId, p.teamId]));
  // Stable array reference across re-renders (OrbitControls re-syncs its target whenever this
  // prop's identity changes, which would otherwise fight the user's own drag every frame).
  const geometryKey = JSON.stringify(geometry);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  const focusTarget = useMemo(() => overlayFocusTarget(geometry) ?? [0, 0, 0], [geometryKey]);

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
        {/* X-Ray evidence overlay: every line traces back to overlay.evidenceIds (real event IDs). */}
        {geometry.map((overlay, index) => (
          <Line
            key={`${overlay.type}-${index}`}
            points={overlay.points.map((p) => toWorldPosition(p).map((n, i) => (i === 1 ? n + 0.3 : n)) as [number, number, number])}
            color={XRAY_OVERLAY_COLOR}
            lineWidth={2}
            dashed
            dashScale={4}
          />
        ))}
        <OrbitControls makeDefault target={focusTarget} minDistance={20} maxDistance={140} maxPolarAngle={Math.PI / 2.1} />
      </Canvas>
    </div>
  );
}
