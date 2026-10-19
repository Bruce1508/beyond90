# 07 — API, contracts and realtime playback

Canonical OpenAPI 3.1 is `contracts/openapi.yaml`; validate that file in CI. Explicit API version `/api/v1` and schema version inside payloads. Public demo routes are read-only except simulation/explain requests and do not write user state.

## REST endpoints P0
| Verb/path | Intent | Key success property |
|---|---|---|
| `GET /api/v1/health` | readiness summary public-safe | 200 response, no secrets |
| `GET /api/v1/matches` | demo match list | fictional and synthetic badges |
| `GET /api/v1/matches/{matchId}` | full match metadata, roster | pitch metadata and versions |
| `GET /api/v1/matches/{matchId}/timeline` | events and moments | stable sort, bounded limit |
| `GET /api/v1/matches/{matchId}/snapshots` | sparse tracking for scenes | coords and IDs consistent with event timeline |
| `GET /api/v1/moments/{momentId}/xray` | typed X-Ray insight + geometry | nonempty evidence references |
| `POST /api/v1/moments/{momentId}/simulate` | isolated alternative branch | idempotency by seed/params; never mutates original |
| `POST /api/v1/moments/{momentId}/explain` | grounded narrative by profile | validated structured response or fallback |
| `GET /api/v1/matches/{matchId}/recap` | stable summary | supported original moments only |

## WebSocket protocol (optional for P0 live-demo flavor)
Connect `wss://<host>/ws/v1/matches/{matchId}?scenarioVersion=...&afterSeq=...` only if server advertises protocol capabilities. Client local playback with REST remains authoritative fallback. Messages use `type`, `protocolVersion`, `matchId`, `scenarioVersion`, `sessionId`, `seq`, `matchClockMs`, `payload`.

Types: `HELLO`, `SNAPSHOT`, `EVENT`, `PLAYBACK_STATE`, `HEARTBEAT`, `ERROR`. The socket never authenticates using an API secret in query string. Public demo can be anonymous with rate limiting; post-hackathon signed user/session tokens.

WebSocket behavior: on HELLO server returns accepted version and latest cursor. Gaps in seq -> fetch `/timeline`, then resume. Duplicate/out-of-order seq -> ignore. Heartbeats every ~20–30 seconds (measured/configurable); reconnect exponential backoff + jitter, max capped; don't spawn multiple sockets on rerender.

## Error taxonomy
- 400 malformed query; 404 unknown match/moment; 409 mismatched scenario version or stale branch; 422 domain-ineligible action/invalid coordinates; 429 public rate limit; 503 provider unavailable only where no fallback exists; 500 unexpected bug with trace ID.
- `application/problem+json` modeled on RFC 9457, fields `type`, `title`, `status`, `detail`, `instance`, `traceId`, optional `violations[]`. Never expose stack traces.

## Idempotency and concurrency
- Simulation requests pure and safe to retry; their normalized result digest is identical with the same snapshot/action/seed. `Idempotency-Key` optional to dedupe inflight.
- AI requests GET-like logically but use POST to carry audience; cache by scenario and prompt version. Provider call bounded; if two clients request same stable moment, request coalescing optional.
- Reject unsupported model runtime or cloud quotas gracefully; rate limit each caller and cap prompt bytes.

## REST vs streaming choice
P0 should be fully demonstrable **without websocket connectivity**. REST returns full replay timeline; frontend can play deterministically while optional WS events illustrate broadcast readiness. This avoids per-viewer shared-clock complexity. Match clock remains canonical regardless of transport.

## Contract validation plan
Use OpenAPI linter, JSON Schema validation for synthetic fixture, Spring MockMvc/WebTestClient tests for status/payload, and TS API types generated from OpenAPI only if generation does not block schedule. Contract examples must always match actual response JSON.
