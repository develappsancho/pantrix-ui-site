package com.pantrixui.site.core.model

import kotlinx.serialization.Serializable

/**
 * The apps built with Pantrix UI that the references page shows, in one language, the first of them the one it
 * features. They are written in the repository's `content/references.json`, both languages in one file, and a new one
 * is a few lines there.
 *
 * @property projects the apps, in the page's order.
 */
@Serializable
data class References(
    val projects: List<Reference> = emptyList()
)

/**
 * An app built with Pantrix UI, as the references page shows it: what it is, where it runs, how it is reached and
 * which of the library's components it draws its own screens with.
 *
 * @property key its key, the same in every language: `pantrix-ui-site`, its card's automation tag.
 * @property name what it is called.
 * @property address where it is, as a reader writes it: `pantrixui.com`.
 * @property line what it is, in one line: what its card in a list says.
 * @property about what it is, in a few sentences: what the card that features it says.
 * @property url where it is visited.
 * @property source where its source is, where it is open; null where it is not.
 * @property download where its desktop app is downloaded, where it has one: an address of the site itself, `/download`,
 *   opens that page in it.
 * @property platforms where it runs, each with what it is there.
 * @property languages the languages it speaks, by their codes.
 * @property components the library's components its own screens are drawn with, by their composables' names.
 * @property made how it is made, a few points.
 */
@Serializable
data class Reference(
    val key: String,
    val name: String,
    val address: String,
    val line: String,
    val about: String,
    val url: String,
    val source: String? = null,
    val download: String? = null,
    val platforms: List<ReferencePlatform> = emptyList(),
    val languages: List<String> = emptyList(),
    val components: List<String> = emptyList(),
    val made: List<ReferencePoint> = emptyList()
)

/**
 * A platform an app runs on.
 *
 * @property kind what kind of platform it is, which the page's filter goes by.
 * @property name what the platform is called: `macOS` for a desktop app on a Mac.
 * @property detail what the app is there, in a few words.
 */
@Serializable
data class ReferencePlatform(
    val kind: PlatformKind,
    val name: String,
    val detail: String
)

/** The kinds of platform the references page tells apps apart by. */
@Serializable
enum class PlatformKind {
    Android,
    Ios,
    Desktop,
    Web
}

/**
 * A point of how an app is made.
 *
 * @property title the point, in a few words.
 * @property detail what it means, in a sentence.
 */
@Serializable
data class ReferencePoint(
    val title: String,
    val detail: String
)
