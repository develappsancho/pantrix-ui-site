package com.pantrixui.site.core.designsystem.device

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
 * A device the site draws an app in, as the design measures it: the adaptive page's simulator offers each, and the
 * home page's tour walks through them.
 *
 * @property width its window's width, in dp.
 * @property height its window's height, in dp.
 * @property fold how it is held: flat, or half open like a book or on a table.
 * @property corner how round its screen's corners are drawn.
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
fun windowOf(
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

/** The hinge's strip across a simulated foldable. */
val HINGE = 40.dp
