package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.model.AskComponent
import com.pantrixui.site.core.model.AskScope
import com.pantrixui.site.core.model.ComponentHit
import com.pantrixui.site.core.model.ComponentHits
import com.pantrixui.site.core.model.ComponentSnippet
import com.pantrixui.site.core.model.DeclarationDetail
import com.pantrixui.site.core.model.DeclarationHit
import com.pantrixui.site.core.model.DeclarationHits
import com.pantrixui.site.core.model.DeclarationPart
import com.pantrixui.site.core.model.DocumentHit
import com.pantrixui.site.core.model.DocumentHits
import com.pantrixui.site.core.model.ExampleList
import com.pantrixui.site.core.model.ExampleNamed
import com.pantrixui.site.core.model.IconHit
import com.pantrixui.site.core.model.IconHits
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.testing.FakeAskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The page that asks the server: which tools a question calls, in which order and language, and what it shows. */
@OptIn(ExperimentalCoroutinesApi::class)
class AskTest {
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
    fun a_question_finds_the_component_and_the_documents_then_the_best_one_s_card() = runTest(dispatcher) {
        val server = server()
        val model = AskViewModel(Language.Turkish, server)
        model.onAction(AskAction.Ask("  bildirim "))
        assertEquals(AskPhase.Searching, model.state.value.phase)
        advanceUntilIdle()
        val state = model.state.value
        assertEquals(AskPhase.Answered, state.phase)
        assertEquals("bildirim", state.asked)
        assertEquals(
            listOf(
                "search_components(bildirim, tr)",
                "search_documentation(bildirim, tr)",
                "get_component_metadata(PantrixSnackbarHost, tr)",
                "list_examples(PantrixSnackbarHost, tr)"
            ),
            server.calls
        )
        assertEquals(
            listOf(
                AskResult.Components(2, "Snackbar"),
                AskResult.Documents(1, listOf("Snackbar")),
                AskResult.Card,
                AskResult.Examples(2)
            ),
            state.steps.map { it.result }
        )
        val answer = assertIs<AskAnswer.Component>(state.answer)
        assertEquals("PantrixSnackbarHost", answer.shown)
        assertEquals(SNACKBAR_CARD, answer.card)
        assertEquals(1, answer.documents?.documents?.size)
    }

    @Test
    fun another_component_the_search_found_shows_its_own_card() = runTest(dispatcher) {
        val server = server()
        val model = AskViewModel(Language.English, server)
        model.onAction(AskAction.Ask("toast"))
        advanceUntilIdle()
        model.onAction(AskAction.ShowComponent("PantrixAlert"))
        val loading = assertIs<AskAnswer.Component>(model.state.value.answer)
        assertNull(loading.card)
        advanceUntilIdle()
        val answer = assertIs<AskAnswer.Component>(model.state.value.answer)
        assertEquals("PantrixAlert", answer.shown)
        assertEquals("Alert", answer.card?.name)
        assertEquals(AskPhase.Answered, model.state.value.phase)
        assertEquals(6, model.state.value.steps.size)
    }

    @Test
    fun a_question_that_finds_nothing_says_so_and_one_that_finds_only_documents_shows_them() = runTest(dispatcher) {
        val empty = AskViewModel(Language.English, FakeAskRepository())
        empty.onAction(AskAction.Ask("spaceship"))
        advanceUntilIdle()
        assertEquals(AskPhase.Nothing, empty.state.value.phase)
        assertNull(empty.state.value.answer)
        val documents = AskViewModel(Language.English, FakeAskRepository(documents = DOCUMENTS))
        documents.onAction(AskAction.Ask("fold"))
        advanceUntilIdle()
        assertIs<AskAnswer.Documents>(documents.state.value.answer)
        assertEquals(AskPhase.Answered, documents.state.value.phase)
    }

    @Test
    fun a_question_the_server_does_not_answer_says_so_and_is_asked_again() = runTest(dispatcher) {
        val server = server().apply { failing = true }
        val model = AskViewModel(Language.English, server)
        model.onAction(AskAction.Ask("toast"))
        advanceUntilIdle()
        val failed = model.state.value
        assertEquals(AskPhase.Failed, failed.phase)
        assertTrue(failed.steps.isNotEmpty() && failed.steps.all { it.result != null })
        assertEquals(AskResult.NoAnswer, failed.steps.last().result)
        server.failing = false
        model.onAction(AskAction.Retry)
        advanceUntilIdle()
        assertEquals(AskPhase.Answered, model.state.value.phase)
        assertEquals("toast", model.state.value.asked)
    }

    @Test
    fun the_api_opens_its_first_declaration_and_each_row_once_it_is_opened() = runTest(dispatcher) {
        val server = server()
        val model = AskViewModel(Language.English, server)
        model.onAction(AskAction.Try("snackbar", AskScope.Api))
        advanceUntilIdle()
        assertEquals(AskScope.Api, model.state.value.scope)
        val first = assertIs<AskAnswer.Api>(model.state.value.answer)
        assertEquals("SnackbarDuration", first.open)
        assertEquals(
            listOf("Short", "Long"),
            first.details.getValue("SnackbarDuration").single().entries.map {
                it.name
            }
        )
        model.onAction(AskAction.ToggleDeclaration("SnackbarResult"))
        advanceUntilIdle()
        val second = assertIs<AskAnswer.Api>(model.state.value.answer)
        assertEquals("SnackbarResult", second.open)
        assertTrue("SnackbarResult" in second.details)
        model.onAction(AskAction.ToggleDeclaration("SnackbarResult"))
        assertNull(assertIs<AskAnswer.Api>(model.state.value.answer).open)
        model.onAction(AskAction.MoreDeclarations)
        advanceUntilIdle()
        assertEquals("search_api(snackbar, en)", server.calls.last())
    }

    @Test
    fun a_scope_asks_the_question_again_and_a_blank_question_clears_the_page() = runTest(dispatcher) {
        val server = server()
        val model = AskViewModel(Language.English, server)
        model.onAction(AskAction.PickScope(AskScope.Icons))
        assertEquals(AskScope.Icons, model.state.value.scope)
        assertTrue(server.calls.isEmpty(), "nothing is asked before a question")
        model.onAction(AskAction.Ask("arrow"))
        advanceUntilIdle()
        assertEquals(listOf("search_icons(arrow, en)"), server.calls)
        assertIs<AskAnswer.Icons>(model.state.value.answer)
        model.onAction(AskAction.PickScope(AskScope.Documents))
        advanceUntilIdle()
        assertEquals("search_documentation(arrow, en)", server.calls.last())
        assertIs<AskAnswer.Documents>(model.state.value.answer)
        model.onAction(AskAction.Ask(" "))
        assertEquals(AskPhase.Empty, model.state.value.phase)
        assertNull(model.state.value.asked)
    }

    @Test
    fun the_tabs_the_tools_and_the_ways_out_of_the_page() = runTest(dispatcher) {
        val model = AskViewModel(Language.English, server())
        model.onAction(AskAction.PickTab(AskTab.Key))
        assertEquals(AskTab.Key, model.state.value.tab)
        model.onAction(AskAction.ToggleTools)
        assertTrue(model.state.value.toolsOpen)
        model.onAction(AskAction.Ask("toast"))
        assertEquals(AskTab.Server, model.state.value.tab, "a question shows the server's answer")
        model.onAction(AskAction.Open("/components/snackbar#examples"))
        assertEquals(AskEffect.Navigate("/components/snackbar#examples"), model.effects.first())
        model.onAction(AskAction.Copy("import x"))
        assertEquals(AskEffect.Copy("import x"), model.effects.first())
    }

    @Test
    fun a_document_leads_to_its_page_its_guide_or_the_reference_the_site_serves() {
        fun hit(
            key: String,
            kind: String
        ) = DocumentHit(key = key, kind = kind, title = key, summary = "")
        assertEquals(
            AskAction.Open("/tr/components/snackbar"),
            documentAction(hit("snackbar", "page"), Language.Turkish)
        )
        assertEquals(AskAction.Open("/foundations#colour"), documentAction(hit("colors", "page"), Language.English))
        assertEquals(AskAction.Open("/docs/guides/ai"), documentAction(hit("guide/ai", "guide"), Language.English))
        val reference = assertIs<AskAction.OpenUrl>(documentAction(hit("skill/forms", "reference"), Language.English))
        assertTrue(reference.url.endsWith("/llms/skills/pantrix-ui/references/forms.md"), reference.url)
        assertTrue(cursorLink().startsWith("cursor://anysphere.cursor-deeplink/mcp/install?name=pantrix-ui&config="))
        assertTrue(vsCodeCommand().startsWith("code --add-mcp '{\"name\":\"pantrix-ui\",\"type\":\"http\",\"url\":\""))
    }

    private fun server() = FakeAskRepository(
        components = ComponentHits(2, listOf(SNACKBAR, ALERT)),
        cards = mapOf("PantrixSnackbarHost" to SNACKBAR_CARD, "PantrixAlert" to ALERT_CARD),
        examples = mapOf("PantrixSnackbarHost" to EXAMPLES, "PantrixAlert" to EXAMPLES),
        declarations = DECLARATIONS,
        details = mapOf(
            "SnackbarDuration" to listOf(
                DeclarationDetail(
                    name = "SnackbarDuration",
                    kind = "enum",
                    signature = "enum class SnackbarDuration",
                    entries = listOf(DeclarationPart("Short"), DeclarationPart("Long"))
                )
            ),
            "SnackbarResult" to
                listOf(
                    DeclarationDetail(name = "SnackbarResult", kind = "enum", signature = "enum class SnackbarResult")
                )
        ),
        icons = IconHits(1, listOf(IconHit("ArrowBack", "PantrixIcons.ArrowBack", "Back.", mirrored = true))),
        documents = DocumentHits(1, listOf(DocumentHit("snackbar", "page", "Snackbar", "A short message.")))
    )

    private companion object {
        val SNACKBAR = ComponentHit(
            name = "Snackbar",
            function = "PantrixSnackbarHost",
            category = "overlay",
            page = "snackbar",
            description = "A short message.",
            keywords = listOf("toast"),
            import = "com.pantrixui.components.overlay.snackbar.PantrixSnackbarHost",
            artifact = "com.pantrixui:components"
        )
        val ALERT = SNACKBAR.copy(name = "Alert", function = "PantrixAlert", page = "alert")
        val SNACKBAR_CARD = AskComponent(
            name = "Snackbar",
            function = "PantrixSnackbarHost",
            category = "overlay",
            description = "A short message.",
            import = SNACKBAR.import,
            artifact = SNACKBAR.artifact,
            page = "snackbar",
            snippets = listOf(ComponentSnippet("Undo", "PantrixSnackbarHost(snackbars)"))
        )
        val ALERT_CARD = SNACKBAR_CARD.copy(name = "Alert", function = "PantrixAlert", page = "alert")
        val EXAMPLES = ExampleList(examples = listOf(ExampleNamed("One", "One"), ExampleNamed("Two", "Two")))
        val DECLARATIONS = DeclarationHits(
            15,
            listOf(
                DeclarationHit(
                    "SnackbarDuration",
                    "SnackbarDuration",
                    "enum",
                    "components",
                    "com.pantrixui:components",
                    "com.pantrixui.components.overlay.snackbar",
                    "com.pantrixui.components.overlay.snackbar.SnackbarDuration",
                    "How long a snackbar stays."
                ),
                DeclarationHit(
                    "SnackbarResult",
                    "SnackbarResult",
                    "enum",
                    "components",
                    "com.pantrixui:components",
                    "com.pantrixui.components.overlay.snackbar",
                    "com.pantrixui.components.overlay.snackbar.SnackbarResult",
                    "How a snackbar left."
                )
            )
        )
        val DOCUMENTS =
            DocumentHits(1, listOf(DocumentHit("adaptive-window", "page", "Adaptive window", "The window.")))
    }
}
