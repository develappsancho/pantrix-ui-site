package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.model.Words
import com.pantrixui.site.core.testing.FakeContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AiTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun answer(anchor: String) = listOf(
        Block.Heading(3, anchor, listOf(Span.Text(anchor))),
        Block.Code("kotlin", "$anchor.kt", listOf(listOf(Token("PantrixTextArea("))))
    )

    private fun page(vararg anchors: String) = Page(
        path = "/ai",
        language = Language.Turkish,
        title = "Kodlama asistanları için",
        description = "",
        blocks = anchors.flatMap { answer(it) }
    )

    @Test
    fun each_size_s_answer_is_read_under_its_heading_in_the_page_s_language() = runTest(dispatcher) {
        val content = FakeContentRepository(listOf(page(ANSWER, ANSWER_TABLET, ANSWER_PHONE)))
        val model = AiViewModel(Language.Turkish, content)
        advanceUntilIdle()
        val answers = model.state.value.answers.valueOrNull.orEmpty()
        assertEquals("answer-phone.kt", answers[ANSWER_PHONE]?.file)
        assertEquals(setOf(ANSWER, ANSWER_TABLET, ANSWER_PHONE), answers.keys)
        val english = AiViewModel(Language.English, content)
        advanceUntilIdle()
        assertTrue(english.state.value.answers is LoadState.Failed, "no English page: no answer, and the page says so")
    }

    @Test
    fun a_page_without_a_size_s_answer_fails_rather_than_draw_half_a_conversation() = runTest(dispatcher) {
        val model = AiViewModel(Language.Turkish, FakeContentRepository(listOf(page(ANSWER, ANSWER_TABLET))))
        advanceUntilIdle()
        assertTrue(model.state.value.answers is LoadState.Failed)
    }

    @Test
    fun the_conversation_opens_whole_and_a_replay_shows_it_a_step_at_a_time() = runTest(dispatcher) {
        val model = AiViewModel(Language.Turkish, FakeContentRepository(emptyList()))
        advanceUntilIdle()
        assertEquals(ALL_STEPS, model.state.value.step, "a reader who never replays sees all of it")
        model.onAction(AiAction.Replay(steps = 6))
        runCurrent()
        assertEquals(0, model.state.value.step)
        advanceTimeBy(STEP + 1)
        assertEquals(1, model.state.value.step)
        // A replay in the middle starts again from the ask, with a phone's fewer steps.
        model.onAction(AiAction.Replay(steps = 5))
        runCurrent()
        assertEquals(0, model.state.value.step)
        advanceTimeBy(STEP * 4 + 1)
        assertEquals(4, model.state.value.step)
        advanceUntilIdle()
        assertEquals(4, model.state.value.step, "it stops at its last step")
    }

    @Test
    fun reading_llms_txt_opens_the_file_where_the_site_serves_it() = runTest(dispatcher) {
        val model = AiViewModel(Language.Turkish, FakeContentRepository(emptyList()))
        model.onAction(AiAction.ReadLlms)
        assertEquals(AiEffect.OpenUrl("${SiteAddress.origin}/llms.txt"), model.effects.first())
    }

    @Test
    fun opening_a_file_opens_its_address() = runTest(dispatcher) {
        val model = AiViewModel(Language.Turkish, FakeContentRepository(emptyList()))
        model.onAction(AiAction.Open(SiteAddress.json))
        assertEquals(AiEffect.OpenUrl("${SiteAddress.origin}/llms/pantrix-ui.json"), model.effects.first())
    }

    @Test
    fun the_release_s_files_come_from_its_page_and_the_documents_from_the_site() {
        val release = "https://github.com/${LibraryFacts.PACKAGES_REPOSITORY}/releases"
        val files = FilesSay.Files.associateBy { it.name }
        val jar = files.getValue("mcp-${LibraryFacts.RELEASE}-all.jar")
        val skill = files.getValue("pantrix-ui-skill-${LibraryFacts.RELEASE}.zip")
        assertEquals("$release/download/v${LibraryFacts.RELEASE}/${jar.name}", jar.url)
        assertEquals("$release/download/v${LibraryFacts.RELEASE}/${skill.name}", skill.url)
        assertEquals("$release/tag/v${LibraryFacts.RELEASE}", files.getValue("v${LibraryFacts.RELEASE}").url)
        // The two the release has are downloaded, with the line that uses each; the site's are opened and read.
        assertEquals(listOf(jar.name, skill.name), FilesSay.Files.filter { it.download }.map { it.name })
        listOf(jar, skill).forEach { file -> assertTrue(file.name in file.code.orEmpty(), file.name) }
        assertEquals("unzip ${skill.name} -d .claude/skills", skill.code)
        val site = FilesSay.Files.filterNot { it.url.startsWith(release) }
        assertEquals(
            listOf(SiteAddress.skill, SiteAddress.llms, SiteAddress.llmsFull, SiteAddress.json, SiteAddress.schema),
            site.map { it.url }
        )
        site.forEach { file -> assertTrue(file.url.endsWith("/${file.name}"), file.url) }
    }

    @Test
    fun the_steps_download_the_release_s_files_and_the_cards_open_the_site_s() {
        assertEquals(
            listOf(LibraryFacts.releaseFile(LibraryFacts.MCP_JAR), LibraryFacts.releaseFile(LibraryFacts.SKILL_ZIP)),
            SetupSay.Steps.mapNotNull { it.link }
        )
        SetupSay.Steps.filter { it.link != null }.forEach { step ->
            assertTrue(step.link.orEmpty().endsWith("/${step.code}"), step.code)
        }
        // The server is for an assistant, which a browser cannot read: its card names it and leads nowhere.
        assertEquals(
            listOf(null, SiteAddress.skill, SiteAddress.llms, SiteAddress.json),
            AiSay.Ways.map { it.link }
        )
        assertEquals("pantrix-ui.json", AiSay.Ways.last().where)
    }

    @Test
    fun picking_an_assistant_shows_its_setup() = runTest(dispatcher) {
        val model = AiViewModel(Language.Turkish, FakeContentRepository(emptyList()))
        model.onAction(AiAction.PickClient(2))
        assertEquals(2, model.state.value.client)
    }

    @Test
    fun each_setup_line_is_a_comment_or_code_and_every_setup_names_the_server_or_its_documents() {
        // The plugin brings the server at the site's address.
        val names =
            listOf("mcp-${LibraryFacts.RELEASE}-all.jar", SiteAddress.mcp, "pantrix-ui@pantrix-ui", "/llms.txt")
        SiteLayout.entries.forEach { layout ->
            val clients = SetupSay.Clients.filter { layout != SiteLayout.Phone || it.onPhone }
            clients.forEach { client ->
                val lines = client.lines.of(layout)
                lines.forEach { line ->
                    assertTrue(line.comment == null || line.code.isEmpty(), client.name.desktop.english)
                    line.comment?.let { assertTrue(it.english.startsWith("# ") && it.turkish.startsWith("# ")) }
                }
                // The server by its address, its jar or the plugin, or the documents without one.
                assertTrue(
                    lines.any { line -> names.any { it in line.code } },
                    "$layout ${client.name.desktop.english}"
                )
            }
        }
        assertEquals(3, SetupSay.Clients.count { it.onPhone }, "a phone has three setups")
    }

    @Test
    fun the_page_says_everything_in_both_languages_in_every_size() {
        val sized: List<BySize<Words>> = AiSay.Stats.map { it.second } + AiSay.ToolCalls.map { it.result } +
            AiSay.Checks + AiSay.Ways.map { it.body } + SetupSay.Clients.flatMap { listOf(it.name, it.note) } +
            ServerSay.Tools.map { it.second } + ServerSay.Rules.flatMap { it.toList() } +
            listOf(AiSay.Conversation, AiSay.WaysLead, SetupSay.SetupTitle, SetupSay.SetupLead) +
            AiSay.Lead.let { lead ->
                listOf(BySize(lead.desktop.first), BySize(lead.tablet.first), BySize(lead.phone.first))
            }
        val words: List<Words> = sized.flatMap { size -> SiteLayout.entries.map { size.of(it) } } +
            AiSay.Ways.map { it.title } + AiSay.Give.flatMap { listOfNotNull(it.case, it.start, it.end) } +
            SetupSay.Steps.map { it.words } + ServerSay.PromptList.map { it.second } + ServerSay.Drift.desktop +
            ServerSay.Drift.tablet + listOf(ServerSay.DriftPhoneStart, ServerSay.DriftPhone) +
            FilesSay.Files.flatMap { listOf(it.title, it.size, it.how) } +
            listOf(FilesSay.Title, FilesSay.Download, FilesSay.Open)
        words.forEach { assertTrue(it.english.isNotBlank() && it.turkish.isNotBlank(), it.english) }
    }

    @Test
    fun a_smaller_size_says_it_shorter_or_the_same() {
        val sized = ServerSay.Tools.map { it.second } + AiSay.Ways.map { it.body } + ServerSay.Rules.map { it.second }
        sized.forEach { words ->
            assertTrue(words.tablet.english.length <= words.desktop.english.length, words.tablet.english)
            assertTrue(words.phone.english.length <= words.tablet.english.length, words.phone.english)
        }
    }

    private companion object {
        const val STEP = 700L
    }
}
