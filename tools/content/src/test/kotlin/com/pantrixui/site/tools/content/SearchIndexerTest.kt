package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ComponentCard
import com.pantrixui.site.core.model.ComponentGroup
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.ContentsEntry
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.SearchKind
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Step
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchIndexerTest {
    private fun text(words: String) = listOf(Span.Text(words))

    private val installation = Page(
        path = "/docs/installation",
        language = Language.English,
        title = "Installation",
        description = "Add Pantrix to a project. It takes a minute.",
        blocks = listOf(
            Block.Heading(2, "requirements", text("What it needs")),
            Block.Steps(
                listOf(
                    Step(
                        "repository",
                        "Add the repository",
                        blocks = listOf(Block.Paragraph(text("Add mavenLocal. Then sync.")))
                    )
                )
            ),
            Block.Heading(2, "platforms", text("On each platform")),
            Block.Paragraph(
                text(
                    "Every target is built the same way, with no extra step on any platform at all " +
                        "for anyone who builds it today"
                )
            )
        ),
        contents = listOf(
            ContentsEntry("requirements", "What it needs"),
            ContentsEntry("repository", "1. The repository"),
            ContentsEntry("platforms", "On each platform")
        )
    )

    @Test
    fun a_part_says_its_first_sentence_a_step_s_own_and_the_page_s_where_it_opens_with_none() {
        assertEquals("Add mavenLocal.", SearchIndexer.firstSentenceUnder(installation, "repository"))
        assertEquals(
            "Add mavenLocal.",
            SearchIndexer.firstSentenceUnder(installation, "requirements"),
            "a heading over steps"
        )
        val long = SearchIndexer.firstSentenceUnder(installation, "platforms")
        assertTrue(long.endsWith("…") && long.length <= 97, long)
        assertEquals("Add Pantrix to a project.", SearchIndexer.firstSentenceUnder(installation, "nowhere"))
    }

    @Test
    fun an_extension_is_named_with_its_receiver() {
        assertEquals(
            "Modifier.clearFocusOnTap",
            SearchIndexer.titleOf("clearFocusOnTap", "fun Modifier.clearFocusOnTap(): Modifier")
        )
        assertEquals(
            "FormState.field",
            SearchIndexer.titleOf("field", "fun <T> FormState.field(id: String): FormField<T>")
        )
        assertEquals(
            "PantrixButton",
            SearchIndexer.titleOf("PantrixButton", "@Composable\nfun PantrixButton(text: String)")
        )
    }

    @Test
    fun the_index_leads_a_component_s_declarations_to_its_page_and_others_to_where_the_site_shows_them() {
        val json =
            """
            {
              "components": [ { "name": "TextArea", "function": "PantrixTextArea", "page": "text-area" } ],
              "declarations": [
                { "name": "PantrixTextArea", "kind": "function", "composable": true, "package": "a.b", "pages": ["text-area"] },
                { "name": "TextLimit", "kind": "enum", "package": "a.c", "pages": ["text-area", "text-field"] },
                { "name": "PantrixColors", "kind": "class", "package": "a.d", "pages": ["colors"] },
                { "name": "Lonely", "kind": "class", "package": "a.e", "pages": [] }
              ]
            }
            """.trimIndent()
        val card = ComponentCard("text-area", "Text Area", "PantrixTextArea", emptyList(), "inputs")
        val index = SearchIndexer(
            json,
            pages = listOf(installation),
            indexes = mapOf(
                Language.English to ComponentIndex(Language.English, listOf(ComponentGroup("inputs", listOf(card))))
            ),
            components = mapOf(Language.English to emptyList())
        ).index(Language.English)
        val api = index.entries.filter { it.kind == SearchKind.Api }.associate { it.title to (it.path to it.anchor) }
        assertEquals(
            mapOf(
                "PantrixTextArea" to ("/components/text-area" to "api"),
                "TextLimit" to ("/components/text-area" to "api"),
                "PantrixColors" to ("/foundations" to "colour")
            ),
            api,
            "a declaration the site has no page for is left out"
        )
        assertEquals("Component · Inputs", index.entries.first { it.kind == SearchKind.Component }.sub)
        val sections = index.entries.filter { it.kind == SearchKind.Section && it.path == "/docs/installation" }
        assertEquals(
            listOf(
                "Installation",
                "Installation › What it needs",
                "Installation › 1. The repository",
                "Installation › On each platform"
            ),
            sections.map { it.title },
            "the page itself, then its parts"
        )
        assertEquals("Add Pantrix to a project.", sections.first().sub)
        assertTrue(
            index.entries.any {
                it.title == "Roadmap" && it.path == "/roadmap"
            },
            "the site's own pages are found"
        )
    }
}
