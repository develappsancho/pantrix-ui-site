package com.pantrixui.site.feature.docs

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.model.Page

/**
 * A page of the documentation, as its screen draws it.
 *
 * @property page the page, while it is read and once it has been: null once read is a page the site does not have.
 * @property copied whether the page was just copied: its button says so for a moment.
 */
data class DocUiState(
    val page: LoadState<Page?> = LoadState.Loading,
    val copied: Boolean = false
) : UiState

/** What the user does on a page of the documentation. */
sealed interface DocAction : UiAction {
    /** Copies the page as Markdown. */
    data object CopyPage : DocAction

    /** The screen put the page on the clipboard, or [done] is false where the platform would not take it. */
    data class Copied(val done: Boolean) : DocAction

    /** Opens what a link leads to: a page of the site, or a web address. */
    data class OpenLink(val href: String) : DocAction

    /** Reads the page again, after it could not be read. */
    data object Retry : DocAction
}

/** What a page of the documentation asks its screen to do once. */
sealed interface DocEffect : UiEffect {
    /** Puts [text] on the clipboard. */
    data class Copy(val text: String) : DocEffect

    /** Opens the page of the site at [path]. */
    data class Navigate(val path: String) : DocEffect

    /** Opens [url], a web address, outside the site. */
    data class OpenUrl(val url: String) : DocEffect
}
