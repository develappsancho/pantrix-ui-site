package com.pantrixui.site.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The site's own icons, for what Pantrix's set has no mark for: a language, the theme, a feed, a lock, a link out,
 * code, a tool, the download page's platforms and marks, and GitHub. Each is drawn as the design draws it, in strokes
 * on a 24 grid, in the colour of where it stands; GitHub's is its own mark, filled.
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

    /** A link to a page of its own: an arrow up and to the end. */
    val ArrowUpRight: ImageVector by lazy {
        icon("ArrowUpRight", strokeWidth = 1.8f) {
            moveTo(7f, 17f)
            lineTo(17f, 7f)
            moveTo(9f, 7f)
            horizontalLineTo(17f)
            verticalLineTo(15f)
        }
    }

    /** Code: an angle bracket on each side of a slash. */
    val Code: ImageVector by lazy {
        icon("Code", strokeWidth = 1.8f) {
            moveTo(8f, 8f)
            lineTo(4f, 12f)
            lineTo(8f, 16f)
            moveTo(16f, 8f)
            lineTo(20f, 12f)
            lineTo(16f, 16f)
            moveTo(13.5f, 6f)
            lineTo(10.5f, 18f)
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

    /** A tool an assistant calls: a wrench. */
    val Wrench: ImageVector by lazy {
        icon("Wrench", strokeWidth = 2f) {
            moveTo(14.7f, 6.3f)
            arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.4f, 5.4f)
            lineTo(3f, 18f)
            lineToRelative(3f, 3f)
            lineToRelative(6.3f, -6.3f)
            arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.4f, -5.4f)
            lineToRelative(-2.6f, 2.6f)
            lineToRelative(-2.8f, -0.6f)
            lineToRelative(-0.6f, -2.8f)
            close()
        }
    }

    /** macOS, as a computer: a laptop's screen over its base. */
    val Laptop: ImageVector by lazy {
        icon("Laptop", strokeWidth = 1.8f) {
            roundRect(4f, 5f, 16f, 11f, 2f)
            moveTo(2f, 19f)
            horizontalLineTo(22f)
        }
    }

    /** Windows: four panes in a square. */
    val Panes: ImageVector by lazy {
        icon("Panes", strokeWidth = 1.8f) {
            roundRect(4f, 4f, 7f, 7f, 1f)
            roundRect(13f, 4f, 7f, 7f, 1f)
            roundRect(4f, 13f, 7f, 7f, 1f)
            roundRect(13f, 13f, 7f, 7f, 1f)
        }
    }

    /** Linux, as its terminal: a prompt and a cursor in a window. */
    val Terminal: ImageVector by lazy {
        icon("Terminal", strokeWidth = 1.8f) {
            roundRect(3f, 4f, 18f, 16f, 2f)
            moveTo(7f, 9f)
            lineTo(10f, 12f)
            lineTo(7f, 15f)
            moveTo(13f, 15f)
            horizontalLineTo(17f)
        }
    }

    /** What is signed and checked: a shield with a tick. */
    val Shield: ImageVector by lazy {
        icon("Shield", strokeWidth = 1.9f) {
            moveTo(12f, 3f)
            lineTo(5f, 6f)
            verticalLineTo(12f)
            curveTo(5f, 16.5f, 8f, 19.5f, 12f, 21f)
            curveTo(16f, 19.5f, 19f, 16.5f, 19f, 12f)
            verticalLineTo(6f)
            close()
            moveTo(9f, 12f)
            lineTo(11f, 14f)
            lineTo(15f, 10f)
        }
    }

    /** Pages to read: a book. */
    val Book: ImageVector by lazy {
        icon("Book", strokeWidth = 1.8f) {
            moveTo(4f, 5f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 3f)
            horizontalLineTo(19f)
            verticalLineTo(19f)
            horizontalLineTo(6f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, 21f)
            close()
            moveTo(4f, 21f)
            verticalLineTo(5f)
        }
    }

    /** Components: three blocks and a fourth being added. */
    val Blocks: ImageVector by lazy {
        icon("Blocks", strokeWidth = 1.8f) {
            roundRect(4f, 4f, 7f, 7f, 2f)
            roundRect(13f, 4f, 7f, 7f, 2f)
            roundRect(4f, 13f, 7f, 7f, 2f)
            moveTo(16.5f, 13f)
            verticalLineTo(20f)
            moveTo(13f, 16.5f)
            horizontalLineTo(20f)
        }
    }

    /** A theme's colours: a painter's palette. */
    val Palette: ImageVector by lazy {
        icon("Palette", strokeWidth = 1.8f) {
            moveTo(12f, 3f)
            arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 21f)
            curveTo(13.1f, 21f, 13.6f, 20.1f, 13.2f, 19.2f)
            curveTo(12.7f, 18.1f, 13.5f, 17f, 14.7f, 17f)
            horizontalLineTo(17f)
            arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 21f, 13f)
            curveTo(21f, 7.5f, 17f, 3f, 12f, 3f)
            close()
            circle(7.5f, 11f, 1.2f)
            circle(11f, 7f, 1.2f)
            circle(15.5f, 8.5f, 1.2f)
        }
    }

    /**
     * GitHub, where the site's source is: GitHub's mark, filled, as GitHub draws it for a link to it (Octicons'
     * `mark-github`, MIT). Its 16 square stands in a 20 one, so that it is as large as the site's drawn icons beside
     * it.
     */
    val GitHub: ImageVector by lazy {
        ImageVector.Builder(
            name = "GitHub",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).addGroup(translationX = 2f, translationY = 2f)
            .addPath(pathData = addPathNodes(GITHUB_MARK), fill = SolidColor(Color.Black))
            .clearGroup()
            .build()
    }

    /** GitHub's mark on a 16 square. */
    private const val GITHUB_MARK =
        "M8 0c4.42 0 8 3.58 8 8a8.013 8.013 0 0 1-5.45 7.59c-.4.08-.55-.17-.55-.38 0-.27.01-1.13.01-2.2 " +
            "0-.75-.25-1.23-.54-1.48 1.78-.2 3.65-.88 3.65-3.95 " +
            "0-.88-.31-1.59-.82-2.15.08-.2.36-1.02-.08-2.12 0 0-.67-.22-2.2.82-.64-.18-1.32-.27-2-.27-.68 " +
            "0-1.36.09-2 .27-1.53-1.03-2.2-.82-2.2-.82-.44 1.1-.16 1.92-.08 2.12-.51.56-.82 1.28-.82 2.15 0 " +
            "3.06 1.86 3.75 3.64 3.95-.23.2-.44.55-.51 " +
            "1.07-.46.21-1.61.55-2.33-.66-.15-.24-.6-.83-1.23-.82-.67.01-.27.38.01.53.34.19.73.9.82 " +
            "1.13.16.45.68 1.31 2.69.94 0 .67.01 1.3.01 1.49 0 .21-.15.45-.55.38A7.995 7.995 0 0 1 0 8c0-4.42 " +
            "3.58-8 8-8Z"

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

    /** A rectangle from ([x], [y]), [width] by [height], its corners rounded by [radius]. */
    private fun PathBuilder.roundRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float
    ) {
        moveTo(x + radius, y)
        horizontalLineTo(x + width - radius)
        arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, x + width, y + radius)
        verticalLineTo(y + height - radius)
        arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, x + width - radius, y + height)
        horizontalLineTo(x + radius)
        arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, x, y + height - radius)
        verticalLineTo(y + radius)
        arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, x + radius, y)
        close()
    }

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
