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

## Next task
P0-03 — One synthetic key moment. `examples/demo-001-mini-fixture.json` already contains a
candidate M01 (pressure -> interception -> possession change, 22 players, 2 snapshots) — next
step is a backend test that loads it, asserts the AC (22 player IDs, score-consistent,
schema-valid, replay fixture digest persisted in test), and wires it behind a loader.
