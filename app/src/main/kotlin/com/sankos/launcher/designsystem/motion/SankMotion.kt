package com.sankos.launcher.designsystem.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * SankOS motion language.
 *
 * - Spatial movements (drawer, panels, reveals) use springs so velocity is
 *   continuous and gestures can hand off mid-flight.
 * - Pure opacity changes use short, softly-eased tweens.
 * - Nothing bounces. Focus-calm transitions use lower stiffness.
 */
object SankMotion {

    /** Drawer panel: heavy enough to feel physical, no overshoot. */
    fun <T> drawerPanel() = spring<T>(dampingRatio = 0.86f, stiffness = 380f)

    /** Home settling back after the drawer closes. */
    fun <T> homeSettle() = spring<T>(dampingRatio = 0.92f, stiffness = 300f)

    /** Small controls: chips, toggles, indicators. */
    fun <T> control() = spring<T>(dampingRatio = 0.8f, stiffness = 500f)

    /** Focus mode transitions: calmer and slightly slower by design. */
    fun <T> focusCalm() = spring<T>(dampingRatio = 1f, stiffness = 180f)

    val fadeFast = tween<Float>(durationMillis = 140, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
    val fadeNormal = tween<Float>(durationMillis = 220, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
}
