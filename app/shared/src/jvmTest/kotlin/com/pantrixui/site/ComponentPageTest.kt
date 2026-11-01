package com.pantrixui.site

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onFirst
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A component's page, Button's: its examples drawn live in each platform's preset, dark and from the right, its
 * contents, its API with each of its forms, its copy for an assistant, its breadcrumb and the sidebar of every
 * component.
 */
class ComponentPageTest {
    @Test
    fun its_examples_are_drawn_in_each_platform_s_preset() = siteAt(Route.Component("button"), SiteSize.Phone) {
        listOf("ios", "desktop", "web", "android").forEach { platform ->
            val segment = hasPantrixTag("Segment", platform, scope = "SegmentButton_exampleplatform")
            press(segment)
            until("$platform chosen") { chosen(segment) }
        }
    }

    @Test
    fun an_example_is_drawn_dark_and_from_the_right() = siteAt(Route.Component("button")) {
        val text = hasPantrixTag("Button", "variant_text")
        scrollTo(text)
        val light = brightnessOf(text)
        press(hasPantrixTag("Button", "exampleDark"))
        until("the example dark") { brightnessOf(text) < light / 2 }
        val solid = hasPantrixTag("Button", "variant_solid")
        val outline = hasPantrixTag("Button", "variant_outline")
        assertTrue(leftOf(solid) < leftOf(outline), "Solid stands before Outline from the left")
        press(hasPantrixTag("Button", "exampleRtl"))
        until("the example from the right") { leftOf(solid) > leftOf(outline) }
    }

    @Test
    fun its_contents_lead_to_its_api_whose_forms_are_chosen_one_at_a_time() =
        siteAt(Route.Component("button"), SiteSize.Phone) {
            press(hasPantrixTag("Button", "contents_api"))
            val onClick = hasPantrixTag("Segment", "onclick", scope = "SegmentButton_forms")
            untilShown("the API in view", onClick)
            press(onClick)
            until("the form of onClick chosen") { chosen(onClick) }
            val text = hasPantrixTag("Segment", "text", scope = "SegmentButton_forms")
            press(text)
            until("the form of text chosen") { chosen(text) && !chosen(onClick) }
        }

    @Test
    fun its_table_of_parameters_shows_them_all_on_asking() = siteAt(Route.Component("button")) {
        val all = hasPantrixTag("Button", "allParameters")
        scrollTo(all)
        val asking = said(all)
        val few = count(WORDS)
        press(all)
        until("every parameter") { count(WORDS) > few && said(all) != asking }
        press(all)
        until("the first ones again") { count(WORDS) == few && said(all) == asking }
    }

    @Test
    fun the_table_of_contents_brings_its_section_into_view() = siteAt(Route.Component("button")) {
        press(hasTestTag("Site_toc_api"))
        untilShown("the API in view", hasPantrixTag("SegmentButton", "forms"))
    }

    @Test
    fun each_example_s_title_is_a_heading_for_a_screen_reader() = siteAt(Route.Component("button")) {
        until("the examples' headings") { headings().containsAll(listOf("Variants", "Sizes", "Loading and disabled")) }
    }

    @Test
    fun the_page_is_copied_for_an_assistant() = siteAt(Route.Component("button")) {
        press(hasPantrixTag("Button", "copyPage"))
        until("the copy") { copied() != null }
        assertTrue("PantrixButton" in copied().orEmpty(), copied())
    }

    @Test
    fun its_breadcrumb_and_the_sidebar_lead_to_other_pages() = siteAt(Route.Component("button")) {
        press(hasTestTag("Site_nav_component_icon-button"))
        untilAt("/components/icon-button")
        press(hasPantrixTag("Item", "0", scope = "Breadcrumb_page"))
        untilAt("/components")
    }

    private companion object {
        /** Every node that says something in words. */
        val WORDS = SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)
    }

    /** How bright the middle of the first node [matcher] finds is drawn, as the sum of its red, green and blue. */
    private fun SiteScreen.brightnessOf(matcher: SemanticsMatcher): Float {
        val image = onAllNodes(matcher, useUnmergedTree = true).onFirst().captureToImage().toPixelMap()
        val pixel = image[image.width / 2, 1]
        return pixel.red + pixel.green + pixel.blue
    }

    /** Where the first node [matcher] finds begins from the window's left. */
    private fun SiteScreen.leftOf(matcher: SemanticsMatcher): Float =
        onAllNodes(matcher, useUnmergedTree = true).onFirst().fetchSemanticsNode().positionInRoot.x
}
