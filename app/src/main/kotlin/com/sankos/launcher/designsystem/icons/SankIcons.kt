package com.sankos.launcher.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * SankOS system glyphs.
 *
 * One visual language: 24dp grid, 2px stroke, round caps and joins, pure
 * geometry, no fills except explicit dots. Stroke color is a placeholder
 * (black); render through `Icon(..., tint = ...)` to colorize.
 *
 * Built in code so the set can later be swapped wholesale for custom vector
 * assets without touching call sites (docs/DESIGN.md, docs/ICONS.md).
 */
object SankIcons {

    private const val STROKE = 2f

    private fun builder(name: String): ImageVector.Builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )

    private fun stroke(builder: ImageVector.Builder, pathBuilder: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
        builder.path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = pathBuilder,
        )
    }

    /** Magnifier: circle + handle. */
    val Search: ImageVector = builder("Sank.Search").apply {
        stroke(this) {
            moveTo(17f, 10.5f)
            arcTo(6.5f, 6.5f, 0f, false, true, 4f, 10.5f)
            arcTo(6.5f, 6.5f, 0f, false, true, 17f, 10.5f)
            moveTo(15.2f, 15.2f)
            lineTo(20f, 20f)
        }
    }.build()

    /** Chevron down — collapses the drawer. */
    val Collapse: ImageVector = builder("Sank.Collapse").apply {
        stroke(this) {
            moveTo(6f, 10f)
            lineTo(12f, 16f)
            lineTo(18f, 10f)
        }
    }.build()

    /** Sliders — SankOS settings / customize. */
    val Sliders: ImageVector = builder("Sank.Sliders").apply {
        stroke(this) {
            moveTo(4f, 7f); lineTo(20f, 7f)
            moveTo(4f, 12f); lineTo(20f, 12f)
            moveTo(4f, 17f); lineTo(20f, 17f)
        }
        path(fill = SolidColor(Color.Black)) {
            circleAt(9f, 7f, 2.2f)
            circleAt(15f, 12f, 2.2f)
            circleAt(11f, 17f, 2.2f)
        }
    }.build()

    /** Back arrow. */
    val Back: ImageVector = builder("Sank.Back").apply {
        stroke(this) {
            moveTo(20f, 12f)
            lineTo(4f, 12f)
            moveTo(10f, 6f)
            lineTo(4f, 12f)
            lineTo(10f, 18f)
        }
    }.build()

    /** Outbound action (call): arrow to top-right. */
    val Outbound: ImageVector = builder("Sank.Outbound").apply {
        stroke(this) {
            moveTo(7f, 17f)
            lineTo(17f, 7f)
            moveTo(9f, 7f)
            lineTo(17f, 7f)
            lineTo(17f, 15f)
        }
    }.build()

    /** Timer: face + stem. */
    val Timer: ImageVector = builder("Sank.Timer").apply {
        stroke(this) {
            moveTo(12f, 2.5f)
            lineTo(12f, 6f)
            moveTo(12f, 21f)
            arcTo(7.5f, 7.5f, 0f, false, true, 4.5f, 13.5f)
            arcTo(7.5f, 7.5f, 0f, false, true, 19.5f, 13.5f)
        }
    }.build()

    /** Check mark. */
    val Check: ImageVector = builder("Sank.Check").apply {
        stroke(this) {
            moveTo(5f, 13f)
            lineTo(10f, 18f)
            lineTo(19f, 7f)
        }
    }.build()

    /** Chevron right. */
    val ChevronRight: ImageVector = builder("Sank.ChevronRight").apply {
        stroke(this) {
            moveTo(9f, 5f)
            lineTo(16f, 12f)
            lineTo(9f, 19f)
        }
    }.build()

    /** Chevron up — the home gesture hint. */
    val Expand: ImageVector = builder("Sank.Expand").apply {
        stroke(this) {
            moveTo(6f, 14f)
            lineTo(12f, 8f)
            lineTo(18f, 14f)
        }
    }.build()

    private fun androidx.compose.ui.graphics.vector.PathBuilder.circleAt(cx: Float, cy: Float, r: Float) {
        moveTo(cx + r, cy)
        arcTo(r, r, 0f, false, true, cx - r, cy)
        arcTo(r, r, 0f, false, true, cx + r, cy)
    }
}
