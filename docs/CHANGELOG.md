# Changelog

All notable changes. Format: [Keep a Changelog](https://keepachangelog.com/), versions are milestones.

## [0.1.0] - 2026-10-04

### Added
- Minimal home screen: large clock (numerals or dot-matrix), dot-matrix date, live battery, auto-resolved dock (dialer / messaging / camera).
- Gesture navigation: finger-following swipe-up drawer with spring handoff, drag-to-close, long-press customize.
- Adaptive app drawer: FREQUENT / EVERYTHING sections ranked by a local, category-aware scoring engine (recency decay, log-scaled frequency/duration, capped time-of-day affinity, distraction damping, pins).
- Smart search with zero-permission quick actions: `timer 25`, `25 min timer`, `timer 1h`, `call <number>`, bare numbers, `settings`.
- Customize sheet: clock style, orange accent toggle, 24-hour clock, usage-access deep link; persisted via DataStore.
- Design system: Ink/Paper/Ember palette, variable Inter + JetBrains Mono, original 5×7 dot-matrix glyph engine, code-built system icons, spring motion language.
- 21 unit tests for ranking, search and usage aggregation; GitHub Actions CI (tests + APK artifact).

### Fixed
- (first release)

### Known Issues
- On-device verification on the Vivo Y200e is pending (device not yet connected to the build machine).
- Swipe-down notifications, Focus Mode, notification batching, control centre and widgets are planned for 0.2–0.4.
- App categories are a package-name heuristic; user overrides arrive in 0.2.
- Release APK is signed with the debug key until the first device-tested tagged release.
