# 18 — Production-readiness scorecard (evidence required)

At project creation all rows are **NOT IMPLEMENTED/NOT VERIFIED**. A documented plan does not imply implementation. Update with commit hashes, test output, deployed URL and measurements.

| Dimension | Target evidence | State at creation |
|---|---|---|
| Functionality | production URL demonstrates entire judge flow | NOT IMPLEMENTED |
| Correctness | fixture/geometry/branch golden suites green | NOT IMPLEMENTED |
| Explainability | all displayed claims have valid evidence graph | NOT IMPLEMENTED |
| Reliability | provider outage and WebGL failure recover gracefully | NOT IMPLEMENTED |
| Security | secrets audit, rate limits, dependency scans, threat review | NOT IMPLEMENTED |
| Performance | documented load and 3D frame profiling reports | NOT MEASURED |
| Availability | external uptime checks and rollback rehearsal | NOT VERIFIED |
| Operations | on-call, dashboard, alert triage, incident response | NOT VERIFIED |
| Accessibility | keyboard + 2D fallback + independent audit | NOT VERIFIED |
| Governance | dataset permissions, artwork/licenses, source register | IN DESIGN |
| Maintainability | contract CI, typecheck, API tests, runbook | IN DESIGN |
| AI integrity | zero unsupported numeric statements in held-out eval | NOT MEASURED |

## Release classifications
- **Hackathon-demonstrable:** L1 + public access + compliance + no stop-ship failures.
- **Production candidate:** above, plus performance/security/accessibility/load tests and supported rollback/monitoring.
- **Production-ready:** production candidate plus sustained reliability evidence, operational ownership, compliance and incident recovery proven under expected user scale. This designation requires evidence, not marketing.

## How to update this file
For each row replace state, add evidence URI/path, tested date, owner, commit SHA, exact observed metric and residual risk. Reviewer should be able to reproduce the claim. Keep failed tests visible.
