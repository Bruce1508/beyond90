# 19 — Java implementation blueprint (Spring Boot 4.x)

This document is an *implementation design*, not a claim these Java files exist yet. Respect `AGENTS.md` and the source-of-truth OpenAPI/JSON Schema contracts.

## Java project structure
```text
apps/api/
  pom.xml
  mvnw, .mvn/wrapper/*
  src/main/java/com/beyond90/
    Beyond90Application.java
    config/{CorsConfig,OpenApiConfig,RateLimitConfig}.java
    match/domain/{Match,Team,Player,MatchEvent,TrackingSnapshot,Moment}.java
    match/fixture/{FixtureRepository,ClasspathFixtureRepository,FixtureValidator}.java
    match/playback/{ReplayProjector,TimelineService,ScoreProjector}.java
    football/geometry/{Point2D,SegmentMath,SpaceGeometry}.java
    football/analytics/{LaneMetricService,PressureDetector,SpaceDetector,EvidencePackBuilder}.java
    football/counterfactual/{ActionValidator,BranchSimulator,DefenseHeuristic,BranchEvaluator}.java
    ai/application/{NarrativeOrchestrator,AnalystStep,ComposerStep,NarrativeVerifier}.java
    ai/provider/{NarrativeProvider,FoundryNarrativeProvider,TemplateNarrativeProvider}.java
    api/{MatchController,MomentController,RecapController,ErrorHandler}.java
  src/main/resources/fixtures/demo-001.json
  src/test/java/com/beyond90/{fixture,analytics,counterfactual,api,ai}/...
```

## Maven dependency policy
Start at `https://start.spring.io` selecting **Java 21**, **Spring Boot 4.1.x**, Spring Web, Validation, Actuator and Test. Spring AI dependency from current 2.0.x BOM following official Spring AI docs; choose ONE inference implementation (`spring-ai-starter-model-openai` or official OpenAI Java SDK) after a verified Azure Foundry smoke test. OpenAPI: springdoc only after verifying a Boot 4-compatible version; static checked contract file may suffice for hackathon. Observability: Micrometer/OTel optional post-core. Avoid JPA, PostgreSQL or Kafka until needed.

## Core Java interfaces and immutable types (illustrative signatures)
```java
record PitchPoint(double x, double y) { /* finite and bounds validated */ }
record PlayerPose(String playerId, PitchPoint position) {}
record MatchEvent(String eventId, long seq, long matchClockMs, EventType type,
                  String teamId, String actorId, PitchPoint location,
                  String snapshotId) {}
interface FixtureRepository {
    MatchFixture getById(String matchId);
    List<MatchSummary> list();
}
interface AnalyticsEngine { EvidencePack inspect(String momentId); }
interface BranchSimulator {
    BranchResult simulate(TrackingSnapshot base, Intervention intervention,
                          long seed, String engineVersion);
}
interface NarrativeProvider { NarrativeCandidate generate(EvidencePack evidence,
                                                    AudienceProfile audience); }
```
Use explicit DTO mapping; keep domain records free of Jackson annotations where practical. `PitchPoint` guards finite values and pitch bounds on construction. Do not assume this sketch supersedes `contracts/openapi.yaml`.

## Deterministic replay
`ReplayProjector.project(fixture, atMatchClockMs)` walks sorted events `<= t` (by `(matchClockMs,seq)`) and returns scoreboard + current possession + last event + current moment. For small P0 fixture O(n) is fine; add binary search/cumulative prefix index only if measured. Player visuals use `TrackingInterpolator` that returns tracking scene with source snapshot IDs; if no snapshot, show last known static positions and warning. No invented exact movement.

## Tactical geometry implementation
- `SegmentMath.minDistance(point, lineStart, lineEnd)`: projection t = dot(P-A,B-A) / dot(B-A,B-A), clamp 0..1, return distance to closest endpoint/interior; zero-length segment handled.
- `LaneMetricService.evaluate`: computes per-defender distances, clearance and considered defenders, returns Metric plus evidence IDs, no probability interpretation.
- `PressureDetector`: consume explicit pressure/turnover temporal pattern and lane metric; return typed rule hits/missingness.
- `SpaceDetector`: needs before/after snapshots; when tracking inconsistent/missing, returns INSUFFICIENT instead of a hallucinated off-ball insight.

## Branch engine
Use seeded `SplittableRandom` per request, not shared mutable instance. Inputs: base immutable `TrackingSnapshot`, enum `PASS|SHOOT`, validated target, seed, dt=100ms, duration <=8s. Arrays bounded to max 100 frames; use copies for branch state. Results contain source IDs and effect labels; `originalUnchanged=true` only after confirming original fixture digest untouched. Do not add xG-like metrics.

## HTTP design
Controllers do input validation and delegate to services. Exceptions mapped to RFC-9457-style `ProblemDetail` with sanitized `traceId` and correct HTTP codes; no entity stack traces. `/api/v1/health` should not leak cloud keys or deployment internal details. Ensure simple public routes don't accidentally enable CSRF-protected writes without explicit handling; if Security is installed configure intentional policy.

## Foundry provider spike
Validate endpoint and deployed model via current Microsoft docs. Build a `NarrativeProvider` adapter with request timeout, bounded concurrency, fallback and structured JSON response. Use project-mode API only if verified; `/openai/v1/` model inference works in Java. Write tests with a fake provider and real integration test controlled by environment variable; never run a paid call in every CI run by default.

## Backend Definition of Done
1. `./mvnw -B verify` green from clean clone.
2. All API endpoints implement and match OpenAPI responses.
3. Fixture validator catches bad IDs, unsorted seq/time, impossible score and missing tracking.
4. Branch engine deterministic, pure and mutation-free.
5. Foundry outage does not affect replay/geometry, tested.
6. One actual Foundry inference evidenced before it is claimed as shipped.
