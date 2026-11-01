package com.pantrixui.site.core.routing

import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import kotlin.test.Test
import kotlin.test.assertEquals

class SiteRoutesTest {
    private val routes = listOf(
        Route.Home,
        Route.Doc("installation"),
        Route.Guides,
        Route.Guide("keyboard-dismissal"),
        Route.Components,
        Route.Component("text-area"),
        Route.Foundations,
        Route.Adaptive,
        Route.Theme,
        Route.Ai,
        Route.Roadmap,
        Route.Changelog
    )

    @Test
    fun every_route_is_the_route_of_its_own_path() {
        routes.forEach { assertEquals(it, SiteRoutes.routeOf(SiteRoutes.pathOf(it))) }
    }

    @Test
    fun every_address_is_the_location_it_was_made_from_in_both_languages() {
        Language.entries.forEach { language ->
            routes.forEach { route ->
                assertEquals(
                    SiteLocation(language, route),
                    SiteRoutes.locationOf(SiteRoutes.addressOf(route, language))
                )
            }
        }
    }

    @Test
    fun english_has_no_prefix_and_turkish_has_its_code() {
        assertEquals("/docs/installation", SiteRoutes.addressOf(Route.Doc("installation"), Language.English))
        assertEquals("/tr/docs/installation", SiteRoutes.addressOf(Route.Doc("installation"), Language.Turkish))
        assertEquals("/", SiteRoutes.addressOf(Route.Home, Language.English))
        assertEquals("/tr", SiteRoutes.addressOf(Route.Home, Language.Turkish))
    }

    @Test
    fun a_query_a_fragment_and_a_closing_slash_do_not_change_the_page() {
        assertEquals(Route.Doc("installation"), SiteRoutes.routeOf("/docs/installation/"))
        assertEquals(Route.Doc("installation"), SiteRoutes.routeOf("/docs/installation?ref=home"))
        assertEquals(Route.Component("text-area"), SiteRoutes.routeOf("/components/text-area#api"))
        assertEquals(SiteLocation(Language.Turkish, Route.Home), SiteRoutes.locationOf("/tr/"))
    }

    @Test
    fun an_address_the_site_has_no_page_for_is_not_found_with_what_was_asked() {
        assertEquals(Route.NotFound("/components/date-pickr/x"), SiteRoutes.routeOf("/components/date-pickr/x"))
        assertEquals(Route.NotFound("/pricing"), SiteRoutes.routeOf("/pricing"))
        assertEquals(Route.NotFound("/docs/Installation"), SiteRoutes.routeOf("/docs/Installation"))
        assertEquals(
            SiteLocation(Language.Turkish, Route.NotFound("/pricing")),
            SiteRoutes.locationOf("/tr/pricing")
        )
    }
}
