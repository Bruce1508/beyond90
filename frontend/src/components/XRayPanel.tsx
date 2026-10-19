"use client";

import { useState } from "react";
import type { TimelineMoment, XRayResponse } from "@/lib/api";

interface XRayPanelProps {
  xray: XRayResponse;
  moment: TimelineMoment | null;
}

/**
 * Plain-language definitions for known metricIds, shown by the evidence inspector. Unknown
 * metricIds (a detector added later without updating this glossary) fall back to a visible
 * "no definition" notice rather than silently omitting the explanation.
 */
const METRIC_DEFINITIONS: Record<string, string> = {
  PRESSURE_ASSOCIATED_TURNOVER:
    "Detector D2: within a configured window before a turnover, at least two PRESSURE events " +
    "from the team that gains possession, linked to an INTERCEPTION or PASS by that same team.",
};

function formatClock(ms: number): string {
  const totalSeconds = Math.floor(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}:${seconds.toString().padStart(2, "0")}`;
}

/**
 * Evidence inspector (AGENTS.md: "a judge can answer 'why does the AI say that?' within one
 * click"; all controls must work with keyboard, not just hover/orbit). Each metric is a real
 * <button> (native Enter/Space activation, no custom key handling needed) that expands to its
 * definition, the moment's real time window (from the already-existing timeline endpoint, not
 * a new field invented on the X-Ray contract), its metricId/evidenceIds, and its caveat.
 */
export function XRayPanel({ xray, moment }: XRayPanelProps) {
  const [expandedMetricId, setExpandedMetricId] = useState<string | null>(null);

  return (
    <section
      aria-label={`X-Ray evidence for moment ${xray.momentId}`}
      className="w-full max-w-3xl rounded border border-amber-700 bg-zinc-900 p-4 text-sm text-zinc-200"
    >
      <h2 className="font-semibold text-amber-400">
        X-Ray — {xray.momentId} <span className="font-normal text-zinc-400">({xray.confidence})</span>
      </h2>
      {xray.metrics.length === 0 ? (
        <p className="mt-2 text-zinc-400">No sufficiently evidenced insight for this moment.</p>
      ) : (
        <ul className="mt-2 space-y-1">
          {xray.metrics.map((metric, index) => {
            const panelId = `xray-inspector-${metric.metricId}`;
            const expanded = expandedMetricId === metric.metricId;
            const caveat = xray.caveats[index] ?? null;

            return (
              <li key={metric.metricId}>
                <button
                  type="button"
                  aria-expanded={expanded}
                  aria-controls={panelId}
                  onClick={() => setExpandedMetricId(expanded ? null : metric.metricId)}
                  className="w-full rounded px-2 py-1 text-left hover:bg-zinc-800 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-amber-400"
                >
                  <span className="font-medium">{metric.metricId}</span>: {metric.value} {metric.unit}{" "}
                  <span className="text-zinc-500">({expanded ? "hide" : "show"} evidence)</span>
                </button>
                {expanded && (
                  <div id={panelId} className="ml-2 mt-1 space-y-1 border-l border-amber-800 pl-3 text-zinc-300">
                    <p>
                      <span className="font-medium text-zinc-400">Definition: </span>
                      {METRIC_DEFINITIONS[metric.metricId] ?? "No definition recorded for this metric."}
                    </p>
                    <p>
                      <span className="font-medium text-zinc-400">Time: </span>
                      {moment ? `${formatClock(moment.startMs)}–${formatClock(moment.endMs)}` : "unavailable"}
                    </p>
                    <p>
                      <span className="font-medium text-zinc-400">ID: </span>
                      {metric.metricId} (algorithm {metric.algorithmVersion}) — evidence:{" "}
                      {metric.evidenceIds.join(", ")}
                    </p>
                    <p className="italic text-zinc-500">
                      <span className="font-medium not-italic text-zinc-400">Caveat: </span>
                      {caveat ?? "No caveat recorded."}
                    </p>
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
