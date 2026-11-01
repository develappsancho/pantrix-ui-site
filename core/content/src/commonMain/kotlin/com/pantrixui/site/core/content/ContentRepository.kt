package com.pantrixui.site.core.content

import com.pantrixui.site.core.content.generated.resources.Res
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.ContentFiles
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.routing.SiteRoutes
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.jetbrains.compose.resources.ExperimentalResourceApi
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
}

/**
 * The site's content from the app's resources: each page is read when it is first asked for, and kept. On the web a
 * page is fetched then, a few kilobytes, rather than all of them with the app.
 */
class ResourceContentRepository : ContentRepository {
    private val pages = mutableMapOf<String, Page?>()
    private val changelogs = mutableMapOf<Language, Changelog>()
    private var minutes: Map<String, Int>? = null
    private val lock = Mutex()

    @OptIn(ExperimentalResourceApi::class)
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

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun changelog(language: Language): Changelog = lock.withLock {
        changelogs.getOrPut(language) {
            val bytes = Res.readBytes(ContentFiles.changelogFile(language))
            ContentFiles.json.decodeFromString(Changelog.serializer(), bytes.decodeToString())
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun guideMinutes(): Map<String, Int> = lock.withLock {
        minutes ?: ContentFiles.json.decodeFromString(
            MapSerializer(String.serializer(), Int.serializer()),
            Res.readBytes(ContentFiles.GUIDE_MINUTES).decodeToString()
        ).also { minutes = it }
    }
}
