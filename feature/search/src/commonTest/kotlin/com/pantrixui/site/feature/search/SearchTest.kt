package com.pantrixui.site.feature.search

import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.SearchEntry
import com.pantrixui.site.core.model.SearchIndex
import com.pantrixui.site.core.model.SearchKind
import com.pantrixui.site.core.testing.FakeContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun component(
        name: String,
        key: String = name.lowercase().replace(' ', '-')
    ) = SearchEntry(SearchKind.Component, name, "Component · Inputs", "/components/$key", mark = name.take(1))

    private val entries = listOf(
        SearchEntry(
            SearchKind.Section,
            "Fitting the window › The window",
            "What it is",
            "/docs/guides/adaptive",
            "window",
            "#"
        ),
        component("Text Field"),
        component("Text Area"),
        component("Filled Text Area"),
        component("Data Table"),
        component("Date Picker"),
        component("Button"),
        SearchEntry(
            SearchKind.Api,
            "PantrixTextArea",
            "Composable · com.pantrixui.components.inputs.textarea",
            "/components/text-area",
            "api",
            "{}"
        ),
        SearchEntry(
            SearchKind.Api,
            "TextLimit",
            "Enum · com.pantrixui.components.inputs.common",
            "/components/text-area",
            "api",
            "{}"
        ),
        SearchEntry(
            SearchKind.Section,
            "Text Area › Testing",
            "TextArea_<tagKey>, the tags a test finds it by",
            "/components/text-area",
            "testing",
            "#"
        ),
        SearchEntry(SearchKind.Guide, "Fitting the window", "Guide", "/docs/guides/adaptive", mark = "§"),
        SearchEntry(SearchKind.Guide, "Teaching Pantrix to an assistant", "Guide", "/docs/guides/ai", mark = "§"),
        SearchEntry(SearchKind.Guide, "Connecting events to analytics", "Guide", "/docs/guides/analytics", mark = "§"),
        SearchEntry(SearchKind.Guide, "İstemci tarafı", "Rehber", "/docs/guides/client", mark = "§")
    )

    @Test
    fun every_word_typed_has_to_be_found_and_a_title_that_starts_with_it_comes_first() {
        val groups = searchGroups(entries, "area", null, SiteLayout.Desktop)
        assertEquals(listOf(SearchKind.Component, SearchKind.Api, SearchKind.Section), groups.map { it.kind })
        val components = groups.first().rows
        assertEquals(listOf("Text Area", "Filled Text Area"), components.map { it.entry.title })
        assertEquals(5..8, components.first().hit, "the word is marked where it stands in the title")
        assertTrue(searchGroups(entries, "text table", null, SiteLayout.Desktop).isEmpty(), "no title holds both words")
        val api = searchGroups(entries, "textarea", SearchKind.Api, SiteLayout.Desktop).single()
        assertEquals(listOf("PantrixTextArea"), api.rows.map { it.entry.title })
    }

    @Test
    fun a_second_line_finds_a_result_too_and_marks_nothing_in_its_title() {
        val limit = searchGroups(entries, "enum", null, SiteLayout.Desktop).single().rows.single()
        assertEquals("TextLimit", limit.entry.title)
        assertNull(limit.hit)
    }

    @Test
    fun i_is_one_letter_in_either_language() {
        assertEquals("istemci tarafi", fold("İstemci Tarafı"))
        assertEquals(
            "İstemci tarafı",
            searchGroups(entries, "ISTEMCI", null, SiteLayout.Phone).single().rows.single().entry.title
        )
        assertEquals(
            "İstemci tarafı".length,
            fold("İstemci tarafı").length,
            "a place found is the same place in the text"
        )
    }

    @Test
    fun every_kind_shows_a_few_and_one_kind_alone_shows_more_and_a_tablet_and_a_phone_leave_parts_out() {
        val many = (1..20).map { component("Text $it", "text-$it") }
        assertEquals(4, searchGroups(many, "text", null, SiteLayout.Desktop).single().rows.size)
        assertEquals(5, searchGroups(many, "text", null, SiteLayout.Tablet).single().rows.size)
        assertEquals(12, searchGroups(many, "text", SearchKind.Component, SiteLayout.Desktop).single().rows.size)
        assertEquals(14, searchGroups(many, "text", SearchKind.Component, SiteLayout.Tablet).single().rows.size)
        val phone = searchGroups(entries, "testing", null, SiteLayout.Phone)
        assertTrue(phone.any { it.kind == SearchKind.Section }, "a phone finds the pages and their parts too")
        val counts = searchCounts(entries, "area")
        assertEquals(2, counts[SearchKind.Component])
        assertEquals(4, counts[null], "every kind together")
        assertEquals(0, searchCounts(entries, " ")[null], "nothing typed finds nothing to count")
    }

    @Test
    fun with_nothing_typed_each_size_offers_its_places_to_start() {
        fun start(layout: SiteLayout) = searchGroups(entries, "  ", null, layout).single().rows.map { it.entry.path }
        assertTrue(
            searchGroups(entries, "", null, SiteLayout.Desktop).single().rows.none {
                it.entry.kind ==
                    SearchKind.Section
            }
        )
        assertEquals(
            listOf(
                "/components/text-area",
                "/components/data-table",
                "/components/date-picker",
                "/docs/guides/adaptive",
                "/docs/guides/ai"
            ),
            start(SiteLayout.Desktop)
        )
        assertEquals(
            listOf("/components/text-area", "/components/data-table", "/docs/guides/adaptive"),
            start(SiteLayout.Tablet)
        )
        assertEquals(
            listOf("/components/text-area", "/components/button", "/docs/guides/analytics"),
            start(SiteLayout.Phone)
        )
    }

    @Test
    fun the_search_opens_afresh_in_the_site_s_language_and_reads_the_other_s_index_when_it_is_picked() = runTest(
        dispatcher
    ) {
        val content = FakeContentRepository(
            searches = listOf(
                SearchIndex(Language.English, entries),
                SearchIndex(Language.Turkish, entries.takeLast(1))
            )
        )
        val model = SearchViewModel(Language.English, content)
        advanceUntilIdle()
        assertEquals(entries.size, model.state.value.index.valueOrNull?.entries?.size)
        model.onAction(SearchAction.Type("area"))
        model.onAction(SearchAction.Move(by = 3, shown = 2))
        assertEquals(1, model.state.value.selected, "the arrows stop at the last result")
        model.onAction(SearchAction.PickLanguage(Language.Turkish))
        advanceUntilIdle()
        assertEquals(1, model.state.value.index.valueOrNull?.entries?.size)
        assertEquals(0, model.state.value.selected)
        model.onAction(SearchAction.PickKind(SearchKind.Guide))
        model.onAction(SearchAction.Open(Language.English))
        advanceUntilIdle()
        with(model.state.value) {
            assertEquals("", query)
            assertNull(kind)
            assertEquals(Language.English, language)
            assertEquals(entries.size, index.valueOrNull?.entries?.size)
        }
    }
}
