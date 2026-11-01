package com.pantrixui.site.core.content

import com.pantrixui.site.core.content.generated.resources.Res
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.ContentFiles
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.SearchIndex
import com.pantrixui.site.core.routing.SiteRoutes
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.jetbrains.compose.resources.MissingResourceException

/** Where a screen gets the site's content: what the content tool wrote, in each language. */
interface ContentRepository {
    /** The page of [route] in [language], or null when the site has no such page. */
    suspend fun page(
        language: Language,
        route: Route
    ): Page?

    /** The library's changelog in [language]. */
    suspend fun changelog(language: Language): Changelog

    /** How many minutes each of the library's guides takes to read, by its key. */
    suspend fun guideMinutes(): Map<String, Int>

    /** The library's components in [language], by group. */
    suspend fun componentIndex(language: Language): ComponentIndex

    /** The page of the component [key] in [language], or null when the library has no such component. */
    suspend fun component(
        language: Language,
        key: String
    ): ComponentPage?

    /** What the site's search finds in [language]. */
    suspend fun searchIndex(language: Language): SearchIndex
}

/**
 * The site's content from the app's resources: each page is read when it is first asked for, and kept. On the web a
 * page is fetched then, a few kilobytes, rather than all of them with the app.
 */
class ResourceContentRepository : ContentRepository {
    private val pages = mutableMapOf<String, Page?>()
    private val changelogs = mutableMapOf<Language, Changelog>()
    private var minutes: Map<String, Int>? = null
    private val indexes = mutableMapOf<Language, ComponentIndex>()
    private val searches = mutableMapOf<Language, SearchIndex>()
    private val components = mutableMapOf<String, ComponentPage?>()
    private val lock = Mutex()

    override suspend fun page(
        language: Language,
        route: Route
    ): Page? {
        val file = ContentFiles.pageFile(language, SiteRoutes.pathOf(route))
        return lock.withLock {
            pages.getOrPut(file) {
                val bytes = try {
                    Res.readBytes(file)
                } catch (@Suppress("SwallowedException") missing: MissingResourceException) {
                    // No file is a page the site does not have: an answer, not a failure.
                    return@getOrPut null
                }
                val text = bytes.decodeToString()
                // A host that answers every address with the site's own page answers a page the site does not
                // have with HTML, not with a 404: that too is no such page.
                if (text.trimStart().startsWith("<")) return@getOrPut null
                ContentFiles.json.decodeFromString(Page.serializer(), text)
            }
        }
    }

    override suspend fun changelog(language: Language): Changelog = lock.withLock {
        changelogs.getOrPut(language) {
            val bytes = Res.readBytes(ContentFiles.changelogFile(language))
            ContentFiles.json.decodeFromString(Changelog.serializer(), bytes.decodeToString())
        }
    }

    override suspend fun componentIndex(language: Language): ComponentIndex = lock.withLock {
        indexes.getOrPut(language) {
            val bytes = Res.readBytes(ContentFiles.componentIndexFile(language))
            ContentFiles.json.decodeFromString(ComponentIndex.serializer(), bytes.decodeToString())
        }
    }

    override suspend fun component(
        language: Language,
        key: String
    ): ComponentPage? {
        val file = ContentFiles.componentFile(language, key)
        return lock.withLock {
            components.getOrPut(file) {
                val bytes = try {
                    Res.readBytes(file)
                } catch (@Suppress("SwallowedException") missing: MissingResourceException) {
                    // No file is a component the library does not have: an answer, not a failure.
                    return@getOrPut null
                }
                val text = bytes.decodeToString()
                // As for a page: a host's own page in place of a file the site does not have is no such component.
                if (text.trimStart().startsWith("<")) return@getOrPut null
                ContentFiles.json.decodeFromString(ComponentPage.serializer(), text)
            }
        }
    }

    override suspend fun searchIndex(language: Language): SearchIndex = lock.withLock {
        searches.getOrPut(language) {
            val bytes = Res.readBytes(ContentFiles.searchFile(language))
            ContentFiles.json.decodeFromString(SearchIndex.serializer(), bytes.decodeToString())
        }
    }

    override suspend fun guideMinutes(): Map<String, Int> = lock.withLock {
        minutes ?: ContentFiles.json.decodeFromString(
            MapSerializer(String.serializer(), Int.serializer()),
            Res.readBytes(ContentFiles.GUIDE_MINUTES).decodeToString()
        ).also { minutes = it }
    }
}
