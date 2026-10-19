# 05 — Java AI orchestration, evidence grounding and fallbacks

## Verified ecosystem boundary
Microsoft Agent Framework officially supports **.NET and Python**, not Java (see Microsoft documentation in `14-RESEARCH-AND-SOURCES.md`). Do not attempt to import fictional Java Microsoft Agent Framework classes. To preserve a pure Java backend, use Spring AI `ChatModel` (compatible with Boot 4.x) and **explicit Java workflow orchestration**. Foundry Java inference uses an official OpenAI-compatible `/openai/v1/` endpoint. If scope later demands genuine Microsoft Agent Framework, introduce a separately scoped .NET/Python agent service only after the hackathon, preserving the Java app's ownership.

## Meaningful roles (not three differently named prompts)
1. **FactPack Builder (deterministic Java tool):** Builds read-only `EvidencePack` from event/snapshot IDs and computed metrics; serializes compact structured inputs; never calls LLM.
2. **Tactical Analyst (LLM agent step):** Receives EvidencePack, produces proposed `claimIds`, possible tactical hypothesis and caveats. Cannot add metric values.
3. **Narrative Composer (LLM agent step):** Receives the *validated* analyst output and profile (FAN/ANALYST; EN/VI if enabled), writes short accessible text with fact reference IDs.
4. **Evidence Verifier (deterministic Java gate):** Parses schema, validates every claim, numeric token/value, allowable vocabulary, evidence ID, prohibited causation terms and limits. Rejects unsafe/unsupported content.
5. **Counterfactual Planner (LLM optional P1):** Can suggest user options from a strict allowlist but cannot fabricate available actions; final validation and simulation remain Java deterministic functions.

Flow: `moment -> EvidencePack -> tactical proposal -> verifier -> narration -> verifier -> DTO`; fallback: `moment -> validated template -> DTO`. For a strong multi-agent demonstration, record distinct handoffs, tool calls, reviewer-visible traces and recovery when a role fails.

## Typed AI I/O (example)
```json
{
  "insightId":"ins_022",
  "locale":"en",
  "audience":"FAN",
  "headline":"The press closed the passing lanes",
  "sentences":[{"text":"Two defenders converged before the interception.","evidenceIds":["snap_m01_b","evt_press_2","evt_interception_1"]}],
  "uncertainty":"Tracking is synthetic; an association is not a proven cause."
}
```

## Prompt system rules
- 'You are an analyst of synthetic football data. Use ONLY the supplied EvidencePack. If a fact is absent, say unknown; NEVER invent names, distances, scores, probabilities or motivations.'
- 'Facts must cite known `evidenceIds` in every sentence. Interpret defensive movement tentatively; do not claim causality, player intent, or prediction accuracy.'
- 'Response MUST match JSON schema exactly. No Markdown or additional properties.'
- 'Audience changes style, never underlying numerical values. Language profile cannot alter facts.'
- Feed user preferences only as enumerated values, never executable instructions. Treat descriptions in event feeds as untrusted data.

## Grounding quality gates
- `claim.evidenceIds` non-empty and all IDs valid.
- Numeric claims must either copy an approved `Metric` value with unit or be omitted. Reject all unapproved percentages.
- Every entity named must exist in roster/team list or be generic (e.g., 'the striker').
- No 'guarantees', 'proved', 'would have scored', 'caused' unless a hard deterministic relationship is defined and explained.
- Responses > configured word/byte limit rejected.
- On validation failure -> 1 repair attempt max if budget allows -> deterministic safe template.
- Maintain a small `fixtures/ai-golden.json` set with adversarial malicious event comments to test prompt injection.

## Integration adapter
`NarrativeProvider` interface:
- `generate(EvidencePack, AudienceProfile, PromptVersion): NarrativeCandidate`
Implement `FoundryNarrativeProvider` and `TemplateNarrativeProvider`. Validate configured base URL and deployment model name at startup; set timeouts, retry budget, concurrency semaphore and circuit breaker. Never allow model/provider exception to block match playback.

## Azure inference notes
Spring AI 2.0.1 and native Java SDK both expose model inference. Choose one implementation after a **Hello World spike**; prefer Spring AI for typed orchestration unless SDK edge cases necessitate official `openai-java`. A Foundry resource/deployment and billing access are prerequisites; never state integration done before a response is returned from *the actual deployed model*.

## Observable operations
Store sanitized logs with `traceId`, `momentId`, `promptVersion`, `modelDeploymentAlias`, `latencyMs`, `fallbackApplied`, `schemaValidationPassed`, `tokenUsage` where available. Do not log secrets, sensitive raw chat, API keys, or full customer data. For hackathon, only fictional public synthetic fixtures are sent.

## Evaluation
See `12-EVALUATION.md`. Compare generic single-stage prompt against analyst->verify->composer orchestration for grounding failure rate, evidence coverage, fallback behavior and latency. This is how agentic complexity earns its cost.

## Performance guidance
Keep inference at key moments/on-click/recap, never per render frame or player movement. Cache stable evidence summaries by `(momentId, scenarioVersion, profile, promptVersion, modelDeploymentAlias)`; explicitly mark when a prepared cache entry is returned versus a live model output. Avoid claims of sub-second latency without measurement.
