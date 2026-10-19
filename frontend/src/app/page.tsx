"use client";

import { useEffect, useRef, useState } from "react";
import { getMatch, getSnapshots, type MatchSummary, type SnapshotsResponse } from "@/lib/api";
import { PitchViewer } from "@/components/PitchViewer";
import { PlaybackControls } from "@/components/PlaybackControls";

const MATCH_ID = "demo-001";

/** Sparse curated snapshots only (contracts/openapi.yaml): show the latest
 * snapshot at or before the cursor, never an interpolated/fabricated position. */
function snapshotAt(snapshots: SnapshotsResponse["snapshots"], ms: number) {
  let current = snapshots[0] ?? null;
  for (const snapshot of snapshots) {
    if (snapshot.matchClockMs <= ms) {
      current = snapshot;
    }
  }
  return current;
}

export default function Home() {
  const [match, setMatch] = useState<MatchSummary | null>(null);
  const [snapshots, setSnapshots] = useState<SnapshotsResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [currentMs, setCurrentMs] = useState(0);
  const [playing, setPlaying] = useState(false);
  const rafRef = useRef<number | null>(null);
  const lastFrameTimeRef = useRef<number | null>(null);

  useEffect(() => {
    Promise.all([getMatch(MATCH_ID), getSnapshots(MATCH_ID)])
      .then(([matchResult, snapshotsResult]) => {
        setMatch(matchResult);
        setSnapshots(snapshotsResult);
        setCurrentMs(snapshotsResult.snapshots[0]?.matchClockMs ?? 0);
      })
      .catch((err: Error) => setError(err.message));
  }, []);

  const minMs = snapshots?.snapshots[0]?.matchClockMs ?? 0;
  const maxMs = snapshots?.snapshots.at(-1)?.matchClockMs ?? 0;

  useEffect(() => {
    if (!playing) {
      lastFrameTimeRef.current = null;
      return;
    }
    const tick = (now: number) => {
      const last = lastFrameTimeRef.current ?? now;
      lastFrameTimeRef.current = now;
      setCurrentMs((prev) => {
        const next = prev + (now - last);
        if (next >= maxMs) {
          setPlaying(false);
          return maxMs;
        }
        return next;
      });
      rafRef.current = requestAnimationFrame(tick);
    };
    rafRef.current = requestAnimationFrame(tick);
    return () => {
      if (rafRef.current !== null) cancelAnimationFrame(rafRef.current);
    };
  }, [playing, maxMs]);

  if (error) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center gap-2 bg-zinc-950 p-8 text-zinc-50">
        <p className="text-red-400">Could not reach the BEYOND90 API: {error}</p>
        <p className="text-sm text-zinc-500">Is the backend running on localhost:8080? (cd backend && ./mvnw spring-boot:run)</p>
      </main>
    );
  }

  if (!match || !snapshots) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-zinc-950 text-zinc-50">
        <p>Loading synthetic fixture…</p>
      </main>
    );
  }

  const activeSnapshot = snapshotAt(snapshots.snapshots, currentMs);

  return (
    <main className="flex min-h-screen flex-col items-center gap-4 bg-zinc-950 p-8 text-zinc-50">
      <h1 className="text-2xl font-semibold">BEYOND90 — Football X-Ray</h1>
      <p className="text-sm text-zinc-400">
        Synthetic fixture {match.matchId} ({match.homeTeam} vs {match.awayTeam}). No real match, club, or player data.
      </p>
      <PitchViewer
        players={match.players}
        homeTeam={match.homeTeam}
        playerPoses={activeSnapshot?.playerPoses ?? []}
        ball={activeSnapshot?.ball ?? null}
      />
      <PlaybackControls
        currentMs={currentMs}
        minMs={minMs}
        maxMs={maxMs}
        playing={playing}
        onTogglePlay={() => setPlaying((p) => !p)}
        onSeek={(ms) => {
          setPlaying(false);
          setCurrentMs(ms);
        }}
      />
    </main>
  );
}
