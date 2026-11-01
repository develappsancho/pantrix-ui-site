package com.pantrixui.site.feature.roadmap

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Words
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RoadmapTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun a_filter_shows_one_stage_and_all_shows_every_one_and_a_sample_is_picked() = runTest(dispatcher) {
        val model = RoadmapViewModel()
        assertNull(model.state.value.filter, "the board opens with every column")
        model.onAction(RoadmapAction.PickFilter(Stage.Next))
        assertEquals(Stage.Next, model.state.value.filter)
        model.onAction(RoadmapAction.PickFilter(null))
        assertNull(model.state.value.filter)
        model.onAction(RoadmapAction.PickSample(Sample.Form))
        assertEquals(Sample.Form, model.state.value.sample)
    }

    @Test
    fun a_link_opens_a_page_of_the_site_or_the_library_s_issues() = runTest(dispatcher) {
        val model = RoadmapViewModel()
        model.onAction(RoadmapAction.OpenLink("/tr/ai"))
        assertEquals(RoadmapEffect.Navigate("/tr/ai"), model.effects.first())
        model.onAction(RoadmapAction.OpenLink(RoadmapSay.ISSUES))
        assertEquals(RoadmapEffect.OpenUrl("${LibraryFacts.REPOSITORY}/issues"), model.effects.first())
    }

    @Test
    fun every_stage_has_one_column_and_server_driven_ui_counts_the_plan_s_steps() {
        assertEquals(Stage.entries.toList(), RoadmapSay.Columns.map { it.stage })
        val sdui = RoadmapSay.Columns.single { it.stage == Stage.Next }.items.single()
        assertEquals("0 of ${SduiSay.Steps.size} steps", sdui.progress?.label?.english)
        assertEquals(Link.Section(SDUI), sdui.link)
        val considered = RoadmapSay.Columns.single { it.stage == Stage.Later }.items.filter {
            it.version.english ==
                "Considered"
        }
        assertEquals(3, considered.size, "what the library's plan leaves out until it is asked for is only considered")
    }

    @Test
    fun every_sample_description_is_json_in_every_size_and_language_and_names_what_the_preview_shows() {
        Sample.entries.forEach { sample ->
            SiteLayout.entries.forEach { layout ->
                Language.entries.forEach { language ->
                    val lines = SduiSay.lines(sample, layout, language)
                    val text = lines.filter { it.kind != TokenKind.Comment }.joinToString("\n") { it.text }
                    val screen = Json.parseToJsonElement(text).jsonObject
                    val body = screen.getValue("body").jsonArray.map { it as JsonObject }
                    val components = body.map { it.getValue("component").jsonPrimitive.content }
                    val expected = when (sample) {
                        Sample.Campaign -> if (layout ==
                            SiteLayout.Desktop
                        ) {
                            listOf("Card", "Button")
                        } else {
                            listOf("Card")
                        }

                        Sample.Form -> if (layout == SiteLayout.Desktop) {
                            listOf("TextField", "SwitchButton", "TextArea", "Button")
                        } else {
                            listOf("TextField", "SwitchButton", "Button")
                        }
                    }
                    assertEquals(expected, components, "$sample $layout $language")
                }
            }
        }
    }

    @Test
    fun the_page_says_everything_in_both_languages_in_every_size() {
        val sized: List<BySize<Words>> =
            RoadmapSay.Columns.flatMap { column -> column.items.flatMap { listOf(it.title, it.body) } } +
                listOf(RoadmapSay.Lead, SduiSay.Pill, SduiSay.Title, SduiSay.Lead, SduiSay.Sketch, NativeSay.Lead) +
                SduiSay.Principles.flatMap { it.toList() }
        val words: List<Words> = sized.flatMap { size -> SiteLayout.entries.map { size.of(it) } } +
            RoadmapSay.Columns.flatMap { listOf(it.name, it.note) + it.items.map { item -> item.version } } +
            RoadmapSay.Versions.flatMap { listOf(it.name, it.title, it.body) } + SduiSay.Steps +
            NativeSay.Libraries.flatMap { listOf(it.project, it.name, it.body) + it.targets } +
            NativeSay.SameRows.flatMap { it.toList() } + NativeSay.Differs +
            Area.entries.map { RoadmapSay.areaName(it) }
        words.forEach { assertTrue(it.english.isNotBlank() && it.turkish.isNotBlank(), it.english) }
    }

    @Test
    fun a_smaller_size_says_an_item_shorter_or_the_same() {
        RoadmapSay.Columns.flatMap { it.items }.flatMap { listOf(it.title, it.body) }.forEach { words ->
            assertTrue(words.tablet.english.length <= words.desktop.english.length, words.tablet.english)
            assertTrue(words.phone.english.length <= words.tablet.english.length, words.phone.english)
        }
    }
}
