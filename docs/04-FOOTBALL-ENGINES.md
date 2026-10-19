# 04 — Football analytics and counterfactual engines

## A. Evidence-first analytics (P0)
All algorithms deterministic, testable, with documented thresholds, caveats and version. Do not call a large model to derive geometry.

### Detector D1: Passing-lane clearance
Given passer P, receiver R and defender points D[i], compute perpendicular Euclidean distance from each defender to **line segment** P-R (clamp projection t to [0,1]); `clearanceMeters = min_i dist(D[i], segment)`. Exclude passer/receiver and optionally defenders far behind endpoint. A lane is `CONTESTED` when clearance below a configurable `k` meters; **k is a chosen heuristic, NOT an empirical intercept probability**. Include defender IDs and the exact snapshot.

**Tests:** straight horizontal segment; defender beyond endpoint; near-zero-length pass; no defender; changing attack direction. Complexity O(Ndefenders).

### Detector D2: Pressure trap (curated scenario)
Within an explicit 5–10 second window before turnover, require:
1. at least 2 pressure events from defending team or explicit distances under a configured radius in 2 snapshots;
2. a constrained outlet metric (count viable nearby passing lanes below a threshold);
3. possession change linked to an interception or pressured pass.
Emit `PRESSURE_ASSOCIATED_TURNOVER`, **not** 'pressure caused the turnover' without experimental evidence. Fallback event-only language if tracking missing.

**Tests:** two press events plus turnover (positive), turnover without pressure (negative), late pressure after turnover (negative), cross-team ID mismatch (negative).

### Detector D3: Off-ball space creation
Requires at least 2 timestamped tracking snapshots. Observe attacking runner displacement and following defender displacement. Compare before/after open-lane clearance or distance to planned receive point. Surface as 'movement consistent with creating space', and show highlighted before/after area; do not assert intent or causality.

**Tests:** defender follows runner and a lane opens (positive), snapshots missing (no output), runner motion without defender following (indeterminate), discontinuous tracking (reject).

### Detector D4 (optional): Momentum/control window
Rolling counts of attacks, box entries, turnovers and shot frequency per fixed match-time windows. Describe as *event-rate shift*, not xG or win probability. Minimum counts before narration; zero-denominator case safe.

## B. Quantitative claims and model confidence
- Confidence categories: `SUPPORTED`, `PARTIAL`, `INSUFFICIENT`; derive deterministically from missingness, coverage and evidence checks, not model self-reported probability.
- Expose thresholds/algorithms in evidence inspector and version in API.
- Never call metrics 'pass success probability' or 'goal expectancy' unless separately calibrated.

## C. Bounded Multiverse simulation (P0)
**Definition:** Given `S0` (observed synthetic base snapshot) and user intervention `a`, generate a *plausible, reproducible trajectory* over next 3–8 seconds. Compare discrete outcomes; no claim of 'the other universe would definitely have scored'.

### Pipeline
1. `ActionValidator`: ensure actor possesses ball and action allowed for context; reject out-of-bounds destinations or target same actor.
2. `BranchFactory`: copy S0 immutably; parent moment ID, original checksum, new branch ID and fixed seed.
3. `Kinematics`: timestep `dt = 100 ms`; ball follows piecewise-linear pass or ballistic-looking shot path (visual height arc only) with configurable synthetic velocities; player response is small, scripted/heuristic limited motion; no claims of realistic humanoid biomechanics.
4. `DefenseResponse`: approximate nearest defender with speed cap and pursuit target; avoid teleport and frame-dependent behavior.
5. `OutcomeEvaluator`: geometric reception/interception vs shot blocked/reaches goal plane; score a small **simulator-specific** categorical outcome.
6. `Compare`: report `pathLengthMeters`, `nearestDefenderMeters`, `reachedTarget: boolean`, `shotOnTarget: boolean` *as simulation outputs*. The front end must label branch results clearly.

### Tactics heuristic examples
- PASS: trajectory from ball to target point; interception if defender distance to ball path at the corresponding time is below synthetic collision radius. Possession transitions only in branch.
- SHOOT: ball moves towards user-selected goal target within mouth; if goal-plane path goes out of goal or crosses defender position, outcome blocked/missed; goalkeeper behavior simplified and disclosed.
- CARRY (P1): ball remains within possession radius, attacker follows path, nearest defender pressure; reject if too much collision complexity.

### Determinism contract
`simulate(baseSnapshot, action, params, seed, engineVersion)` produces byte-equivalent normalized result (excluding transient request/branch IDs) for repeated runs. Do not use wall clock, global mutable Random, browser frame rate, or model-generation as the physical outcome engine.

### Reproducibility and scientific integrity
- For each branch show intervention, seed, engine version and limitations.
- If running Monte Carlo, seeded replicates and intervals are **within-model variability**, not validated real-world success odds. P0 can use just 1–5 deterministic runs. Avoid numeric uncertainty until evaluation.
- A counterfactual is never ground truth; explain unmodeled factors (fatigue, intent, goalkeeper anticipation, teammates' unseen moves, true tracking error).

### Golden tests
- Same snapshot/seed/action -> identical trajectory.
- PASS interception threshold positive and negative geometry.
- SHOOT outside goal rectangle -> MISS.
- Invalid action -> 422, original digest unchanged.
- No trajectory leaves pitch unless ball-out is annotated.
- Event stream score and original timeline unchanged after 100 branch calls.

## D. 3D overlays as projection
The backend emits labeled 2D geometry (circles, line segments, area polygons, path points) in canonical **pitch metres**. The frontend projects into R3F Three.js world. Do not return WebGL scene graph shapes from Java APIs.
