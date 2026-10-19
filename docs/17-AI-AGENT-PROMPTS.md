# 17 — Copy/paste prompts for Claude Code / coding agent

Use these prompts in sequence, changing only task IDs as work progresses. Agents must read `AGENTS.md` and exact contracts. Avoid 'build the entire platform autonomously' with no verification gates.

## Prompt A — Bootstrap, inspect repo
```text
You are the lead software engineer for BEYOND90. Read AGENTS.md, README.md, docs/00-EXECUTIVE-BRIEF.md, docs/01-PRD.md, docs/02-ARCHITECTURE.md, docs/03-DATA-AND-EVENTS.md, docs/11-DELIVERY-PLAN.md, and contracts/*. Do not implement features yet. Inspect the repository and environment, identify contradictions, missing credentials and version compatibility risks. Return an actionable P0-01/P0-02 plan, exact commands, and a short proposed directory tree. No fabricated benchmark results or dependencies.
```

## Prompt B — Execute one task with tests
```text
Implement the next unblocked task in docs/11-DELIVERY-PLAN.md, starting with P0-01. Read its AC and relevant design docs. Write or update contract and tests before app code. Backend MUST be Java 21 Spring Boot 4.1.x; no Python backend. Frontend Next.js/TypeScript/R3F with 2D fallback. Keep pure deterministic algorithms in Java. Run all applicable commands (test, typecheck, lint, build) and record actual results. If a gate fails, fix it rather than move on. Update status and report: changed files, tested behavior, AC pass/fail, blocker, next task. Never claim a cloud call happened without a real response.
```

## Prompt C — Architecture review / anti-slop
```text
Act as a skeptical senior engineer reviewing this BEYOND90 PR. Read AGENTS.md, docs/03-DATA-AND-EVENTS.md, docs/04-FOOTBALL-ENGINES.md, docs/05-AI-ORCHESTRATION.md, docs/08-QUALITY.md, and the diff. Find contract drift, hidden frontend mock logic, non-reproducible simulation, missing tracking evidence, made-up probabilities, insecure cloud credentials, dead interactions, accessibility failures, and unnecessary complexity. Give prioritized findings with exact file:line references and fixes. Do not praise code or assume a test passed without evidence.
```

## Prompt D — Focused real Foundry integration
```text
Read docs/05-AI-ORCHESTRATION.md and docs/14-RESEARCH-AND-SOURCES.md. Implement a Java-only Foundry narrative adapter behind NarrativeProvider using the verified current official SDK/API for the configured deployment. Microsoft Agent Framework is not a Java SDK. First perform a real minimal model call with a synthetic EvidencePack if credentials exist; otherwise flag integration BLOCKED without claiming success. Validate typed JSON, citation IDs, numeric grounding and timeouts; add deterministic template fallback and tests with a mock provider. No API keys in browser or git.
```

## Prompt E — Production readiness gate
```text
Perform the release preflight for BEYOND90 using docs/08-QUALITY.md, docs/09-SECURITY-AND-PRIVACY.md, docs/10-PLATFORM-AND-OPS.md, docs/12-EVALUATION.md and docs/13-SUBMISSION.md. Run available tests; inspect real deploy health, URLs and assets; measure actual values only. Produce a release report with explicit 'PASS/FAIL/NOT TESTED' per gate, list blockers and exact steps to fix. Do not call the project production-ready unless all L3/L4 gates have evidence. Prioritize submission-critical fixes before optional enhancements.
```

## Parallelization guidance
Safe separate tracks after contracts freeze: (A) Java fixture/replay, (B) R3F renderer using DTO stubs strictly derived from shared contracts, (C) research/legality/DevOps docs. Do NOT simultaneously allow two agents to modify OpenAPI/data schema or force unreviewed merges. An integration owner resolves conflicts after each task checkpoint.
