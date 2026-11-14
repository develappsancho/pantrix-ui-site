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
        val demo = hasText("Pantrix UI's demo")
        val formatico = hasText("Formatico")
        press(hasPantrixTag("Button", "platform_ios"))
        until("iOS's apps, with no Formatico") { has(demo) && !has(formatico) }
        press(hasPantrixTag("Button", "platform_web"))
        until("the web's apps") { has(demo) && has(formatico) }
        press(hasPantrixTag("Button", "platform_all"))
        until("every app") { has(demo) && has(formatico) }
    }

    @Test
    fun each_app_is_visited_read_and_downloaded_outside_the_site() = siteAt(Route.References) {
        listOf(
            hasPantrixTag("Button", "visit_pantrix_ui_site"),
            hasPantrixTag("Button", "source_pantrix_ui_site"),
            hasPantrixTag("Link", "visit_pantrix_ui_demo"),
            hasPantrixTag("Link", "download_pantrix_ui_demo"),
            hasPantrixTag("Link", "visit_formatico"),
            hasPantrixTag("Link", "download_formatico"),
            hasPantrixTag("Button", "source_formatico")
        ).forEachIndexed { index, way ->
            press(way)
            until("${way.description} opened") { services.opened.size == index + 1 || address() != "/references" }
            if (address() != "/references") {
                back()
                untilAt("/references")
            }
        }
        assertTrue(services.opened.any { it.endsWith(".dmg") }, "${services.opened}")
        assertTrue("https://formatico.pantrixui.com" in services.opened, "${services.opened}")
        assertTrue("https://github.com/develappsancho/formatico-cmp" in services.opened, "${services.opened}")
        assertTrue(
            "https://github.com/develappsancho/formatico-cmp/releases/latest" in services.opened,
            "${services.opened}"
        )
    }

    /** Formatico's card shows its own screen, and the demo's, which has none, its mark. */
    @Test
    fun an_app_s_card_shows_its_screen_where_it_has_one() = siteAt(Route.References) {
        scrollTo(hasPantrixTag("Image", "picture_formatico"))
        until("Formatico's screen") { has(hasPantrixTag("Image", "picture_formatico")) }
        assertTrue(!has(hasPantrixTag("Image", "picture_pantrix_ui_demo")), "the demo has no picture of its own")
    }

    @Test
    fun a_project_is_added_by_an_issue() = siteAt(Route.References) {
        press(hasPantrixTag("Button", "addProjectCard"))
        until("an address opened") { services.opened.isNotEmpty() }
        assertTrue("github.com" in services.opened.single(), services.opened.single())
    }
}
