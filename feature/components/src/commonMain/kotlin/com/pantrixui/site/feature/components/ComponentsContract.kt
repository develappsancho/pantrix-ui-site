package com.pantrixui.site.feature.components

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.model.ComponentCard
import com.pantrixui.site.core.model.ComponentGroup
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.Span

/**
 * The components page, as its screen draws it: the library's components in the page's language, and what the reader
 * keeps of them.
 *
 * @property index the components, while they are read and once they have been.
 * @property query what the filter keeps: components whose name, function or summary holds it.
 * @property group the key of the group whose components alone are shown, or null for every group.
 */
data class ComponentsUiState(
    val index: LoadState<ComponentIndex> = LoadState.Loading,
    val query: String = "",
    val group: String? = null
) : UiState {
    /** The groups the filter and the chosen group leave, each with its components that match, by name. */
    val shown: List<ComponentGroup>
        get() = index.valueOrNull?.groups.orEmpty()
            .filter { group == null || it.key == group }
            .map { group -> group.copy(components = group.components.filter(::matches).sortedBy { it.name }) }
            .filter { it.components.isNotEmpty() }

    /** How many components of the group [key], or of every group for null, the filter leaves. */
    fun count(key: String?): Int = index.valueOrNull?.groups.orEmpty()
        .filter { key == null || it.key == key }
        .sumOf { group -> group.components.count(::matches) }

    private fun matches(card: ComponentCard): Boolean {
        val words = query.trim().lowercase()
        if (words.isEmpty()) return true
        return words in card.name.lowercase() || words in card.function.lowercase() ||
            words in card.summary.joinToString("") { it.plain }.lowercase()
    }
}

/** What a span says, without its marks. */
internal val Span.plain: String
    get() = when (this) {
        is Span.Text -> text
        is Span.Code -> text
        is Span.Link -> text.joinToString("") { it.plain }
    }

/** What the user does on the components page. */
sealed interface ComponentsAction : UiAction {
    /** Keeps the components [query] matches. */
    data class Filter(val query: String) : ComponentsAction

    /** Shows the components of the group [key] alone, or of every group for null. */
    data class PickGroup(val key: String?) : ComponentsAction

    /** Opens what a link leads to: a page of the site, or a web address. */
    data class OpenLink(val href: String) : ComponentsAction

    /** Reads the components again, after they could not be read. */
    data object Retry : ComponentsAction
}

/** What a page of the components asks of the platform: to open a page or an address, or to copy a text. */
sealed interface ComponentsEffect : UiEffect {
    /** Opens the page of the site at [path]. */
    data class Navigate(val path: String) : ComponentsEffect

    /** Opens [url], a web address, outside the site. */
    data class OpenUrl(val url: String) : ComponentsEffect

    /** Puts [text] on the clipboard. */
    data class Copy(val text: String) : ComponentsEffect
}
