package com.pantrixui.site

import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.model.Language

/**
 * What the site's frame holds, around every page: the language it is read in, the theme and the menu of a narrow
 * window.
 *
 * @property language the language of the site's words and pages.
 * @property dark whether the theme is dark, once the reader has chosen; null follows the system.
 * @property menuOpen whether the menu of a tablet or a phone is open.
 */
data class SiteUiState(
    val language: Language,
    val dark: Boolean? = null,
    val menuOpen: Boolean = false
) : UiState

/** What the reader does in the site's frame. */
sealed interface SiteAction : UiAction {
    /** Switches to the other theme; [systemDark] is the system's, which the site follows until a choice. */
    data class ToggleTheme(val systemDark: Boolean) : SiteAction

    /** Reads the site in [language]. */
    data class ChooseLanguage(val language: Language) : SiteAction

    /** Opens the menu of a narrow window. */
    data object OpenMenu : SiteAction

    /** Closes it. */
    data object CloseMenu : SiteAction
}

/** The frame asks its screen for nothing once: what it does is all in its state. */
sealed interface SiteEffect : UiEffect
