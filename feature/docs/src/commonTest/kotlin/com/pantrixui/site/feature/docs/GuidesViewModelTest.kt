package com.pantrixui.site.feature.docs

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.model.GuideCatalogue
import com.pantrixui.site.core.model.GuideTopic
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GuidesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val guides = Page(
        path = "/docs/guides",
        language = Language.English,
        title = "Guides",
        description = "Answers.",
        blocks = emptyList()
    )

    @Test
    fun the_page_and_the_guides_minutes_are_read_as_the_model_opens() = runTest(dispatcher) {
        val model = GuidesViewModel(Language.English, FakeContentRepository(listOf(guides), minutes = mapOf("ai" to 9)))
        advanceUntilIdle()
        assertEquals(LoadState.Loaded(guides), model.state.value.page)
        assertEquals(mapOf("ai" to 9), model.state.value.minutes)
    }

    @Test
    fun every_topic_shows_its_guides_in_the_catalogue_s_order_until_one_is_chosen() = runTest(dispatcher) {
        val model = GuidesViewModel(Language.English, FakeContentRepository(listOf(guides)))
        advanceUntilIdle()
        val groups = model.state.value.groups(Language.English)
        assertEquals(GuideTopic.entries, groups.map { it.topic })
        assertEquals(GuideCatalogue.entries, groups.flatMap { it.guides })
        model.onAction(GuidesAction.PickTopic(GuideTopic.Platform))
        advanceUntilIdle()
        assertEquals(listOf(GuideTopic.Platform), model.state.value.groups(Language.English).map { it.topic })
        assertEquals(GuideCatalogue.entries.size, model.state.value.count(Language.English, null))
    }

    @Test
    fun the_filter_reads_titles_and_summaries_in_the_page_s_language_and_counts_each_topic() = runTest(dispatcher) {
        val model = GuidesViewModel(Language.English, FakeContentRepository(listOf(guides)))
        model.onAction(GuidesAction.Filter("  KEYBOARD "))
        advanceUntilIdle()
        val state = model.state.value
        val shown = state.groups(Language.English).flatMap { it.guides }.map { it.key }
        assertEquals(listOf("scroll-padding", "system-bars", "keyboard-dismissal", "keyboard-shortcuts"), shown)
        assertEquals(shown.size, state.count(Language.English, null))
        assertEquals(0, state.count(Language.English, GuideTopic.Quality))
        // The Turkish page filters its own words: "klavye" is not an English one.
        model.onAction(GuidesAction.Filter("klavye"))
        advanceUntilIdle()
        assertTrue(model.state.value.groups(Language.English).isEmpty())
        assertTrue(model.state.value.groups(Language.Turkish).isNotEmpty())
    }
}
