# 08 — Engineering quality, tests and measurable release gates

## CI workflow (required)
Every pull request/push to main:
1. Java: `./mvnw -B verify` (JUnit 5, AssertJ, MockMvc, Spring context and fixture golden tests). Maven Wrapper committed by implementation agent.
2. Frontend: `corepack pnpm install --frozen-lockfile && pnpm lint && pnpm typecheck && pnpm test && pnpm build` (create scripts as part of bootstrapping).
3. Contract lint:
   - OpenAPI: `npx @redocly/cli@latest lint contracts/openapi.yaml --extends minimal` (structural validity; `minimal` ruleset skips cosmetic/style-only warnings).
   - Fixture JSON Schema: `(cd backend && ./mvnw test -Dtest=FixtureSchemaValidatorTest)` (`com.networknt:json-schema-validator` 1.5.6, draft 2020-12, loads `contracts/match-event.schema.json` straight off the classpath — no duplicated copy, so contract drift fails the build instead of silently diverging).
   Fail if contract drift.
4. Dependency and secret scans (Dependabot/dependency-review, CodeQL if available, gitleaks locally or supported GitHub secrets scanning). Do not push unreviewed secrets.
5. Container image build and smoke startup (P0 before deployment).

## Test pyramid
| Layer | Mandatory examples | Gate |
|---|---|---|
| Unit/domain | Euclidean lane projection, normalization, timestamps, seed determinism, score transitions | All P0 test vectors pass |
| Data fixture | JSON Schema + referential integrity + snapshot coverage | No invalid fixture loaded |
| Application | simulate endpoint immutability, X-Ray coverage, fallback explanation | All core APIs pass |
| Provider contract | Foundry adapter mocked shape, timeouts and invalid JSON | Fallback test passes |
| E2E UI | start replay, pause, seek, open M01, simulate branch, inspect evidence | 1 production-build smoke flow green |
| Accessibility | keyboard-only story, reduced motion, WebGL-disabled 2D | No blocking issue in core story |
| Visual regression | primary viewport and inspector capture in deterministic scene | No serious diff |
| Security | malformed JSON, excessive data, CORS, unsafe prompt, secret scan | No critical issue |
| Performance | loading, 3D FPS, API latency, request concurrency | Document measured values |

## Golden test vectors
- `pass_lane_horizontal`: passer (10,20) receiver (30,20), defender (20,22) -> clearance 2m; point beyond endpoint must clamp.
- `no_tracking_no_off_ball_claim`: event-only fixture -> `INSUFFICIENT`; explicit downgrade.
- `pressure_then_turnover`: pressure events precede interception, never reverse causal time.
- `rewind_replay`: seek to 10', then 50', then 10' yields exact scoreboard/event state at each fixed position.
- `branch_isolated`: 100 simulations, base fixture sha256 unchanged, no original score change.
- `foundry_timeout`: <= configured timeout, deterministic template response, no blocked replay.
- `schema_attack`: model claims 91% or references fake event -> rejected and logged.

## P0 release criteria by priority
**Stop-ship:** false claims shown as factual, broken score, branch changing original, API secrets in frontend, demo URL inaccessible, GitHub repo private, unlicensed protected content, Play/Seek broken, X-Ray moment no evidence, 3D-only blocking failure without fallback.
**Fix if possible:** animation stutter, mobile inspector layout, second locale failures, noncritical warnings.
**After hackathon:** multi-replica WS resilience, fine-grained enterprise SSO, long-running soak tests, formal threat review, disaster recovery game-day.

## Production candidate release criteria (NOT guaranteed by hackathon)
- Published service SLOs, alert ownership, tested rollback and restoration.
- Measured p95/p99 API and model fallback performance under target traffic; defined load profile and run report.
- Pen-test/threat model and remediation of critical/high items.
- Accessibility audit and evidence: WCAG 2.2 AA core tasks.
- SBOM, dependency/license check, GitHub secret scanning, signed or provenance-trackable release artifacts.
- Backups/restoration tested if/when durable data exists; no persistence claim now.
- Synthetic validity study with held-out authored scenarios and domain review (if sold as coaching support).

## Review protocol for AI coding agents
Agent must show command outputs, test names, whether actual Foundry call occurred, screenshot/recording if tested, changed files, and known limitations. A commit with no tests is not done. Do not forge test logs or quantitative measurements.
