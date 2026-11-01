package com.pantrixui.site

import com.pantrixui.site.core.i18n.SiteStrings
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
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
        assertEquals(SitePart.Download, Route.Download.part)
        // The page that asks the server stands under the page for coding assistants, which the header marks.
        assertEquals(SitePart.Ai, Route.Ask.part)
        assertEquals(listOf("ai"), headerLinks(SiteStrings.English, Route.Ask).filter { it.current }.map { it.key })
        assertNull(Route.Home.part)
        // The apps built with Pantrix UI are the header's last part, and the menu marks them as it does the others.
        assertEquals(SitePart.References, Route.References.part)
        assertEquals(
            listOf("references"),
            menuSections(SiteStrings.English, Language.English, Route.References)
                .flatMap { it.items }.filter { it.current }.map { it.key }
        )
        val links = headerLinks(SiteStrings.English, Route.Guide("testing"))
        assertEquals(listOf("docs"), links.filter { it.current }.map { it.key })
        // The header's last link is the apps built with Pantrix UI, which the header marks while they are on show.
        val references = headerLinks(SiteStrings.Turkish, Route.References).last()
        assertEquals(
            Triple("references", "Referanslar", true),
            Triple(references.key, references.label, references.current)
        )
    }

    @Test
    fun the_pages_the_header_opens_switch_as_tabs_and_the_pages_opened_from_them_come_in_over_them() {
        val parts = listOf(Route.Home) + SitePart.entries.map { it.route }
        parts.forEach { route -> assertTrue(route.isSitePart, "$route") }
        val opened = listOf(
            Route.Doc("installation"),
            Route.Guides,
            Route.Guide("testing"),
            Route.Changelog,
            Route.Component("text-area"),
            Route.NotFound("/nowhere")
        )
        opened.forEach { route -> assertFalse(route.isSitePart, "$route") }
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
        // The repository is private: the sidebar leads no one to its issues.
        assertEquals(LibraryFacts.REPOSITORY_IS_OPEN, items.any { it.key == "issues" })
    }

    @Test
    fun the_menu_holds_every_part_of_the_header_in_its_order_and_github() {
        val menu = menuSections(SiteStrings.English, Language.English, Route.Theme)
        assertEquals("Pantrix UI", menu.last().title)
        val parts = menu.last().items
        assertEquals(
            headerLinks(SiteStrings.English, Route.Theme).map { it.key } + "repository",
            parts.map { it.key }
        )
        val names = "Docs, Components, Foundations, Adaptive UI, Theme builder, For AI, Roadmap, Download, References"
        assertEquals(names, parts.dropLast(1).joinToString { it.label })
        assertEquals(
            listOf("/docs/introduction", "/ai", "/roadmap", "/download", "/references"),
            listOf("docs", "ai", "roadmap", "download", "references").map { key -> parts.single { it.key == key }.href }
        )
        // The site's source on GitHub, which the header leaves out on a phone narrower than the design.
        assertEquals("https://github.com/develappsancho/pantrix-ui-site", parts.last().href)
        assertEquals("theme", menu.flatMap { it.items }.single { it.current }.key)
        assertEquals("Theme", menuTitle(SiteStrings.English, Route.Theme))
        assertEquals("Pantrix UI", menuTitle(SiteStrings.English, Route.Home))
        // Each row has a tag of its own.
        val keys = menu.flatMap { it.items }.map { it.key }
        assertEquals(keys.distinct(), keys)
    }

    @Test
    fun a_page_in_the_sidebar_s_groups_and_the_header_s_parts_is_marked_once() {
        fun marked(route: Route) =
            menuSections(SiteStrings.Turkish, Language.Turkish, route).flatMap { it.items }.filter { it.current }
        assertEquals(listOf("project_roadmap"), marked(Route.Roadmap).map { it.key })
        assertEquals(listOf("introduction"), marked(Route.Doc("introduction")).map { it.key })
        val ai = marked(Route.Ai).single()
        assertEquals("ai" to "/tr/ai", ai.key to ai.href)
    }

    @Test
    fun the_foundations_sidebar_leads_to_its_sections_and_to_the_pages_about_the_theme() {
        val sections = foundationsSections(SiteStrings.Turkish, Language.Turkish, section = "shape")
        assertEquals(listOf("Temeller", "Tema"), sections.map { it.title })
        val parts = sections.first().items
        assertEquals("/tr/foundations#colour", parts.first().href)
        assertEquals("Şekil", parts.single { it.current }.label)
        assertEquals("/tr/docs/theming#preset", sections.last().items.first().href)
        assertEquals("/tr/theme", sections.last().items.last().href)
        // An address with no section, or one the page has not, marks the first.
        val first = foundationsSections(SiteStrings.English, Language.English, section = "nothing").first().items
        assertEquals("foundation_colour", first.single { it.current }.key)
    }
}
