package com.pantrixui.site.tools.prerender

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Token
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The static pages: their words in HTML, their head in the app's `index.html`, their files, and `llms.txt`. */
class StaticPagesTest {
    @Test
    fun blocks_are_written_as_plain_html_and_a_link_of_the_site_keeps_the_page_s_language() {
        val html = Html(Language.Turkish)
        val written = html.blocks(
            listOf(
                Block.Heading(2, "why", listOf(Span.Text("Neden <ve> nasıl"))),
                Block.Paragraph(
                    listOf(
                        Span.Text("Bak: ", strong = true),
                        Span.Link(listOf(Span.Text("kurulum")), "/docs/installation#steps"),
                        Span.Code("a & b")
                    )
                ),
                Block.Code("kotlin", "App.kt", listOf(listOf(Token("val x = \"<\""))))
            )
        )
        assertTrue("<h2 id=\"why\">Neden &lt;ve&gt; nasıl</h2>" in written, written)
        assertTrue("<a href=\"/tr/docs/installation#steps\">kurulum</a>" in written, written)
        assertTrue("<strong>Bak: </strong>" in written, written)
        assertTrue("<code>a &amp; b</code>" in written, written)
        assertTrue("<pre><code class=\"language-kotlin\">val x = &quot;&lt;&quot;</code></pre>" in written, written)
        assertEquals("https://example.com/x", Html(Language.Turkish).addressOf("https://example.com/x"))
    }

    @Test
    fun a_page_s_file_has_its_head_its_other_language_and_its_words_before_the_app() {
        val document = StaticDocument(TEMPLATE, "https://pantrixui.com")
        val english = page(Language.English, "Forms")
        val turkish = page(Language.Turkish, "Formlar")
        val file = document.of(turkish, english)
        assertTrue("<html lang=\"tr\">" in file, file)
        assertTrue("<title>Formlar · Pantrix UI</title>" in file, file)
        assertTrue("<meta name=\"description\" content=\"Formlar&#39;ın açıklaması\">" in file, file)
        assertTrue("<link rel=\"canonical\" href=\"https://pantrixui.com/tr/docs/forms\">" in file, file)
        assertTrue("hreflang=\"en\" href=\"https://pantrixui.com/docs/forms\"" in file, file)
        assertTrue("hreflang=\"x-default\" href=\"https://pantrixui.com/docs/forms\"" in file, file)
        assertTrue("content=\"https://pantrixui.com/og/tr/docs/forms.png\"" in file, file)
        assertTrue(file.indexOf("<div id=\"static\">") < file.indexOf("<!-- The app. -->"), file)
        assertTrue("<a href=\"/docs/forms\" hreflang=\"en\">English</a>" in file, file)
        val missing = document.notFound(Language.English)
        assertTrue("<meta name=\"robots\" content=\"noindex\">" in missing, missing)
    }

    @Test
    fun every_page_has_a_file_at_its_address_and_a_line_of_the_sitemap_in_each_language() {
        val out = createTempDirectory("static").toFile()
        val pages = Language.entries.associateWith { listOf(page(it, it.name)) }
        val written = StaticSite(
            pages,
            StaticDocument(TEMPLATE, "https://pantrixui.com"),
            "https://pantrixui.com"
        ).write(out)
        assertEquals(2, written)
        assertTrue(File(out, "docs/forms/index.html").isFile)
        assertTrue(File(out, "tr/docs/forms/index.html").isFile)
        assertTrue(File(out, "404.html").isFile)
        val sitemap = File(out, "sitemap.xml").readText()
        assertEquals(2, Regex("<loc>").findAll(sitemap).count(), sitemap)
        assertTrue("Sitemap: https://pantrixui.com/sitemap.xml" in File(out, "robots.txt").readText())
        assertEquals(File(out, "index.html"), StaticSite.fileOf(out, "/"))
        assertEquals(File(out, "tr/index.html"), StaticSite.fileOf(out, "/tr"))
        out.deleteRecursively()
    }

    @Test
    fun llms_txt_leads_to_the_site_s_pages_and_to_its_own_copy_of_the_rest() {
        val llms = LlmsFiles(File("unused.jar"), "https://pantrixui.com")
        val pages = mapOf("text-field" to "text-field")
        assertEquals("https://pantrixui.com/components/text-field", llms.addressOf("pages/text-field.md", pages))
        assertEquals("https://pantrixui.com/foundations#colour", llms.addressOf("pages/colors.md", pages))
        assertEquals("https://pantrixui.com/docs/guides/files", llms.addressOf("guides/files.md", pages))
        assertEquals("https://pantrixui.com/changelog", llms.addressOf("CHANGELOG.md", pages))
        assertEquals("https://pantrixui.com/llms/api/components.md", llms.addressOf("api/components.md", pages))
        assertEquals("https://example.com/a", llms.addressOf("https://example.com/a", pages))
        val text = "- [Colours](pages/colors.md)\n```\n[not](pages/colors.md)\n```"
        val relinked = llms.relinked(text, pages)
        assertTrue("[Colours](https://pantrixui.com/foundations#colour)" in relinked, relinked)
        assertTrue("[not](pages/colors.md)" in relinked, relinked)
        assertFalse("llms/pages/colors" in relinked, relinked)
    }

    private fun page(
        language: Language,
        title: String
    ) = StaticPage(Route.Doc("forms"), language, title, "$title'ın açıklaması", "Docs", "<h1>$title</h1>")

    private companion object {
        val TEMPLATE =
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <title>Pantrix UI</title>
                <meta name="description" content="The app.">
            </head>
            <body>
                <!-- The app. -->
                <div id="app"></div>
            </body>
            </html>
            """.trimIndent()
    }
}
