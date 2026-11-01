package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.PlatformKind
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** The apps built with Pantrix UI, read from one file in each language. */
class ReferencesReaderTest {
    /** The repository, above the module's own folder. */
    private val repository = generateSequence(File("").absoluteFile) { it.parentFile }
        .first { File(it, "content/${ReferencesReader.FILE}").isFile }

    @Test
    fun a_text_said_in_each_language_is_read_in_one_and_the_rest_as_it_is() {
        val text = """
            { "projects": [ { "key": "notes", "name": "Notes", "address": "notes.app",
              "line": { "en": "Notes on every device.", "tr": "Her cihazda notlar." },
              "about": { "en": "About.", "tr": "Hakkında." }, "url": "https://notes.app",
              "platforms": [ { "kind": "Ios", "name": "iOS", "detail": { "en": "iPhone", "tr": "iPhone" } } ] } ] }
        """
        val english = ReferencesReader.read(text, Language.English).projects.single()
        val turkish = ReferencesReader.read(text, Language.Turkish).projects.single()
        assertEquals("Notes on every device." to "Her cihazda notlar.", english.line to turkish.line)
        assertEquals("Notes" to "notes.app", turkish.name to turkish.address)
        assertEquals(PlatformKind.Ios, turkish.platforms.single().kind)
        assertEquals(null, turkish.source, "an app whose source is not open")
    }

    @Test
    fun a_text_missing_in_a_language_stops_the_build() {
        val text = """{ "projects": [ { "key": "a", "name": "A", "address": "a", "url": "a",
            "line": { "en": "One line." }, "about": "About." } ] }"""
        // A text in one language alone is no text in each, and a line is no object: the reading fails.
        assertFailsWith<IllegalArgumentException> { ReferencesReader.read(text, Language.Turkish) }
    }

    /** The site's own file is read in both languages, the site first, its texts in the language asked for. */
    @Test
    fun the_site_s_file_is_read_in_both_languages() {
        val folder = File(repository, "content")
        val english = ReferencesReader.read(folder, Language.English).projects
        val turkish = ReferencesReader.read(folder, Language.Turkish).projects
        assertEquals("pantrix-ui-site", english.first().key)
        assertEquals(english.map { it.key }, turkish.map { it.key })
        assertTrue(turkish.first().about != english.first().about, "the Turkish about is the English one")
        assertEquals(setOf("en", "tr"), english.first().languages.toSet())
    }

    /**
     * The components the site's entry names are the ones its own screens are drawn with: each is called somewhere in
     * the sources of its app, its parts and its pages, the examples and the previews left out.
     */
    @Test
    fun every_component_the_site_names_is_one_its_screens_draw_with() {
        val site = ReferencesReader.read(File(repository, "content"), Language.English).projects.first()
        val sources = listOf("app", "core", "feature").map { File(repository, it) }
            .flatMap { root -> root.walkTopDown().filter { it.isScreenSource() }.toList() }
            .joinToString("\n") { it.readText() }
        val missing = site.components.filterNot { name -> Regex("""\b$name\s*[({]""").containsMatchIn(sources) }
        assertEquals(emptyList(), missing, "named in ${ReferencesReader.FILE} and drawn with nowhere")
        assertEquals(site.components.size, site.components.toSet().size, "a component named twice")
    }

    /** A file of a screen's code: Kotlin, in a main source set, and no preview. */
    private fun File.isScreenSource(): Boolean = isFile && extension == "kt" && "/build/" !in path &&
        "/src/commonMain/" in invariantSeparatorsPath && !name.endsWith("Previews.kt")
}
