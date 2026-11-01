package com.pantrixui.site

import com.pantrixui.site.core.i18n.SiteStrings
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SiteNavigationTest {
    @Test
    fun every_page_of_the_documentation_marks_the_docs_link() {
        val docs = listOf(Route.Doc("installation"), Route.Guides, Route.Guide("testing"), Route.Changelog)
        docs.forEach { route -> assertEquals(SitePart.Docs, route.part, "$route") }
        assertEquals(SitePart.Components, Route.Component("text-area").part)
        assertEquals(SitePart.Roadmap, Route.Roadmap.part)
        assertNull(Route.Home.part)
        val links = headerLinks(SiteStrings.English, Route.Guide("testing"))
        assertEquals(listOf("docs"), links.filter { it.current }.map { it.key })
    }

    @Test
    fun the_sidebar_stands_beside_the_documentation_and_the_page_for_no_page() {
        assertTrue(Route.Doc("installation").hasDocsSidebar)
        assertTrue(Route.NotFound("/nowhere").hasDocsSidebar)
        assertFalse(Route.Home.hasDocsSidebar)
        assertFalse(Route.Components.hasDocsSidebar)
    }

    @Test
    fun the_sidebars_links_are_addresses_in_the_language_with_the_page_on_show_marked() {
        val sections = docsSections(SiteStrings.Turkish, Language.Turkish, Route.Doc("installation"))
        val items = sections.flatMap { it.items }
        assertEquals(listOf("Başlarken", "Rehberler", "Proje"), sections.map { it.title })
        assertEquals("/tr/docs/installation", items.single { it.current }.href)
        assertEquals("/tr/docs/guides/keyboard-dismissal", items.single { it.key == "guide_keyboard-dismissal" }.href)
        // A web address stays one in every language.
        val issues = items.single { it.key == "issues" }
        assertEquals("https://github.com/developersancho/pantrix-ui-cmp/issues", issues.href)
    }

    @Test
    fun the_menu_adds_the_parts_of_the_site_the_sidebar_leaves_out() {
        val menu = menuSections(SiteStrings.English, Language.English, Route.Theme)
        assertEquals("Pantrix UI", menu.last().title)
        assertEquals(listOf("components", "foundations", "adaptive", "theme"), menu.last().items.map { it.key })
        assertEquals("theme", menu.flatMap { it.items }.single { it.current }.key)
        assertEquals("Theme", menuTitle(SiteStrings.English, Route.Theme))
        assertEquals("Pantrix UI", menuTitle(SiteStrings.English, Route.Home))
    }
}
