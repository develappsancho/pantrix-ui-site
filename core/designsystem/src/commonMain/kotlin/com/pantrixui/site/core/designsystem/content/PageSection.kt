package com.pantrixui.site.core.designsystem.content

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The part of the page on show that the site is at, as its address names it after a `#`: a page scrolls to it when it
 * is drawn and whenever it changes, and moves to the part the reader picks in its "On this page".
 */
@Stable
interface PageSection {
    /** The part the page is at, by its anchor, or null for none. */
    val current: String?

    /** Moves the page to its part [anchor]. */
    fun show(anchor: String)
}

/**
 * The [PageSection] of the site, or null where the site keeps none, as in a test: a page there only scrolls to the
 * part the reader picks.
 */
val LocalPageSection = staticCompositionLocalOf<PageSection?> { null }
