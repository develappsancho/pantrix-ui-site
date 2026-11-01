package com.pantrixui.site

import androidx.compose.ui.test.hasText
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertTrue

/** The references: the apps by platform, each one's way to visit, to read and to download, and a project added. */
class ReferencesPageTest {
    @Test
    fun a_platform_keeps_the_apps_that_run_on_it() = siteAt(Route.References) {
        val web = hasPantrixTag("Button", "platform_web")
        press(web)
        until("the web's apps") { has(hasText("Pantrix UI's demo")) }
        press(hasPantrixTag("Button", "platform_all"))
        until("every app") { has(hasText("Pantrix UI's demo")) }
    }

    @Test
    fun each_app_is_visited_read_and_downloaded_outside_the_site() = siteAt(Route.References) {
        listOf(
            hasPantrixTag("Button", "visit_pantrix_ui_site"),
            hasPantrixTag("Button", "source_pantrix_ui_site"),
            hasPantrixTag("Link", "visit_pantrix_ui_demo"),
            hasPantrixTag("Link", "download_pantrix_ui_demo")
        ).forEachIndexed { index, way ->
            press(way)
            until("${way.description} opened") { services.opened.size == index + 1 || address() != "/references" }
            if (address() != "/references") {
                back()
                untilAt("/references")
            }
        }
        assertTrue(services.opened.any { it.endsWith(".dmg") }, "${services.opened}")
        assertTrue(services.opened.any { "github.com" in it }, "${services.opened}")
    }

    @Test
    fun a_project_is_added_by_an_issue() = siteAt(Route.References) {
        press(hasPantrixTag("Button", "addProjectCard"))
        until("an address opened") { services.opened.isNotEmpty() }
        assertTrue("github.com" in services.opened.single(), services.opened.single())
    }
}
