package com.pantrixui.site

import com.pantrixui.site.core.model.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SiteViewModelTest {
    @Test
    fun the_theme_follows_the_system_until_the_reader_switches_it() {
        val model = SiteViewModel(Language.English)
        assertEquals(null, model.state.value.dark)
        model.onAction(SiteAction.ToggleTheme(systemDark = true))
        assertEquals(false, model.state.value.dark)
        // Once chosen, the system's theme no longer decides.
        model.onAction(SiteAction.ToggleTheme(systemDark = false))
        assertEquals(true, model.state.value.dark)
    }

    @Test
    fun the_language_and_the_menu_are_the_readers_choice() {
        val model = SiteViewModel(Language.Turkish)
        assertEquals(Language.Turkish, model.state.value.language)
        model.onAction(SiteAction.ChooseLanguage(Language.English))
        assertEquals(Language.English, model.state.value.language)
        model.onAction(SiteAction.OpenMenu)
        assertTrue(model.state.value.menuOpen)
        model.onAction(SiteAction.CloseMenu)
        assertFalse(model.state.value.menuOpen)
    }
}
