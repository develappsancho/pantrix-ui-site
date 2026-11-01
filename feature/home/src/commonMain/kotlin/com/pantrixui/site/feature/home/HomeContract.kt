package com.pantrixui.site.feature.home

import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.model.Block

/**
 * The landing page, as its screen draws it.
 *
 * @property code the code its sections show, while it is read and once it has been.
 * @property platform the platform whose conventions the live form at its top is drawn in.
 * @property codeTab which file the code of "Write it once" shows: the form's, or the preset's.
 * @property copied whether the line that installs the library was just copied: its button says so for a moment.
 */
data class HomeUiState(
    val code: LoadState<HomeCode> = LoadState.Loading,
    val platform: PlatformType = PlatformType.Ios,
    val codeTab: Int = 0,
    val copied: Boolean = false
) : UiState

/**
 * The code the landing page shows, from its page of the site's content.
 *
 * @property form the review form, as a desktop shows it.
 * @property wideForm the same form with each call on one line, as a tablet shows it.
 * @property shortForm the field of the form alone, as a phone shows it.
 * @property preset the site's own preset.
 */
data class HomeCode(
    val form: Block.Code,
    val wideForm: Block.Code,
    val shortForm: Block.Code,
    val preset: Block.Code
)

/** What the user does on the landing page. */
sealed interface HomeAction : UiAction {
    /** Draws the live form in [type]'s conventions. */
    data class PickPlatform(val type: PlatformType) : HomeAction

    /** Shows the file [index] of "Write it once". */
    data class PickCodeTab(val index: Int) : HomeAction

    /** Copies the line that installs the library. */
    data object CopyInstall : HomeAction

    /** Tells the model whether the platform took the line: [done] when it did. */
    data class Copied(val done: Boolean) : HomeAction

    /** Opens what a link leads to: a page of the site, or a web address. */
    data class OpenLink(val href: String) : HomeAction
}

/** What the landing page asks of the platform: to open a page or an address, or to copy a text. */
sealed interface HomeEffect : UiEffect {
    /** Opens the page of the site at [path]. */
    data class Navigate(val path: String) : HomeEffect

    /** Opens [url], a web address, outside the site. */
    data class OpenUrl(val url: String) : HomeEffect

    /** Puts [text] on the clipboard. */
    data class Copy(val text: String) : HomeEffect
}
