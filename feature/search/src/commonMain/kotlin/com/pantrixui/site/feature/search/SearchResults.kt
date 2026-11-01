package com.pantrixui.site.feature.search

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.model.SearchEntry
import com.pantrixui.site.core.model.SearchKind

/**
 * A result: what it finds, and where in its title the first word typed stands, for the result to mark it.
 *
 * @property entry what it finds.
 * @property hit where in its title the first word typed stands, or null where it stands only in its second line.
 */
internal class SearchRow(
    val entry: SearchEntry,
    val hit: IntRange?
)

/**
 * The results of one kind, or the places to start while nothing is typed.
 *
 * @property kind what they are, or null for the places to start.
 * @property rows the results, best first.
 */
internal class SearchGroup(
    val kind: SearchKind?,
    val rows: List<SearchRow>
)

/**
 * The kinds of results, in the order of their filters. The design leaves the pages out of a tablet's and a phone's
 * search; there a reader could not find a page of the documentation itself, so every size shows them.
 */
internal val KINDS = listOf(SearchKind.Component, SearchKind.Api, SearchKind.Guide, SearchKind.Section)

/**
 * What [entries] the search finds for [query], kind by kind, the kinds in the order of [GROUP_ORDER]: a few of each
 * while every kind is shown, more of the one [kind] alone. With nothing typed, the places to start in [layout].
 */
internal fun searchGroups(
    entries: List<SearchEntry>,
    query: String,
    kind: SearchKind?,
    layout: SiteLayout
): List<SearchGroup> {
    val words = wordsOf(query)
    if (words.isEmpty()) {
        // A page itself, not one of its parts or a declaration it shows.
        val rows = POPULAR.of(layout)
            .mapNotNull { path -> entries.firstOrNull { it.path == path && it.anchor == null } }
            .map { SearchRow(it, null) }
        return listOf(SearchGroup(null, rows)).filter { it.rows.isNotEmpty() }
    }
    val (few, more) = CAPS.of(layout)
    val shown = KINDS
    return GROUP_ORDER.filter { it in shown && (kind == null || it == kind) }.map { each ->
        val rows = found(entries, words, each)
        SearchGroup(each, rows.take(if (kind == null) few else more))
    }.filter { it.rows.isNotEmpty() }
}

/** How many results of each kind [query] finds, every kind together under null. */
internal fun searchCounts(
    entries: List<SearchEntry>,
    query: String
): Map<SearchKind?, Int> {
    val words = wordsOf(query)
    val counts = KINDS.associateWith { found(entries, words, it).size }
    return counts.mapKeys<SearchKind, Int, SearchKind?> { it.key } + (null to counts.values.sum())
}

/**
 * The results of [kind] that hold every one of [words], those whose title starts with the first word first; none
 * while nothing is typed.
 */
private fun found(
    entries: List<SearchEntry>,
    words: List<String>,
    kind: SearchKind
): List<SearchRow> = entries
    .filter { words.isNotEmpty() && it.kind == kind && words.all { word -> word in fold(it.title + " " + it.sub) } }
    .map { entry ->
        val at = fold(entry.title).indexOf(words.first())
        SearchRow(entry, if (at >= 0) at until at + words.first().length else null)
    }
    .sortedBy { row ->
        when {
            row.hit == null -> RANK_SUB
            row.hit.first == 0 -> RANK_START
            else -> RANK_TITLE
        }
    }

/** The words typed, folded: what a result has to hold. */
private fun wordsOf(query: String): List<String> = fold(query).split(' ', '\t', '\n').filter { it.isNotEmpty() }

/**
 * [text] in small letters, its I's all one: `İ`, `I` and `ı` are `i`, so that a search typed in either language finds
 * a word written in the other's way. Each letter stays one letter, so a place found in the folded text is the same
 * place in [text].
 */
internal fun fold(text: String): String = buildString(text.length) {
    text.forEach { char ->
        append(
            when (char) {
                'İ', 'I', 'ı' -> 'i'
                else -> char.lowercaseChar()
            }
        )
    }
}

/** The order the kinds' groups are listed in. */
private val GROUP_ORDER = listOf(SearchKind.Component, SearchKind.Api, SearchKind.Section, SearchKind.Guide)

/** How many results of a kind are listed while every kind is shown, and while that kind is shown alone. */
private val CAPS = BySize(4 to 12, tablet = 5 to 14, phone = 4 to 12)

/** The places to start while nothing is typed, by their paths: a few components and guides. */
private val POPULAR = BySize(
    listOf(
        "/components/text-area",
        "/components/data-table",
        "/components/date-picker",
        "/docs/guides/adaptive",
        "/docs/guides/ai"
    ),
    tablet = listOf("/components/text-area", "/components/data-table", "/docs/guides/adaptive"),
    phone = listOf("/components/text-area", "/components/button", "/docs/guides/analytics")
)

private const val RANK_START = 0
private const val RANK_TITLE = 1
private const val RANK_SUB = 2
