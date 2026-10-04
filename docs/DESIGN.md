# SankOS Design System

## Principles

1. **Every element earns its place.** If a thing does not make the phone faster, calmer, more useful or more personal, it is removed.
2. **90 / 8 / 2.** Roughly 90% ink (blacks), 8% paper (off-whites), 2% ember (orange). The accent marks *active*, *selected*, *progress*, *live* — nothing decorative.
3. **Machine voice, human voice.** Inter speaks for humans (clock, labels, search). The dot-matrix and mono speak for the machine (date, metadata, sections). You can tell at a glance what is content and what is system.
4. **No borrowed chrome.** No Material defaults for identity surfaces, no glass, no gradients, no feed. Material3 exists only for plumbing (sheets, switches) reskinned into the palette.

## Color

| Token | Hex | Role |
|---|---|---|
| `Ink` | `#080808` | primary background |
| `InkElevated` | `#111111` | panels, chips, icon wells |
| `InkRaised` | `#171716` | pressed / selected control surfaces |
| `InkLine` | `#232320` | hairlines, dot-grid "off" dots |
| `Paper` | `#F2F0EC` | primary text |
| `PaperDim` | `#9A968F` | secondary text (≥4.5:1 on Ink; the spec's `#77736D` reserved for large text) |
| `PaperFaint` | `#5C5852` | tertiary / hints |
| `Ember` | `#FF6A00` | accent — echoes the orange hardware |
| `EmberSoft` | `#FF8A3D` | large accent fields only |

Accent can be disabled entirely (Customize sheet) — the UI resolves every accent use through `sankAccent()` and stays fully usable in gray.

## Typography

Two variable fonts, bundled locally (one file each, weights via variation settings):

- **Inter** — reading voice. The clock is a design element: `Medium`, tight tracking (−3.4sp @ 84sp), display optical feel.
- **JetBrains Mono** — machine voice: `SectionLabel` (uppercase, +1.6sp tracking), `StatusMono` metadata.

Scale: `ClockLarge 84 / ClockMedium 56 / Title 20 / Body 15 / SearchInput 17 / SectionLabel 11 / StatusMono 12`. Weight rarely exceeds Medium — hierarchy comes from size, spacing and voice, not boldness.

## The dot-matrix engine

SankOS's signature texture. `designsystem/dotmatrix/` implements an **original 5×7 bitmap glyph set** in code (`DotGlyphs`) and a Canvas renderer (`DotText`):

- Grid: 5 columns × 7 rows per glyph, 1 column advance between glyphs.
- Lit dots = `color`; unlit dots may render faint (`gridColor`, typically `InkLine`) to expose the machine grid.
- Sizes are pure dp math: width = 6·(dot+gap)·n − gap, height = 7·(dot+gap). Home date uses 1.8dp dots; the dot-matrix clock style uses 5dp; the launcher mark uses the same glyph data as vector circles.
- Used for: home date, dot-matrix clock option, app launcher mark. Reserved for future: focus timer, notification counters.

**Replacement path:** the glyph table is the only source of truth. Swapping in custom SVG-derived assets later means replacing this one object (or mapping glyph → vector asset); call sites don't change.

## Icon rules

System icons (`SankIcons`) are built in code on a 24dp grid:

- stroke 2px, round caps/joins, pure geometry, no fills except explicit knobs/dots
- no shadows, no two-tone, no brand copies
- color is applied by tinting; black source + `Icon(tint=)` keeps the set theme-independent

App icons in the drawer/dock are the **real** app icons inside `InkElevated` geometric wells — recognizability beats purity; a monochrome re-iconing engine (custom SVG per package, persisted overrides) is architected for v0.4.

## Motion

Springs for anything spatial; short eases for pure opacity. One table (`SankMotion`):

| Spec | Damping | Stiffness | Use |
|---|---|---|---|
| `drawerPanel` | 0.86 | 380 | drawer open/close, no overshoot |
| `homeSettle` | 0.92 | 300 | home returning |
| `control` | 0.80 | 500 | chips, toggles |
| `focusCalm` | 1.00 | 180 | Focus transitions — slower by design (v0.5) |

Rules: gestures hand off velocity (finger-following `Animatable`, spring on release); nothing bounces; nothing blocks input; reduced-motion respect and degradation knobs arrive with the accessibility pass (v0.8).

## Background

Flat `Ink` everywhere in v0.1. Planned, gated on "never visible at arm's length": a barely-there dot-grid texture at ~2% paper opacity, matching the dot-matrix language.
