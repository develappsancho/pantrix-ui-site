package com.pantrixui.site.feature.docs

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.model.ChangeGroup
import com.pantrixui.site.core.model.ChangeSection
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.Release

/**
 * The changelog page, as its screen draws it: the library's changelog in the page's language, and the module and
 * the kind of change the reader keeps.
 *
 * @property changelog the changelog, while it is read and once it has been.
 * @property module the key of the module whose changes alone are shown, or null for every module.
 * @property breakingOnly whether only the changes that break a call or a behaviour are shown.
 * @property copied whether the page was just copied: its button says so for a moment.
 */
data class ChangelogUiState(
    val changelog: LoadState<Changelog> = LoadState.Loading,
    val module: String? = null,
    val breakingOnly: Boolean = false,
    val copied: Boolean = false
) : UiState {
    /** [release]'s changes that the chosen module and the breaking switch leave, empty kinds and modules left out. */
    fun shown(release: Release): List<ChangeSection> = release.sections.mapNotNull { section ->
        val groups = section.groups
            .filter { module == null || it.key == module }
            .map { group -> group.copy(entries = group.entries.filter { !breakingOnly || it.breaking }) }
            .filter { it.entries.isNotEmpty() }
        if (groups.isEmpty()) null else section.copy(groups = groups)
    }
}

/**
 * One of the changelog's modules, as its chip shows it.
 *
 * @property key its name in no language, what the filter keeps.
 * @property name what the changelog calls it in its language.
 * @property count how many lines it has in every version.
 */
data class ChangeModule(
    val key: String,
    val name: String,
    val count: Int
)

/** The modules [this] changelog names, in the order it first names them, each with how many lines it has. */
fun Changelog.modules(): List<ChangeModule> {
    val groups: List<ChangeGroup> = releases.flatMap { release -> release.sections.flatMap { it.groups } }
    return groups.groupBy { it.key }.map { (key, ofKey) ->
        ChangeModule(key, ofKey.first().module, ofKey.sumOf { it.entries.size })
    }
}

/** What the user does on the changelog page. */
sealed interface ChangelogAction : UiAction {
    /** Shows the changes of the module [key] alone, or of every module for null. */
    data class PickModule(val key: String?) : ChangelogAction

    /** Shows only the changes that break a call or a behaviour, or every change again. */
    data object ToggleBreaking : ChangelogAction

    /** Copies the changelog as Markdown. */
    data object CopyPage : ChangelogAction

    /** The screen put the page on the clipboard, or [done] is false where the platform would not take it. */
    data class Copied(val done: Boolean) : ChangelogAction

    /** Opens what a link leads to: a page of the site, or a web address. */
    data class OpenLink(val href: String) : ChangelogAction

    /** Reads the changelog again, after it could not be read. */
    data object Retry : ChangelogAction
}
