package com.pantrixui.site.core.model

import kotlinx.serialization.Serializable

/**
 * The library's changelog in one language, as the content tool reads it from the library's documents at build time.
 *
 * @property language the language it is written in.
 * @property title what the changelog calls itself: its page's heading.
 * @property description its first paragraph: what it is for.
 * @property releases its versions, the newest first.
 * @property markdown the changelog as it is written, what its page's "Copy page" button copies.
 * @property upcoming the number of the version still to come, which its "Unreleased" entry describes: `0.1.0`.
 */
@Serializable
data class Changelog(
    val language: Language,
    val title: String,
    val description: List<Span>,
    val releases: List<Release>,
    val markdown: String,
    val upcoming: String? = null
)

/**
 * A version of the library and what it changed.
 *
 * @property version its number, `0.1.0`, or null for the version still to come, which the changelog keeps under
 *   "Unreleased".
 * @property date the day it was released, `2026-11-02`, or null while it has not been.
 * @property intro what the changelog says of it before its changes, before its list of [targets].
 * @property targets the platforms it is built for, where the changelog lists them.
 * @property sections its changes, by kind.
 * @property lead what the changelog says after the list of targets, leading into the changes.
 */
@Serializable
data class Release(
    val version: String? = null,
    val date: String? = null,
    val intro: List<Block> = emptyList(),
    val targets: List<List<Span>> = emptyList(),
    val sections: List<ChangeSection> = emptyList(),
    val lead: List<Block> = emptyList()
)

/**
 * The changes of one kind in a version.
 *
 * @property kind what kind they are: what was added, changed, deprecated, removed, fixed or done for security.
 * @property title what the changelog calls the kind, in its language: "Added", "Eklenenler".
 * @property groups its changes, by the module they are in.
 */
@Serializable
data class ChangeSection(
    val kind: ChangeKind,
    val title: String,
    val groups: List<ChangeGroup>
)

/** The kinds of change a changelog sorts its lines into, as Keep a Changelog names them. */
@Serializable
enum class ChangeKind {
    Added,
    Changed,
    Deprecated,
    Removed,
    Fixed,
    Security
}

/**
 * The changes of one module.
 *
 * @property module the module, as the changelog names it in its language: "Foundation", "Bileşenler".
 * @property key the module's name in no language, what a filter keeps: `foundation`, `components`, `tooling`.
 * @property artifacts the artifacts the module is published as: `com.pantrixui:foundation`.
 * @property entries its lines.
 */
@Serializable
data class ChangeGroup(
    val module: String,
    val key: String,
    val artifacts: List<String> = emptyList(),
    val entries: List<ChangeEntry>
)

/**
 * One line of a changelog.
 *
 * @property label its bold lead, "Theme", where it has one.
 * @property blocks what it says after the lead, with the list under it where it has one.
 * @property breaking whether the change breaks a call or a behaviour: the changelog marks it so.
 */
@Serializable
data class ChangeEntry(
    val label: String? = null,
    val blocks: List<Block>,
    val breaking: Boolean = false
)
