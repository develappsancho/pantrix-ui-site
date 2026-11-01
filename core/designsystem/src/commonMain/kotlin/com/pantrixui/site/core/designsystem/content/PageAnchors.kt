package com.pantrixui.site.core.designsystem.content

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Where each part of a page that its contents name stands, from the top of what scrolls: what "On this page" jumps
 * to. The page marks what scrolls with [Modifier.pageAnchorRoot] and each named part with [Modifier.pageAnchor].
 */
@Stable
class PageAnchors {
    private val offsets = mutableStateMapOf<String, Int>()
    private var root: LayoutCoordinates? = null

    /** How far below the top of what scrolls the part named [anchor] begins, once it has been laid out. */
    fun offsetOf(anchor: String): Int? = offsets[anchor]

    internal fun attachRoot(coordinates: LayoutCoordinates) {
        root = coordinates
    }

    internal fun record(
        anchor: String,
        coordinates: LayoutCoordinates
    ) {
        val within = root?.takeIf { it.isAttached && coordinates.isAttached } ?: return
        offsets[anchor] = within.localPositionOf(coordinates, Offset.Zero).y.toInt()
    }
}

/** Marks what scrolls on a page: the parts in it are measured from its top. */
fun Modifier.pageAnchorRoot(anchors: PageAnchors?): Modifier =
    if (anchors == null) this else onGloballyPositioned { anchors.attachRoot(it) }

/** Marks the part of a page that [anchor] names. */
fun Modifier.pageAnchor(
    anchors: PageAnchors?,
    anchor: String
): Modifier = if (anchors == null) this else onGloballyPositioned { anchors.record(anchor, it) }
