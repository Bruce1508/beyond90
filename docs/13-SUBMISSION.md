# 13 — Hackathon eligibility and submission playbook

## Official dates (Microsoft's official rules, not platform estimates)
- Registration closes **October 20, 2026 at 12:00 PM Pacific**.
- Submission closes **October 27, 2026 at 11:59 PM Pacific** (Toronto EDT: October 28 02:59 AM).
- Judging occurs Oct 27 – Nov 10, 2026; project must be freely accessible for testing through judging.
- Verify entrant/team eligibility and registration individually; this pack cannot establish your account's registration status.

## Required submission
- [ ] Newly built working project using *synthetic*, football-realistic data.
- [ ] Clear project pitch: problem, solution, technical implementation, Microsoft/Azure technologies actually used.
- [ ] Public GitHub code repository with reproducible setup, screenshots, licenses and architecture; never publish secrets.
- [ ] Demo video **strictly under 2 minutes**, footage of actually running project on target device/platform, public supported URL.
- [ ] Public working demo/test build with free judge access and no restrictive sign-up; include testing instructions, working credentials only if needed (avoid auth for P0).
- [ ] Submission text, video and instructions in **English** (or an English translation for every non-English component).
- [ ] Proper license/permissions for any external materials. Use no Premier League footage, third-party logos or copyrighted music without valid rights.
- [ ] Confirm submission on hackathon site **before** deadline and retain confirmation.

## Judging criteria (equal 20% each)
1. Technical implementation: optimized synthetic data, engineering quality, Microsoft stack, maintainability.
2. Agentic design and innovation: specialized collaboration, tool use, handoffs and recoverability.
3. Real-world impact: concrete usefulness, path to production, credible adoption.
4. UX/presentation: compelling functioning video and coherent balanced frontend/backend.
5. Category adherence: comply with named category requirements.

Consider category `Best Enterprise Solution` for readiness discipline or `Best Multi-Agent System` if the evaluated Java role workflow is genuinely meaningful; category choice must match eligibility/submission options available on site. Do not claim use of Microsoft Agent Framework in Java.

## 110–115 second demo script (English, editable)
- **00–15** Problem: 'We can see every goal. But can we see the movement that created it?'
- **15–35** Show fictional 3D match: score, running match clock, events.
- **35–60** Click X-Ray on pressure trap: camera reframes, passing lanes light up, inspect original event IDs and geometry.
- **60–87** Freeze a fork moment: Pass vs Shoot, show two bounded trajectories, compare using transparent 'synthetic simulation' caveat.
- **87–102** Fan vs Analyst toggle, show evidence-grounded Foundry explanation and actual fallback/recovery, if time.
- **102–115** Java/Spring Boot + Foundry + 3D + Azure diagram/technical proof and GitHub/deployed URL.
- **115–119** Closing one-line product vision; end before 120 sec.

## Project pitch draft
> **BEYOND90** is an interactive football intelligence experience that turns original synthetic match events into explainable tactical stories. Our Java/Spring Boot analytics engine links pressing, space creation and passing lanes to replayable evidence on a 3D pitch; the Multiverse feature lets fans change a decision and observe a clearly labeled, bounded simulation. Microsoft Foundry generates audience-tailored explanations only from validated facts, with deterministic fallback and transparent uncertainty. Deployed to Azure, BEYOND90 demonstrates a responsible path from data to live-feeling overlays for fans, analysts and broadcasters.

Update final pitch only to describe **actually shipped** features; do not copy aspirational parts if cut.

## Judge test instructions draft
1. Open PUBLIC_DEMO_URL (no login), choose `Blue FC vs Red United`.
2. Press Play, seek to moment M01 and select X-Ray.
3. Click 'Evidence' to see metric/event IDs; switch Fan/Analyst mode.
4. Select moment M03; choose Pass vs Shoot, compare original and generated branch.
5. Read 'About this data': all names/events are fictional and synthetic; counterfactuals are model assumptions.
6. GitHub: PUBLIC_REPO_URL; video: PUBLIC_VIDEO_URL; submitter support: PUBLIC_CONTACT_OPTIONAL.

## Final QA
Record screenshots and a fresh incognito walkthrough; audit duration, URL permissions, repo visibility, external API health, Azure credits/budgets, project availability through judging, and system time zone. Submit early to avoid platform issues.

Official source: https://github.com/microsoft/insidethegamehackathon/blob/main/OFFICIAL%20RULES.md
