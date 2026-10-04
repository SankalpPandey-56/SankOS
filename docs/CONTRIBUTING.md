# Contributing to SankOS

## Ground rules

1. **Working software first.** `main` must build and pass tests. Don't commit unverified changes; run `./gradlew testDebugUnitTest assembleDebug` first.
2. **Every element earns its place.** New UI needs a "why" against the four wins: faster, calmer, more useful, more personal.
3. **Honesty about platform limits.** Never ship a UI affordance that pretends to have system powers a normal app doesn't have. Document the AOSP seam instead.
4. **Local-only.** No network permission, no analytics, no tracking SDKs. Behavioral data stays on-device.

## Workflow

- Feature branches off `main` (`feature/app-drawer` style), merged back via PR when green.
- Conventional commits: `feat:`, `fix:`, `perf:`, `docs:`, `test:`, `chore:`.
- One logical milestone per commit; no "update everything" dumps.
- File issues for bugs (with device + repro steps) and for design decisions that need discussion before code.

## Code style

- Kotlin, official style; Compose for all UI; no new dependencies without an architecture note in the PR.
- `domain/` stays pure Kotlin — no Android imports in signatures — so it can migrate into AOSP services later.
- Design tokens (colors, type, motion) come from `designsystem/`; never hardcode a hex value in UI code.

## Testing

- Ranking/search/usage logic changes need unit tests in `app/src/test`.
- UX changes need an entry (or update) in `docs/TESTING.md`'s device matrix.
