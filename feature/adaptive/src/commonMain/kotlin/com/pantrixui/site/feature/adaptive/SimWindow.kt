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

/**
 * A window the simulator offers, as the design measures it.
 *
 * @property width its width, in dp.
 * @property height its height, in dp.
 * @property fold how its device is held: flat, or half open like a book or on a table.
 * @property corner how round the device's frame is drawn.
 */
@Suppress("MagicNumber") // The windows' sizes and corners are the design's table of them.
enum class SimDevice(
    val width: Int,
    val height: Int,
    val fold: SimFold,
    val corner: Int
) {
    Phone(390, 844, SimFold.Flat, 44),
    PhoneSide(874, 402, SimFold.Flat, 44),
    Book(951, 669, SimFold.Book, 30),
    Table(669, 951, SimFold.Table, 30),
    Tablet(834, 1194, SimFold.Flat, 30),
    Laptop(1280, 800, SimFold.Flat, 14),
    Desktop(1680, 1050, SimFold.Flat, 14)
}

/** How a simulated device is held. */
enum class SimFold { Flat, Book, Table }

/**
 * The window [device] is, for Pantrix: its size and, for a foldable held half open, its fold across the middle, a
 * hinge [HINGE] wide whose strip the content keeps clear of, in [density]'s pixels as a fold is measured.
 */
internal fun windowOf(
    device: SimDevice,
    density: Density
): PantrixWindow {
    val size = DpSize(device.width.dp, device.height.dp)
    val posture = when (device.fold) {
        SimFold.Flat -> WindowPosture.Flat

        SimFold.Book, SimFold.Table -> with(density) {
            val width = size.width.toPx()
            val height = size.height.toPx()
            val half = HINGE.toPx() / 2
            val vertical = device.fold == SimFold.Book
            val strip = if (vertical) {
                Rect(width / 2 - half, 0f, width / 2 + half, height)
            } else {
                Rect(0f, height / 2 - half, width, height / 2 + half)
            }
            WindowPosture(
                listOf(
                    WindowFold(
                        bounds = strip,
                        orientation = if (vertical) FoldOrientation.Vertical else FoldOrientation.Horizontal,
                        isHalfOpened = true,
                        isSeparating = true,
                        isOccluding = false,
                        clearance = strip
                    )
                )
            )
        }
    }
    return PantrixWindow(size, posture = posture, breakpoints = PantrixBreakpoints.Default)
}

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

/** The hinge's strip across a simulated foldable. */
internal val HINGE = 40.dp

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
