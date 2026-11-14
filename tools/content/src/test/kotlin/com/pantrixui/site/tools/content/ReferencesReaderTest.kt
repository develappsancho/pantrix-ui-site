package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.PlatformKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The apps built with Pantrix UI, read from one file in each language. */
class ReferencesReaderTest {
    /** The repository, above the module's own folder. */
    private val repository = generateSequence(File("").absoluteFile) { it.parentFile }
        .first { File(it, "content/${ReferencesReader.FILE}").isFile }

    /** The library's components, by their composables, as the `com.pantrixui:mcp` jar on the tests' path lists them. */
    private val library: List<String> by lazy {
        val text = checkNotNull(javaClass.classLoader.getResource("pantrix-ui/pantrix-ui.json")).readText()
        Json.parseToJsonElement(text).jsonObject.getValue("components").jsonArray
            .map { it.jsonObject.getValue("function").jsonPrimitive.content }
    }

    /** The site's own file in [language]. */
    private fun site(language: Language) =
        ReferencesReader.read(File(repository, "content"), language, library).projects

    @Test
    fun a_text_said_in_each_language_is_read_in_one_and_the_rest_as_it_is() {
        val text = """
            { "projects": [ { "key": "notes", "name": "Notes", "address": "notes.app",
              "line": { "en": "Notes on every device.", "tr": "Her cihazda notlar." },
              "about": { "en": "About.", "tr": "Hakkında." }, "url": "https://notes.app",
              "platforms": [ { "kind": "Ios", "name": "iOS", "detail": { "en": "iPhone", "tr": "iPhone" } } ] } ] }
        """
        val english = ReferencesReader.read(text, Language.English, library).projects.single()
        val turkish = ReferencesReader.read(text, Language.Turkish, library).projects.single()
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
        assertFailsWith<IllegalArgumentException> { ReferencesReader.read(text, Language.Turkish, library) }
    }

    /** The count the build makes of the library's components is the length of the list the jar has. */
    @Test
    fun the_build_counts_the_library_s_components_as_its_jar_lists_them() {
        assertEquals(library.size, LibraryFacts.COMPONENTS)
    }

    /** A text names the library's count as `{{components}}`, and an app that draws with every one says "all". */
    @Test
    fun the_count_is_written_in_and_all_is_every_component_of_the_library() {
        val text = """
            { "projects": [ { "key": "demo", "name": "Demo", "address": "demo.app", "url": "https://demo.app",
              "line": { "en": "Each of its {{components}} components.", "tr": "{{components}} bileşeninin her biri." },
              "about": "About.", "components": "all" } ] }
        """
        val english = ReferencesReader.read(text, Language.English, library).projects.single()
        val turkish = ReferencesReader.read(text, Language.Turkish, library).projects.single()
        assertEquals("Each of its ${LibraryFacts.COMPONENTS} components.", english.line)
        assertEquals("${LibraryFacts.COMPONENTS} bileşeninin her biri.", turkish.line)
        assertEquals(library.sorted(), english.components)
    }

    @Test
    fun a_component_the_library_does_not_have_stops_the_build() {
        val text = """{ "projects": [ { "key": "a", "name": "A", "address": "a", "url": "a", "line": "A.",
            "about": "About.", "components": ["PantrixButton", "PantrixWidget"] } ] }"""
        val failure = assertFailsWith<IllegalStateException> { ReferencesReader.read(text, Language.English, library) }
        assertTrue("PantrixWidget" in failure.message.orEmpty(), failure.message)
    }

    /** The site's own file is read in both languages, the site first, its texts in the language asked for. */
    @Test
    fun the_site_s_file_is_read_in_both_languages() {
        val english = site(Language.English)
        val turkish = site(Language.Turkish)
        assertEquals("pantrix-ui-site", english.first().key)
        assertEquals(english.map { it.key }, turkish.map { it.key })
        assertTrue(turkish.first().about != english.first().about, "the Turkish about is the English one")
        assertEquals(setOf("en", "tr"), english.first().languages.toSet())
        (english + turkish).flatMap { listOf(it.line, it.about) }.forEach { text ->
            assertFalse("{{" in text, "a text names something the reader did not write in: $text")
        }
    }

    /**
     * Formatico, beside the library's demo in the list: on the web, Android and a Mac, with no iOS app; its source
     * open; and the 48 components its own code draws with. The demo draws with every one of the library's.
     */
    @Test
    fun formatico_is_listed_beside_the_demo_and_the_demo_draws_with_every_component() {
        val english = site(Language.English)
        assertEquals(listOf("pantrix-ui-site", "pantrix-ui-demo", "formatico"), english.map { it.key })
        val demo = english[1]
        assertEquals(LibraryFacts.COMPONENTS, demo.components.size)
        val formatico = english.last()
        assertEquals(
            listOf(PlatformKind.Web, PlatformKind.Android, PlatformKind.Desktop),
            formatico.platforms.map {
                it.kind
            }
        )
        assertEquals("https://formatico.pantrixui.com", formatico.url)
        assertEquals("https://github.com/develappsancho/formatico-cmp", formatico.source)
        assertEquals("https://github.com/develappsancho/formatico-cmp/releases/latest", formatico.download)
        assertEquals(48, formatico.components.size)
        assertTrue(site(Language.Turkish).last().line.startsWith("JSON, XML, YAML"))
    }

    /**
     * The components the site's entry names are the ones its own screens are drawn with: each is called somewhere in
     * the sources of its app, its parts and its pages, the examples and the previews left out.
     */
    @Test
    fun every_component_the_site_names_is_one_its_screens_draw_with() {
        val site = site(Language.English).first()
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
