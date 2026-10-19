const PITCH_LENGTH_M = 105;
const PITCH_WIDTH_M = 68;

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center gap-4 bg-zinc-950 p-8 text-zinc-50">
      <h1 className="text-2xl font-semibold">BEYOND90 — Football X-Ray</h1>
      <p className="text-sm text-zinc-400">
        Synthetic fixture pitch preview. No real match, club, or player data.
      </p>
      <svg
        viewBox={`0 0 ${PITCH_LENGTH_M} ${PITCH_WIDTH_M}`}
        className="h-auto w-full max-w-3xl rounded border border-emerald-700 bg-emerald-900"
        role="img"
        aria-label={`Synthetic football pitch, ${PITCH_LENGTH_M} by ${PITCH_WIDTH_M} metres`}
      >
        <rect
          x={0.5}
          y={0.5}
          width={PITCH_LENGTH_M - 1}
          height={PITCH_WIDTH_M - 1}
          fill="none"
          stroke="white"
          strokeWidth={0.3}
        />
        <line
          x1={PITCH_LENGTH_M / 2}
          y1={0.5}
          x2={PITCH_LENGTH_M / 2}
          y2={PITCH_WIDTH_M - 0.5}
          stroke="white"
          strokeWidth={0.3}
        />
        <circle
          cx={PITCH_LENGTH_M / 2}
          cy={PITCH_WIDTH_M / 2}
          r={9.15}
          fill="none"
          stroke="white"
          strokeWidth={0.3}
        />
      </svg>
    </main>
  );
}
