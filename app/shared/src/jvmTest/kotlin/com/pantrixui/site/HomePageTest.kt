package com.pantrixui.site

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The home page: its ways into the site, the install line, the live form, the adaptive tour and its foot. */
class HomePageTest {
    @Test
    fun its_ways_in_lead_to_their_pages() = siteAt(Route.Home) {
        mapOf(
            hasTestTag("Site_homeNews") to "/docs/forms",
            hasPantrixTag("Button", "homeGetStarted") to "/docs/installation",
            hasPantrixTag("Button", "homeBrowse") to "/components",
            hasPantrixTag("Button", "homeThemeBuilder") to "/theme",
            hasPantrixTag("Button", "homeTokens") to "/foundations",
            hasPantrixTag("Button", "builtReferences") to "/references",
            hasPantrixTag("Button", "homeCallGetStarted") to "/docs/installation",
            hasPantrixTag("Button", "homeComponentPage") to "/components/text-area",
            hasTestTag("Site_homeGuides") to "/docs/guides"
        ).forEach { (way, path) ->
            press(way)
            untilAt(path)
            back()
            untilAt("/")
        }
    }

    @Test
    fun its_foot_leads_to_the_docs_and_to_the_site_in_turkish() = siteAt(Route.Home) {
        mapOf(
            "introduction" to "/docs/introduction",
            "theming" to "/docs/theming",
            "changelog" to "/changelog",
            "mcp" to "/ai",
            "roadmap" to "/roadmap",
            "references" to "/references",
            "group_chart" to "/components"
        ).forEach { (key, path) ->
            press(hasTestTag("Site_foot_$key"))
            untilAt(path)
            back()
            untilAt("/")
        }
        press(hasTestTag("Site_footLanguage_tr"))
        untilAt("/tr")
        until("the Turkish words") {
            has(hasPantrixTag("IconButton", "theme") and hasContentDescription("Koyu temaya geç"))
        }
    }

    @Test
    fun the_install_line_is_copied() = siteAt(Route.Home) {
        press(hasPantrixTag("Button", "copyInstall"))
        until("the copy") { copied() != null }
        assertEquals("implementation(\"com.pantrixui:components:0.1.1\")", copied())
    }

    @Test
    fun the_live_form_is_drawn_in_the_platform_chosen_and_its_code_has_two_files() = siteAt(Route.Home) {
        listOf("ios", "desktop", "web", "android").forEach { platform ->
            val segment = hasPantrixTag("Segment", platform, scope = "SegmentButton_homeplatform")
            press(segment)
            until("$platform chosen") { chosen(segment) }
        }
        press(hasPantrixTag("Button", "codetab1"))
        until("the preset's file") { has(hasText("PantrixPreset", substring = true)) }
        press(hasPantrixTag("Button", "codetab0"))
        until("the form's file") { has(hasText("ReviewForm", substring = true)) }
    }

    @Test
    fun the_tour_pauses_goes_to_a_step_and_its_api_leads_to_the_component() = siteAt(Route.Home) {
        val play = hasPantrixTag("Button", "tourPlay")
        scrollTo(play)
        assertEquals("Pause the tour", said(play))
        press(play)
        until("the tour paused") { said(play) != "Pause the tour" }
        press(play)
        until("the tour playing again") { said(play) == "Pause the tour" }
        // A step chosen by hand stops the tour there: the foldable held open, with a rail at its side.
        press(hasTestTag("Site_tour_book"))
        until("the tour paused on the foldable") { said(play) != "Pause the tour" }
        press(hasTestTag("Site_tourApi_pantrixnavigationrail"))
        untilAt("/components/navigation-rail")
    }

    @Test
    fun the_tour_s_box_holds_its_button_whole_and_keeps_one_height_on_every_window_in_both_languages() =
        SiteSize.entries.forEach { size ->
            Language.entries.forEach { language ->
                siteAt(Route.Home, size, language) {
                    // The column beside a desktop's stage once ran past the box's 600dp and lost the button under its
                    // edge; and a box as tall as the step on show moved the page under it as the tour turned.
                    val play = hasPantrixTag("Button", "tourPlay")
                    val first = hasTestTag("Site_tour_phone")
                    val heights = listOf("phone", "book", "table", "tablet", "desktop", "web").map { step ->
                        press(hasTestTag("Site_tour_$step"))
                        settle()
                        assertTrue(whole(play), "the tour's button is cut at the step $step, $size, $language")
                        place(play).bottom - place(first).top
                    }
                    assertEquals(1, heights.distinct().size, "the tour is not one height, $size, $language: $heights")
                }
            }
        }

    @Test
    fun the_site_s_source_opens_on_github() = siteAt(Route.Home) {
        press(hasPantrixTag("Button", "builtSource"))
        until("an address opened") { services.opened.isNotEmpty() }
        assertTrue(services.opened.single().startsWith("https://github.com/develappsancho/pantrix-ui-site"))
        assertEquals("/", address())
    }
}
