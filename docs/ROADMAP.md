# SankOS Roadmap

Every milestone must produce a working, installable build.

## SankOS 0.1 — Basic launcher *(this release)*
- [x] Project initialization, pinned toolchain, CI
- [x] Design system (palette, typography, dot-matrix engine, icons, motion)
- [x] Minimal home screen (clock, date, battery, dock)
- [x] Gesture navigation (swipe-up drawer, spring handoff, long-press customize)
- [x] App drawer foundation with FREQUENT/EVERYTHING
- [x] Local usage aggregation + adaptive, category-damped ranking
- [x] Smart search foundation (apps + timer/dial/settings actions)
- [x] Customize sheet (clock style, accent, 24h)
- [ ] On-device verification on Vivo Y200e *(blocked: device not connected to the build machine yet)*

## SankOS 0.2 — Notification & focus foundations
- [ ] NotificationListener integration: count + batch by app (held notifications)
- [ ] Held-notification panel ("3 notifications" on home → grouped list)
- [ ] User-defined app categories + pins
- [ ] Contact-name calling (`call dad`) with explicit contacts permission

## SankOS 0.3 — Focus system
- [ ] Focus Mode (timer, allowed apps, held notifications, completion screen)
- [ ] App launch friction (gentle intervention: "checked N times, no new messages")
- [ ] Notification windows (schedule)
- [ ] Unlock-awareness ("15 unlocks in 30 minutes — do you need something?")

## SankOS 0.4 — Control centre & widgets
- [ ] Control centre (geometric toggles, spring reveal)
- [ ] SankOS widget language (battery / music / timer instruments)
- [ ] Lock-screen-adjacent essentials panel (via launcher surface, not keyguard)
- [ ] Custom icon engine: per-package vector overrides

## SankOS 0.5–0.8 — Depth
- [ ] Usage dashboard (screen time, unlocks, most used — minimal, non-gamified)
- [ ] Intent sessions (WORK / COMMUNICATION / ENTERTAINMENT with timer)
- [ ] Accessibility pass (font scaling, reduced motion, contrast audit)
- [ ] Performance pass (startup, memory, 120Hz validation, R8)

## SankOS 1.0 — Stable daily driver
- [ ] Signed release, regression suite, device-tested on Vivo Y200e

## SankOS AOSP
- [ ] AOSP fork; SystemUI replacement (shade, keyguard, recents)
- [ ] Privileged focus/restriction service
- [ ] Device bring-up for Y200e (kernel/HAL study)
