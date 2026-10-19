# 20 — Frontend implementation blueprint (Next.js + R3F)

Implementation design, not a running build. Shared OpenAPI contracts take precedence over these illustrative interfaces.

## Suggested project organization
```text
apps/web/
  app/{page.tsx,match/[id]/page.tsx,about/page.tsx}
  src/components/{MatchShell,Scoreboard,ReplayBar,EventList,XRayInspector,BranchPanel}
  src/pitch/{Pitch3D,Pitch2D,PitchLines,PlayerInstances,Ball,CameraRig,OverlayGeometry}
  src/features/replay/{replayReducer,clockProjection,selectors}
  src/features/xray/{useXray,renderEvidence}
  src/features/multiverse/{branchReducer,useSimulation}
  src/lib/{apiClient,coordinateTransform,contracts,timeLabels,errorTypes}
  src/styles/{tokens.css,globals.css}
  tests/{replay.spec.ts,evidence.spec.ts,branch.spec.ts}
```

## Playback state and pure selectors
```ts
type ReplayState = {
  status: 'loading'|'paused'|'playing'|'seeking'|'error';
  matchClockMs: number;
  speed: 0.5|1|2;
  scenarioVersion: string;
  latestReceivedSeq: number;
};
```
- `seek(t)` explicitly sets matchClockMs and derives scoreboard/event list from original timeline; doesn't rely on elapsed Date.now accumulation.
- `play` uses wall-time delta only to advance within known match fixture clock while playing. On tab visibility change, refresh anchor and clamp; no speed spike.
- `openXray(moment)` pauses and stores prior state for return; `simulate` does not move original cursor.
- `replayReducer` forbids conflicting status combinations; prefer unit tests over ad-hoc useState soup.

## Rendering contract
- One pure coordinate transform function `pitchToWorld({x,y}) -> [x-52.5, height, y-34]`, same test vectors for R3F and 2D projection.
- API-owned `snapshotId` identifies each frame. When snapshots sparse, position interpolation is only a **visual affordance**, disclosed and disabled if gap too wide; tactical conclusions always refer to actual snapshots.
- Player markers can be instanced, colored by fictional team with accessible player list off-canvas. Ball height from z (not pitch y).
- Camera transitions 300–600ms for normal motion; 0ms reduced motion; bound OrbitControls and add reset.
- Overlay comes from API geometry: path (line), radius (circle), pressure region (polygon). Never invent arrows/pressure metrics in JSX.

## Data fetching strategy
- Query cache by `matchId+scenarioVersion`; all failed fetch states explicit. One match metadata+timeline+snapshot load on page entry, X-Ray lazy on click, Branch lazy on demand, explanation lazy on inspector open.
- Native fetch/React Query/SWR all viable; pick one, no unnecessary state library proliferation.
- WS optional enhancement; after reconnect, use timeline reconciliation and ignore stale seq. App must work when WS endpoint absent.
- Abort stale X-Ray/branch requests on seek or latest-request-wins; no outdated branch results in UI.

## Component acceptance examples
**ReplayBar:** keyboard Enter/Space play/pause, ArrowLeft/ArrowRight step, time text source from canonical game time; resilient on slow fetch.
**EvidenceInspector:** shows claim, evidence IDs, metric formulas/unit, tracking coverage and uncertainty; one click from highlighted overlay.
**Multiverse:** select PASS or SHOOT (valid options only), computing skeleton, original/branch toggle, no invented percentages, returned metrics explained as simulator outputs.
**AIStatus:** visually distinguishes verified Foundry narrative, template fallback and unavailable.
**ErrorBoundary:** gracefully swaps WebGL scene to 2D; preserves current match clock.

## Frontend performance and accessibility tests
- Jest/Vitest unit tests for reducer and transforms; Playwright for full flow incl. disabled WebGL where possible.
- Use PerformanceObserver/browser profiler or manual capture for actual FPS; record device/browser.
- Stable element IDs/data-testid only for durable core controls.
- DOM labels and textual scene narrative available to screen readers; reduced motion respects OS preference.
- Maintain contrast, responsive 2-column->stack layout, high pixel density budget, no offscreen CPU burn.

## Design acceptance
A professional 'sports intelligence' feel means clean typography, tactical hierarchy, source-linked insight and informative motion — not gratuitous glowing HUDs. No nonfunctional pills, fake percent cards, placeholder data from other clubs or dead settings.
