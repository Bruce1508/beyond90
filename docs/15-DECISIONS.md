# 15 — Architecture decision records (ADRs)

## ADR-001 — Java modular monolith [Accepted, 2026-10-19]
**Context:** User explicitly requires Java backend; deadline <10 days.
**Decision:** Java 21 + Spring Boot 4.1.x in one backend with package/module boundaries; Maven, no separate Python simulation service.
**Alternatives:** FastAPI faster for numerical tasks but violates user constraint; full Spring microservices produce unnecessary deployment complexity.
**Consequences:** Math and simulation written/tested in Java, infrastructure simpler, portfolio demonstrates backend fundamentals.

## ADR-002 — Microsoft Foundry Java orchestration [Accepted]
**Context:** Need Microsoft AI and credible agentic design, but official Microsoft Agent Framework is Python/.NET-only.
**Decision:** Use Java Spring AI model integration, own typed agent/handoff workflow, Java verification gates; real Foundry model endpoint.
**Consequences:** Truthful claims, easier ops, less framework branding but better reliability; might be weaker in Best Multi-Agent prize without real evaluation. Revisit only if P0 complete and time remains.

## ADR-003 — 3D for insight, 2D for fallback [Accepted]
**Context:** 3D wow vs performance/accessibility risk.
**Decision:** one coordinate source drives R3F and SVG. No fully animated player rig. 3D camera and trajectories must convey evidence.
**Consequences:** Lower development cost; accessible fallback; no visual precision unsupported by tracking.

## ADR-004 — Synthetic, curated snapshot fixtures [Accepted]
**Context:** Organizers require synthetic football-realistic data; complete tracking/video expensive or licensable.
**Decision:** write original event stream and three detailed decision scenes, no real club names/logos/footage.
**Consequences:** reproducible; clearly limited realism. Later support dataset adapters only if rights and completeness pass.

## ADR-005 — Counterfactual is a bounded simulator [Accepted]
**Context:** 'What if' risks turning into fabricated probabilities.
**Decision:** deterministic short-horizon geometry and simple defensive heuristics; disclose model assumptions, avoid calibrated probability claims.
**Consequences:** strong interaction with honest uncertainty, less 'EA FC-like' realism.

## ADR-006 — REST as source, WebSocket enhancement [Accepted]
**Context:** Judge demo cannot depend on live shared WS reliability, serverless replicas and reconnection.
**Decision:** REST fixture/timeline is sufficient for deterministic client-local replay; WS optionally streams synthetic events. Both share source clock and version.
**Consequences:** simpler, replay resilient; pitch can function even with streaming blocked.

## ADR-007 — No database in P0 [Accepted]
**Context:** Immutable synthetic fixtures, anonymous judge UI, no user state.
**Decision:** versioned JSON in application resources. Repository interfaces preserve future replacement with object storage/Postgres.
**Consequences:** fewer moving parts; no durable saved branches or multi-user session sync until needed.

## ADR-008 — Production-oriented, not untested 'production-ready' [Accepted]
**Context:** 'Production-ready' means measurable reliability/security, cannot be certified by plans alone.
**Decision:** Release levels L0–L4 and hard launch gates. Submission gets highest feasible L2, then hardening.
**Consequences:** avoids inflated portfolio claims and protects hackathon deadline.

## Decisions still open, with action owners
| ID | Decision | Trigger | Owner |
|---|---|---|---|
| O-01 | Actual Foundry deployment and model pricing | P0-16 Azure spike | Developer |
| O-02 | R3F React/Next version pins | P0-01 install/build | Developer |
| O-03 | Public API ingress vs Next BFF | Azure proof + CORS/security | Developer |
| O-04 | Video recording tool (OBS/QuickTime/etc.) | P0-23 | Developer |
| O-05 | Native Spring AI vs openai-java adapter | Foundry call contract in P0-16 | Developer |
