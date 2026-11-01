package com.pantrixui.site

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import com.pantrixui.testing.onPantrixNode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The site's frame as a visitor uses it, at a phone's size, a tablet's and a desktop's: the menu, the header's links
 * and its tools, the language, the theme and the search. Each page's own behaviour is in its own test, `…PageTest`.
 * Each test finds what it presses by the tag Pantrix UI gives a component, as the flows in `app/maestro` do on a
 * device; these run in every `jvmTest`.
 */
class SiteAppTest {
    @Test
    fun the_menu_leads_to_a_page_and_its_own_button_closes_it() = siteAt(Route.Home, SiteSize.Phone) {
        onPantrixNode("IconButton", "openMenu").performClick()
        untilShown("the menu's Installation", INSTALLATION_ITEM)
        press(INSTALLATION_ITEM)
        until("the Installation page, the menu closed behind it") { !has(MENU) }
        untilAt("/docs/installation")
        onPantrixNode("IconButton", "openMenu").performClick()
        untilShown("the menu's own button", hasPantrixTag("SheetClose", "menu"))
        press(hasPantrixTag("SheetClose", "menu"))
        until("the menu closed on the same page") { !has(MENU) }
        assertEquals("/docs/installation", address())
    }

    @Test
    fun the_brand_leads_home() = siteAt(Route.Components, SiteSize.Phone) {
        press(hasTestTag("Site_home"))
        untilAt("/")
    }

    @Test
    fun the_language_menu_turns_the_words_and_the_address_into_turkish_and_back() =
        siteAt(Route.Doc("installation"), SiteSize.Phone) {
            chooseLanguage("tr")
            untilAt("/tr/docs/installation")
            onPantrixNode("IconButton", "search").assertContentDescriptionEquals("Belgelerde ara")
            onPantrixNode("IconButton", "openMenu").assertContentDescriptionEquals("Menüyü aç")
            chooseLanguage("en")
            untilAt("/docs/installation")
            onPantrixNode("IconButton", "search").assertContentDescriptionEquals("Search the docs")
        }

    @Test
    fun the_theme_button_turns_the_dark_theme_on_and_off_and_names_the_next() = siteAt(Route.Home, SiteSize.Phone) {
        val theme = onPantrixNode("IconButton", "theme")
        val light = brightness()
        theme.assertContentDescriptionEquals("Switch to the dark theme")
        theme.performClick()
        until("the dark theme") { brightness() < light / 2 }
        theme.assertContentDescriptionEquals("Switch to the light theme")
        theme.performClick()
        until("the light theme again") { brightness() == light }
        theme.assertContentDescriptionEquals("Switch to the dark theme")
    }

    @Test
    fun the_search_finds_a_component_keeps_the_components_and_opens_its_page() = siteAt(Route.Home, SiteSize.Phone) {
        onPantrixNode("IconButton", "search").performClick()
        untilShown("the search", SEARCH_FIELD)
        type(SEARCH_FIELD, "date picker")
        until("the results") { has(result(0)) && has(result(3)) }
        press(hasTestTag("Site_searchFilter_component"))
        until("the three components alone") { has(result(2)) && !has(result(3)) }
        press(result(0))
        until("the search closed") { !has(SEARCH) }
        untilAt("/components/date-picker")
        // Cancel closes the search where it stands.
        onPantrixNode("IconButton", "search").performClick()
        untilShown("the search's Cancel", hasTestTag("Site_searchCancel"))
        press(hasTestTag("Site_searchCancel"))
        until("the search closed on the same page") { !has(SEARCH) }
        assertEquals("/components/date-picker", address())
    }

    @Test
    fun the_search_finds_the_pages_in_the_language_it_is_told() = siteAt(Route.Home, SiteSize.Phone) {
        onPantrixNode("IconButton", "search").performClick()
        untilShown("the search", SEARCH_FIELD)
        press(hasTestTag("Site_searchLanguage_tr"))
        type(SEARCH_FIELD, "kurulum")
        until("a Turkish page") { has(result(0)) }
        press(result(0))
        until("the search closed") { !has(SEARCH) }
        until("a Turkish address") { address().startsWith("/tr/") }
    }

    @Test
    fun a_tablet_s_header_opens_the_search_from_its_box() = siteAt(Route.Home, SiteSize.Tablet) {
        press(hasTestTag("Site_search"))
        untilShown("the search", SEARCH_FIELD)
        type(SEARCH_FIELD, "button")
        until("the results") { has(result(0)) }
    }

    @Test
    fun the_header_s_links_lead_to_their_parts_on_a_desktop() = siteAt(Route.Home, SiteSize.Desktop) {
        mapOf(
            "docs" to "/docs/introduction",
            "components" to "/components",
            "foundations" to "/foundations",
            "adaptive" to "/adaptive",
            "theme" to "/theme",
            "ai" to "/ai",
            "roadmap" to "/roadmap",
            "download" to "/download",
            "references" to "/references"
        ).forEach { (key, path) ->
            press(hasPantrixTag("Button", "nav_$key"))
            untilAt(path)
        }
        press(hasTestTag("Site_home"))
        untilAt("/")
        until("the home page") { has(hasPantrixTag("Button", "homeBrowse")) }
    }

    /** Chooses the language [code] in the header's menu, and waits until the header's button names it. */
    private fun SiteScreen.chooseLanguage(code: String) {
        press(hasPantrixTag("Button", "language"))
        val item = hasPantrixTag("MenuItem", "language_$code", scope = "Menu_language")
        untilShown("the language menu", item)
        press(item)
        val chosen = hasPantrixTag("Button", "language") and
            hasContentDescription(": ${code.uppercase()}, ", substring = true)
        until("the header in $code") { has(chosen) && !has(item) }
    }

    private companion object {
        val MENU = hasPantrixTag("Sheet", "menu")
        val INSTALLATION_ITEM = hasTestTag("Site_nav_installation")
        val SEARCH = hasPantrixTag("Modal", "search")
        val SEARCH_FIELD = hasPantrixTag("TextField", "search")

        /** The search's result at [index], in the order it lists them. */
        fun result(index: Int) = hasPantrixTag("ListItem", "searchresult_$index")
    }
}
