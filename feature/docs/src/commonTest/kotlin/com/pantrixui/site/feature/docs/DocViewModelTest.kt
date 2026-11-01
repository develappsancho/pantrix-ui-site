package com.pantrixui.site.feature.docs

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.testing.FakeContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DocViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val installation = Page(
        path = "/docs/installation",
        language = Language.English,
        title = "Installation",
        description = "Add Pantrix UI.",
        blocks = emptyList(),
        markdown = "# Installation"
    )

    private fun model(
        content: FakeContentRepository = FakeContentRepository(listOf(installation)),
        language: Language = Language.English,
        route: Route = Route.Doc("installation")
    ) = DocViewModel(route, language, content)

    @Test
    fun the_page_is_read_in_its_language_as_the_model_opens() = runTest(dispatcher) {
        val english = model()
        val turkish = model(language = Language.Turkish)
        assertEquals(LoadState.Loading, english.state.value.page)
        advanceUntilIdle()
        assertEquals(LoadState.Loaded(installation), english.state.value.page)
        // The site has no Turkish page here: an answer, not a failure.
        assertEquals(LoadState.Loaded(null), turkish.state.value.page)
    }

    @Test
    fun a_page_that_cannot_be_read_is_read_again_on_retry() = runTest(dispatcher) {
        val content = FakeContentRepository(listOf(installation)).apply { failure = IllegalStateException("offline") }
        val model = model(content)
        advanceUntilIdle()
        assertEquals(LoadState.Failed("offline"), model.state.value.page)
        content.failure = null
        model.onAction(DocAction.Retry)
        advanceUntilIdle()
        assertEquals(LoadState.Loaded(installation), model.state.value.page)
        assertEquals(2, content.reads)
    }

    @Test
    fun copying_hands_the_markdown_to_the_screen_and_says_so_only_once_it_is_copied() = runTest(dispatcher) {
        val model = model()
        advanceUntilIdle()
        model.onAction(DocAction.CopyPage)
        assertEquals(DocEffect.Copy("# Installation"), model.effects.first())
        assertFalse(model.state.value.copied)
        model.onAction(DocAction.Copied(done = false))
        assertFalse(model.state.value.copied)
        model.onAction(DocAction.Copied(done = true))
        assertTrue(model.state.value.copied)
        advanceTimeBy(1_999)
        assertTrue(model.state.value.copied)
        advanceTimeBy(2)
        assertFalse(model.state.value.copied)
    }

    @Test
    fun copying_again_keeps_the_note_for_its_whole_time() = runTest(dispatcher) {
        val model = model()
        advanceUntilIdle()
        model.onAction(DocAction.Copied(done = true))
        advanceTimeBy(1_500)
        model.onAction(DocAction.Copied(done = true))
        advanceTimeBy(1_500)
        assertTrue(model.state.value.copied)
        advanceTimeBy(600)
        assertFalse(model.state.value.copied)
    }

    @Test
    fun a_page_not_read_yet_has_nothing_to_copy() = runTest(dispatcher) {
        val model = model()
        model.onAction(DocAction.CopyPage)
        model.onAction(DocAction.OpenLink("/docs/theming"))
        // The first effect the screen gets is the link's: the copy sent none.
        assertEquals(DocEffect.Navigate("/docs/theming"), model.effects.first())
    }

    @Test
    fun a_link_opens_a_page_of_the_site_or_a_web_address() = runTest(dispatcher) {
        val model = model()
        model.onAction(DocAction.OpenLink("/tr/docs/theming"))
        assertEquals(DocEffect.Navigate("/tr/docs/theming"), model.effects.first())
        model.onAction(DocAction.OpenLink("https://github.com/developersancho/pantrix-ui-cmp"))
        assertIs<DocEffect.OpenUrl>(model.effects.first())
    }
}
