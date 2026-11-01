package com.pantrixui.site

import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A page of the docs, Installation's, and a guide, Testing's: their code in tabs, their contents, their copy for an
 * assistant, the cards that lead on, the breadcrumb, and a guide's live sketch.
 */
class DocPageTest {
    @Test
    fun its_code_tabs_show_each_way_of_getting_the_library() = siteAt(Route.Doc("installation")) {
        press(hasPantrixTag("Button", "codetab1"))
        until("the local Maven repository") { has(hasText("mavenLocal()", substring = true)) }
        press(hasPantrixTag("Button", "codetab0"))
        until("GitHub Packages again") { !has(hasText("mavenLocal()", substring = true)) }
    }

    @Test
    fun its_contents_bring_a_section_into_view() = siteAt(Route.Doc("installation")) {
        press(hasTestTag("Site_toc_preset"))
        untilShown("the section in view", hasText("4. Your brand") and !hasAnyAncestor(hasTestTag("Site_toc_preset")))
    }

    @Test
    fun the_page_is_copied_for_an_assistant() = siteAt(Route.Doc("installation")) {
        press(hasPantrixTag("Button", "copyPage"))
        until("the copy") { copied() != null }
        assertTrue("com.pantrixui:components" in copied().orEmpty(), copied())
    }

    @Test
    fun its_cards_and_its_breadcrumb_lead_on() = siteAt(Route.Doc("installation")) {
        press(hasPantrixTag("Card", "next0"))
        until("another page") { address() != "/docs/installation" }
        back()
        untilAt("/docs/installation")
        press(hasPantrixTag("Item", "0", scope = "Breadcrumb_page"))
        until("the section's first page") { address() != "/docs/installation" }
    }

    @Test
    fun a_guide_leads_to_the_next_and_the_one_before() = siteAt(Route.Guide("testing")) {
        press(hasPantrixTag("Card", "next"))
        until("the next guide") { address().startsWith("/docs/guides/") && address() != "/docs/guides/testing" }
        back()
        untilAt("/docs/guides/testing")
        press(hasPantrixTag("Card", "previous"))
        until("the guide before") { address().startsWith("/docs/guides/") && address() != "/docs/guides/testing" }
        back()
        untilAt("/docs/guides/testing")
        press(hasPantrixTag("Item", "1", scope = "Breadcrumb_page"))
        untilAt("/docs/guides")
    }

    @Test
    fun a_guide_s_sketch_runs_live() = siteAt(Route.Guide("testing")) {
        val tags = hasTestTag("Site_sketch_tags")
        scrollTo(tags)
        val shown = on(tags)
        press(tags)
        until("the tags turned") { on(tags) != shown }
        val turkish = hasPantrixTag("Segment", "1", scope = "SegmentButton_sketch_language")
        press(turkish)
        until("the sketch in its other language") { chosen(turkish) }
    }
}
