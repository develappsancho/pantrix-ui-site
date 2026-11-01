package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** The library's guides as the site's guide pages, from guides written here by hand in the documents' form. */
class GuidePagesTest {
    private val pages = GuidePages(setOf("files", "clipboard", "typography"))

    private val english =
        """
        # Guide to Copying Text

        This document explains why copying text is another job on each platform. The problem is not Pantrix's own: it
        shows in every project. Section 3 gives the solution.

        ## 1. The Problem

        ### Symptom

        *   A button says "copied" when nothing was.

        ### Why

        | Platform | What it does |
        | :--- | :--- |
        | Web | Refuses on an insecure page |

        ## 2. The Solution

        See [the files guide](../guides/files.md), [the section below](#why) and [Chrome](https://example.com).
        `[not a link](../guides/nowhere.md)` stays code.

        ### 2.1. The Copy Button in `PantrixTheme`

        ```kotlin
        | not a table |
        ```

        ## 3. Where in This Repository

        *   Here.
        """.trimIndent()

    private val turkish =
        """
        # Metin Kopyalama Rehberi

        Bu belge, metin kopyalamanın neden her platformda başka bir iş olduğunu açıklar. Sorun Pantrix'e özgü değildir.

        ## 1. Sorun

        ### Belirti

        *   Hiçbir şey kopyalanmadığında düğme "kopyalandı" der.

        ## 2. Çözüm

        ### 2.1. İlk Kullanım ve İOS

        ## 3. Bu Depoda Nerede

        *   Burada.
        """.trimIndent()

    @Test
    fun the_head_has_the_site_s_title_and_a_lead_cut_from_the_first_paragraph() {
        val page = pages.page("clipboard", Language.English, english, english, "Copying text to the clipboard")
        val head = page.lines().take(4)
        assertEquals("title: Copying text to the clipboard", head[1])
        assertEquals(
            "description: Why copying text is another job on each platform. The problem is not Pantrix's own: it " +
                "shows in every project.",
            head[2]
        )
    }

    @Test
    fun a_turkish_lead_names_what_the_guide_explains() {
        val page = pages.page("clipboard", Language.Turkish, turkish, english, "Panoya metin kopyalama")
        assertTrue(
            "description: Metin kopyalamanın neden her platformda başka bir iş olduğu. Sorun Pantrix'e özgü " +
                "değildir." in page,
            page
        )
    }

    @Test
    fun sections_take_ids_from_the_english_headings_in_both_languages() {
        val inEnglish = pages.page("clipboard", Language.English, english, english, "Copying")
        val inTurkish = pages.page("clipboard", Language.Turkish, turkish, english, "Kopyalama")
        assertTrue("## 1. The problem {#problem}" in inEnglish, inEnglish)
        assertTrue("## 2. The solution {#fix}" in inEnglish, inEnglish)
        assertTrue("## 3. Where in this repository {#repository toc=\"3. In the repository\"}" in inEnglish, inEnglish)
        assertTrue("## 1. Sorun {#problem}" in inTurkish, inTurkish)
        assertTrue("## 3. Bu depoda nerede {#repository toc=\"3. Depoda\"}" in inTurkish, inTurkish)
    }

    @Test
    fun a_heading_is_in_sentence_case_and_keeps_its_number_s_next_word_its_names_and_its_code() {
        val page = pages.page("clipboard", Language.English, english, english, "Copying")
        assertTrue("### 2.1. The copy button in `PantrixTheme`" in page, page)
        assertEquals("İlk ışık ve SDK", GuidePages.sentence("İlk Işık ve SDK", Language.Turkish))
        assertEquals("What the Web and iOS do", GuidePages.sentence("What The Web And iOS Do", Language.English))
    }

    @Test
    fun links_lead_to_the_site_s_guides_and_a_link_within_the_guide_is_left_as_its_words() {
        val page = pages.page("clipboard", Language.English, english, english, "Copying")
        assertTrue("See [the files guide](/docs/guides/files), the section below and " in page, page)
        assertTrue("[Chrome](https://example.com)." in page, page)
        assertTrue("`[not a link](../guides/nowhere.md)` stays code." in page, page)
    }

    @Test
    fun a_link_to_a_guide_the_site_has_not_stops_the_writer() {
        val broken = english.replace("../guides/files.md", "../guides/nowhere.md")
        assertFailsWith<IllegalArgumentException> { pages.page("clipboard", Language.English, broken, broken, "x") }
    }

    @Test
    fun tables_are_the_site_s_table_piece_and_code_is_left_as_it_is() {
        val page = pages.page("clipboard", Language.English, english, english, "Copying")
        assertTrue(":::table\n| Platform | What it does |" in page, page)
        assertTrue("| Web | Refuses on an insecure page |\n:::" in page, page)
        assertTrue("```kotlin\n| not a table |\n```" in page, page)
    }

    @Test
    fun a_phone_s_sketch_stands_beside_the_symptom_and_a_window_s_under_it() {
        val phone = pages.page("clipboard", Language.English, english, english, "Copying")
        val aside = ":::aside clipboard\n### Symptom\n\n*   A button says \"copied\" when nothing was.\n:::"
        assertTrue(aside in phone, phone)
        val window = pages.page("typography", Language.English, english, english, "Type")
        assertTrue("when nothing was.\n\n:::sketch typography\n:::\n\n### Why" in window, window)
    }
}
