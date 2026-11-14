package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ContentFiles
import com.pantrixui.site.core.model.ContentsEntry
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Tone
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PageReaderTest {
    private fun read(source: String) = PageReader(Language.English, "/docs/test").read(source)

    @Test
    fun the_head_gives_the_title_the_description_and_the_section() {
        val page = read("---\ntitle: Installation\ndescription: Add it.\nsection: Getting started\n---\nText.")
        assertEquals("Installation", page.title)
        assertEquals("Add it.", page.description)
        assertEquals("Getting started", page.section)
        assertEquals(listOf(Block.Paragraph(listOf(Span.Text("Text.")))), page.blocks)
    }

    @Test
    fun a_paragraph_keeps_its_code_its_links_and_its_bold() {
        val page = read("---\ntitle: T\n---\nIn `a.kts`, see **the [guide](/docs/guides/x)** now.")
        assertEquals(
            listOf(
                Span.Text("In "),
                Span.Code("a.kts"),
                Span.Text(", see "),
                Span.Text("the ", strong = true),
                Span.Link(listOf(Span.Text("guide")), "/docs/guides/x"),
                Span.Text(" now.")
            ),
            (page.blocks.single() as Block.Paragraph).text
        )
    }

    @Test
    fun steps_are_numbered_in_the_contents_by_their_short_names() {
        val page = read(
            """
            ---
            title: T
            ---
            ## What it needs {#requirements}
            :::steps
            ## Add the repository {#repository toc="The repository"}
            One.
            ## Give it your brand {#preset optional}
            Two.
            :::
            """.trimIndent()
        )
        val steps = page.blocks.filterIsInstance<Block.Steps>().single().steps
        assertEquals(listOf("repository", "preset"), steps.map { it.anchor })
        assertEquals(listOf(false, true), steps.map { it.optional })
        assertEquals(
            listOf(
                ContentsEntry("requirements", "What it needs"),
                ContentsEntry("repository", "1. The repository"),
                ContentsEntry("preset", "2. Give it your brand")
            ),
            page.contents
        )
    }

    @Test
    fun tabs_hold_their_code_and_a_callout_inside_one() {
        val page = read(
            """
            ---
            title: T
            ---
            :::tabs
            ### One
            ```kotlin settings.gradle.kts
            maven { name = "A" }
            ```
            :::callout warning
            **Careful.** Text.
            :::
            ### Two
            ```kotlin
            mavenLocal()
            ```
            :::
            """.trimIndent()
        )
        val tabs = page.blocks.single() as Block.Tabs
        assertEquals(listOf("One", "Two"), tabs.tabs.map { it.label })
        val code = tabs.tabs[0].blocks[0] as Block.Code
        assertEquals("settings.gradle.kts", code.file)
        assertEquals(
            Token("\"A\"", TokenKind.StringLiteral),
            code.lines.single().first {
                it.kind ==
                    TokenKind.StringLiteral
            }
        )
        val callout = assertIs<Block.Callout>(tabs.tabs[0].blocks[1])
        assertEquals(Tone.Warning, callout.tone)
        assertEquals(1, tabs.tabs[1].blocks.size)
    }

    @Test
    fun facts_rows_cards_and_a_captioned_table_are_read_from_their_lists() {
        val page = read(
            """
            ---
            title: T
            ---
            :::facts
            - Kotlin: 2.4.21 or newer
            :::
            :::rows
            - **iOS**: The `bridge` comes with it.
            :::
            :::cards
            - [Theming](/foundations): Colour roles.
            :::
            :::table The other modules
            - `com.pantrixui:testing`: Finding by tag
            :::
            """.trimIndent()
        )
        val facts = page.blocks[0]
        val rows = page.blocks[1]
        val cards = page.blocks[2]
        val table = page.blocks[3]
        assertEquals("2.4.21 or newer", (facts as Block.Facts).facts.single().value)
        assertEquals("iOS", (rows as Block.Rows).rows.single().label)
        assertEquals(
            listOf(Span.Text("The "), Span.Code("bridge"), Span.Text(" comes with it.")),
            rows.rows.single().text
        )
        assertEquals("/foundations", (cards as Block.Cards).cards.single().href)
        assertEquals("Colour roles.", cards.cards.single().text)
        val modules = table as Block.Table
        assertEquals("The other modules", modules.caption)
        assertEquals(listOf(Span.Code("com.pantrixui:testing")), modules.rows.single().first())
    }

    @Test
    fun the_site_s_own_pages_are_read_in_both_languages() {
        // The repository's content folder, above the module's own, which is also called "content".
        val folder = generateSequence(File("").absoluteFile) { it.parentFile }
            .map { File(it, "content") }
            .first { File(it, "en").isDirectory }
        val pages = SitePages.read(folder, SITE)
        val installation = pages.filter { it.path == "/docs/installation" }
        assertEquals(setOf(Language.English, Language.Turkish), installation.map { it.language }.toSet())
        assertTrue(
            installation.all { page -> page.contents.size == 7 },
            "the contents of Installation: ${installation.map { it.contents }}"
        )
        // The landing page's code, a block under each heading its screen looks for.
        val landing = pages.filter { it.path == "/" }
        assertEquals(setOf(Language.English, Language.Turkish), landing.map { it.language }.toSet())
        landing.forEach { page ->
            val anchors = listOf("review-form", "review-form-wide", "review-form-short", "preset")
            anchors.forEach { anchor ->
                val heading = page.blocks.indexOfFirst { it is Block.Heading && it.anchor == anchor }
                val code = page.blocks.getOrNull(heading + 1) as? Block.Code
                assertTrue(heading >= 0 && code?.language == "kotlin", "#$anchor in ${page.language}")
            }
        }
        // The site's own address, named as {{site}}, is written as the build gives it, in the text and in the code.
        assertTrue(pages.none { SitePages.SITE in it.markdown.orEmpty() })
        // The library's counts, named as {{components}} and {{groups}}, are written too: the groups as a word.
        assertTrue(pages.none { "{{" in it.markdown.orEmpty() || "{{" in it.description })
        val introduction = pages.filter { it.path == "/docs/introduction" }.associateBy { it.language }
        val count = LibraryFacts.COMPONENTS
        assertTrue("Components: $count, in eight groups" in introduction.getValue(Language.English).markdown.orEmpty())
        assertTrue("Bileşenler: $count, sekiz grupta" in introduction.getValue(Language.Turkish).markdown.orEmpty())
        val assistants = pages.filter { it.path == "/docs/ai-assistants" }
        assertEquals(2, assistants.size)
        assistants.forEach { page ->
            assertTrue("claude mcp add -t http pantrix-ui $SITE/mcp" in page.markdown.orEmpty(), page.markdown)
            val written = ContentFiles.json.encodeToString(Page.serializer(), page)
            assertTrue("pantrix-ui $SITE/mcp" in written, "the page's blocks give the address too")
        }
    }

    private companion object {
        /** An address no page names, so that each place a page gives the site's own is seen. */
        const val SITE = "https://site.example"
    }
}
