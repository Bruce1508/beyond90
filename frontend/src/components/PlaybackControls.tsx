"use client";

interface PlaybackControlsProps {
  currentMs: number;
  minMs: number;
  maxMs: number;
  playing: boolean;
  onTogglePlay: () => void;
  onSeek: (ms: number) => void;
}

function formatClock(ms: number): string {
  const totalSeconds = Math.floor(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}:${seconds.toString().padStart(2, "0")}`;
}

export function PlaybackControls({ currentMs, minMs, maxMs, playing, onTogglePlay, onSeek }: PlaybackControlsProps) {
  return (
    <div className="flex w-full max-w-3xl items-center gap-3 text-sm text-zinc-200">
      <button
        type="button"
        onClick={onTogglePlay}
        className="rounded bg-emerald-700 px-3 py-1.5 font-medium hover:bg-emerald-600 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white"
        aria-label={playing ? "Pause replay" : "Play replay"}
      >
        {playing ? "Pause" : "Play"}
      </button>
      <input
        type="range"
        min={minMs}
        max={maxMs}
        value={currentMs}
        onChange={(event) => onSeek(Number(event.target.value))}
        className="flex-1 accent-emerald-600"
        aria-label="Seek match clock"
      />
      <span className="w-14 text-right font-mono tabular-nums">{formatClock(currentMs)}</span>
    </div>
  );
}
