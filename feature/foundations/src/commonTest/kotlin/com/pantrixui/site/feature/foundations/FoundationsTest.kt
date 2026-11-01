package com.pantrixui.site.feature.foundations

import androidx.compose.ui.graphics.Color
import com.pantrixui.foundation.l10n.PantrixStrings
import com.pantrixui.foundation.token.color.contrastRatio
import com.pantrixui.site.core.designsystem.PantrixPreset
import com.pantrixui.site.core.model.FoundationSection
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.ThemingLink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FoundationsTest {
    @Test
    fun the_model_shows_either_set_of_colours_and_sends_the_dots_to_and_fro() {
        val model = FoundationsViewModel()
        assertFalse(model.state.value.dark, "the light colours first, whatever the site's theme")
        model.onAction(FoundationsAction.ShowDark(true))
        model.onAction(FoundationsAction.Play)
        assertTrue(model.state.value.dark)
        assertTrue(model.state.value.moved)
        model.onAction(FoundationsAction.Play)
        assertFalse(model.state.value.moved, "a second play sends them back")
    }

    @Test
    fun a_contrast_is_graded_as_text_is_read_and_written_as_the_language_writes_a_number() {
        assertEquals("4.6:1 AA", grade(4.62f, PantrixStrings(), Language.English))
        assertEquals("3,4:1 AA büyük", grade(3.4f, PantrixStrings.Turkish, Language.Turkish))
        assertEquals("2.0:1 fails", grade(2f, PantrixStrings(), Language.English))
    }

    @Test
    fun a_colour_s_code_is_six_capital_digits() {
        assertEquals("#2264FF", Color(0xFF2264FF).hex())
        assertEquals("#05070A", Color(0xFF05070A).hex())
    }

    @Test
    fun what_the_site_wears_on_its_roles_reads_as_text_in_both_themes() {
        listOf(false, true).forEach { dark ->
            val colors = PantrixPreset.colors(dark)
            listOf(colors.primary, colors.success, colors.warning, colors.danger).forEach { role ->
                assertTrue(contrastRatio(role.onColor, role.color) >= 4.5f, "on ${role.color.hex()}, dark = $dark")
            }
        }
    }

    @Test
    fun every_section_and_link_has_its_words_in_both_languages() {
        FoundationSection.entries.forEach { assertTrue(it.title.turkish.isNotBlank() && it.anchor.isNotBlank()) }
        ThemingLink.entries.forEach { assertTrue(it.title.english != it.title.turkish, it.key) }
        assertEquals(FoundationsSay.uses.keys, FoundationsSay.uses.filterValues { it.turkish.isNotBlank() }.keys)
        assertEquals(8, BLUES.size)
        assertEquals(11, GREYS.size)
    }
}
