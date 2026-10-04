# SankOS Architecture

## Guiding constraints

1. **Working software first.** Every phase ends in an installable build.
2. **No fake system access.** A normal Android app cannot do what SystemUI does. SankOS documents the boundary instead of pretending it away.
3. **Local-only intelligence.** Ranking and behavior analysis run on-device. v0.1 has no `INTERNET` permission, so privacy is structural, not a policy.
4. **Migration path to AOSP.** UI and "intelligence" are separated so the intelligence can later move into system services without being rewritten.

## Module layout (v0.1)

Single Gradle module `:app`, layered by package:

```
com.sankos.launcher
├── designsystem/
│   ├── theme/       SankColors, SankType (variable fonts), SankTheme
│   ├── motion/      SankMotion — spring specs, one motion language
│   ├── dotmatrix/   DotGlyphs (5x7 grid), DotText renderer
│   └── icons/       SankIcons — ImageVectors built in code
├── domain/          PURE Kotlin, no Android imports in signatures
│   ├── UsageAnalysis  events → launches / duration / hour histogram
│   ├── AppRanker      usage + categories → ranked drawer list
│   └── SearchEngine   query → quick actions + app hits
├── data/
│   ├── AppRepository    LauncherApps scan, dock resolution, launch intents
│   ├── UsageRepository  UsageStatsManager behind the special permission
│   ├── PrefsRepository  DataStore-backed SankPrefs
│   └── model/           AppEntry, AppUsage, AppCategory
└── launcher/
    ├── LauncherRoot   shell: gesture progress, drawer transform, sheet
    ├── HomeScreen     clock, date, battery, dock, hint
    ├── DrawerScreen   ranked sections + search
    ├── CustomizeSheet settings
    └── LauncherViewModel  orchestration, IO on Dispatchers.IO/Default
```

**Why one module:** v0.1 optimizes for iteration speed and a single source of truth. The `domain` package is already Android-free and unit-tested, so it can be lifted into a shared `:core` module (or an AOSP service) unchanged. Splitting into Gradle modules now would add build friction without changing the dependency direction — `launcher → domain → data`, never sideways.

## The ranking pipeline

```
UsageStatsManager.queryEvents(14d)
  → UsageEvent(package, ts, isStart)[]          (data layer, Android API)
  → UsageAnalysis.aggregate()                    (pure: sessions, durations, hour histogram)
  → AppRanker.rank(apps, usage, now, zone)       (pure: score = (0.45·recency + 0.35·frequency + 0.20·duration)
                                                  × categoryWeight × timeOfDayAffinity [+ pinBoost])
  → DrawerScreen sections                        (FREQUENT > 0.12, then EVERYTHING)
```

Category weights: TOOL 1.0, PRODUCTIVITY 1.05, NAVIGATION 0.95, MUSIC 1.0, COMMUNICATION 0.9, UNKNOWN 0.8, ENTERTAINMENT 0.7, **DISTRACTION 0.5** — the anti-compulsion primitive. Categories come from a package-name heuristic in v0.1 (`AppRepository.categorize`); user overrides are planned for v0.2.

Recency is exponential decay with a ~25-hour half-life. Frequency and duration are log-scaled so 200 opens ≠ 200× prominence. Time-of-day affinity is capped at +50% and needs ≥5 launches of history.

## Gesture / surface model

The home and drawer are **one composable space**, not two destinations. `LauncherRoot` owns an `Animatable<Float>` progress:

- Vertical drag on home moves progress directly (finger-following); release hands off to a spring (`dampingRatio 0.86, stiffness 380`).
- Home scales to 0.95 and fades as the drawer rises; the drawer translates 240px, scales 0.96→1.0 and fades in — spatially connected, not a screen swap.
- Leaving the launcher (launching an app, screen off) snaps progress to 0 so returning users always land on calm home.

## Permission ledger

| Permission | Why | Grant type | Without it |
|---|---|---|---|
| `QUERY_ALL_PACKAGES` | a launcher must enumerate launchable apps | normal (sideload context) | n/a |
| `PACKAGE_USAGE_STATS` | adaptive ranking | special — user enables in Settings | drawer is alphabetical; nothing breaks |
| `VIBRATE` | gesture feedback | normal | no haptics |

Deliberately absent: `INTERNET` (no network in v0.1), `READ_CONTACTS` (contact-name calling is v0.2 with an explicit grant), notification listener (v0.2+).

## What requires privileged / system access (AOSP boundary)

| Capability | Launcher reality | AOSP future |
|---|---|---|
| Replace notification shade | impossible; we can only read via NotificationListener + user grant | custom SystemUI owns the shade |
| Enforce app launch limits / Focus restrictions | soft friction only | `DevicePolicyManager`-grade or system-service enforcement |
| Per-app network/firewall | impossible | netd/iptables integration |
| True lock screen customization | impossible (Keyguard is system) | SystemUI keyguard plugin |
| Frame-accurate app-open animation from icon origin | approximated via activity transitions | `RemoteTransition`/WindowManager Shell integration |
| Boot-time defaults, OTA, power-user controls | n/a | system services + HAL work |

These are the seams where SankOS launcher code will be refactored when the AOSP fork begins (Phase 4). Nothing in v0.1 pretends to have these powers.

## Data flow & persistence

- `sankos_prefs` DataStore: clock style, accent, 24h, drawer-hint flag. All user-settable, all local.
- Usage signals are computed on demand (drawer open, resume) and **not persisted** — recomputation from UsageStatsManager is cheap and keeps zero behavioral traces beyond what the OS itself already holds.
