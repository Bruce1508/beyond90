import type { XRayResponse } from "@/lib/api";

interface XRayPanelProps {
  xray: XRayResponse;
}

/**
 * Accessible DOM text companion to the geometry overlay drawn on the pitch (AGENTS.md: all
 * controls must work with keyboard/screen readers, not just a visual highlight). Every metric
 * and overlay in XRayResponse already carries evidenceIds; this just makes that text visible
 * and readable without relying on hovering a 2D <title> or orbiting the 3D scene.
 */
export function XRayPanel({ xray }: XRayPanelProps) {
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
        <ul className="mt-2 space-y-2">
          {xray.metrics.map((metric) => (
            <li key={metric.metricId}>
              <p>
                <span className="font-medium">{metric.metricId}</span>: {metric.value} {metric.unit}{" "}
                <span className="text-zinc-500">(algorithm {metric.algorithmVersion})</span>
              </p>
              <p className="text-zinc-400">Evidence IDs: {metric.evidenceIds.join(", ")}</p>
            </li>
          ))}
        </ul>
      )}
      {xray.caveats.map((caveat) => (
        <p key={caveat} className="mt-2 italic text-zinc-500">
          {caveat}
        </p>
      ))}
    </section>
  );
}
