# SankOS Testing Strategy

## Status legend

- **Verified (automated)** — ran locally in this session, green.
- **Verified (build)** — compiles/links and resource-merges; not yet exercised on hardware.
- **Device-pending** — needs the physical Vivo Y200e connected.

## Automated (Verified — 21 tests, 0 failures)

| Suite | Cases | Covers |
|---|---|---|
| `AppRankerTest` | 6 | category damping (tool > distraction at equal usage), recency dominance, neutral floor for never-used apps, pin boost, hidden exclusion, capped time-of-day affinity |
| `SearchEngineTest` | 10 | `timer 25` / `25 min timer` / `timer 1h` parsing, range rejection, `call <number>` + bare numbers, `settings` → SankOS action + system app, prefix > substring scoring, subsequence fallback |
| `UsageAnalysisTest` | 5 | closed-session aggregation, open-session-at-window-edge, duplicate-start guard, hour histogram bucketing, stray-pause tolerance |

Run: `./gradlew testDebugUnitTest`. CI (`.github/workflows/ci.yml`) runs these plus `assembleDebug` on every push to `main` and uploads the APK artifact.

## Build verification

- `./gradlew assembleDebug` — **Verified**, BUILD SUCCESSFUL.
- Release variant compiles (debug-key signed for sideloading) — **Verified** via project config; release APK build to be run before first device install.

## Device test matrix (Device-pending — Vivo Y200e 5G)

1. **Launcher adoption** — install, press home, set SankOS default; cold boot straight into SankOS.
2. **Gestures** — swipe-up drawer with finger-following + release spring; drag-to-close; back button behavior; long-press sheet.
3. **Drawer** — ranking shifts after a day of real usage; distraction damping observable (e.g., WhatsApp used heavily but not auto-crowned vs Maps); usage-access off → alphabetical.
4. **Search actions** — `timer 25` opens system timer UI; `call <number>` pre-fills dialer; `settings` opens sheet.
5. **Persistence** — clock style / accent / 24h survive reboot.
6. **Lifecycle** — launch app from drawer → return lands on calm home (drawer reset); rotate launcher; screen off/on.
7. **Battery & performance** — no wakelocks, no services; 120Hz scroll feel; systrace if jank.
8. **Accessibility** — TalkBack traverses home/drawer; font-scale 1.3× layout integrity.

Known device-specific risks to watch: Vivo's Funtouch launcher defaults (it may re-default after a Funtouch settings visit), aggressive battery killers on UsageStats queries, and edge-gesture conflicts with Vivo's own navigation. All get logged as issues if reproduced.

## Rules

- No feature is "done" until its logic has automated tests or an explicit device-checklist entry, and `main` builds.
- Bugs found on-device are filed as GitHub issues referencing the checklist number.
