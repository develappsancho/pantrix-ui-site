package com.pantrixui.site.core.content

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ResourceContentRepositoryTest {
    private val repository = ResourceContentRepository()

    @Test
    fun a_page_is_read_from_what_the_content_tool_wrote_in_each_language() = runTest {
        val english = repository.page(Language.English, Route.Doc("installation"))
        val turkish = repository.page(Language.Turkish, Route.Doc("installation"))
        assertEquals("Installation", english?.title)
        assertEquals("Kurulum", turkish?.title)
        assertTrue(english?.blocks.orEmpty().any { it is Block.Steps })
    }

    @Test
    fun a_page_the_site_does_not_have_is_null() = runTest {
        assertNull(repository.page(Language.English, Route.Doc("no-such-page")))
    }

    @Test
    fun a_page_read_once_is_kept() = runTest {
        val first = repository.page(Language.English, Route.Doc("installation"))
        assertSame(first, repository.page(Language.English, Route.Doc("installation")))
    }

    @Test
    fun the_changelog_and_the_guides_minutes_are_read_as_the_content_tool_wrote_them() = runTest {
        val english = repository.changelog(Language.English)
        val turkish = repository.changelog(Language.Turkish)
        // The version of the library the site is built on, without -SNAPSHOT: it rises with the catalogue's.
        assertEquals("0.1.0-alpha03", english.upcoming)
        assertEquals(english.releases.size, turkish.releases.size)
        assertTrue(english.releases.first().sections.isNotEmpty())
        assertTrue("keyboard-dismissal" in repository.guideMinutes())
    }
}
