# BEYOND90 — Football X-Ray × Multiverse

**Engineering documentation and build instructions** | Baseline: 2026-10-19 | Hackathon submission: 2026-10-27, 23:59 PT

> See the invisible. Explore the possible.
>
> BEYOND90 is an evidence-first, 3D football intelligence application built with Java/Spring Boot, Next.js/React Three Fiber, and Microsoft Foundry. It analyzes *synthetic* football events and tracking snapshots, explains tactical moments, and lets viewers replay bounded alternative decisions.

Vietnamese quickstart: [`START-HERE-VI.md`](START-HERE-VI.md).

## Running locally
Requires Java 21+, Node 20+, and `pnpm` (`npm install -g pnpm` if missing).

```bash
# Backend — Spring Boot 4.1.1, Java 21, port 8080
cd backend
./mvnw test   # unit + MockMvc health-endpoint test
./mvnw spring-boot:run
curl http://localhost:8080/actuator/health   # {"status":"UP"}

# Frontend — Next.js 16 / React 19, port 3000 (fetches the backend above at runtime)
cd frontend
pnpm install
pnpm build    # production build
pnpm dev      # http://localhost:3000 — 3D pitch (2D SVG fallback without WebGL)
```

The backend must be running for the frontend to show real data (it fetches `/api/v1/matches/demo-001`
and `/snapshots`); otherwise the page shows an explicit "could not reach the API" state. If port 3000
is taken, `pnpm dev` falls back to 3001+ — `backend/.../WebConfig.java`'s CORS allowlist already
covers both. Override the API origin with `NEXT_PUBLIC_API_BASE_URL` if the backend isn't on
`localhost:8080`.

Both commands above were run against a clean clone and verified green on 2026-10-19 (`./mvnw test`,
`pnpm build`); the full playback UI was verified live in a real browser via Playwright (see
`docs/PROGRESS.md`, task P0-05/06).

## Start here (human)
1. Read `docs/00-EXECUTIVE-BRIEF.md` and `docs/01-PRD.md`.
2. Read `docs/02-ARCHITECTURE.md` and `docs/15-DECISIONS.md` before selecting libraries.
3. Execute `docs/11-DELIVERY-PLAN.md` in order; this is a **time-boxed** hackathon, not a promise to ship every enterprise feature.
4. Keep `docs/12-EVALUATION.md`, `docs/08-QUALITY.md`, and `docs/13-SUBMISSION.md` as release gates.

## Start here (coding agent)
1. Read **`AGENTS.md`** (canonical cross-agent instructions); Claude Code also reads `CLAUDE.md`.
2. Read the feature's spec and `contracts/openapi.yaml` / `contracts/match-event.schema.json` before coding.
3. Implement *one vertical slice* at a time: fixture -> Java API -> timeline -> 2D fallback/3D view -> X-Ray explanation -> counterfactual branch.
4. Every completed task must contain tests, updated docs, reproducible demo evidence, and a clear status report.
5. The agent MUST NOT claim that the system is production-ready solely because these documents exist.

## Architecture at a glance

```text
Offline seeded synthetic match events + tracking snapshots
               |
      Java 21 + Spring Boot 4.x
               |
  +------------+--------------+-----------------+
  |            |              |                 |
Playback     Football       Counterfactual    AI orchestration
service      analytics      simulator         (Java + Spring AI)
  |            |              |                 |
  +------------+--------------+-----------------+
               |
 REST/OpenAPI + WebSocket events; immutable versioned evidence
               |
 Next.js / TypeScript / React Three Fiber 3D pitch + 2D fallback
               |
  Match Room -> X-Ray Inspector -> Multiverse Branch Comparison
```

## Two delivery tracks
- **Submission MVP (P0):** deterministic synthetic live replay, grounded X-Ray, at least one interactive branch point, Java backend, Foundry integration with safe fallback, a deployable public demo, repository, and sub-2-minute demo video.
- **Production-hardening (P1/P2):** authentication, durable persistence, scaling realtime to multiple replicas, SLO monitoring, SAST/DAST, load testing, threat review, rollout/rollback and repeatable operations. Architecture and requirements are specified now; completion must be evidenced, not asserted.

## Scope warning
A 3D interface cannot *infer* missing tracking data. Do not fabricate off-ball movements or claim accurate counterfactual probabilities; where a scene is curated, label it as synthetic. No Premier League footage or trademarks without permission. The synthetic data must be the basis of the judged project.

## Documents
| Path | Purpose |
|---|---|
| `AGENTS.md`, `CLAUDE.md` | Persistent engineering guardrails and implementation order |
| `docs/00-EXECUTIVE-BRIEF.md` | Scope, differentiation, milestones and honest readiness |
| `docs/01-PRD.md` | Product and user stories, scope and acceptance criteria |
| `docs/02-ARCHITECTURE.md` | Boundaries, topology, deployment, data flow and ADR pointers |
| `docs/03-DATA-AND-EVENTS.md` | Canonical time, coordinates, snapshot data, event schema |
| `docs/04-FOOTBALL-ENGINES.md` | Deterministic analytics and bounded counterfactual simulation |
| `docs/05-AI-ORCHESTRATION.md` | Java agent roles, trust boundaries, prompts and fallbacks |
| `docs/06-3D-EXPERIENCE.md` | Product UI, 3D camera, performance and 2D accessible view |
| `docs/07-API-AND-REALTIME.md` | REST and WebSocket contracts, replay semantics and errors |
| `docs/08-QUALITY.md` | Test matrix, CI release gates and performance budgets |
| `docs/09-SECURITY-AND-PRIVACY.md` | Threat model, licenses, data governance |
| `docs/10-PLATFORM-AND-OPS.md` | Azure plan, environments, cost and incident response |
| `docs/11-DELIVERY-PLAN.md` | Day-by-day task IDs, order, Definition of Done and cuts |
| `docs/12-EVALUATION.md` | Evidence grounding, simulator sanity checks, scoring |
| `docs/13-SUBMISSION.md` | Hackathon compliance, demo script and final checklist |
| `docs/14-RESEARCH-AND-SOURCES.md` | Verified authoritative references and usage boundaries |
| `docs/15-DECISIONS.md` | Architecture decision records and alternatives |
| `docs/16-OPERATIONS-RUNBOOK.md` | Reproducibility, diagnosis, rollback, known failure modes |
| `docs/17-AI-AGENT-PROMPTS.md` | Ready-to-use prompts for coding/review/release agents |
| `docs/18-PRODUCTION-READINESS-SCORECARD.md` | Evidence-based readiness tracking |
| `docs/19-JAVA-IMPLEMENTATION-BLUEPRINT.md` | Java packages, signatures, dependency and testing notes |
| `docs/20-FRONTEND-IMPLEMENTATION-BLUEPRINT.md` | React/3D state, API projection and UI notes |
| `contracts/*` | Machine-readable API/data contracts |
| `examples/*` | Synthetic small fixtures, not real match data |

## Authoritative external rulebook
Official rules: https://github.com/microsoft/insidethegamehackathon/blob/main/OFFICIAL%20RULES.md

**Current status:** design pack only. No working production deployment, verified integration, or measured performance is claimed.
