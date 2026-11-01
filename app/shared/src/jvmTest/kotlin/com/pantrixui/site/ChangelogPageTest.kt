package com.pantrixui.site

import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertTrue

/** The changelog: a module's changes alone, the breaking ones alone, its versions in its contents, and its copy. */
class ChangelogPageTest {
    @Test
    fun a_module_keeps_its_own_changes() = siteAt(Route.Changelog) {
        until("every change") { words().size > 0 }
        val all = words().size
        press(hasPantrixTag("Button", "module_testing"))
        until("the testing module's changes alone") { words().size < all }
        press(hasPantrixTag("Button", "module_all"))
        until("every change again") { words().size == all }
    }

    @Test
    fun the_breaking_changes_alone_on_asking() = siteAt(Route.Changelog) {
        val breaking = hasPantrixTag("SwitchButton", "onlyBreaking")
        scrollTo(breaking)
        val all = words().size
        press(breaking)
        until("the breaking changes alone") { on(breaking) && words().size < all }
        press(breaking)
        until("every change again") { !on(breaking) && words().size == all }
    }

    @Test
    fun its_contents_bring_a_version_into_view() = siteAt(Route.Changelog) {
        press(hasTestTag("Site_toc_v010alpha03"))
        untilShown(
            "the version in view",
            hasText("0.1.0-alpha03", substring = true) and !hasAnyAncestor(hasTestTag("Site_toc_v010alpha03"))
        )
    }

    @Test
    fun the_page_is_copied_for_an_assistant() = siteAt(Route.Changelog) {
        press(hasPantrixTag("Button", "copyPage"))
        until("the copy") { copied() != null }
        assertTrue("0.1.0" in copied().orEmpty(), copied())
    }
}
