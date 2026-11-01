package com.pantrixui.site

import androidx.compose.ui.test.hasTestTag
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test

/** The page an address with no page shows: its ways to start again and its search. */
class NotFoundPageTest {
    @Test
    fun its_cards_lead_to_where_a_visitor_starts() = siteAt(Route.NotFound("/no-such-page")) {
        mapOf(
            "installation" to "/docs/installation",
            "components" to "/components",
            "guides" to "/docs/guides",
            "changelog" to "/changelog"
        ).forEach { (key, path) ->
            press(hasPantrixTag("Card", "start_$key"))
            untilAt(path)
            back()
            until("the page that was not found") { has(hasPantrixTag("Card", "start_$key")) }
        }
    }

    @Test
    fun its_search_opens_the_search() = siteAt(Route.NotFound("/no-such-page")) {
        press(hasTestTag("Site_notFoundSearch"))
        untilShown("the search", hasPantrixTag("TextField", "search"))
    }
}
