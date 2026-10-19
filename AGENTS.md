# BEYOND90 — Instructions for ALL AI coding agents

These rules are authoritative for implementation. Read `README.md`, `docs/11-DELIVERY-PLAN.md`, then the relevant technical document before changing files. `CLAUDE.md` is a compatibility pointer. For conflicts: official contest rules > security/legal constraints > `AGENTS.md` > contracts > detailed specs > generated code.

## Mission, deadlines and non-negotiables
- Build a **real, publicly testable submission**, not a static concept mock. Submission closes **2026-10-27 23:59 Pacific**. Aim to finish/upload earlier; judge test availability continues through **2026-11-10**.
- Backend is **Java 21 with Spring Boot 4.1.x** (pin a verified stable patch when bootstrapping); no Python backend and no Node backend APIs except Next.js framework plumbing. Maven preferred; use Spring AI 2.0.x with compatibility verified against Spring Boot.
- Frontend is **Next.js + TypeScript**, 3D via **React Three Fiber** + Drei, 2D accessible fallback. Use React 19-compatible R3F major. Production build MUST work without WebGL.
- Microsoft Foundry provides model inference. Use a **Java orchestration state machine** with specialized steps; Microsoft Agent Framework does **not** currently expose a Java implementation. Do not fake its use.
- The critical path is **deterministic**: seeded synthetic football events/tracking, consistent playback/seek, geometry-derived X-Ray evidence, isolated branch simulation. AI may narrate verified results, not invent them.
- No copying copyrighted match video, team badges, real club branding, or real player likenesses without verified permissions. Use fictional clubs and original graphics.
- A cool 3D scene is not an accepted task unless it supports an interactive explanation with source evidence.

## Agent workflow (required each task)
1. Read the relevant spec + task acceptance criteria and list assumptions and blockers.
2. Pick ONE task ID from `docs/11-DELIVERY-PLAN.md`, identify dependencies. Prefer topologically complete vertical slices.
3. Write/adjust contracts and tests first, then minimal implementation. Test adverse inputs and error states.
4. Run format, lint, typecheck, unit tests, integration tests, and build that apply; report exact commands and results. Never claim tests passed without execution.
5. Update README, architecture, and docs when behavior changes. Do not silently drift from contracts.
6. Provide a concise completion report: files changed, behavior, test results, evidence, risks, next task.
7. Commit in coherent, reviewable units if allowed in environment. Never force push, overwrite user work, or delete unexpected files.

## Hard technical rules
- `matchClockMs` is game-time since kickoff; halftime/stoppage are represented explicitly, never derived from wall-clock alone. Wire messages carry `seq`, `sessionId`, `matchId`, `scenarioVersion`, `matchClockMs`.
- Coordinate domain is `x ∈ [0,105] m`, `y ∈ [0,68] m` with attack direction per period in match metadata. Validate every incoming event and reject broken referential integrity.
- Tracking snapshots **must be explicit** for off-ball/motion claims. Do not reconstruct exact movement from events alone. Scene interpolation is visualization, not scientific evidence.
- All tactical insight claims carry typed evidence IDs, metric IDs, confidence category, algorithm version and caveats; reject model facts without supporting evidence.
- Counterfactual branches are immutable; never change canonical original event stream, original score, or timestamps. Branch must record its parent snapshot/seed and surface uncertainty.
- Never show '83% goal probability' without a calibrated, held-out evaluation. No unsupported causal claims or professional coaching guarantees.
- Model outputs: strict structured JSON schema, validation, maximum output size, no unrestricted tool invocation, deterministic template fallback, provider timeout/circuit breaker.
- No secrets, private emails, personal identifiers, API keys, or cloud credentials committed. Avoid leaking raw prompts or user input into logs.
- API version under `/api/v1`; consistent error responses; route-level request validation/rate limits, CORS allowlist, auth plan for post-hackathon.
- `main` always builds; use feature flags to disable unstable 3D or multi-agent features. Do not add DB, message bus, ML training, Kubernetes, or microservices without evidence of necessity.

## Engineering standard: no slop
Reject: fake metrics, random values with 'AI confidence' labels, polished nonfunctional buttons, synthetic events inconsistent with score, spaghetti state, mismatched coordinates, frontend-only fake API, mocked 'agent collaboration' dressed as reality, and scripts that work only on the developer's laptop.

## Definition of Done
Task implementation is complete only when acceptance criteria, deterministic tests, documented API behavior, keyboard/error states where relevant, and demo fixture all work. Product is 'production-ready' only after independent security, reliability, performance and operations gates in `docs/08-QUALITY.md` are met. Until then label it **production-oriented hackathon MVP**.

## Agent collaboration
Agents may implement independent bounded modules (contract-first), but assign a single owner for contracts/integration. Never parallelize edits to the same file. Each task handoff should include changed interfaces and migration steps.

## When blocked
Do not fabricate SDK methods or citations. Search official documentation, record what is verified/unknown in `docs/14-RESEARCH-AND-SOURCES.md`, implement a provider adapter with an offline stub only if clearly labeled and preserve a real Foundry integration path. Escalate decisions that would materially harm submission readiness.
