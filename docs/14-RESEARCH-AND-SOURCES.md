# 14 — Verified research and resource register

Research date: **2026-10-19**. Prefer vendor docs and project-owner repositories over blog paraphrases. URLs are starting points, NOT permission to copy content or a guarantee API versions won't change. Revalidate exact dependencies and Azure SKU availability at implementation time.

## Hackathon — primary authority
1. [Microsoft × Premier League — Official Rules](https://github.com/microsoft/insidethegamehackathon/blob/main/OFFICIAL%20RULES.md) — dates, synthetic five-stage pipeline, prizes/judging, required submission, permitted footage and access constraints. Treat official text as authoritative; don't hardcode unsupported interpretations.
2. [Microsoft Hackathon Resources](https://github.com/microsoft/insidethegamehackathon/blob/main/RESOURCE%20PLAYLISTS.md) — organizer-maintained resource collection.
3. [Microsoft Reactor Inside the Game series](https://developer.microsoft.com/en-us/reactor/series/S-1709/) — reference architecture/training, including AKS/ACA soccer insights session (Oct 14, 2026). Useful technical inspiration, no copying protected demo assets.

## Java backend and model integration
4. [Spring Boot 4 reference](https://docs.spring.io/spring-boot/reference/) — Boot's production features (health, observability, configuration). At verification, Boot 4.1.1 listed stable.
5. [Spring AI Getting Started](https://docs.spring.io/spring-ai/reference/getting-started.html) — current Spring AI 2.0.1; **supports Spring Boot 4.0.x and 4.1.x**. Resolve actual BOM/modules in P0-01.
6. [Spring AI OpenAI Chat](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html) — model adapter and structured output examples. GPT-5 style models may reject temperature settings; confirm actual deployment.
7. [Spring AI Azure OpenAI migration](https://docs.spring.io/spring-ai/reference/api/chat/azure-openai-chat.html) — Spring AI 2.0.0-M5 onward advises OpenAI Chat for Azure/Foundry-deployed OpenAI models.
8. [Microsoft Foundry SDKs and endpoints](https://learn.microsoft.com/en-us/azure/foundry/how-to/develop/sdk-overview) — Java Foundry and OpenAI SDK choices; Agent Framework **Python/.NET**, not Java.
9. [Microsoft Foundry model inference endpoint](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/endpoints) — `/openai/v1` path, deployment alias, keyless auth preference, Java sample.
10. [Microsoft Agent Framework FAQ](https://learn.microsoft.com/en-us/agent-framework/support/faq) — officially lists .NET/C# and Python languages only. Do not claim Java framework support.
11. [Java Foundry SDK library overview](https://learn.microsoft.com/en-us/java/api/overview/azure/ai-projects-readme?view=azure-java-stable) — Java project SDK; version could change, and `azure-ai-agents` vs Foundry client APIs need spike validation.
12. [Spring Boot Actuator probes](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html) — liveness/readiness paths and failure discipline.

## Frontend / 3D
13. [React Three Fiber Introduction](https://r3f.docs.pmnd.rs/) — React + Three.js; React 19 requires R3F v9, v10 alpha not default.
14. [React Three Fiber Scaling](https://r3f.docs.pmnd.rs/advanced/scaling-performance) — instancing, frame budgeting and renderer performance.
15. [Three.js documentation](https://threejs.org/docs/) — world transforms/camera/lines/shapes.
16. [Next.js documentation](https://nextjs.org/docs) — supported build, routing and client component boundary.
17. [W3C WCAG 2.2](https://www.w3.org/TR/WCAG22/) — accessible controls, keyboard alternatives and contrast.

## Azure deployment and operations
18. [Azure Container Apps Java](https://learn.microsoft.com/en-us/azure/container-apps/java-overview) — deployment/monitoring considerations.
19. [Azure Container Apps ingress](https://learn.microsoft.com/en-us/azure/container-apps/ingress-overview) — HTTPS, **WebSockets**, termination and ingress behavior.
20. [Azure Container Apps observability](https://learn.microsoft.com/en-us/azure/container-apps/observability) — logs and Azure Monitor metrics.
21. [Azure Container Apps security](https://learn.microsoft.com/en-us/azure/container-apps/security) — identities, permissions and secrets.
22. [Azure Container Apps Key Vault references](https://learn.microsoft.com/en-us/azure/container-apps/manage-secrets) — managed identity and secret lifecycle.

## Public football data — *reference only until legal review*
23. [StatsBomb Open Data (Hudl)](https://github.com/hudl/open-data) — published event JSON and select 360 freeze frames. Read README, license PDF and attribution rules before reuse. Useful to understand event definitions, not automatic permission to package matches as demo or to pretend 360 freeze frames are complete tracking.
24. [Metrica Sports sample data](https://github.com/metrica-sports/sample-data) — anonymized tracking+event examples on 105x68 pitch; normalized axes [0,1] in their format and attribution note. Use to study geometry, not automatically redistribute.
25. [Friends of Tracking educational resources](https://github.com/Friends-of-Tracking-Data-FoTD) — training/analysis approaches; obey data licenses. Any code reuse requires project license check.

## Evaluation and security
26. [OWASP API Security Top 10](https://owasp.org/API-Security/editions/2023/en/0x11-t10/) — API threat inventory.
27. [GitHub Actions security](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions) — OIDC, pinning and least privilege.
28. [OpenTelemetry](https://opentelemetry.io/docs/) — instrumentation and trace correlation.

## Resource utilization decision log
- Adopt now: Spring Boot 4.1.x, Spring AI 2.0.x, Java Foundry inference, R3F + Three, Azure Container Apps, OTel/Actuator, original synthetic fixtures.
- Research only: StatsBomb, Metrica; cite if findings adapted; do not copy datasets without permission and legal review.
- Explicitly avoid: random Kaggle football datasets of unclear licensing, live broadcast scraping, logo packs, video feeds without rights, closed proprietary xG predictors masquerading as original implementation.
- Unverified before implementation: exact Azure region/model quota, deployable Foundry model name, cost, cloud credit, Starter dependency artifacts, R3F performance, web domain availability. Record results in task P0-16 and operational notes.
