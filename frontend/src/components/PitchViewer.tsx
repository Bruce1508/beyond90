"use client";

import { useEffect, useState } from "react";
import { hasWebGl } from "@/lib/webgl";
import { Pitch2D } from "@/components/Pitch2D";
import { Pitch3D } from "@/components/Pitch3D";
import type { BallPosition, GeometryOverlay, PlayerPose, PlayerSummary } from "@/lib/api";

interface PitchViewerProps {
  players: PlayerSummary[];
  homeTeam: string;
  playerPoses: PlayerPose[];
  ball: BallPosition | null;
  geometry?: GeometryOverlay[];
  branchTrajectory?: { x: number; y: number }[];
}

/** AGENTS.md: "production build and core workflow must work without WebGL." */
export function PitchViewer(props: PitchViewerProps) {
  const [webGlAvailable, setWebGlAvailable] = useState<boolean | null>(null);

  useEffect(() => {
    // One-time read of a platform capability (WebGL support) on mount, not
    // state derived from props/other state — the canonical effect use case,
    // just one the strict set-state-in-effect rule can't distinguish.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setWebGlAvailable(hasWebGl());
  }, []);

  if (webGlAvailable === null) {
    return <div className="h-[420px] w-full max-w-3xl" aria-hidden="true" />;
  }

  return webGlAvailable ? <Pitch3D {...props} /> : <Pitch2D {...props} />;
}
