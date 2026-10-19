# Claude Code entrypoint — BEYOND90

Read `AGENTS.md` in full; it is the canonical engineering policy. Then read `docs/11-DELIVERY-PLAN.md` and the relevant product/technical docs. Do not autonomously expand scope past the MVP or invent completion/benchmarks. Work task-by-task using verified test results and a buildable main branch.

## Start prompt
> Read AGENTS.md, README.md, docs/01-PRD.md, docs/02-ARCHITECTURE.md, docs/03-DATA-AND-EVENTS.md, docs/11-DELIVERY-PLAN.md, and the contracts. Summarize blockers, then implement task P0-01 and its tests only. Do not change the contract without updating it. Report actual build/test output and propose P0-02.

## Continuing prompt
> Read AGENTS.md and current git diff/status. Identify the highest-priority unblocked P0 task from docs/11-DELIVERY-PLAN.md. Implement it end-to-end with tests, docs, and validation. If task acceptance criteria fail, fix them before advancing. Do not use 'all tasks done' based on code generation alone.
