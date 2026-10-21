# BEYOND90 — Progress log

Terse, factual, append-only per task. See `AGENTS.md` §9 for the resume contract.

## Last verified good state (2026-10-19)
- Backend: `cd backend && ./mvnw test` -> BUILD SUCCESS, 4 tests (contextLoads, health endpoint UP,
  demo fixture passes schema, out-of-bounds event rejected).
- Frontend: `cd frontend && pnpm build` -> green, static `/` page (synthetic pitch SVG placeholder).
- Git: committed on `main`, no uncommitted changes as of this entry.

## P0-01 — Repo bootstrap — DONE
- Backend: Spring Boot 4.1.1 / Java 21, Maven wrapper, Actuator `/actuator/health`.
- Frontend: Next.js 16 / React 19 / Tailwind v4, pnpm, synthetic 105x68m pitch SVG placeholder page.
- Verified: `./mvnw test` green, `pnpm build` green, README commands reproduced on this machine.
- Commit: `7927413`.

## P0-02 — Contract baseline — DONE
- Added `backend/src/main/java/com/beyond90/api/fixture/FixtureSchemaValidator.java`
  (`com.networknt:json-schema-validator` 1.5.6, draft 2020-12) loading
  `contracts/match-event.schema.json` directly off the classpath (backend `pom.xml` wires
  `../contracts` as a resource dir — single source of truth, no duplicated copy to drift).
- Test `FixtureSchemaValidatorTest`: `examples/demo-001-mini-fixture.json` passes schema;
  an inline fixture with an event at `x=999` (pitch max is 105) is rejected with a `maximum`
  validation error.
- OpenAPI structural validity verified: `npx @redocly/cli@latest lint contracts/openapi.yaml
  --extends minimal` -> "Your API description is valid" (19 cosmetic-only warnings: missing
  `summary`/`security` fields, expected pre-auth).
- Fixture IDs / canonical axes / clock: already consistent across `contracts/*`, `AGENTS.md`,
  and `examples/demo-001-mini-fixture.json` (x in [0,105], y in [0,68], `matchClockMs` ms since
  kickoff) — no changes needed, confirmed by the passing schema test.
- Verified: `(cd backend && ./mvnw test)` -> BUILD SUCCESS, 4/4 tests.
- Commands documented in `docs/08-QUALITY.md` §Contract lint.
- Not yet done: cross-event referential-integrity validator (score/order/ID resolution) —
  deferred to P0-04 fixture loader per schema's own doc comment ("Java FixtureValidator").

## P0-03 — One synthetic key moment — DONE
- `examples/demo-001-mini-fixture.json` already authored M01 (pressure -> interception ->
  possession change, 22 players, 2 snapshots) prior to this session; added the verification
  layer the AC requires.
- Added `FixtureIntegrityValidator` (duplicate-ID, team-cardinality, dangling-reference checks
  that JSON Schema can't express) and `DemoFixtureM01Test`:
  - schema-valid (reuses `FixtureSchemaValidator`, 0 errors)
  - 22 unique player IDs across exactly 2 teams
  - referentially consistent (all event/moment ID references resolve) and score-consistent
    (0 GOAL events in M01 -> implied 0-0, trivially consistent; a GOAL-bearing moment is
    exercised when M02/M03 land in P0-11)
  - stable SHA-256 digest of the fixture bytes pinned as a regression test
    (`4f6b1fe1...b1aad`, computed via `python3 -c "import hashlib; ..."` / `shasum -a 256`)
- Verified: `(cd backend && ./mvnw test)` -> BUILD SUCCESS, 8/8 tests.
- Gate G1 ("Java endpoint returns a valid loaded fixture and website draws it") is **not yet
  met** — fixture is validated but not yet exposed over HTTP. That's P0-04.

## P0-04 — Fixture loader + timeline — DONE
- `FixtureRepository` (`@Component`): loads `examples/demo-001-mini-fixture.json` once at
  startup, running schema + integrity validation first (fail-fast: a broken bundled fixture
  never serves). Parses into typed `Fixture` domain records (`com.beyond90.api.fixture.Fixture`).
- `MatchController`: `GET /api/v1/matches/{matchId}` (MatchSummary) and
  `/api/v1/matches/{matchId}/timeline` (Timeline) per `contracts/openapi.yaml`; events are
  always re-sorted by canonical `seq` (not file authoring order) before serialization.
  Unknown `matchId` -> `MatchNotFoundException` -> `ApiExceptionHandler` -> RFC 7807
  `application/problem+json` 404, matching the `Problem` schema.
- Score projection: contract has no score field (`MatchSummary`/`Timeline` don't define one) —
  did not invent one; "same time -> same state" AC is met by the timeline being a pure,
  deterministic function of the immutable fixture (verified by a repeated-call equality test),
  with score left as a client-side derivation from `GOAL` events per architecture ("rendering
  is a pure projection of canonical data").
- Bug caught and fixed before commit: `examples/` was wired only as a **test** resource, so
  `spring-boot:run` crashed at startup (`BeanCreationException` on `FixtureRepository`,
  `IllegalStateException: Missing classpath resource`). Fixed by moving it into
  `<build><resources>` in `pom.xml` (the demo fixture ships with the running app, not just
  tests).
- Verified: `(cd backend && ./mvnw test)` -> BUILD SUCCESS, 15/15 tests. Live smoke test via
  `./mvnw spring-boot:run` + `curl`: `/matches/demo-001` returns the full fixture (22 players,
  homeTeam=blue/awayTeam=red); `/timeline` returns 4 events ordered seq 1-4 and moment `m01`;
  unknown matchId returns `404` with `{"status":404,"title":"Resource absent",...}`.
- **Gate G1 Java-side requirement met**: a real endpoint returns a valid loaded fixture.
  Website-draws-it half is still open — current frontend page is a static SVG placeholder
  (P0-01), not wired to the API yet. That's P0-05/P0-06.

## P0-04b — snapshots endpoint (unplanned, needed to draw real positions) — DONE
- Added `GET /api/v1/matches/{matchId}/snapshots` (`startMs`/`endMs` optional filters, sorted by
  clock) — not explicitly in P0-04's task text but required by `contracts/openapi.yaml` and by
  P0-05/06 to draw real player/ball positions (roster alone has no coordinates).
- Added CORS allowlist (`WebConfig`) for the Next.js dev origins.
- Verified: 2 new MockMvc tests, `(cd backend && ./mvnw test)` -> 17/17 green.

## P0-05 — 3D pitch + fallback — DONE
## P0-06 — UI playback integration — DONE (partial: event feed/overlay deferred)
- `frontend/src/lib/pitch.ts`: shared coordinate transform (canonical x∈[0,105]/y∈[0,68] ->
  SVG viewBox units for 2D, centered world X/Z for 3D) — single source of truth for both views.
- `frontend/src/lib/webgl.ts` + `PitchViewer.tsx`: real WebGL feature-detection; renders
  `Pitch3D` (R3F Canvas + OrbitControls, drei/three pinned to React-19-compatible versions
  `@react-three/fiber@9.8.2`, `@react-three/drei@9.122.0`, `three@0.186.1`) when available,
  `Pitch2D` (SVG) otherwise. AGENTS.md: "production build and core workflow must work without
  WebGL" — satisfied structurally (no R3F import at module scope blocks render; fallback path
  has zero WebGL dependency).
- `frontend/src/lib/api.ts`: typed fetch client for `/matches/{id}` and `/snapshots`.
- `page.tsx`: fetches match + snapshots on mount, drives a `requestAnimationFrame` clock,
  displays the latest snapshot at-or-before the cursor (never interpolated — "sparse curated
  snapshots" per contract, interpolation would be a scientific-claim violation per AGENTS.md).
  Synthetic-data label always visible. Loading and fetch-error states both render (no blank
  screen if backend is down).
- Camera presets: scoped down to interactive `OrbitControls` only (no fixed preset buttons) —
  unrequested extra UI, can add named presets later if the demo needs a one-click angle.
- Event feed / moment overlay: deferred — contract has no time-filtered event endpoint yet and
  this naturally belongs with the X-Ray UI (P0-09/10), not bare playback.
- **Verified live** (not just build/lint/typecheck, since this is a UI change):
  - Started both dev servers (`./mvnw spring-boot:run`, `pnpm dev`); `pnpm dev` fell back to
    port 3001 (3000 was bound by an unrelated local Docker `hbci` listener) — added 3001 to the
    CORS allowlist as a pragmatic dev convenience.
  - No Claude-in-Chrome extension available in this environment; used the Playwright MCP plugin
    instead for a real rendered-browser check.
  - Screenshot confirms: 11 blue + 11 red player markers correctly colored by team, white ball
    marker, synthetic-fixture label, Play button, seek slider at the correct initial clock
    (`21:50` = M01's `matchClockMs` 1,310,000). Console: 0 errors (1 benign `THREE.Clock`
    deprecation warning from inside drei/three, not our code).
  - Clicked Play via `browser_evaluate`; re-snapshot showed the clock advance (21:50 -> 21:53)
    and the button flip to "Pause replay" — playback loop genuinely works, not just renders.
  - Fixed on the way: default `create-next-app` page `<title>` ("Create Next App") was still
    live — replaced with the real app name/description.
  - Not verified live: the 2D (WebGL-off) fallback path specifically — code path is symmetric
    with the 3D one and shares the same data/props, but no WebGL-disabled browser context was
    exercised this session.
- `(cd backend && ./mvnw test)`: 17/17 green. `(cd frontend && pnpm lint && pnpm build)`: green.

## P0-07 — Lane metrics + tests — DONE
- `com.beyond90.api.analytics.LaneClearanceCalculator` (Detector D1, docs/04-FOOTBALL-ENGINES.md):
  perpendicular distance from each defender to the passer-receiver line segment, projection
  clamped to `[0,1]`; `clearanceMeters` = min over defenders (passer/receiver excluded);
  `contested` when `clearanceMeters < thresholdMeters` (default 2.0m, a heuristic per the doc,
  not an empirical intercept probability). Returns `LaneClearanceResult` carrying `metricId`,
  `evidenceIds` (snapshotId + considered defender IDs), `algorithmVersion` (`D1.v1`), `contested`,
  `defenderCount`, `contestingDefenderCount` — satisfies the AC directly (no separate DTO mapping
  needed yet; P0-09 will map this into the contract's `InsightMetric`).
- Golden-vector tests (`LaneClearanceCalculatorTest`, 6 cases) per the doc's own D1 test list:
  straight horizontal segment, defender beyond endpoint (projection clamps to the receiver),
  near-zero-length pass (segment collapses to the passer point), no defenders (clearance =
  `+Infinity`, not contested), attack-direction invariance (reversing passer/receiver doesn't
  change the perpendicular distance — the calculator is purely geometric on x/y), and
  passer/receiver self-exclusion from the defender set.
- Not implemented (optional per the doc, out of scope for this AC): excluding defenders "far
  behind" the segment's start — only the required `[0,1]` projection clamp was built.
- Verified: `(cd backend && ./mvnw test)` -> BUILD SUCCESS, 23/23 tests (17 prior + 6 new).

## P0-08 — Pressure detector — DONE
- `com.beyond90.api.analytics.PressureDetector` (Detector D2, docs/04-FOOTBALL-ENGINES.md):
  within an 8s window (default, inside the doc's required 5-10s band; configurable per call)
  before a `POSSESSION_CHANGE`, requires >=2 `PRESSURE` events from the possessing team AND a
  linking `INTERCEPTION`/`PASS` event from that same team in-window. Emits
  `PRESSURE_ASSOCIATED_TURNOVER` with an explicit non-causal caveat ("NOT evidence that pressure
  caused the turnover") — satisfies the AC by construction, not just by test assertion.
- Golden-vector tests (`PressureDetectorTest`, 6 cases), the first mirroring the real M01 fixture
  values (`evt_press_1/2`, `evt_interception_1`, `evt_poss_1`) exactly: two-press-plus-turnover
  (positive, matches M01), turnover without pressure, late pressure after the turnover, cross-team
  ID mismatch, below-minimum single pressure event, and pressure events falling outside a
  shortened window — all four doc-specified negative cases covered plus two extra boundary cases.
- Verified: `(cd backend && ./mvnw test)` -> BUILD SUCCESS, 29/29 tests (23 prior + 6 new).

## P0-09 — X-Ray DTO endpoint — DONE
- `GET /api/v1/moments/{momentId}/xray` (`XRayController` -> `XRayService`), implementing
  `contracts/openapi.yaml`'s `XRayResponse`/`InsightMetric`/`GeometryOverlay`/`Point` schemas.
  `FixtureRepository.findFixtureContainingMoment` added (the contract path has no matchId).
  `MomentNotFoundException` -> 404 `application/problem+json` via `ApiExceptionHandler`
  (factored the existing match-404 logic into a shared `notFound(...)` helper, same response
  shape, no behavior change to the existing `/matches/{matchId}` 404).
- **Only Detector D2 (pressure) is wired, not D1 (lane clearance)**: M01 has no `PASS` event with
  a recorded passer/receiver pair — it's a pressure/interception moment, not a passing-lane one.
  Wiring D1 against M01 would mean inventing an evidence basis the fixture doesn't have (AGENTS.md
  anti-slop: "do not invent measurements"). D1 wiring is deferred to P0-11's M02/M03 fixtures,
  which are explicitly scoped to author a moment where a real pass+defenders exist.
  `LaneClearanceCalculator` itself (P0-07) is unchanged and ready to be called once that data exists.
- Confidence derivation (doc §B: "derive deterministically from missingness, coverage and evidence
  checks, not model self-reported probability"), concretely for this detector: `SUPPORTED` when
  `PressureDetector` fires with full evidence (M01's case), `PARTIAL` when a turnover exists but the
  detector's own thresholds aren't met, `INSUFFICIENT` when the moment has no turnover event at all.
- For M01: `confidence=SUPPORTED`, one `InsightMetric` (`PRESSURE_ASSOCIATED_TURNOVER`, value=2,
  unit=count, 4 evidenceIds, algorithmVersion=`D2.v1`), one `LINE` geometry overlay connecting the
  two real `PRESSURE` event locations, top-level `evidenceIds` = the union of all metric evidence
  (all 4 IDs resolve to real M01 events — asserted directly in `XRayServiceTest`), one caveat
  carrying the detector's explicit non-causal disclaimer.
- Tests: `XRayServiceTest` (4, incl. the AC's own "all referenced IDs resolve" check against the
  real fixture's event IDs) + `XRayControllerTest` (2, MockMvc, 200 shape + 404 problem).
- Verified: `(cd backend && ./mvnw test)` -> BUILD SUCCESS, 35/35 tests (29 prior + 6 new). Live
  smoke: `./mvnw spring-boot:run` + `curl localhost:8080/api/v1/moments/m01/xray` returned the
  exact expected JSON (confidence SUPPORTED, 4 evidenceIds, D2.v1); `/moments/nope/xray` -> 404.
- **Gate G3 ("ONE truthful insight") MET** — a real, evidence-backed, non-causal insight is served
  over a real HTTP endpoint for a real fixture moment.

## P0-10 — 3D tactical overlay — DONE
- `frontend/src/lib/api.ts`: `getXRay(momentId)` + `InsightMetric`/`GeometryOverlay`/`XRayResponse`
  types mirroring the P0-09 contract response exactly.
- `Pitch2D.tsx` / `Pitch3D.tsx`: both gained an optional `geometry?: GeometryOverlay[]` prop,
  rendered as an amber dashed line (SVG `<polyline>` with a `<title>` for 2D; drei `<Line dashed>`
  for 3D) — visually distinct from real tracked positions, since it's derived evidence, not a
  tracked path. `PitchViewer.tsx` passes it through unchanged (existing `{...props}` spread).
- Camera X-Ray focus: `Pitch3D` computes the overlay's centroid (`overlayFocusTarget`, memoized on
  a stable key so `OrbitControls` doesn't fight the user's own drag every render) and sets it as
  `OrbitControls`'s `target`, recentering the view on the evidence when X-Ray mode is active.
- `XRayPanel.tsx` (new): accessible DOM text region (`aria-label`, real `<h2>`/`<p>`/`<ul>`, not
  relying on hover-only SVG `<title>` or orbiting the 3D scene) listing every metric's ID, value,
  algorithm version, evidenceIds, and caveats — satisfies the AC "traceable to metric/snapshot ID"
  independent of whether WebGL is available.
- `page.tsx`: a "Show X-Ray evidence (m01)" toggle button lazily fetches `/moments/m01/xray` on
  first click (no unconditional extra request on page load) and feeds `geometry` into
  `PitchViewer` + renders `XRayPanel`; has its own loading/error state, consistent with the
  existing match/snapshot fetch pattern.
- Not implemented: only `LINE` overlays exist to render (D1/lane-clearance's `CIRCLE`/`POLYGON`
  cases aren't wired backend-side yet per P0-09's note — nothing to build against until P0-11).
- Verified: `(cd frontend && pnpm lint && pnpm build)` -> both green (TypeScript clean).
  **Verified live**: started both dev servers, used Playwright MCP — clicked the toggle,
  confirmed the accessible panel renders the real metric/evidenceIds/caveat text, and the amber
  dashed overlay line is visible on the 3D pitch between the two real PRESSURE event locations,
  camera recentered near it. Console: 0 errors (1 benign pre-existing THREE.Clock warning).
  **Not verified live** (same gap noted in P0-05/06): the 2D WebGL-off fallback path specifically
  — code is symmetric with the 3D path and shares the same `geometry` prop/data, but no
  WebGL-disabled browser context was exercised this session either.

## P0-11 — M02/M03 fixtures — DONE
- Extended `examples/demo-001-mini-fixture.json` (same single fixture, same 22-player roster --
  no new fixture file) with two new moments, reusing M01's players/teams:
  - **M02** (off-ball space creation, Detector D3): `snap_m02_a`/`snap_m02_b`, a focused 3-player
    subset (ball-holder `blue_01`, runner `blue_09` separating from `9 -> 1` to `21` units,
    marking defender `red_09` lagging behind). `eventIds: []` deliberately -- D3 has no logged
    event type of its own, it's derived purely from the snapshot pair, so this is the fixture's
    own "missingness" shape, not a gap.
  - **M03** (Multiverse decision fork): `snap_m03_a`, the full 22-player roster repositioned into
    a realistic SECOND_HALF attacking scene (blue now attacks right-to-left per the period's own
    `attackDirectionByTeam`), ball at `blue_07`'s feet, `availableActions: [PASS, SHOOT]`,
    `actorId: blue_07`, `baseSnapshotId: snap_m03_a`. `eventIds: []` too -- the decision is still
    pending, nothing has "happened" yet at this freeze-frame.
  - **Did not** add a PASS `MatchEvent` for M02/M03 (would need an actor/target pair + a real
    pass attempt) -- D1 (lane clearance) still has no genuine evidence to wire into the X-Ray
    endpoint; that stays deferred until a moment actually needs it (P1 X-Ray expansion, not P0).
- New test: `DemoFixtureM02M03Test` (5 cases) -- exactly 3 moments (`m01,m02,m03`), M02's
  missingness shape, M03's decision-fork labeling + 22-player scene, schema/integrity validity,
  fixture-level `synthetic`/`scenarioVersion` labeling still present.
- Fixed fallout from extending the shared fixture file (not new bugs, expected consequences):
  `DemoFixtureM01Test`'s SHA-256 digest-pin updated to the new real file hash (verified via
  `shasum -a 256`, not guessed); `MatchControllerTest`'s two snapshot-count assertions updated
  (2 -> 5 total snapshots, 1 -> 4 for the `startMs=1316000` filter) and renamed
  (`...ReturnsBothSnapshotsOrderedByClock` -> `...ReturnsAllSnapshotsOrderedByClock`).
- **Process note**: the first `./mvnw test` run after editing the fixture reported a false
  green (used a stale `target/classes` resource copy left over from the earlier P0-10 live-smoke
  session) -- caught by manually re-computing the file's SHA-256 and comparing against the test's
  pinned constant before trusting the result, then re-ran with `./mvnw clean test` to confirm.
- Verified: `(cd backend && ./mvnw clean test)` -> BUILD SUCCESS, 40/40 tests (35 prior + 5 new).

## P0-12 — Evidence inspector — DONE
- `frontend/src/lib/api.ts`: `getTimeline(matchId)` + `TimelineMoment`/`TimelineResponse` types,
  reusing the already-existing P0-04 `/matches/{matchId}/timeline` endpoint for the moment's real
  `startMs`/`endMs` rather than inventing a new field on the P0-09 X-Ray contract.
- `XRayPanel.tsx` rewritten: each metric is now a real `<button>` (native Enter/Space activation,
  `aria-expanded`/`aria-controls`) that toggles an expanded detail block showing **Definition**
  (a small `METRIC_DEFINITIONS` glossary keyed by metricId, with a visible "no definition
  recorded" fallback for an unknown future metricId rather than silently omitting it), **Time**
  (the moment's real `startMs`-`endMs` window formatted as `mm:ss`), **ID** (metricId + algorithm
  version + evidenceIds), and **Caveat** (matched to the metric by array position — the contract
  ties caveats to metrics only positionally today, both lists have length 1 in practice).
- `page.tsx`: the existing X-Ray toggle now fetches `getXRay` + `getTimeline` together and passes
  the matching moment down; closing X-Ray clears both.
- Verified: `(cd frontend && pnpm lint && pnpm build)` -> both green. **Verified live** via
  Playwright, keyboard-only (not a mouse click, to actually prove the AC): clicked "Show X-Ray"
  once, then `Tab` to the metric button + `Enter` — expanded correctly
  (`[expanded]` state), showing the real definition text, `Time: 21:50-21:56` (matches M01's
  actual `startMs`/`endMs` 1310000-1316200ms exactly), the real evidenceIds, and the non-causal
  caveat. 0 console errors.
- **Gate G4 ("why does the AI say that? within one click") MET** — one click (or one Tab+Enter)
  reveals the full evidenced explanation, keyboard-only, independent of WebGL/3D.

## P0-13 — Pure simulator — DONE
- New package `com.beyond90.api.simulation`: `BranchSimulator` (pure, static, no Spring
  dependency — same style as `PressureDetector`/`LaneClearanceCalculator`), `SimulationDtos`
  (`SimulationAction`, `TrajectoryPointDto`, `SimulationResult`, `ActionType`/`Outcome` enums),
  `InvalidActionException` (unmapped for now — HTTP 422 mapping is P0-14's job).
- Pipeline per docs/04-FOOTBALL-ENGINES.md section C: `ActionValidator` (actor must be within
  `POSSESSION_RADIUS_METERS=3.0` of the ball; PASS target must be a different, existing player;
  destination must be inside the pitch; SHOOT target must sit on the attacking goal line within
  `GOAL_LINE_TOLERANCE_METERS=0.5`) -> straight-line kinematics at `dt=100ms` (`PASS` 18 m/s,
  `SHOOT` 26 m/s, capped at `MAX_DURATION_MS=8000`) -> defender collision check (perpendicular
  distance from each defender to the straight ball path, clamped projection, same geometry as D1's
  `LaneClearanceCalculator`, intentionally duplicated rather than cross-package-coupled) ->
  outcome (`COMPLETED`/`INTERCEPTED` for PASS; `ON_TARGET`/`BLOCKED`/`MISS` for SHOOT, goal mouth
  = `GOAL_WIDTH_METERS=7.32` centered on the pitch width).
- `ponytail` simplifications, both flagged in code comments with an upgrade path: (1) defenders
  are static during the trajectory (no `DefenseResponse` pursuit yet — add if/when evaluation
  needs moving defenders); (2) a defender's interception point is approximated as their closest-
  approach point on the path rather than solving exact circle-entry — good enough for the
  geometric pass/fail check, not a physics claim.
- `seed` is accepted and echoed back in the result but unused by the (currently fully
  deterministic, randomness-free) geometry — wire in if/when Monte Carlo variants are added per
  the doc's Reproducibility section.
- 11 golden-vector tests in `BranchSimulatorTest`: determinism (same snapshot/seed/action ->
  `assertThat(result1).isEqualTo(result2)`), PASS intercepted (defender exactly on the line) vs.
  completed (defender far off-line), SHOOT on-target / outside-goal-rectangle MISS / blocked,
  and 4 validator-rejection cases (actor not possessing ball, PASS targeting self, SHOOT target
  off the goal line, destination outside the pitch) each asserting `InvalidActionException` +
  a specific message fragment.
- **Did not** touch `examples/demo-001-mini-fixture.json` or any existing file — original fixture
  digest is unchanged by construction (`git status --short` shows only new files under
  `backend/src/{main,test}/java/com/beyond90/api/simulation/`), satisfying the AC without needing
  a redundant digest-recompute test (Java records are immutable; nothing in this task had write
  access to the fixture).
- Verified: `(cd backend && ./mvnw clean test)` -> BUILD SUCCESS, 51/51 tests (40 prior + 11 new).

## Next task
P0-14 — Branch API + tests (Day 5). `POST /simulate` wiring `BranchSimulator` into a real HTTP
endpoint: map `InvalidActionException` -> 422 in `ApiExceptionHandler`, resolve a moment's
`actorId`/`baseSnapshotId`/pitch/attack-direction/opposing-defenders from the `Fixture` the way
`XRayService` already resolves a moment, add rate/size limits, and prove idempotency + parent-
fixture immutability across 100 branch calls (per the doc's golden test list). Then P0-15 (branch
UX: Original/Alternative toggle + trajectory preview), closing Gate G5 ("what if pass vs shoot?
interactive, calculated, truthful").

## Correction before P0-14 — P0-13's `SimulationAction` did not match the wire contract
- Caught while reading `contracts/openapi.yaml` before building the API layer: `SimulateRequest`
  only has `scenarioVersion`/`action`/`target`/`seed` — no receiver player ID. P0-13's
  `SimulationAction(type, actorId, targetActorId, target)` invented a field the real contract
  doesn't have.
- Fixed at the root (not patched around): dropped `targetActorId` from `SimulationAction` (now
  `type, actorId, target`); `BranchSimulator`'s PASS validation changed from "target a different,
  existing player" to "target must be >= `POSSESSION_RADIUS_METERS` from the actor's own current
  position" (a degenerate zero-distance pass is still rejected, just without inventing a receiver
  identity); the defender-exclusion filter now excludes only the actor, not a non-existent
  receiver. Updated `BranchSimulatorTest` accordingly (same 11 cases, `passTargetingSelf_rejected`
  renamed to `passTargetingOwnPosition_rejected`).
- Re-verified: `(cd backend && ./mvnw clean test)` -> BUILD SUCCESS, 51/51 (unchanged count,
  same tests re-pointed at the corrected shape).

## P0-14 — Branch API + tests — DONE
- New in `com.beyond90.api.simulation`: `SimulationService` (resolves a moment's actor/base
  snapshot/pitch/attack-direction/opposing-defenders from the `Fixture`, the same way
  `XRayService` resolves a moment for its endpoint), `SimulationController` (`POST
  /api/v1/moments/{momentId}/simulate`), and wire DTOs added to `SimulationDtos`
  (`PointDto`/`SimulateRequestDto`/`BranchFrameDto`/`BranchResultDto`) matching
  `contracts/openapi.yaml`'s `SimulateRequest`/`BranchResult` schemas exactly — no contract
  changes needed.
- Validation order in `SimulationService.simulate`: `scenarioVersion` must match the loaded
  fixture's (rejects a stale client replaying against a since-changed fixture) -> moment must
  have an `actorId`/`baseSnapshotId` (m01/m02 don't — they're not decision-fork moments) ->
  requested `action` must be in the moment's own `availableActions` list -> `action` must be
  PASS or SHOOT (CARRY is a declared P1 value in the contract's enum with no engine support yet,
  rejected cleanly rather than faked) -> `target` must be present. All five rejection paths throw
  `InvalidActionException`, newly mapped in `ApiExceptionHandler` to HTTP 422
  (`application/problem+json`, matching the X-Ray endpoint's existing problem-response style).
- Attack direction is resolved from `fixture.periods()` at the moment's actual `startMs` (not the
  team's nominal `firstHalfAttackDirection`) — m03 sits in `SECOND_HALF` (`startMs=3,000,000` >=
  the second period's `2,800,000`), where blue's attack direction flips to `RIGHT_TO_LEFT`; using
  the team's first-half default would have pointed the goal-line check at the wrong end of the
  pitch.
- Idempotency: in-memory `ConcurrentHashMap<String, BranchResultDto>` keyed by
  `momentId + "|" + Idempotency-Key` header (when present) — a replayed key returns the exact
  cached `BranchResultDto` (same `branchId`) instead of recomputing, proven by a test that sends
  two different request bodies under the same key and asserts identical `branchId`s back.
  Unbounded (no TTL/eviction) — fine for this single-process synthetic demo; flagged in code as
  a `ponytail` simplification with an upgrade path if this ever serves real concurrent traffic.
- `BranchResult`'s wire schema has no `outcome`/`reachedTarget`/`shotOnTarget`/`nearestDefenderMeters`
  fields (only `frames` + `caveats`) — the engine's richer `SimulationResult` is folded into two
  `caveats` strings: a fixed disclosure line (synthetic kinematics, unmodeled fatigue/intent/
  goalkeeper anticipation/tracking error) plus an outcome-specific line (e.g. "Outcome: BLOCKED.
  Nearest defender approach: 0.3m. Path length: 4.2m.") — preserves the "label branch results
  clearly" requirement without inventing new contract fields.
- 12 new tests: 8 in `SimulationServiceTest` (happy path, mismatched scenarioVersion, action not
  in availableActions, unknown moment 404, moment without actor/snapshot, missing target,
  idempotent replay, independent keys) + 4 in `SimulationControllerTest` (happy path over
  MockMvc, CARRY -> 422 problem+json, unknown moment -> 404 problem+json, and the AC's own
  **100 branch calls across both actions, then `assertThat(fixtureAfter).isEqualTo(fixtureBefore)`**
  proving parent-fixture immutability).
- Verified: `(cd backend && ./mvnw clean test)` -> BUILD SUCCESS, 63/63 tests (51 prior + 12 new).

## P0-15 — Branch UX — DONE
- `api.ts`: `postJson` helper (mirrors `getJson`, surfaces a Problem's `detail` text on non-2xx)
  plus `simulateMoment(momentId, request)` and `SimulateRequest`/`BranchFrame`/`BranchResult`
  types matching the P0-14 contract exactly.
- `Pitch2D.tsx`/`Pitch3D.tsx`/`PitchViewer.tsx`: new optional `branchTrajectory?: {x,y}[]` prop,
  rendered as a **solid violet** polyline/Line — deliberately a different color and style from
  the X-Ray evidence overlay's amber dashed line, so a simulated branch can never be mistaken
  for real evidence. `Pitch2D`'s and `Pitch3D`'s existing `geometry` prop (evidence, tied to real
  `evidenceIds`) is untouched; the branch trajectory is a separate prop precisely so the two
  concepts — observed evidence vs. simulated what-if — never share a code path.
- New `BranchPanel.tsx`: an Original/Alternative toggle (two real `<button>`s with
  `aria-pressed`, same interaction pattern as `XRayPanel`'s evidence buttons) that, when
  "Alternative" is active, shows a `SIMULATED — NOT A REAL EVENT` badge, the engine
  version/seed/branchId, and the branch's `caveats` list verbatim (never summarized away).
- `page.tsx`: two preset buttons, "What if — PASS?" and "What if — SHOOT?", both targeting
  moment `m03` (the only authored decision-fork moment) with fixed, hand-picked targets and a
  fixed seed (`BRANCH_SEED=1`) — no click-to-target UI yet, out of scope for this task (P1); the
  seed is fixed only for demo repeatability, not because the engine needs it (it's already fully
  deterministic). `PitchViewer`'s `branchTrajectory` prop is only populated while
  `showingAlternative` is true, so switching to "Original" genuinely removes the overlay rather
  than just graying it out.
- **Found and fixed a real rendering bug during live verification** (not caught by
  `pnpm lint`/`pnpm build`, which are type/syntax checks, not visual ones): the 3D branch
  trajectory `<Line>` was invisible on first click — it sat exactly on the pitch mesh's y=0
  surface and z-fought with it. The X-Ray evidence overlay already had the fix for this exact
  problem (`+0.3` to the world-space y/height component), just never applied to the new branch
  line. Caught via Playwright screenshot (not by reading the code), fixed, re-verified.
- Verified: `(cd frontend && pnpm lint && pnpm build)` -> both green (twice — before and after
  the z-fighting fix). **Verified live** via Playwright against the real running backend
  (`./mvnw spring-boot:run`) + frontend (`pnpm dev`): clicked "What if — SHOOT?" -> toggle
  switched to "Alternative (simulated)" automatically, panel showed the real
  `engineVersion=SIM.v1`, `seed=1`, a real `branchId`, and caveats including
  `Outcome: BLOCKED. Nearest defender approach: 0.0m. Path length: 17.0m.` (red_01 sits exactly
  on the goal-line shot path in the real fixture — a legitimate BLOCKED outcome, not a forced
  demo value); the violet trajectory line rendered on the pitch after the fix. Clicked "Original"
  -> trajectory disappeared, button states swapped. 0 console errors (1 pre-existing, unrelated
  `THREE.Clock` deprecation warning from the R3F library itself).
- **Gate G5 ("what if pass vs shoot? interactive, calculated, truthful") MET** — both branches
  are real HTTP round-trips through the P0-13 engine, not canned/free-form text; the BLOCKED
  outcome in particular demonstrates the simulation is not rigged to always "succeed."

## Next task
Day 6 (Oct 24) — Real Microsoft Foundry and credible agent workflow, starting with **P0-16 Java
Foundry spike**: verify deployment, credentials, a real Java API call against Foundry, validate
response shape/timeouts. AC: actual model inference observed + a sanitised trace. If Foundry
access is unavailable, log the blocker immediately rather than stubbing around it — per
AGENTS.md, do not fabricate "Foundry integration" with a fake/local call.

## BLOCKER — P0-16 cannot start: no Foundry credentials in this environment (2026-10-21)
- Checked before attempting P0-16, per `docs/10-PLATFORM-AND-OPS.md`'s own instruction ("If
  Foundry access or credits are unavailable, immediately document the blocker... resolve real
  provider integration before claim of category fit or submission").
- `docs/10-PLATFORM-AND-OPS.md` names the expected env vars: `FOUNDRY_BASE_URL`,
  `FOUNDRY_DEPLOYMENT`, `FOUNDRY_API_KEY` (local fallback only), `AZURE_CLIENT_ID` (when needed).
  None of these are set in this shell (`env | grep -i "FOUNDRY\|AZURE"` -> empty).
- No `.env*` file exists anywhere in the repo (`find . -iname "*.env*"` -> empty, excluding
  `node_modules`/`.git`/`target`).
- `backend/src/main/resources/application.properties` has exactly one line
  (`spring.application.name=beyond90-api`) — no Foundry/Azure config block has ever been added.
- **Not attempting**: a fake/local stand-in call that pretends to be Foundry inference. P0-16's
  own AC requires "actual model inference observed + a sanitised trace" — that's unsatisfiable
  without a real deployment, and AGENTS.md explicitly forbids fabricating integration claims.
- **Unblocking this requires a human**: an Azure AI Foundry resource + model deployment
  provisioned, and `FOUNDRY_BASE_URL`/`FOUNDRY_DEPLOYMENT`/`FOUNDRY_API_KEY` (or
  `AZURE_CLIENT_ID` for managed identity) supplied via environment variables or a local
  `.env`/secrets file not committed to the repo. Until then, P0-16/17/18 (all of Day 6) stay
  blocked.
- Per the doc's own fallback contract, this does not block the rest of the app: the frontend's
  deterministic fallback (template-based, no model call) is the documented behavior when Foundry
  is unreachable, and nothing built in P0-07 through P0-15 depends on Foundry.

## 2D WebGL-off fallback — verified live, gap closed (2026-10-21)
- Open since P0-05/06, re-flagged after P0-10/P0-12 (which both touch `Pitch2D`/`XRayPanel`) and
  again after P0-15 (adds a third overlay, `branchTrajectory`, to the same components). With
  Day 6 blocked on Foundry, this was the highest-priority unblocked unverified item.
- Forced WebGL off in a real browser (Playwright, `page.addInitScript` overriding
  `HTMLCanvasElement.prototype.getContext` to return `null` for `webgl`/`webgl2`/
  `experimental-webgl` — `hasWebGl()` in `lib/webgl.ts` then correctly reports `false` and
  `PitchViewer` renders `Pitch2D`, not `Pitch3D`), against the real backend (port 8081 — see
  note below) and real frontend (`pnpm dev`).
- Confirmed working, **no code changes needed**: the 2D SVG fallback renders all 22 player poses
  + ball; the X-Ray amber dashed evidence overlay (P0-10); and the branch trajectory violet solid
  overlay (P0-15) — all three simultaneously, correctly colored and positioned, no overlap bugs.
  0 console errors, 0 warnings (cleaner than the 3D path, which has an unrelated pre-existing
  `THREE.Clock` deprecation warning from R3F that obviously doesn't apply when R3F never loads).
- **This closes the gap for good** — it's been re-flagged as open three times across P0-05/06,
  P0-10/12, and P0-15 without ever actually being exercised in a real browser; now it has been,
  with both overlays that exist as of this session proven correct together.
- Environment note, not a code issue: local port 8080 was held by Docker Desktop's own proxy
  (`com.docke...`, unrelated container), so the backend was run on `--server.port=8081` with
  `NEXT_PUBLIC_API_BASE_URL=http://localhost:8081` for this verification session only — no
  project config changed.
