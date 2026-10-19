# 03 — Canonical data, events and temporal semantics

## Data source policy
Synthetic match events and curated tracking snapshots are original project assets. Public football datasets can **inform plausibility**, but do not bundle third-party event datasets or videos without explicit license review and attribution. References: StatsBomb Open Data and Metrica Sports sample-data, described in `14-RESEARCH-AND-SOURCES.md`.

## Pitch and coordinates
- Physical pitch dimensions **105 m x 68 m**.
- `x = 0` left goal line, `x = 105` right; `y = 0` top touchline in canonical overhead diagram, `y = 68` bottom; x/y always metres, origin fixed for entire match.
- Team attack direction is metadata per period: `LEFT_TO_RIGHT` or `RIGHT_TO_LEFT`. Changing ends **must not** flip stored coordinates, only interpretation and camera projection.
- For WebGL, map pitch to `worldX = x - 52.5`, `worldZ = y - 34`; elevation maps to `worldY`; camera looks toward pitch; never mix `y` (pitch coordinate) with `worldY` (height).
- For SVG viewBox 1050x680, pxX = x * 10, pxY = y * 10.

## Time model
- `matchClockMs` is a **monotonically increasing match-elapsed timeline** starting at kick-off. Timeline may include stoppage; no claim all minutes map to precise 90*60*1000.
- `period` enum `FIRST_HALF | SECOND_HALF | EXTRA_FIRST | EXTRA_SECOND | PENALTIES`; `periodClockMs` increments independently in each period; `matchClockMs` is monotonic absolute match-elapsed value. A `clockLabel` is derived from period metadata; never use synthetic wall time to compute score/tactical changes.
- For events at equal `matchClockMs`, `seq` is a positive unique ordering key; sort `(matchClockMs, seq)`.
- Every non-visual narrative/overlay has `effectiveAtMs` and optional `expiresAtMs`, plus `sourceInsightId`.

## Core entities
- `Match`: matchId, scenarioVersion, fixtureSchemaVersion, generatorSeed, pitch config, 2 fictional teams, player roster, **explicit period metadata (`startMs`, `attackDirectionByTeam`)**, featureAvailability.
- `Event`: eventId, seq, period, matchClockMs, type, teamId, actorId, location, optional targetActorId/endLocation, outcome, snapshotId, provenance=`SYNTHETIC`.
- `TrackingSnapshot`: snapshotId, matchClockMs, ball `(x,y,z)`, list of 22 `PlayerPose` `(playerId, x, y, speedKnown?, headingKnown?)`, reliability metadata. Missing pose => reason to refuse off-ball claims.
- `Moment`: momentId, intervalMs, linked eventIds, linked snapshotIds, `availableActions[]`, originalActionId, targetPlayerId.
- `Metric`: id, name, value, unit, definitionVersion, computationWindow, supporting event/snapshot IDs, optional geometry entities.
- `Insight`: id, type, headline, factClaims[], metricIds[], evidenceIds[], confidenceCategory, caveat[], algorithmVersion.
- `Branch`: branchId, baseSnapshotId, parentMomentId, intervention enum, seed, engineVersion, pathFrames[], metrics[], limitations[], branchHash. Original immutable.

## Event types P0
`PASS`, `CARRY`, `SHOT`, `TACKLE`, `INTERCEPTION`, `POSSESSION_CHANGE`, `PRESSURE`, `GOAL`, `FOUL`, `PERIOD_START`, `PERIOD_END`.

## Domain invariant tests (REQUIRED)
1. `0<=x<=105`, `0<=y<=68`, all numeric finite (no NaN/Infinity).
2. Each eventId unique; each seq unique and positive within match; ordered by `(matchClockMs, seq)`.
3. actorId belongs to teamId and exists in roster; linked IDs resolve to fixture.
4. PASS endLocation or targetActorId is present; GOAL increments correct score once; SHOT does not auto-increment score.
5. Every X-Ray off-ball insight has sufficient snapshots; snapshot contains expected player identities (or explicit missingness).
6. Ball/player position transitions do not exceed configurable plausibility thresholds; explicit discontinuities (kick-off restart/teleport for cuts) must be annotated.
7. Scenario version is included in browser cache key, WS handshake and API responses.
8. Branch result never changes original timeline checksum.

## Synthetic fixture design
`demo-001` Blue FC vs Red United: three curated scenes:
- **M01 Pressure trap** (minute ~22): defenders close two outlets, pass gets intercepted; evidence uses player positions, pressure event and turnover.
- **M02 Off-ball decoy run** (~47): one attacker draws a defender, lane opens for runner; evidence uses before/after snapshots, not only event stream.
- **M03 Decision fork** (~67): attacking player with pass/shoot opportunities; original stored, at least two interventions simulated from same base snapshot.

Fixture should also contain mundane possession sequences and score-consistent milestones so it feels like football rather than a scripted one-minute animation. Do not create a full 90-minute 25Hz tracking dataset; sparse snapshots around known key moments are enough when appropriately labeled.

## Dataset versioning/reproducibility
Each fixture has `schemaVersion = 1.0.0`, `scenarioVersion` opaque version string and `generatorSeed` integer. Persist fixtures in git, use deterministic generation script where available, and hash normalized JSON to detect drift. Golden fixtures checked into tests. Any change to event semantics updates contracts + fixtures + tests + change log.

## Provenance classification
- `OBSERVED_SYNTHETIC_EVENT`: exists in authored canonical fixture.
- `DERIVED_METRIC`: deterministic computation on original synthetic fixture.
- `MODEL_EXPLANATION`: LLM paraphrase of validated metrics/events.
- `SIMULATED_ALTERNATIVE`: output of counterfactual engine, not observed or calibrated probability.
These categories must survive through API and UI text.
