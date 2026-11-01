package com.pantrixui.site.core.testing

import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.References
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.SearchIndex
import com.pantrixui.site.core.routing.SiteRoutes

/**
 * The site's content for a test: the [pages] it is given, found by their path and language. A page it was not given
 * is a page the site does not have; [failure], when it is set, is thrown for every read instead.
 *
 * @param pages the pages it knows.
 * @param changelogs the changelogs it knows, one a language.
 * @param minutes how long each guide takes to read, by its key.
 * @param indexes the lists of the components it knows, one a language.
 * @param components the components' pages it knows.
 * @param searches what its search finds, one index a language.
 * @param references the apps built with Pantrix UI it knows, by language; none in a language it was not given.
 */
class FakeContentRepository(
    private val pages: List<Page> = emptyList(),
    private val changelogs: List<Changelog> = emptyList(),
    private val minutes: Map<String, Int> = emptyMap(),
    private val indexes: List<ComponentIndex> = emptyList(),
    private val components: List<ComponentPage> = emptyList(),
    private val searches: List<SearchIndex> = emptyList(),
    private val references: Map<Language, References> = emptyMap()
) : ContentRepository {
    /** Thrown by every read while it is set: a source that cannot be read. */
    var failure: Exception? = null

    /** How many times a page was asked for. */
    var reads: Int = 0
        private set

    override suspend fun page(
        language: Language,
        route: Route
    ): Page? {
        reads++
        failure?.let { throw it }
        val path = SiteRoutes.pathOf(route)
        return pages.firstOrNull { it.language == language && it.path == path }
    }

    override suspend fun changelog(language: Language): Changelog {
        reads++
        failure?.let { throw it }
        return changelogs.first { it.language == language }
    }

    override suspend fun guideMinutes(): Map<String, Int> {
        reads++
        failure?.let { throw it }
        return minutes
    }

    override suspend fun componentIndex(language: Language): ComponentIndex {
        reads++
        failure?.let { throw it }
        return indexes.first { it.language == language }
    }

    override suspend fun component(
        language: Language,
        key: String
    ): ComponentPage? {
        reads++
        failure?.let { throw it }
        return components.firstOrNull { it.language == language && it.card.key == key }
    }

    override suspend fun searchIndex(language: Language): SearchIndex {
        reads++
        failure?.let { throw it }
        return searches.first { it.language == language }
    }

    override suspend fun references(language: Language): References {
        reads++
        failure?.let { throw it }
        return references[language] ?: References()
    }
}
