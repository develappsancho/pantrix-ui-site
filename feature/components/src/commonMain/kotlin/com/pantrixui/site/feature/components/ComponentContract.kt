package com.pantrixui.site.feature.components

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.model.ComponentPage

/**
 * A component's page, as its screen draws it.
 *
 * @property page the component's page, while it is read and once it has been; null for a component the library does
 *   not have.
 * @property form which of the component's forms its API table shows.
 * @property allParameters whether the API table shows every parameter, rather than the first few.
 * @property copied whether the page was just copied: its button says so for a moment.
 */
data class ComponentUiState(
    val page: LoadState<ComponentPage?> = LoadState.Loading,
    val form: Int = 0,
    val allParameters: Boolean = false,
    val copied: Boolean = false
) : UiState

/** What the user does on a component's page. */
sealed interface ComponentAction : UiAction {
    /** Shows the API of the component's form at [index]. */
    data class PickForm(val index: Int) : ComponentAction

    /** Shows every parameter of the form on show, or the first few again. */
    data object ToggleParameters : ComponentAction

    /** Copies the page as Markdown. */
    data object CopyPage : ComponentAction

    /** The screen put the page on the clipboard, or [done] is false where the platform would not take it. */
    data class Copied(val done: Boolean) : ComponentAction

    /** Opens what a link leads to: a page of the site, or a web address. */
    data class OpenLink(val href: String) : ComponentAction

    /** Reads the page again, after it could not be read. */
    data object Retry : ComponentAction
}
