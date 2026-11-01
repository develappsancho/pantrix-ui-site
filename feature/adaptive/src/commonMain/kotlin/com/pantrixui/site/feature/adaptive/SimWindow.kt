package com.pantrixui.site.feature.adaptive

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.pantrixui.foundation.adaptive.FoldOrientation
import com.pantrixui.foundation.adaptive.PantrixBreakpoints
import com.pantrixui.foundation.adaptive.PantrixWindow
import com.pantrixui.foundation.adaptive.WindowFold
import com.pantrixui.foundation.adaptive.WindowPosture
import com.pantrixui.site.core.designsystem.device.SimDevice
import com.pantrixui.site.core.designsystem.device.SimFold

/** How the simulated inbox places its navigation, as the scaffold decides it for the window. */
internal enum class SimNavigation { Bottom, Rail, Sidebar }

/**
 * Where the simulated inbox's navigation stands in [device]'s window: at the bottom while the window is narrower than
 * a rail needs, and on a foldable on a table; a sidebar from a Large window on; a rail between.
 */
internal fun navigationOf(device: SimDevice): SimNavigation = when {
    device.width < RAIL_FROM || device.fold == SimFold.Table -> SimNavigation.Bottom
    device.width >= SIDEBAR_FROM -> SimNavigation.Sidebar
    else -> SimNavigation.Rail
}

/** How wide the simulated inbox's navigation is beside its content. */
internal fun navigationWidth(navigation: SimNavigation): Int = when (navigation) {
    SimNavigation.Bottom -> 0
    SimNavigation.Rail -> RAIL_WIDTH
    SimNavigation.Sidebar -> SIDEBAR_WIDTH
}

/** The name of a width class, as the readout writes it. */
internal fun widthClassName(width: Int): String = when {
    width < MEDIUM_FROM -> "Compact"
    width < EXPANDED_FROM -> "Medium"
    width < LARGE_FROM -> "Expanded"
    width < EXTRA_LARGE_FROM -> "Large"
    else -> "ExtraLarge"
}

/** How many panes the list-detail scaffold lays out in [room] dp: two in a book whatever its width. */
internal fun panesIn(
    room: Int,
    fold: SimFold
): Int = when {
    fold == SimFold.Book -> 2
    room < EXPANDED_FROM -> 1
    room < LARGE_FROM -> 2
    else -> THREE_PANES
}

/** Where the classes' widths start, as Pantrix's default breakpoints have them. */
internal const val MEDIUM_FROM = 600
internal const val EXPANDED_FROM = 840
internal const val LARGE_FROM = 1200
internal const val EXTRA_LARGE_FROM = 1600

/** Where a rail and a sidebar start, and how wide each is in the simulated inbox. */
private const val RAIL_FROM = MEDIUM_FROM
private const val SIDEBAR_FROM = LARGE_FROM
private const val RAIL_WIDTH = 88
private const val SIDEBAR_WIDTH = 248
private const val THREE_PANES = 3
