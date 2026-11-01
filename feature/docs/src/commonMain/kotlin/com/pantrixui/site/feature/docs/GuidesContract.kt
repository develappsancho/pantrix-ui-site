package com.pantrixui.site.feature.docs

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.model.GuideCatalogue
import com.pantrixui.site.core.model.GuideEntry
import com.pantrixui.site.core.model.GuideTopic
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page

/**
 * The guides page, as its screen draws it: its head, read like any page's, and the library's guides by topic, those
 * that the filter and the chosen topic leave.
 *
 * @property page the page's head and what it copies, while it is read and once it has been.
 * @property minutes how long each guide takes to read, by its key.
 * @property query what the filter holds.
 * @property topic the topic chosen, or null for every one.
 * @property copied whether the page was just copied: its button says so for a moment.
 */
data class GuidesUiState(
    val page: LoadState<Page?> = LoadState.Loading,
    val minutes: Map<String, Int> = emptyMap(),
    val query: String = "",
    val topic: GuideTopic? = null,
    val copied: Boolean = false
) : UiState {
    /** The topics the page shows in [language], each with the guides of it the filter leaves, empty ones left out. */
    fun groups(language: Language): List<GuideGroup> = GuideTopic.entries
        .filter { topic == null || it == topic }
        .map { topic -> GuideGroup(topic, matching(language).filter { it.topic == topic }) }
        .filter { it.guides.isNotEmpty() }

    /** How many guides of [topic], or of every topic for null, the filter leaves: what its chip counts. */
    fun count(
        language: Language,
        topic: GuideTopic?
    ): Int = matching(language).count { topic == null || it.topic == topic }

    /** The guides whose title or summary in [language] holds the filter's words, in the catalogue's order. */
    private fun matching(language: Language): List<GuideEntry> {
        val words = query.trim().lowercase()
        return GuideCatalogue.entries.filter { guide ->
            words.isEmpty() || words in "${guide.title.of(language)} ${guide.summary.of(language)}".lowercase()
        }
    }
}

/**
 * One topic of the guides page and the guides it shows.
 *
 * @property topic the topic.
 * @property guides its guides, in the catalogue's order.
 */
data class GuideGroup(
    val topic: GuideTopic,
    val guides: List<GuideEntry>
)

/** What the user does on the guides page. */
sealed interface GuidesAction : UiAction {
    /** Writes [query] in the filter. */
    data class Filter(val query: String) : GuidesAction

    /** Shows the guides of [topic] alone, or every one for null. */
    data class PickTopic(val topic: GuideTopic?) : GuidesAction

    /** Copies the page as Markdown. */
    data object CopyPage : GuidesAction

    /** The screen put the page on the clipboard, or [done] is false where the platform would not take it. */
    data class Copied(val done: Boolean) : GuidesAction

    /** Opens what a link leads to: a page of the site, or a web address. */
    data class OpenLink(val href: String) : GuidesAction

    /** Reads the page again, after it could not be read. */
    data object Retry : GuidesAction
}
