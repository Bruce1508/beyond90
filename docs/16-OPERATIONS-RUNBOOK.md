# 16 — Operations and failure runbook

## Bootstrap: developer environment
Prerequisites: JDK 21, Maven 3.9+ or project wrapper, Node LTS, Corepack/pnpm, Docker Desktop or equivalent, Azure CLI if deploying, a Microsoft Foundry model deployment for real AI. Follow generated project READMEs for exact install commands once app is bootstrapped.

1. Clone repo, inspect `AGENTS.md`, contracts and versions.
2. Copy `.env.example` into ignored `.env.local` / API local environment; never expose Foundry key in frontend or terminal capture.
3. Run Java API tests and start Spring Boot in `local` profile; `/api/v1/health` returns green and sample fixture endpoint validates.
4. Run Next.js dev server, inspect match page; compare scoreboard/time from API; try WebGL disabled.
5. Call Foundry health probe through secure server-only smoke (never from public browser). Record actual returned model alias, status, duration. If unavailable, deterministic template mode should still function.

## Judge demo verification (manual, ~90 seconds)
From incognito/browser without local cookies:
- Open public app URL over HTTPS (no sign-in), logo generic/fictional and `SYNTHETIC` visible.
- Load demo-001, Play, Pause, Seek to M01; same events/score on replay.
- Open X-Ray, identify relevant player markers, read evidence references and caveat.
- Jump to M03, simulate Pass then Shoot; separate trajectories; original score unchanged.
- Toggle FAN/ANALYST; AI explanation either validated or honest template fallback.
- Refresh and retest; alternative: disable WebGL and repeat in 2D.
- Check public repository and public video, video <120 sec.

## Troubleshooting decision tree
**Blank 3D viewport:** check browser WebGL and console -> fallback 2D; test R3F in production build; remove shader/postprocessing dependencies; check camera/world transforms.
**Score desynchronized:** check matchClockMs, event seq ordering, period metadata and client seek reducer; compare fixture digest; don't paper over via front-end hardcode.
**Duplicate moments:** dedupe by insight/event ID + scenarioVersion; check WS reconnect `afterSeq` and seek reset.
**Missing X-Ray details:** check linked snapshot IDs; if absent return `INSUFFICIENT` insight, do not draw guessed lines.
**Foundry 429/401/5xx:** verify deployment URL, role/API credentials, model alias, region/quota/billing, timeout and adapter contract. Switch to template explanation; do not block replay.
**Azure container won't start:** image tag/digest, environment variables, container port, probes, `java -jar`, Java memory flags and log stream.
**Public URL fails:** check ACA ingress, TLS, DNS, CORS, allowed origin, WebSocket wss, console network panel; judge should still access REST fallback.
**Spending spike:** disable external inference via env flag; switch to cached/template; inspect call counts; keep site available.

## Release and rollback
1. Stage builds from pinned commits, use build/contract tests, verify from browser.
2. Deploy immutable image tag/digest to demo-prod; watch health and errors.
3. If failure, activate previous known-good ACA revision; confirm public demo; record incident root cause and follow-up.
4. Emergency feature flags separately disable advanced 3D visuals and external inference; preserve X-Ray evidence playback.

## Postmortem template
```
Time range, affected services, impact on judge journey:
Detected via:
Root cause and contributing factors:
Immediate mitigation / rollback:
Corrective task and owner:
Verification and follow-up date:
```

## Data and license release preflight
No embedded footage, club badges, real players or unlicensed datasets. All original fixture generators/inputs checked into repository. If any third-party art/audio/data added, maintain a license/attribution register before publishing.
