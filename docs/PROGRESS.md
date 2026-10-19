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

## Next task
P0-09 — X-Ray DTO endpoint (Day 3). `GET /moments/m01/xray`: REST data with geometry IDs, linked
features and missingness, wiring D1 (lane clearance) and D2 (pressure) into the contract's
`XRayResponse`/`InsightMetric`/`GeometryOverlay` schemas. AC: response valid, all referenced IDs
resolve. Completes Gate G3 ("ONE truthful insight").
