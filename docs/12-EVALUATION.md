# 12 — Evaluation plan and anti-hallucination benchmarks

## Evaluation philosophy
The model is not the physics engine. Measure **groundedness, fidelity, utility and failure recovery**, not how confident an LLM sounds. Do not conflate synthetic test coverage with real-football validity.

## Ground truth / gold fixtures
Build 12 small fixture cases authored by the project owner; 4 clear pressure, 4 non-pressure, 2 off-ball with two snapshots, 2 insufficient-tracking cases. At least 3 are held out from iterative prompt tuning (goldens). Include exact expected events and metric IDs; no synthetic label 'verified against real matches'.

## Core analytic metric suite
| Metric | Formula / check | Target (internal, not claimed measured) |
|---|---|---|
| Fixture validity | valid fixtures / total | 100% |
| Key event ordering | monotonic `(matchClockMs, seq)` | 100% |
| Evidence coverage | assertions with >=1 valid evidence reference / assertions | 100% |
| Unsupported claim count | numerical/causal claims without support | 0 in golden set |
| Determinism | equal digest for same normalized seed and input | 100% |
| Negative detectors | absence of false positive in curated no-pressure fixtures | all pass |
| Counterfactual isolation | original digest remains fixed | 100% |
| Failure recovery | core playback + template when model 5xx/timeout | 100% in tests |

## Agentic value experiment
A/B evaluate: A = one LLM prompt generates final insight; B = deterministic factpack -> analyst -> verifier -> composer -> verifier. Compare the **same 12 cases** with output validation pass %, metric drift, unsupported claims, narrative usefulness rated blind by 1–2 reviewers, total latency and cost. No claim that multi-agent helps until measured. If B fails to beat A on groundedness or clarity, simplify without misrepresenting 'multi-agent orchestration'.

## Interpretable football geometry
For each detector, document exact scalar features, thresholds, false positive example, missing-data policy and impact. Include video/screenshot overlays of actual geometry along with evidence IDs in PR or evaluation report.

## Counterfactual sanity
- Identical seeds and actions reproduce the same frames/outcomes.
- Changing a pass target changes its geometry; an impossible target yields 422.
- Defender on path is more likely to intercept in simulator than a far defender **for controlled identical snapshots**; do not imply real-world probabilities.
- One intervention cannot mutate other branch state; no future information leakage in inputs.
- Evaluate within-model plausibility only; not predictive accuracy.

## UX evaluation
One independent tester on laptop, one on mobile/2D. Tasks: play -> pause -> key moment -> inspect evidence -> simulate -> compare -> close. Record time to complete, confusion, dead clicks, scroll traps and performance. Fix blocker-level issues.

## Honest evaluation report template
```
Date / commit SHA / scenarioVersion / algorithmVersion:
Hardware/browser:
Test set counts, selection and held-out policy:
Pass/fail metrics with numerators/denominators:
Human review notes:
Foundry model/deployment & promptVersion:
Latency/cost method + number of calls:
Limitations and negative examples:
Conclusion (narrow, falsifiable):
```
