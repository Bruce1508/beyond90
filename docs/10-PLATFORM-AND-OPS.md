# 10 — Azure infrastructure, deployment and operations

## Environment architecture
`local`: frontend + API + fixture files; Foundry test credentials optional; deterministic fallback always works.
`staging`: same build artifact as production candidate, separate environment settings.
`demo-prod`: two Azure Container Apps (web/api) with HTTPS; one revision active, known immutable synthetic content; monitoring and cost caps.

## Azure component map
- Azure Container Apps (web/api): external frontend ingress; API may use separate public HTTPS ingress with controlled CORS, or frontend BFF proxy if properly secured. WebSocket supported at ingress per Microsoft docs.
- Microsoft Foundry (model deployment): standard inference v1 endpoint, Java credential / model deployment alias stored in env/secrets.
- Managed identity and Key Vault: protect secrets; Key Vault referenced by Container Apps identity.
- Azure Container Registry (optional): host Docker images with managed identity pulls.
- Log Analytics / Azure Monitor: structured logs, availability requests, latency, status, provider fallbacks. Resource provisioning cost must be reviewed.
- GitHub Actions: build, test, push images, deploy with federated OIDC credentials where feasible; avoid long-lived Azure deploy passwords.

## Dependency-spike gate
Before building the full app, validate:
1. Spring Boot 4.1.x + Spring AI 2.0.x resolve in Maven with the exact selected modules.
2. `Hello World` Java Foundry response from actual model deployment returns JSON and exposes latency/usage as available.
3. 3D Next.js + R3F builds with chosen React major and renders in production build.
4. Container Apps can deploy the hello-world Java container and health endpoint; cost acceptable.

If Foundry access or credits are unavailable, immediately document the blocker, preserve its adapter and continue offline; resolve real provider integration before claim of category fit or submission.

## Environment variables (names only)
`SPRING_PROFILES_ACTIVE`, `PORT`, `ALLOWED_ORIGINS`, `DEMO_SCENARIO_VERSION`, `FOUNDRY_BASE_URL`, `FOUNDRY_DEPLOYMENT`, `FOUNDRY_API_KEY` (local fallback only), `AZURE_CLIENT_ID` (when needed), `APP_AI_TIMEOUT_MS`, `APP_SIM_MAX_FRAMES`, `NEXT_PUBLIC_API_BASE_URL` (public API URL only), `NEXT_PUBLIC_DEMO_MATCH_ID`, `OTEL_SERVICE_NAME`.

## Release checklist
- One source-controlled `Dockerfile` per service, non-root user, health command/probe.
- CI checks unit/integration/frontend E2E and image build.
- Run staging smoke check with source from same commit digest.
- Roll demo-prod revision with a verified URL. Keep previous known-good revision for rollback; no deploy last hour before submission unless fixing stop-ship bug.
- From incognito on unrelated device/network: open site, press Play, X-Ray, Branch, inspect facts; check API health and fallback if Foundry unreachable.
- Verify free public access through November 10 judging; ensure resource quotas and availability plan.

## Operational telemetry
`http_request_duration_ms`, `http_5xx_total`, `fixture_load_failure_total`, `ws_connections_active`, `ws_resync_total`, `ai_provider_latency_ms`, `ai_fallback_total`, `ai_schema_rejections_total`, `branch_sim_duration_ms`, `branch_rejections_total`, `client_webgl_fallback_total` (if opt-in). Traces correlated via traceId, no secret/raw prompt logging. Alerts: high 5xx, app unavailable, model fallback spike, cloud spend threshold.

## Cost control (avoid invented dollar claims)
No reliable spend estimate until Azure region, plan, model deployment, expected traffic and available credits are known. Set subscription budget and alerts before load test. Keep demo traffic limits small, model calls on-click rather than per tick, and cache stable outputs. Use scale-to-zero only after verifying cold-start impact; judge demo must not appear dead. Scale to 1 replica during evaluation if budget permits and requires it.

## Rollback and failure handling
If fresh deployment breaks, route traffic to previous ACA revision; keep schema backward compatible for same fixture; toggles disable AI and 3D separately. If app cannot connect to Foundry, display template fallback and log degraded status. See `16-OPERATIONS-RUNBOOK.md`.
