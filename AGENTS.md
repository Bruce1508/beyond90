# BEYOND90 — Autonomous Shipping Policy for AI Coding Agents

> **Operating mode: SHIP FAST, PROVE IT WORKS.** Work independently in short verified increments until the hackathon-ready product ships. Do not stop after planning, after one task, or to ask for routine approval. Fast means less waiting and less waste—not skipped validation.

## 0. Authority and startup (once per session)

Priority on conflicts: **official contest rules > safety/security/legal constraints > this file > versioned API/data contracts > detailed specs > generated code**. `CLAUDE.md` is a pointer, not a competing policy.

1. Read `README.md`, `docs/11-DELIVERY-PLAN.md`, relevant technical docs, and `contracts/` once; selectively revisit pertinent sections. Do not repeatedly re-read the whole repository.
2. Inspect `git status`, current code, tests, and `docs/PROGRESS.md` (create if missing). Preserve user work and respect the current branch.
3. Discover installed Addy Osmani skills via actual local skill manifests/tools; use relevant ones selectively for architecture, security, review, and testing. **Never invent skill names, invoke every skill by default, or let skill discovery block shipping.**
4. Check critical dependency compatibility against official docs when installing/upgrading; pin working versions. Flag unsupported combinations immediately.
5. Resume the **highest-priority unblocked acceptance-criteria-driven task**. Start implementation in the same session, not another planning round.

## 1. Mission and deadline

- Ship a **real publicly accessible, judge-testable application**, not a static design. Submission deadline: **2026-10-27 23:59 Pacific**; internal feature freeze **2026-10-25**, demo/reliability **2026-10-26**, submit with buffer **2026-10-27**. Keep judge access working through **2026-11-10** if required by official rules; verify dates against the contest portal.
- Product: **BEYOND90**, an interactive football intelligence platform combining explainable **X-Ray** analysis and bounded **Multiverse** counterfactual replay, with an impressive **3D viewer** and an accessible **2D fallback**.
- Optimize for a **complete vertical slice first**, then deepen. An impressive feature is valuable only if it works end-to-end, follows evidence, and is reproducible in the deployed demo.
- Apply production-oriented architecture; **do not claim production-ready** before independent reliability, security, performance, and operations gates in `docs/08-QUALITY.md` pass.

## 2. Frozen technology direction

- **Java 21 + Spring Boot**, modular monolith, Maven. Pin a **currently available, stable, verified compatible** Spring Boot version rather than blindly assuming a specified minor is released. Use Spring AI with a version compatible with that Boot version, verified before adding dependencies. No Python service/backend; no Node business APIs beyond Next.js framework needs.
- **Next.js + TypeScript + React Three Fiber/Three.js + Drei**. Pin mutually compatible React/Next/R3F versions. 3D shares one canonical match state with accessible 2D. **The production build and core workflow must work without WebGL.**
- **Microsoft Foundry** via a real Java provider adapter; an offline narrative fallback is permitted only when clearly labeled. Prefer a small, observable Java orchestration workflow/state machine. Do **not** claim the Microsoft Agent Framework Java SDK exists unless verified.
- **Azure** deploy target; containerized, configured with environment variables. Avoid premature microservices, Kubernetes, message buses, databases, novel frameworks, auth systems, or paid tooling unless a concrete acceptance criterion requires them.
- **Synthetic seeded match/tracking scenarios** are the authoritative demo source. No unauthorized match broadcasts, club identities/badges, player likenesses, or scraped proprietary data.

## 3. Autonomous execution loop — no routine approval requests

Repeat until all in-scope deliverables pass, deadline forces scope freeze, or a real blocker requires intervention:

1. **Select** the highest-priority unblocked task ID in `docs/11-DELIVERY-PLAN.md`; check dependencies and definition of done. P0 functioning demo outranks polish. Do not create a second competing backlog.
2. **Implement** a small end-to-end increment. Lock/change the contract first where necessary; implement actual behavior with focused automated tests. Favor real integrations over facade mocks. Use existing patterns and minimal dependencies.
3. **Verify** using the cheapest sufficient checks after each edit (targeted test/typecheck), and run full relevant integration, build, and smoke checks at milestone boundaries and before submission. Log exact commands, pass/fail, and known skips. A generated test is not a passed test.
4. **Review** the diff for correctness, security, performance, contracts, UX accessibility, hidden placeholders, and unjustified complexity. Use installed review skills when useful; repair actionable findings.
5. **Integrate**: ensure mainline stays buildable; commit coherent changes when allowed, with a task ID in the message. No force push, destructive cleanup, automatic secret rotation, or overwriting user changes.
6. **Record** in `docs/PROGRESS.md`: task ID, actual behavior, verification commands/results, commits, blockers, next task. Keep status terse. Continue immediately to the next unblocked task.

**Do not pause** after a plan, status summary, commit, or ordinary design choice. Make reversible decisions within documented boundaries and proceed. **Do pause/escalate** for missing required access/credentials, potentially destructive or costly cloud actions, licensing/privacy issues, conflicting requirements with large blast radius, or three materially different unsuccessful repair attempts. If a noncritical external service blocks progress, preserve its adapter and complete offline functionality without misrepresenting it as live.

## 4. Shipping priorities and scope gates

**P0 — Demo foundation:** synthetic seeded fixtures, validated event/snapshot contracts, Java HTTP APIs, consistent match clock, play/pause/seek, baseline 2D renderer, deployable vertical slice.

**P1 — Differentiated value:** evidence-grounded X-Ray (at least one completely valid tactical moment, then expand), 3D synchronized viewer with camera/overlays, immutable short Multiverse branch with at least two alternative actions.

**P2 — AI and experience:** real Foundry integration with validated structured outputs, Java orchestration, fan/analyst explanations, recap, deliberate visual polish and accessible fallbacks.

**P3 — Submission reliability:** Azure URL, end-to-end smoke tests, deterministic guided demo, failure states, observability, reproducible setup, pitch/video, README, security/licensing review. **By Oct 25: feature freeze.** If schedule slips, cut breadth in reverse priority, **never** authenticity, basic security, testability, or the complete X-Ray/Multiverse demo path.

Scope cuts must be explicit in `docs/PROGRESS.md`, with real behavior still connected end-to-end. Don't turn features into fake buttons to satisfy a checklist.

## 5. Non-negotiable correctness contracts

- `matchClockMs` means game-time since kickoff; periods/halftime/stoppage explicitly modeled, never guessed from wall clock. Realtime messages include `seq`, `sessionId`, `matchId`, `scenarioVersion`, `matchClockMs`; seek/reconnect must never duplicate/rewind canonical events incorrectly.
- Field coordinates use meters: `x ∈ [0,105]`, `y ∈ [0,68]`; attack direction comes from period metadata. Validate input, references, team/player IDs, event ordering, positions, score progression, and scenario version.
- Off-ball, movement, or pressing assertions require **explicit tracking snapshots**. Interpolation is visual-only evidence, never a basis for scientific claims.
- Tactical insights include **typed evidence event/snapshot IDs, metric IDs, algorithm version, confidence category, and caveats**. AI narrates verified metrics; it does not invent measurements, causal certainty, or sources.
- Multiverse branches must be **immutable**, seeded, tied to a parent snapshot, visibly marked **simulation**, and must not mutate original match timeline/score. No invented calibrated probabilities or professional coaching claims.
- AI output is schema-constrained, parsed and validated, size-limited, time-bounded, and protected against untrusted prompt instructions. Implement error fallbacks, bounded retries and circuit-breaker behavior where appropriate. Never represent offline fallback as a successful Foundry call.
- Version HTTP APIs under `/api/v1`; maintain consistent validation/errors, reasonable rate limits and CORS allowlist. No secrets, credentials, sensitive prompts or private user information in repository or logs.
- All controls must work; include loading/error/empty and keyboard states. Shared frontend renderer state should match backend event and timestamp semantics.

## 6. Verification policy: fast feedback, hard milestone gates

- **Per change:** relevant fast tests and static checks only; don't run the entire suite after trivial edits.
- **Per completed task:** related unit tests, schema validation, lint/typecheck, API/component checks as applicable.
- **Per milestone and before merge/release:** full relevant backend tests, frontend typecheck/lint/build, API contract checks, integrated browser smoke, WebGL-off/2D fallback smoke, clean-start instructions.
- **Before submission:** reproduce demo from a fresh checkout/clean container; deployed URL works from a non-developer session; no missing env variables in public flow; measure and document observed failures/performance. Never invent passed checks, usage metrics, AI quality numbers, or live deployment.
- **Evidence:** report concise `PASS / FAIL / NOT RUN (reason)` with commands; when checks are blocked, leave them explicitly open. Fix regressions before proceeding to higher-priority cosmetic work.

## 7. Parallel agents and efficiency

- Parallelize **bounded non-overlapping modules**, for example Java API/domain, 3D viewer, deterministic analytics tests, docs/review. Assign **one integration/contracts owner**; communicate exact interfaces and acceptance criteria.
- Never let multiple agents concurrently edit the same file or public contract. Do not fork duplicate backends or independent match state implementations. Integrate early; run joint smoke tests.
- Prefer batch file reading, focused searches, existing libraries, small diffs, targeted tests, and reusable fixtures. Avoid repeated full architecture debates and bloated status reports.
- Do not use unrestricted permission-bypass options merely for speed. Respect environment and user authorization boundaries.

## 8. Anti-slop and blockers

Reject: fake metrics/percentages, unsupported SDK calls, hallucinated docs, browser-only fake API, polished dead buttons, incoherent synthetic score/event timelines, AI agent theater, unstable 3D without fallback, unpublished integration posing as deployed, silent contract drift, and local-only demo scripts.

When blocked, inspect logs, reproduce the failure, consult **official documentation** for APIs/SDKs, and record verified findings in `docs/14-RESEARCH-AND-SOURCES.md`. Attempt up to three **meaningfully different** repairs; then isolate the problem, choose a safe scoped fallback if possible, or escalate with exact blocker and required action. Never silently mark blocked features done.

## 9. End-of-session checkpoint (for context/session limits)

Before the session ends, ensure `docs/PROGRESS.md` contains the last verified good state, uncommitted changes, next task, exact resume command, and remaining blockers. A new agent/session should **resume shipping immediately**, not start a new plan.

**Default next action: execute the next unblocked task, verify it, commit if safe, update progress, and continue.**
