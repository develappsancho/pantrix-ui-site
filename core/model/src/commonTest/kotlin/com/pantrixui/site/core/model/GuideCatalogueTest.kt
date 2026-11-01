package com.pantrixui.site.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GuideCatalogueTest {
    @Test
    fun a_guide_leads_to_the_guides_listed_beside_it() {
        assertEquals("modal", GuideCatalogue.previousOf("keyboard-dismissal")?.key)
        assertEquals("number-keyboard", GuideCatalogue.nextOf("keyboard-dismissal")?.key)
        assertNull(GuideCatalogue.previousOf(GuideCatalogue.entries.first().key))
        assertNull(GuideCatalogue.nextOf(GuideCatalogue.entries.last().key))
        assertNull(GuideCatalogue.previousOf("no-such-guide"))
        assertNull(GuideCatalogue.nextOf("no-such-guide"))
    }

    @Test
    fun a_guide_s_source_is_the_library_s_text_in_the_page_s_language() {
        val guide = GuideCatalogue.of("keyboard-dismissal")!!
        val folder = "https://github.com/develappsancho/pantrix-ui-cmp/blob/main/docs/guide/keyboard-dismissal"
        assertEquals("$folder/KEYBOARD_DISMISSAL_GUIDE.en.md", guide.source(Language.English))
        assertEquals("$folder/KEYBOARD_DISMISSAL_GUIDE.md", guide.source(Language.Turkish))
    }

    @Test
    fun the_catalogue_lists_each_guide_once_and_as_many_as_the_library_has() {
        assertEquals(LibraryFacts.GUIDES, GuideCatalogue.entries.map { it.key }.toSet().size)
    }
}
