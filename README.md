# SankOS

A minimal, distraction-resistant mobile operating system experience, designed as a launcher first and an AOSP-based OS over time.

> **v0.1 — working launcher.** Installs on any Android 10+ phone and can be set as the default home screen. No flashing, no root, no modified partitions.

## Philosophy

> Everything should be easy when I intentionally need it, and slightly harder when I'm mindlessly checking it.

SankOS is built around a simple rule — **every element must earn its place**. The home screen is a clock, a date, a battery line and three essential apps. The drawer learns how you *actually* use your phone, but it deliberately distinguishes **frequent** from **habitual**: opening Instagram twenty times a day does not buy it prime placement the way a navigation or notes app earns.

- ~90% ink, ~8% paper, ~2% ember (orange). Beautiful even with the accent off.
- All behavioral analysis is **local**. v0.1 requests **no network permission** at all.
- Friction, not punishment. The system never locks you out; it makes you notice.

## Features (implemented in v0.1)

| Feature | Status |
|---|---|
| Minimal home: numerals **or** dot-matrix clock, dot-matrix date, live battery | ✅ |
| Dock auto-resolved to the device's dialer / messaging / camera | ✅ |
| Swipe-up app drawer with finger-following spring animation | ✅ |
| **Adaptive ranking** (UsageStats-based, category-damped, 14-day window, local) | ✅ |
| FREQUENT / EVERYTHING drawer sections | ✅ |
| Instant search + quick actions: `timer 25`, `25 min timer`, `timer 1h`, `call <number>`, bare numbers, `settings` | ✅ |
| Customize sheet: clock style, orange accent toggle, 24-hour time, usage-access deep link | ✅ |
| Preferences persisted with DataStore | ✅ |
| 21 unit tests covering ranking, search parsing, usage aggregation | ✅ |
| Usage-access permission gracefully optional (alphabetical fallback) | ✅ |

**Planned next** (see [docs/ROADMAP.md](docs/ROADMAP.md)): notification batching + held-notification panel, Focus Mode, anti-checking interventions, control centre, lock screen, usage dashboard.

## Screenshots

_To be added after the first on-device capture session on the Vivo Y200e._

## Architecture

SankOS is a single-module app with strict package layering, designed so components can migrate into an AOSP build in later phases:

```
designsystem/   palette, typography, dot-matrix engine, icons, motion
domain/         pure intelligence: usage aggregation, ranking, search (unit-tested)
data/           PackageManager, UsageStatsManager, DataStore repositories
launcher/       home, drawer, customize sheet, shell gestures
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full breakdown, including which capabilities require privileged/system access in a future AOSP build.

## Installation

**Build from source** (macOS/Linux, JDK 17, Android SDK with platform 36):

```bash
git clone https://github.com/SankalpPandey-56/SankOS.git
cd SankOS
./gradlew assembleDebug        # APK at app/build/outputs/apk/debug/
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Set as default launcher:** press Home after installing → choose **SankOS** → *Always*.

**Enable adaptive ranking (optional):** open SankOS settings (long-press home, or search `settings`) → *Usage access* → allow SankOS. Without it, the drawer is alphabetical and nothing else changes.

## Development

```bash
./gradlew testDebugUnitTest    # domain unit tests
./gradlew assembleDebug        # debug APK
./gradlew lint                 # Android lint
```

Requirements: JDK 17, Android SDK (platform android-36). CI runs tests + build on every push to `main`.

## Device

Initial target: **Vivo Y200e 5G** (Funtouch OS 14 / Android 14, 120Hz AMOLED). minSdk 29 supports Android 10+.

## Current limitations (honest list)

- Swipe-down notifications, Focus Mode, notification batching, control centre, lock screen: **not in v0.1** — tracked in the roadmap.
- A normal app cannot replace the system notification shade or enforce app limits; those parts need privileged access or AOSP work, and are documented as such rather than faked.
- App categories use a package-name heuristic; user-defined categories arrive in v0.2.
- Release builds are signed with the debug key for now; a dedicated keystore comes with the first tagged device-tested release.

## License

[GPL-3.0](LICENSE) — SankOS is and will remain open source.
