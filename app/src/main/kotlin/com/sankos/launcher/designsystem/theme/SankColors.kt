package com.sankos.launcher.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * SankOS palette.
 *
 * Rule of thumb: ~90% ink (blacks), ~8% paper (off-whites), ~2% ember (orange).
 * The UI must remain beautiful with the accent removed entirely, so accent
 * usage is resolved through [SankTheme]/[sankAccent] and can be neutralized.
 *
 * The spec's secondary text #77736D was refined to #9A968F for contrast on
 * Ink (>= 4.5:1) at small sizes; #77736D is kept for large text only.
 */
val Ink = Color(0xFF080808)          // primary background
val InkElevated = Color(0xFF111111)  // secondary background: panels, chips
val InkRaised = Color(0xFF171716)    // pressed / tertiary surfaces
val InkLine = Color(0xFF232320)      // hairline borders

val Paper = Color(0xFFF2F0EC)        // primary text
val PaperDim = Color(0xFF9A968F)     // secondary text
val PaperFaint = Color(0xFF5C5852)   // tertiary text / disabled

val Ember = Color(0xFFFF6A00)        // accent — active, selected, progress
val EmberSoft = Color(0xFFFF8A3D)    // accent tint for large fields only
