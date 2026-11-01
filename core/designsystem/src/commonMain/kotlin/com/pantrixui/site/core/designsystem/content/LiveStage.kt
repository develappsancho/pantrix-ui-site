package com.pantrixui.site.core.designsystem.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pantrixui.foundation.a11y.PantrixPlatform
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixTheme

/**
 * [content] in [type]'s conventions, whatever the site runs on: its touch targets, its controls' shapes and their
 * answer to a press. The colours, the type and the language stay the site's, and so do the system's bars.
 *
 * @param type the platform whose conventions [content] is drawn in.
 * @param content what is drawn in them.
 */
@Composable
fun OnPlatform(
    type: PlatformType,
    content: @Composable () -> Unit
) {
    val host = PantrixTheme.platform
    PantrixTheme(platform = PantrixPlatform.forType(type, isTouch = host.isTouch), followSystemBars = false) {
        content()
    }
}

/**
 * A ground of dots under what is drawn on it, as the design draws the stage a live component stands on.
 *
 * @param color the dots' colour.
 * @param step how far apart the dots are, across and down.
 */
fun Modifier.dotted(
    color: Color,
    step: Dp = 16.dp
): Modifier = drawBehind {
    val gap = step.toPx()
    val radius = DOT_RADIUS.toPx()
    var y = gap / 2
    while (y < size.height) {
        var x = gap / 2
        while (x < size.width) {
            drawCircle(color, radius, Offset(x, y))
            x += gap
        }
        y += gap
    }
}

/** What the site calls a platform, on a switch between them and over a column of a table. */
val PlatformType.siteName: String
    get() = when (this) {
        PlatformType.Android -> "Android"
        PlatformType.Ios -> "iOS"
        PlatformType.Desktop -> "Desktop"
        PlatformType.Web -> "Web"
    }

private val DOT_RADIUS = 1.dp
