# 01 — Product requirements document (PRD)

## Users and jobs-to-be-done
**Casual fan:** 'Show what I missed, in plain language, without fake jargon.'
**Football analyst/student:** 'Show evidence and alternative options so I can audit the conclusion.'
**Broadcaster/content creator:** 'Produce a synchronized, readable moment overlay and recap with source provenance.'

## Problem statement
Football broadcast overlays show isolated outcomes (shots, passes, possession), but rarely connect events, positional evidence, and tactical explanations or allow safe exploration of counterfactual decisions. Existing AI narratives often lack traceable provenance. BEYOND90 offers inspectable explanations and bounded simulations from synthetic scenarios.

## Information architecture
- `/` landing + 'Launch demonstration' (one CTA; no marketing maze)
- `/match/demo-001` Match Room 3D viewport, top scoreboard, match time, playback rail, event sidebar
- Inline X-Ray inspector drawer with camera focus and highlighted entities
- Inline Multiverse comparison modal/pane sharing the same pitch renderer; side-by-side or toggle
- `/about` disclosure of synthetic data, model limitations, licensing/credits, team, GitHub

## User stories / acceptance criteria
**US-01 Replay:** Given match loaded, when user presses Play, timeline advances from a canonical game clock; event cards appear in timestamp order; Pause/Seek restore exact state and do not duplicate overlays. User can select 0.5x, 1x, 2x playback (nice-to-have, not critical).

**US-02 Tactical explanation:** Given a known key moment, when user selects X-Ray, route/camera highlights the actor/defenders and cites at least one snapshot and event/metric IDs. If tracking is insufficient, display 'Tracking unavailable — no off-ball conclusion'.

**US-03 Counterfactual:** Given curated decision point, when user selects PASS or SHOOT, server computes an isolated branch based on original snapshot and seed. UI shows animation, labels 'synthetic simulation', and returns scenario metrics. Original remains unchanged.

**US-04 Personalization:** Given fan/analyst selector, narration style changes without changing facts, metrics, references or uncertainty. Fan text uses plain speech; Analyst displays detailed metric panel. Required P0 language EN; VI P1 if stable.

**US-05 Model errors:** Given Foundry unavailable/slow, analytics, playback, replay, and deterministic explanations continue. Show transparent 'AI explanation temporarily unavailable' only if the deterministic fallback is also unavailable.

**US-06 Production experience:** Responsive laptop layout, keyboard accessible buttons, focus-visible, skeleton/error/empty states, no dead controls; WebGL unavailable means users can switch automatically to 2D.

**US-07 Judge flow:** Reviewer with no account can complete main story and click through evidence under 90 seconds; repository, video, demo link and testing steps work independently.

## UX principles
1. Evidence is discoverable within one click of every interpretive claim.
2. Only show metrics the backend actually computes. Do not fake xG, xT or player speed.
3. Distinguish fact, inference, and simulation by text and visual treatment.
4. An action always produces a visible state change, completion response, or explicit failure.
5. The user can compare original and branch while preserving the temporal anchor.

## Product states
Loading; playing; paused; seeking; reconnecting; offline fallback; insufficient tracking; branch calculating; branch ready; AI timed out; WebGL unsupported. Design each explicitly.

## Non-functional requirements (targets, not measured claims)
- P0: < 2 seconds local fixture load and < 150 ms UI reaction to Play/Pause/Seek on a dev laptop; no hard guarantee over internet.
- P0: at least 30 FPS in 3D on a mid-range laptop for 22 markers, with quality fallback; verify using browser profiling.
- P0: deterministic replay for same seed/scenarioVersion and full error coverage on API routes.
- P1: p95 API query latency < 300 ms excluding Foundry; p95 simulation under 1.5s for curated branch; Foundry timeout <= 8s with fallback.
- P1: WCAG 2.2 AA-oriented navigation audit, Lighthouse budget and screen reader test.

## Feature tiers
**P0:** replay, one high-quality pressing narrative, 2 other replayable moments, evidence inspector, 2 branch actions, model adapter, 3D and 2D, Fan/Analyst, simple recap.
**P1 after P0 green:** EN/VI, 3 actions, branch overlay comparison, richer camera transitions, structured telemetry dashboard.
**P2:** live licensed event provider adapter, dynamic scenario editor, calibration/evaluation framework, role-based accounts, durable persistence.

## Explicit refusals
Do not present simulations as actual Premier League events; never identify a real player's decision without licensed real tracking; don't label simplistic heuristics as scientifically calibrated probabilities. No unlicensed third-party logo/video/music.

## Success metrics
- Judge can complete 6-step story without assistance (pass/fail).
- 100% narrative numeric claims from backend feature values and valid evidence references (automated validator).
- 100% deterministic original replay state across repeated seek tests.
- 0 broken primary CTAs in automated E2E.
- At least one demonstrated end-to-end Microsoft Foundry inference, with measurable prompt/version/cost log (no secrets).
