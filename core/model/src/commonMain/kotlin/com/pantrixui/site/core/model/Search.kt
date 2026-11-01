package com.pantrixui.site.core.model

import kotlinx.serialization.Serializable

/**
 * What the site's search finds in one language, as the content tool writes it at build time: the components, the
 * library's API, the parts of the pages and the guides.
 *
 * @property language the language its words are in.
 * @property entries what it finds, kind by kind.
 */
@Serializable
data class SearchIndex(
    val language: Language,
    val entries: List<SearchEntry>
)

/**
 * One thing the search finds.
 *
 * @property kind what it is.
 * @property title what a result says first: `Text Area`, `PantrixTextArea`, `Installation › Add the repository`.
 * @property sub what a result says under it: what it is and where.
 * @property path the page it opens, in no language: `/components/text-area`.
 * @property anchor the part of the page it opens at, or null for the page's top.
 * @property mark what its result's square shows: the component's first letter, `{}`, `#` or `§`.
 */
@Serializable
data class SearchEntry(
    val kind: SearchKind,
    val title: String,
    val sub: String,
    val path: String,
    val anchor: String? = null,
    val mark: String
)

/** What a thing the search finds is, in the order the results list them. */
@Serializable
enum class SearchKind { Component, Api, Section, Guide }
