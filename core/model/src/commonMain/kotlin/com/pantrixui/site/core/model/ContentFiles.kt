package com.pantrixui.site.core.model

import kotlinx.serialization.json.Json

/**
 * Where the content tool writes a page and where the app reads it, and the JSON both use: one place, so that the
 * two cannot part.
 */
object ContentFiles {
    /** The JSON a page is written in: no empty fields, and a field the reader does not know yet is passed over. */
    val json: Json = Json {
        encodeDefaults = false
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    /**
     * The file of the page at [path] in [language], among the app's resources:
     * `files/content/en/docs/installation.json`, and `files/content/en/index.json` for the landing page.
     */
    fun pageFile(
        language: Language,
        path: String
    ): String {
        val name = path.trim('/').ifEmpty { "index" }
        return "files/content/${language.code}/$name.json"
    }

    /** The file of the library's changelog in [language]: `files/content/en/releases.json`. */
    fun changelogFile(language: Language): String = "files/content/${language.code}/releases.json"

    /** The file of the library's components in [language], by group: `files/content/en/components.json`. */
    fun componentIndexFile(language: Language): String = "files/content/${language.code}/components.json"

    /** The file of the page of the component [key] in [language]: `files/content/en/components/text-area.json`. */
    fun componentFile(
        language: Language,
        key: String
    ): String = "files/content/${language.code}/components/$key.json"

    /** The file of what the site's search finds in [language]: `files/content/en/search.json`. */
    fun searchFile(language: Language): String = "files/content/${language.code}/search.json"

    /** The file of the apps built with Pantrix UI in [language]: `files/content/en/references.json`. */
    fun referencesFile(language: Language): String = "files/content/${language.code}/references.json"

    /** The file of how long each guide takes to read, by its key, the same in every language. */
    const val GUIDE_MINUTES: String = "files/content/guide-minutes.json"
}
