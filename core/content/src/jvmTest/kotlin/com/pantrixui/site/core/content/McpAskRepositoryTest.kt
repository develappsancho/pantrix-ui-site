package com.pantrixui.site.core.content

import com.pantrixui.mcp.PantrixMcp
import com.pantrixui.site.core.model.Language
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * The page's calls read against the library's own MCP server, of the version the site is built on, run here: what the
 * server answers is what the page is drawn from, in both languages.
 */
class McpAskRepositoryTest {
    private val mcp = PantrixMcp.http()
    private val repository = McpAskRepository(address = "/mcp") { _, body, headers ->
        mcp.answer("POST", headers + ("Content-Type" to JSON), body).takeIf { it.status == OK }?.body
    }

    @Test
    fun a_component_is_found_by_its_words_in_either_language_and_said_in_the_one_asked_for() = runTest {
        val english = repository.searchComponents("toast", Language.English, 5)
        assertEquals("PantrixSnackbarHost", english.components.first().function)
        assertTrue(english.components.first().description.startsWith("A short message"))
        val turkish = repository.searchComponents("bildirim", Language.Turkish, 5)
        val snackbar = turkish.components.first()
        assertEquals("snackbar", snackbar.page)
        assertTrue(snackbar.description.startsWith("Uygulamanın"), snackbar.description)
        assertEquals("com.pantrixui.components.overlay.snackbar.PantrixSnackbarHost", snackbar.import)
        val sentence = repository.searchComponents("a comment box with several lines", Language.English, 5)
        assertEquals("PantrixTextArea", sentence.components.first().function)
        assertTrue(sentence.total > sentence.components.size)
    }

    @Test
    fun a_component_s_card_has_its_short_examples_and_its_page_its_examples() = runTest {
        val card = repository.component("PantrixTextArea", Language.English)
        assertEquals("TextArea", card.name)
        assertEquals("com.pantrixui:components", card.artifact)
        assertTrue(card.snippets.isNotEmpty() && card.snippets.first().code.isNotBlank())
        val examples = repository.examples("PantrixTextArea", Language.Turkish)
        assertEquals("text-area", examples.page?.key)
        assertTrue(examples.examples.isNotEmpty())
    }

    @Test
    fun the_api_the_icons_and_the_documents_are_found_and_a_declaration_given_in_full() = runTest {
        val api = repository.searchApi("snackbar", Language.English, 5)
        assertTrue(api.declarations.any { it.title == "SnackbarDuration" }, api.declarations.toString())
        val duration = repository.declaration("SnackbarDuration", Language.Turkish).single()
        assertEquals(listOf("Short", "Long", "UntilDismissed"), duration.entries.map { it.name })
        assertTrue("snackbar" in duration.description, duration.description)
        val icons = repository.searchIcons("kapat", Language.Turkish, 5)
        assertEquals("PantrixIcons.Close", icons.icons.first().reference)
        val documents = repository.searchDocumentation("toast", Language.English, 3)
        assertEquals("snackbar", documents.documents.first().key)
        assertTrue(documents.documents.all { it.kind in setOf("page", "guide", "reference") })
    }

    @Test
    fun an_answer_that_is_an_error_is_a_failure() = runTest {
        assertFailsWith<AskFailure> { repository.component("Buton", Language.English) }
    }

    private companion object {
        const val OK = 200
    }
}
