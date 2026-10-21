"use client";

import type { BranchResult } from "@/lib/api";

interface BranchPanelProps {
  branch: BranchResult;
  showingAlternative: boolean;
  onToggle: () => void;
}

/**
 * Branch UX (P0-15, docs/04-FOOTBALL-ENGINES.md section C): "front end must label branch
 * results clearly" — this panel never lets the simulated outcome read as a real match fact.
 * Original/Alternative is a real toggle (not just an overlay appearing) so a judge can compare
 * the live snapshot against the synthetic branch directly.
 */
export function BranchPanel({ branch, showingAlternative, onToggle }: BranchPanelProps) {
  return (
    <section
      aria-label={`Simulated branch for moment ${branch.momentId}`}
      className="w-full max-w-3xl rounded border border-violet-700 bg-zinc-900 p-4 text-sm text-zinc-200"
    >
      <div className="flex items-center justify-between">
        <h2 className="font-semibold text-violet-400">What if? — {branch.momentId}</h2>
        <div className="flex gap-1" role="group" aria-label="Original or simulated alternative">
          <button
            type="button"
            aria-pressed={!showingAlternative}
            onClick={() => showingAlternative && onToggle()}
            className="rounded px-2 py-1 text-xs font-medium aria-pressed:bg-zinc-700 aria-pressed:text-white text-zinc-400 hover:bg-zinc-800 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-violet-400"
          >
            Original
          </button>
          <button
            type="button"
            aria-pressed={showingAlternative}
            onClick={() => !showingAlternative && onToggle()}
            className="rounded px-2 py-1 text-xs font-medium aria-pressed:bg-violet-700 aria-pressed:text-white text-zinc-400 hover:bg-zinc-800 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-violet-400"
          >
            Alternative (simulated)
          </button>
        </div>
      </div>
      {showingAlternative && (
        <div className="mt-2 space-y-1">
          <p className="inline-block rounded bg-violet-900 px-2 py-0.5 text-xs font-semibold uppercase tracking-wide text-violet-200">
            Simulated — not a real event
          </p>
          <p className="text-zinc-400">
            Engine {branch.engineVersion}, seed {branch.seed}, branch {branch.branchId}.
          </p>
          <ul className="list-disc space-y-1 pl-5 italic text-zinc-500">
            {branch.caveats.map((caveat) => (
              <li key={caveat}>{caveat}</li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}
