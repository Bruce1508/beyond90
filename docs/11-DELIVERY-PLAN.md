# 11 — Execution roadmap, task registry and stop/go gates

## Reality check
Timebox: **Oct 19 through Oct 27, 2026** (deadline 23:59 Pacific, Oct 28 02:59 Toronto). Assume solo developer 5–7 focused hours/day. This is NOT enough to certify enterprise production readiness. Deliver a credible production-oriented MVP plus clearly documented further hardening requirements. Reserve Oct 26 and 27 for stabilization and submission; deploy by Oct 25.

## Operating procedure
- Work in ID order unless a blocking dependency is not ready; do not parallel-edit overlapping contract files.
- For each task, mark status `[ ] -> [x]` only after tests/build plus artifact evidence.
- Each task yields: implementation, one test or fixture, updated contract/readme if changed, exact commands/results.
- Review at 2 checkpoints/day: scope remaining and critical path. If behind, apply cut plan, never invent passing states.

## Day 1 — Oct 19: Environment / contracts / vertical slice
- [x] **P0-01 Repo bootstrap (2h)**. Init monorepo, Maven wrapper Java 21 Spring Boot 4.1.x; Next.js TS R3F compatible versions; pnpm lockfile; no secrets; health API and a basic pitch page. **AC:** `./mvnw test`, `pnpm build` green; README start commands work on clean clone.
- [x] **P0-02 Contract baseline (1.5h)**. Validate `contracts/openapi.yaml` + `contracts/match-event.schema.json`; finalize fixture IDs and canonical axes/clock. **AC:** invalid event out-of-bounds rejected, one contract validator command documented.
- [x] **P0-03 One synthetic key moment (2h)**. Author M01 with roster, timed events, snapshots. **AC:** 22 player IDs, score-consistent, schema-valid, replay fixture digest persisted in test.
**Gate G1:** Java endpoint returns a valid loaded fixture and website draws it. If not, STOP styling. — **NOT YET MET**: fixture is validated but not yet served by an HTTP endpoint (P0-04).

## Day 2 — Oct 20: Replay, 3D pilot, dependable clock
- [x] **P0-04 Fixture loader + timeline (2h)**. Immutable repo, score projection and seek; `GET /matches/demo-001` and `/timeline`; tests for deterministic ordering and seek. **AC:** same time always yields same event/score state.
- [x] **P0-05 3D pitch + fallback (2.5h)**. 105x68 plane, 22 markers, ball, 2D SVG fallback; shared coordinate transform; camera presets; Play/Pause/Seek.
- [x] **P0-06 UI playback integration (1.5h, partial)**. Fetch fixture/drive 3D from canonical clock; event feed and overlay on moment. **AC:** direct browser demonstration with seek/replay and clear synthetic label. — event feed/overlay deferred to P0-09/10 (X-Ray UI); play/seek/clock/synthetic-label verified live.
**Gate G2:** end-to-end one replay path running without model provider — **MET**, verified live via Playwright (screenshot + console check, 0 errors); if behind, build 2D first and defer complex 3D effects.

## Day 3 — Oct 21: Real geometry analytics
- [x] **P0-07 Lane metrics + tests (2h)**. Segment distance clamping and defender count with golden vectors. **AC:** metrics include event/snapshot refs and `algorithmVersion`.
- [x] **P0-08 Pressure detector (2h)**. Derive M01 explanation evidence; short prior window and turnover; positive/negative fixtures. **AC:** unsupported causal language disallowed.
- [x] **P0-09 X-Ray DTO endpoint (1.5h)**. REST data with geometry IDs, linked features and missingness; JSON snapshot tests. **AC:** `/moments/m01/xray` valid, all referenced IDs resolve.
**Gate G3:** ONE truthful insight. If failing, do not begin agent orchestration. — **MET**, verified live (see PROGRESS.md).

## Day 4 — Oct 22: X-Ray UX + other moments
- [x] **P0-10 3D tactical overlay (2.5h)**. camera X-Ray focus, lane lines, pressure halo; accessible DOM text. **AC:** any highlighted geometry traceable to metric/snapshot ID.
- [x] **P0-11 M02/M03 fixtures (2h)**. Author off-ball snapshot pair + decision fork scene; missingness tests and scenario labeling. **AC:** three demonstrable moments, 22-player scene where required.
- [x] **P0-12 Evidence inspector (1.5h)**. Click insight -> metric definition/time/ID + caveat; full keyboard support and fallback.
**Gate G4:** a judge can answer 'why does the AI say that?' within one click. — **MET**, verified live via keyboard-only interaction (see PROGRESS.md).

## Day 5 — Oct 23: Bounded counterfactual
- [x] **P0-13 Pure simulator (3h)**. PASS and SHOOT actions, deterministic seed, bounded <=8s trajectory; speed/goal intersection; no global mutable state. **AC:** golden runs byte-equivalent (except transient IDs), original digest unchanged.
- [x] **P0-14 Branch API + tests (1.5h)**. POST `/simulate`, error handling, limits, idempotency. **AC:** invalid action 422; parent untouched across 100 runs.
- [ ] **P0-15 Branch UX (1.5h)**. Original/Alternative toggle + trajectory preview + differences. **AC:** branch is visibly different and marked simulated.
**Gate G5:** 'What if pass vs shoot?' is interactive, calculated, and truthful (not free-form AI text).

## Day 6 — Oct 24: Real Microsoft Foundry and credible agent workflow
- [ ] **P0-16 Java Foundry spike (1.5h)**. Verify deployment, credentials, Java API call, validate shape/timeouts. **AC:** actual model inference observed + sanitised trace. If unavailable, log blocker immediately.
- [ ] **P0-17 Structured agent orchestration (2.5h)**. FactPack -> Analyst -> Java Verifier -> Composer -> Verifier, bounded retries, template fallback. **AC:** fake numerical claims/reference IDs rejected, all successful narratives grounded.
- [ ] **P0-18 Personalized explanations (1.5h)**. FAN/ANALYST EN; recap from validated moments; language VI only when core stable. **AC:** same facts across mode variants, meaningful difference in wording.
**Gate G6:** Foundry + Java in one verified end-to-end narrative; can still replay with model offline.

## Day 7 — Oct 25: Deploy + feature freeze
- [ ] **P0-19 Quality integration (2h)**. E2E tests for full story, manual keyboard/WebGL-off test, fix P0 bugs. **AC:** green smoke flow from clean production build.
- [ ] **P0-20 Azure deployment (2h)**. Containerize, set budget, deploy ACA web/api, health/logs, Foundry secrets not in browser; production URL from incognito. **AC:** core flow works on Azure.
- [ ] **P0-21 Freeze & polish (1.5h)**. UI spacing, camera stability, loading/errors, primary screenshot and trace ID. **AC:** no dead UI or missing disclosure.
**Gate G7:** deploy URL ready. Freeze feature additions by end of day.

## Day 8 — Oct 26: Reliability and submission assets
- [ ] **P0-22 Hardening (2h)**. rate limits, mock provider outage, refresh/seek/reconnect, resource cap; fix stop-ship failures.
- [ ] **P0-23 Demo video (2h)**. Record functioning demo, English narration/captions, no licensed music, duration <120s. **AC:** independent viewing communicates problem/value.
- [ ] **P0-24 Docs and source audit (1.5h)**. public README, setup, architecture, license inventory, screenshots, credits, roadmap, judge test guide, verify no secrets.
**Gate G8:** complete submission package before deadline day.

## Day 9 — Oct 27: Submission buffer
- [ ] **P0-25 Preflight (1h)**. incognito judge URL + public repo + video + all metadata, verify 2-min rule, Model/Azure actual usage.
- [ ] **P0-26 Submit (0.5h)**. Submit on official platform, verify confirmation and URL links; no assumption until confirmed.
- [ ] **P0-27 Availability watch (0.5h)**. Ensure demo does not sleep/fail before Nov 10; alerts and billing budget.

## Priority cut plan if slipping
1. Drop bilingual VI (keep EN); drop 3D advanced effects but keep simple 3D with 2D fallback.
2. Drop M02 off-ball detector complexity; keep scripted curated scene and honest caveat.
3. Drop optional WebSocket broadcast; REST-driven deterministic playback is sufficient for MVP story if overlay synchronization demonstrated.
4. Drop CARRY third action; retain PASS vs SHOOT.
5. Drop multiple AI rounds; retain one real Foundry narrative step + deterministic evidence verifier, documenting reduced agentic ambition.
6. Drop recap visual polish, not functioning X-Ray / Multiverse.
**Never cut:** public demo, valid synthetic input, grounded insight, honest branch labels, operational security, correct repo/video requirements.

## Handoff/report template
```
Task ID:
Files changed:
Contract changes:
Tests run (actual command/output):
Demo evidence (path/screenshot/URL):
Foundry called? Yes/No; if Yes, model deployment alias and latency:
Open issues/risks:
Next unblocked task:
```
