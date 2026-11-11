package com.pantrixui.site.feature.home

import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.model.TokenKind
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
class HomeTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun code(name: String) = Block.Code("kotlin", "$name.kt", listOf(listOf(Token(name))))

    private fun heading(anchor: String) = Block.Heading(3, anchor, listOf(Span.Text(anchor)))

    private val landing = Page(
        path = "/",
        language = Language.Turkish,
        title = "Pantrix UI",
        description = "",
        blocks = listOf(
            heading("review-form"),
            code("Form"),
            heading("review-form-wide"),
            code("Wide"),
            heading("review-form-short"),
            code("Short"),
            heading("preset"),
            code("Preset")
        )
    )

    @Test
    fun the_page_s_code_is_the_block_under_each_of_its_headings() {
        val code = HomeViewModel.codeOf(landing)
        val shown = listOf(code.form, code.wideForm, code.shortForm, code.preset)
        assertEquals(listOf("Form", "Wide", "Short", "Preset"), shown.map { it.lines.single().single().text })
    }

    @Test
    fun a_page_without_its_code_fails_to_load_rather_than_draw_an_empty_panel() = runTest(dispatcher) {
        val model = HomeViewModel(Language.English, FakeContentRepository(listOf(landing)))
        advanceUntilIdle()
        // The landing page is only written in Turkish here: the English one is not found.
        assertIs<LoadState.Failed>(model.state.value.code)
        val missing = landing.copy(blocks = landing.blocks.dropLast(1))
        val broken = runCatching { HomeViewModel.codeOf(missing) }
        assertTrue(broken.isFailure, "a page whose preset has no code")
    }

    @Test
    fun the_code_is_read_in_the_page_s_language_and_the_choices_are_kept() = runTest(dispatcher) {
        val model = HomeViewModel(Language.Turkish, FakeContentRepository(listOf(landing)))
        assertEquals(LoadState.Loading, model.state.value.code)
        assertEquals(PlatformType.Ios, model.state.value.platform, "the design opens on iOS")
        advanceUntilIdle()
        assertEquals("Preset.kt", model.state.value.code.valueOrNull?.preset?.file)
        model.onAction(HomeAction.PickPlatform(PlatformType.Web))
        model.onAction(HomeAction.PickCodeTab(1))
        assertEquals(PlatformType.Web, model.state.value.platform)
        assertEquals(1, model.state.value.codeTab)
    }

    @Test
    fun copying_hands_the_install_line_to_the_screen_and_says_so_for_a_moment() = runTest(dispatcher) {
        val model = HomeViewModel(Language.English, FakeContentRepository(listOf(landing)))
        model.onAction(HomeAction.CopyInstall)
        assertEquals(
            HomeEffect.Copy("implementation(\"com.pantrixui:components:0.2.0\")"),
            model.effects.first()
        )
        model.onAction(HomeAction.Copied(done = false))
        assertFalse(model.state.value.copied, "a clipboard that did not take it")
        model.onAction(HomeAction.Copied(done = true))
        assertTrue(model.state.value.copied)
        advanceTimeBy(2_001)
        assertFalse(model.state.value.copied)
    }

    @Test
    fun a_link_opens_a_page_of_the_site_or_an_address_outside_it() = runTest(dispatcher) {
        val model = HomeViewModel(Language.English, FakeContentRepository())
        model.onAction(HomeAction.OpenLink("/tr/components"))
        assertEquals(HomeEffect.Navigate("/tr/components"), model.effects.first())
        model.onAction(HomeAction.OpenLink("https://github.com/develappsancho/pantrix-ui-cmp"))
        assertEquals(HomeEffect.OpenUrl("https://github.com/develappsancho/pantrix-ui-cmp"), model.effects.first())
    }

    @Test
    fun the_terminal_asks_in_the_page_s_language_and_a_phone_s_is_shorter() {
        val desktop = terminalLines(Language.Turkish, short = false)
        val phone = terminalLines(Language.Turkish, short = true)

        fun List<Token>.text() = joinToString("") { it.text }
        assertEquals("> Her siparişin altına en çok 280 karakterlik bir yorum kutusu", desktop[2].text())
        assertEquals("$ claude mcp add -t http pantrix-ui ${SiteAddress.mcp}", desktop.first().text())
        assertEquals(TokenKind.Number, desktop.last().single { it.text == "280" }.kind)
        assertEquals(
            listOf("$ claude mcp add -t http \\", "    pantrix-ui ${SiteAddress.mcp}"),
            phone.take(2).map { it.text() }
        )
        assertEquals(5, phone.size)
    }

    @Test
    fun every_feature_card_leads_to_a_page_and_says_itself_in_both_languages() {
        assertEquals(9, HomeSay.Features.size)
        HomeSay.Features.forEach { feature ->
            assertTrue(feature.title.english.isNotBlank() && feature.title.turkish.isNotBlank(), feature.api)
            assertTrue(feature.body.english != feature.body.turkish, feature.api)
        }
        assertEquals(HomeSay.Stats.size, HomeSay.StatsShort.size, "a short label for each number")
    }

    /**
     * The tour turns from the phone to the last step and back to the phone while it plays; a step the reader picks
     * stops it there, and its button starts it again.
     */
    @Test
    fun the_tour_turns_while_it_plays_and_stops_where_the_reader_picks() = runTest(dispatcher) {
        val model = HomeViewModel(Language.English, FakeContentRepository())
        assertEquals(TourStep.Phone to true, model.state.value.tourStep to model.state.value.tourPlaying)
        repeat(TourStep.entries.size - 1) { model.onAction(HomeAction.NextTourStep) }
        assertEquals(TourStep.Web, model.state.value.tourStep)
        model.onAction(HomeAction.NextTourStep)
        assertEquals(TourStep.Phone, model.state.value.tourStep, "after the last, the first")
        model.onAction(HomeAction.PickTourStep(TourStep.Table))
        assertEquals(TourStep.Table to false, model.state.value.tourStep to model.state.value.tourPlaying)
        model.onAction(HomeAction.NextTourStep)
        assertEquals(TourStep.Table, model.state.value.tourStep, "a stopped tour does not turn")
        model.onAction(HomeAction.ToggleTour)
        model.onAction(HomeAction.NextTourStep)
        assertEquals(TourStep.Tablet, model.state.value.tourStep)
    }

    /** Each step says itself in both languages, names the parts of the library that change it, and has its device. */
    @Test
    fun every_tour_step_says_itself_in_both_languages() {
        assertEquals(6, TourStep.entries.size)
        TourStep.entries.forEach { step ->
            assertTrue(step.line.english != step.line.turkish, step.name)
            assertTrue(step.apis.isNotEmpty(), step.name)
        }
        assertEquals(listOf(1680, 1280), listOf(TourStep.Desktop.device.width, TourStep.Web.device.width))
    }
}
