# 09 — Security, privacy, legal and governance

## Assets, actors, boundaries
- Public anonymous judge/fan: controls playback and branch parameters; untrusted origin.
- Trusted Java API: domain validation, rate limiting, Foundry access and bounded computation.
- External model: untrusted output even when paid; strictly validated before user display.
- CI/CD and Azure: privileged deployment identities; credentials never in repo/browser.
- Synthetic fixture: authored data treated as *data* not instructions; datasets may contain untrusted descriptions.

## Threat model / mitigations
| Threat | Mitigation | Test |
|---|---|---|
| Prompt injection in event descriptions | Model only sees numeric/enum facts, escape descriptions, strict tools/allowlist | Adversarial fixture |
| Cost/DoS via repeated explain | Rate limit, cache stable outputs, concurrency semaphore, per-call timeout, Azure quota/budget | load/spam smoke |
| Compute DoS via simulation payload | bounded actions, max frames, dt, duration, seed and coordinate validation | invalid 422 |
| Unauthorized endpoint or CORS abuse | HTTPS, strict allowlist, read-only demo, Auth plan for edits | preflight tests |
| Secrets exfiltration | environment/Key Vault, managed identity, secret scan, deny output of raw creds | scan git/history |
| Supply-chain compromise | lockfiles, pinned build actions, dependency review, SBOM, signed artifacts when feasible | CI gate |
| Viewer injection/XSS | no dangerouslySetInnerHTML, render text as text, CSP | escaped payload UI test |
| Model data leakage | send only fictional minimal event facts, no PII, redacted logs | logging audit |
| Unauthorized asset use | fictional club/team IDs, original graphics/assets, dependency license register | asset audit |

## Data privacy posture
Hackathon demo collects no accounts, no real names, no webcam, no user location, no billing. Minimal system access logs with sensible retention. Fan/Analyst mode in local memory only. Do not add analytics trackers/cookies without disclosure.

## Licensed material
Official contest rules require entrants to license any footage they use; no footage provided. Premier League logo/club crest/player portraits/music are not bundled. Terms of public football datasets can be restrictive; StatsBomb requests explicit attribution/logo when publishing work based on data; Metrica requests acknowledgment for public use. **P0 content is original synthetic data**. Public sources are for modeling and validation research only until reviewed.

## Microsoft Foundry credentials
- Local: environment variables loaded from ignored `.env.local` (never `.env` tracked).
- Azure: prefer Managed Identity + Entra ID for inference where supported; Azure Key Vault secret reference for any required API key.
- Separate local/test/prod credentials; never pass key through Next.js `NEXT_PUBLIC_*` variables.
- Rotate if leaked, invalidate old key, audit GitHub actions logs and git history.

## Security configs P0
Request JSON max size, CORS allowlist from env, no origin wildcard for credentialed access, 429 throttling on simulation/AI, request validation + standardized error bodies, CSP and security headers, secure external links. Actuator exposes health only publicly, detailed diagnostics restricted. Basic dependency vulnerability scan.

## Production follow-up P1/P2
Entra identity or OIDC for privileged authoring, JWT audience/issuer validation, per-user quotas, rate-limited WebSocket auth, audit trail, roles for editors, abuse detection, security incident runbook and log retention policy. Build only when functionality needs accounts.
