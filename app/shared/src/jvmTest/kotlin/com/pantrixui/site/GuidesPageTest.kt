package com.pantrixui.site

import androidx.compose.ui.test.hasContentDescription
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertEquals

/** The guides' page: the filter by name, the topics, and the cards that open a guide. */
class GuidesPageTest {
    @Test
    fun the_filter_keeps_the_guides_it_names() = siteAt(Route.Guides) {
        until("every guide") { has(guide("adaptive")) }
        type(hasPantrixTag("TextField", "guideFilter"), "keyboard")
        until("the keyboard's guides alone") {
            has(guide("keyboard_dismissal")) && has(guide("keyboard_shortcuts")) && !has(guide("adaptive"))
        }
    }

    @Test
    fun a_topic_keeps_its_own_guides() = siteAt(Route.Guides) {
        press(hasPantrixTag("Button", "topic_platform"))
        until("the platform's services alone") { !has(guide("adaptive")) && has(guide("clipboard")) }
        assertEquals(4, count(hasPantrixTag("Card") and !hasPantrixTag("Card", "guidesForAssistants") and !HUB))
        press(hasPantrixTag("Button", "topic_all"))
        until("every guide again") { has(guide("adaptive")) }
    }

    @Test
    fun a_card_opens_its_guide() = siteAt(Route.Guides) {
        press(guide("testing"))
        untilAt("/docs/guides/testing")
        back()
        untilAt("/docs/guides")
        press(hasPantrixTag("Card", "guidesForAssistants"))
        until("the page for assistants") { address() != "/docs/guides" }
    }

    private companion object {
        /** The card of the guide [key] names. */
        fun guide(key: String) = hasPantrixTag("Card", "guide_$key")

        /** The cards of the docs' frame, which are not the guides'. */
        val HUB = hasContentDescription("Components", substring = true)
    }
}
