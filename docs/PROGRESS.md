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

## Next task
P0-05 — 3D pitch + fallback (Day 2). 105x68 plane, 22 markers, ball, 2D SVG fallback; shared
coordinate transform; camera presets; Play/Pause/Seek. Frontend still needs to fetch
`/api/v1/matches/demo-001` and `/timeline` and render it — that's what actually closes Gate G1.
