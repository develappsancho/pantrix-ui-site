package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pantrixui.foundation.adaptive.BoxWithPantrixWindow
import com.pantrixui.foundation.adaptive.LocalPantrixWindow
import com.pantrixui.foundation.adaptive.PantrixWindow
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A window [width] by [height] wide whatever room the page leaves it, drawn smaller where it does not fit, and told
 * to its content as its window: what the content asks the window, its class and whether it is narrow, it asks this
 * one. A sketch of a layout that changes with the window shows a desktop's window on a phone.
 */
@Composable
internal fun ScaledWindow(
    width: Dp,
    height: Dp,
    content: @Composable (window: PantrixWindow) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val scale = min(1f, maxWidth / width)
        BoxWithPantrixWindow(
            Modifier.layout { measurable, _ ->
                val placeable = measurable.measure(Constraints.fixed(width.roundToPx(), height.roundToPx()))
                layout((placeable.width * scale).roundToInt(), (placeable.height * scale).roundToInt()) {
                    placeable.placeWithLayer(0, 0) {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                }
            }
        ) { window ->
            CompositionLocalProvider(LocalPantrixWindow provides window) { content(window) }
        }
    }
}

/** The widths a sketch of a layout offers, each a kind of window. */
internal enum class SketchWidth(
    val width: Dp,
    val height: Dp
) {
    Phone(390.dp, 520.dp),
    Foldable(700.dp, 520.dp),
    Tablet(1000.dp, 520.dp),
    Desktop(1280.dp, 560.dp)
}
