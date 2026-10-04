@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.sankos.launcher.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sankos.launcher.R

/**
 * SankOS typography.
 *
 * Two voices:
 *  - Inter (variable) for reading: the clock, labels, search.
 *  - JetBrains Mono for machine information: metadata, sections, status.
 *
 * Both are variable fonts bundled locally; weights resolve through
 * variation settings so one file serves every weight (small APK, no
 * per-weight files).
 */
private fun interFont(weight: FontWeight, variation: Int) = Font(
    resId = R.font.inter_var,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(variation)),
)

private fun monoFont(weight: FontWeight, variation: Int) = Font(
    resId = R.font.jetbrains_mono_var,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(variation)),
)

val InterFamily = FontFamily(
    interFont(FontWeight.Normal, 400),
    interFont(FontWeight.Medium, 500),
    interFont(FontWeight.SemiBold, 600),
    interFont(FontWeight.Bold, 700),
)

val MonoFamily = FontFamily(
    monoFont(FontWeight.Normal, 400),
    monoFont(FontWeight.Medium, 500),
)

object SankType {
    /** The home clock. Numbers as industrial design: tight tracking, display optical size. */
    val ClockLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 84.sp,
        lineHeight = 84.sp,
        letterSpacing = (-3.4).sp,
    )

    val ClockMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 56.sp,
        lineHeight = 56.sp,
        letterSpacing = (-2.2).sp,
    )

    /** Uppercase section headers in the drawer, e.g. FREQUENT / EVERYTHING. */
    val SectionLabel = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.6.sp,
    )

    /** Small machine metadata (battery, hints, subtitles). */
    val StatusMono = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.6.sp,
    )

    val Title = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp,
    )

    val Body = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.1).sp,
    )

    val SearchInput = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
    )
}
