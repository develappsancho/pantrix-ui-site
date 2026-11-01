package com.pantrixui.site.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The site's own icons, for what Pantrix's set has no mark for: a language, the theme, a feed, a lock. Each is drawn
 * as the design draws it, in strokes on a 24 grid, in the colour of where it stands.
 */
object SiteIcons {
    /** A language: a globe with its equator and a meridian. */
    val Globe: ImageVector by lazy {
        icon("Globe", strokeWidth = 1.8f) {
            circle(12f, 12f, 9f)
            moveTo(3f, 12f)
            horizontalLineTo(21f)
            moveTo(12f, 3f)
            arcTo(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 21f)
            arcTo(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 3f)
            close()
        }
    }

    /** The dark theme: a crescent moon. */
    val Moon: ImageVector by lazy {
        icon("Moon", strokeWidth = 1.8f) {
            moveTo(20f, 14.5f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.5f, 4f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = false, 20f, 14.5f)
            close()
        }
    }

    /** The light theme: a sun and its rays. */
    val Sun: ImageVector by lazy {
        icon("Sun", strokeWidth = 1.8f) {
            circle(12f, 12f, 4f)
            listOf(
                floatArrayOf(12f, 2f, 12f, 4f),
                floatArrayOf(12f, 20f, 12f, 22f),
                floatArrayOf(4.9f, 4.9f, 6.3f, 6.3f),
                floatArrayOf(17.7f, 17.7f, 19.1f, 19.1f),
                floatArrayOf(2f, 12f, 4f, 12f),
                floatArrayOf(20f, 12f, 22f, 12f),
                floatArrayOf(4.9f, 19.1f, 6.3f, 17.7f),
                floatArrayOf(17.7f, 6.3f, 19.1f, 4.9f)
            ).forEach { ray ->
                moveTo(ray[0], ray[1])
                lineTo(ray[2], ray[3])
            }
        }
    }

    /** A feed to follow: two arcs over a dot, as a feed reader marks one. */
    val Feed: ImageVector by lazy {
        icon("Feed", strokeWidth = 2f) {
            moveTo(5f, 11f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13f, 19f)
            moveTo(5f, 5f)
            arcTo(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19f, 19f)
            circle(6f, 18f, 1.5f)
        }
    }

    /** What is kept closed: a padlock, beside a token that must stay out of the project. */
    val Lock: ImageVector by lazy {
        icon("Lock", strokeWidth = 2f) {
            moveTo(6f, 10f)
            horizontalLineTo(18f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 12f)
            verticalLineTo(19f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18f, 21f)
            horizontalLineTo(6f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 19f)
            verticalLineTo(12f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 10f)
            close()
            moveTo(8f, 10f)
            verticalLineTo(7f)
            arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 7f)
            verticalLineTo(10f)
        }
    }

    private fun icon(
        name: String,
        strokeWidth: Float,
        draw: PathBuilder.() -> Unit
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = draw
    ).build()

    private fun PathBuilder.circle(
        x: Float,
        y: Float,
        radius: Float
    ) {
        moveTo(x - radius, y)
        arcTo(radius, radius, 0f, isMoreThanHalf = true, isPositiveArc = true, x + radius, y)
        arcTo(radius, radius, 0f, isMoreThanHalf = true, isPositiveArc = true, x - radius, y)
        close()
    }
}
