package com.pantrixui.site.feature.foundations

import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState

/**
 * The foundations page, as its screen draws it.
 *
 * @property dark whether the colours shown are the preset's dark ones, whichever theme the site is in.
 * @property moved whether the motion's dots stand at the end of their tracks: playing them sends them to the other.
 */
data class FoundationsUiState(
    val dark: Boolean = false,
    val moved: Boolean = false
) : UiState

/** What the user does on the foundations page. */
sealed interface FoundationsAction : UiAction {
    /** Shows the preset's dark colours, or its light ones. */
    data class ShowDark(val dark: Boolean) : FoundationsAction

    /** Sends the motion's three dots to the other end of their tracks, each in its own duration. */
    data object Play : FoundationsAction
}

/** The foundations page asks nothing of the platform: its links are the site's frame's. */
sealed interface FoundationsEffect : UiEffect
