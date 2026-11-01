package com.pantrixui.site

import androidx.compose.ui.test.hasTestTag
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The roadmap: a stage's work alone, a piece of work's page, the drawn-from-data sample, and a suggestion. */
class RoadmapPageTest {
    @Test
    fun a_stage_keeps_its_own_work() = siteAt(Route.Roadmap) {
        until("every stage") { has(item("shipped")) && has(item("later")) }
        press(hasTestTag("Site_filter_later"))
        until("what comes later alone") { has(item("later")) && !has(item("shipped")) }
        press(hasTestTag("Site_filter_all"))
        until("every stage again") { has(item("shipped")) && has(item("later")) }
    }

    @Test
    fun a_piece_of_work_leads_to_its_page() = siteAt(Route.Roadmap) {
        press(item("shipped"))
        until("its page or its address") { address() != "/roadmap" || services.opened.isNotEmpty() }
    }

    @Test
    fun the_sample_drawn_from_data_shows_its_other_screen() = siteAt(Route.Roadmap) {
        val form = hasPantrixTag("Segment", "form", scope = "SegmentButton_sduisample")
        press(form)
        until("the form drawn") { chosen(form) && !has(hasPantrixTag("Button", "sduiShopNow")) }
        val campaign = hasPantrixTag("Segment", "campaign", scope = "SegmentButton_sduisample")
        press(campaign)
        until("the campaign drawn") { chosen(campaign) && has(hasPantrixTag("Button", "sduiShopNow")) }
    }

    @Test
    fun a_suggestion_opens_an_issue_where_the_repository_is_open() = siteAt(Route.Roadmap) {
        val suggest = hasPantrixTag("Button", "roadmapSuggest") or hasTestTag("Site_roadmapSuggest")
        until("the board") { has(item("shipped")) }
        if (LibraryFacts.REPOSITORY_IS_OPEN) {
            press(suggest)
            until("an address opened") { services.opened.isNotEmpty() }
            assertTrue(services.opened.single().startsWith("https://github.com/"), "${services.opened}")
        } else {
            assertFalse(has(suggest), "no way to suggest while the repository is private")
        }
    }

    private companion object {
        /** The first piece of work of the [stage]. */
        fun item(stage: String) = hasTestTag("Site_roadmap_${stage}_0")
    }
}
