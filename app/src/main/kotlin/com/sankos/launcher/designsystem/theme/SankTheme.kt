package com.sankos.launcher.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val SankColorScheme = darkColorScheme(
    background = Ink,
    onBackground = Paper,
    surface = InkElevated,
    onSurface = Paper,
    surfaceVariant = InkRaised,
    onSurfaceVariant = PaperDim,
    outline = InkLine,
    outlineVariant = InkLine,
    primary = Ember,
    onPrimary = Color(0xFF140A03),
    secondary = PaperDim,
    onSecondary = Ink,
    error = Color(0xFFFF5C5C),
)

/** The resolved accent: ember when enabled, quiet gray when disabled. */
val LocalSankAccent = staticCompositionLocalOf { Ember }

@Composable
fun sankAccent(): Color = LocalSankAccent.current

@Composable
fun SankTheme(
    accentEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val accent = if (accentEnabled) Ember else PaperFaint
    CompositionLocalProvider(LocalSankAccent provides accent) {
        MaterialTheme(
            colorScheme = SankColorScheme,
            content = content,
        )
    }
}
