# 06 — 3D experience, interaction design and accessibility

## Visual north star
A premium editorial football intelligence console, NOT a generic sci-fi admin dashboard. Deep charcoal/navy surface, restrained electric cyan for selected analytical geometry, coral/orange for opponent pressure, off-white for data. Typography: modern legible sans and monospace figures; hierarchy driven by match context. Strong brand identity from original diagrams and tactical language, not visual clutter, excessive neon, 'AI brain' clip-art, or stock sports photography.

## Main canvas design
Desktop >1280px: app header (brand, Match status), main **3D pitch** (60–70% width), tactical inspector (right 30–40%), match timeline across bottom; branch comparison replaces inspector with 2-up decisions / overlay toggle. Tablet: pitch above inspector; mobile: fallback 2D as default. Match clock visible at all times.

### Essential components
`MatchHeader`, `Scoreboard`, `ReplayControls`, `TacticalPitch3D`, `TacticalPitch2D`, `PlayerMarker`, `BallMarker`, `CameraModePicker`, `EventTimeline`, `XRayGeometryLayer`, `EvidenceInspector`, `BranchActionPicker`, `BranchComparison`, `AudienceToggle`, `AIStatus`, `SourceDisclosure`, `ErrorBoundary`, `ReducedMotionToggle`.

## 3D rendering
- Next.js with R3F/Three.js client-only scene. Import via client boundary with SSR disabled only for canvas; server components can render surrounding content.
- Geometry: procedural 105x68 plane, pitch markings; low-poly discs/capsules for 22 players, ball sphere, optional jersey numbers as simple sprites; no licensed team crests or player models.
- Pitch axes are in `03-DATA-AND-EVENTS.md`; `worldX = pitchX - 52.5`, `worldZ = pitchY - 34`, up-axis `worldY`. Use consistent coordinate adapter with unit tests.
- 3 camera presets: `BROADCAST` (oblique angle), `TACTICAL` (near top-down), `XRAY_FOCUS` (zoomed to bounding box of evidentiary players). Orbit can be bounded to avoid flipping below pitch. 'Reset view' always works.
- Highlight geometry is driven by `InsightOverlayDTO`: trajectories as tubes/lines, defender pressure rings, transparent polygons for open zones and labels anchored to players. Screen-reader explanation duplicated in accessible DOM.
- Frame interpolation from immutable game-time keyed snapshots: no accumulation drift; P0 uses sparse animation with labeled interpolation. When tracking missing, show static event marker, **not imagined off-ball trajectories**.

## Performance budget (targets that must be verified)
- Laptop 1280x720 baseline: 30 FPS minimum on a mid-range device for 22 markers, aspirational 60 FPS.
- Keep draw calls low; reuse geometry/materials and prefer instanced objects for repeated player markers. Render on demand when paused; frame-loop only while animating.
- Avoid per-frame React state setters or spawning mesh objects per frame. Use refs/animation loop where appropriate; group separate state updates.
- Cap device pixel ratio; support low-quality setting and disable postprocessing by default. No massive stadium geometry or crowd assets.
- WebGL context loss: recover if possible or switch to 2D; 2D provides identical replay and explanation functionality.

## State machine / interaction
`LOADING -> READY_PAUSED -> PLAYING -> READY_PAUSED`, with `SEEKING`, `XRAY_OPEN`, `BRANCH_COMPUTING`, `BRANCH_READY`, `ERROR_RECOVERABLE`, `OFFLINE`. Use explicit reducer/state machine, not scattered booleans with impossible combinations. When seeking, cancel previous requests or accept only most recent request ID. On opening branch, freeze original time anchor and preserve replay position when returning.

## Interactive explanations
- Hover/focus event moment -> concise summary with exact time.
- Click X-Ray -> timeline pauses, camera moves to tactical moment, evidence points are highlighted; `View evidence` opens IDs and derived metric details.
- Click 'Explore alternative' -> selected action appears in clearly separate branch color/path. Include side-by-side toggles `Original`, `Alternative`, `Difference` and exact disclaimers.
- Mode switch changes only narrative level and metric detail; same facts, geometry, ID provenance.

## Accessibility and honest UX
- 2D mode uses standard HTML buttons and SVG with DOM-linked textual summaries; not all critical data trapped inside canvas.
- Keyboard: Space play/pause unless typing; Left/Right seek; Enter activate selection; Escape close inspector; focus return to originating element; documented shortcut overlay.
- Respect `prefers-reduced-motion`: disable animated camera transitions, keep step-play option. Buttons have visible focus, aria-label, WCAG-conscious contrast.
- Show data provenance chip `SYNTHETIC`, branch label `SIMULATED`, AI status `Grounded explanation`, `Template fallback` or `Unavailable`; never disguise generated scenarios as video evidence.
- Loading skeletons, empty states, recoverable errors and reconnect banner, not spinner purgatory.

## Design acceptance / screenshots
1. Scoreboard, timer, 3D pitch and relevant events visible above fold on 1440x900.
2. M01 highlight clearly explains constrained outlets before turnover without excessive overlap.
3. Branch differs visibly from original; selected path can be inspected without spinning camera.
4. Full story functions with WebGL disabled and keyboard only.
5. 30 FPS baseline measured via browser profiling; record hardware, browser and scene quality.

## Visual no-slop checklist
No placeholder logos, random cards, generic charts without actionable meaning, unlabeled precision, drifting camera, fake 3D video, nonfunctional toggle, or fake simulation statistics. If an animation does not convey evidence, remove it.
