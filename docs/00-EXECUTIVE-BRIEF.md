# 00 — Executive brief and delivery strategy

## Positioning
BEYOND90 is **evidence-first interactive football intelligence**, with a 3D tactical viewer (X-Ray) and bounded user-controlled alternative outcomes (Multiverse). Its primary value is enabling a person to *inspect* why a moment mattered and *explore* a plausible alternative, without presenting guesses as facts.

### North-star interaction (90-second self-guided demo)
1. Open a public URL to a fictional, synthetic match, no login.
2. See a smooth playback with a real match clock and synchronized event stream.
3. Click a flagged pressing moment; camera zooms to action with highlighted space, players and pass lanes.
4. Inspect the exact snapshots/events/features underpinning a tactical claim; switch Fan/Analyst explanation.
5. Pick Pass vs Shoot from a curated decision snapshot; see alternate 3D paths and typed outcome comparison.
6. Read a short AI-generated recap with evidence references; if Foundry unavailable, clear fallback text appears.

## Differentiation thesis
- The viewer can interrogate an insight, not just consume a statistic.
- 'What if' is a **bounded counterfactual sandbox**, not an oracle or gambling predictor.
- Inference is auditable: `insight -> metrics -> events/snapshots -> visualization`.
- 3D is an explanatory tool. A 2D view remains functional and accessible.
- Agents have separable responsibilities and documented handoffs, not a handful of role-playing prompts.

## Constraints
- Official rules require synthetic, football-realistic data; 5 stages ingest, interpret, explain, render, personalize. Original source: `docs/14-RESEARCH-AND-SOURCES.md`.
- No Premier League footage is supplied. Do not stream unlicensed footage or duplicate protected identities.
- Deadline **2026-10-27 23:59 PT**; video <2 minutes, public GitHub, testable free demo, materials English. Keep demo online through judging (ends Nov 10 PT).
- Plan assumes **solo developer**, ~5–7 focused hours/day, with cuts if throughput is lower.

## P0 non-negotiable features
| Feature | Minimum acceptance |
|---|---|
| Replay | Consistent full synthetic timeline + 3 tactical snapshots, Play/Pause/Seek, score synced |
| X-Ray | 1 excellent, evidence-backed tactical scene + 2 more supported scene types |
| Multiverse | 1 decision point with 2 bounded alternatives computed by the engine |
| 3D | Navigable pitch and markers, replay synchronized, camera presets; 2D fallback |
| AI | Real Foundry call on main demo path, typed output validation, fail-safe deterministic explanation |
| Personalize | Fan vs Analyst English, another language only if time allows |
| Platform | Deployed accessible URL, basic health, logs, secrets outside git, smoke tests |
| Submission | Public repo, pitch, demo under 120s, judge instructions |

## Not in scope (until post-submission)
Official professional-quality 3D player animation; optical tracking from video; production-real xG/xT calibration; licensed live matches; large-scale distributed match fanout; accounts/billing; auto-generating unseen full-match player tracking; long-term data lake; unsupervised coaching decisions.

## Release ladder
- **L0 Design-ready:** completed architecture/contracts/docs (this pack).
- **L1 Running MVP:** coherent local demo, actual tests, real Java backend and real Foundry adapter.
- **L2 Hackathon release:** public live URL, smoke checks, stable demo, no critical bugs, copyright compliant.
- **L3 Production candidate:** security/reliability/performance/accessibility gates evidenced; support rotation and rollback tested.
- **L4 Production-ready:** sustained monitoring, deployment SLOs, disaster recovery tests, independent review, usage rights verified.

This pack describes L3/L4 practices but does not assert they have been implemented. Build L1 then L2 before attempting L3.
