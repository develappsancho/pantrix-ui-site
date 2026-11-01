package com.pantrixui.site

import androidx.compose.ui.test.hasTestTag
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test

/** The window simulator: a device chosen, an app chosen, the app used inside it, and the way to the guides. */
class AdaptivePageTest {
    @Test
    fun each_device_is_chosen_in_turn() = siteAt(Route.Adaptive) {
        listOf("phone", "phoneside", "book", "table", "tablet", "laptop", "desktop").forEach { device ->
            val choice = hasTestTag("Site_device_$device")
            press(choice)
            until("the $device chosen") { chosen(choice) }
        }
    }

    @Test
    fun the_video_call_takes_the_inbox_s_place_and_gives_it_back() = siteAt(Route.Adaptive) {
        until("the inbox") { has(MAIL) }
        val call = hasPantrixTag("Segment", "videocall", scope = "SegmentButton_simapp")
        press(call)
        until("the video call alone") { chosen(call) && !has(MAIL) }
        val inbox = hasPantrixTag("Segment", "inbox", scope = "SegmentButton_simapp")
        press(inbox)
        until("the inbox again") { chosen(inbox) && has(MAIL) }
    }

    @Test
    fun the_inbox_s_sidebar_and_its_mail_are_used_inside_the_simulator() = siteAt(Route.Adaptive) {
        val orders = hasPantrixTag("Item", "orders", scope = "Sidebar_simsidebar")
        press(orders)
        until("the orders chosen") { chosen(orders) }
        val inbox = hasPantrixTag("Item", "inbox", scope = "Sidebar_simsidebar")
        press(inbox)
        until("the inbox chosen") { chosen(inbox) }
        press(MAIL)
        until("the simulator stays on its page") { address() == "/adaptive" }
    }

    @Test
    fun it_leads_to_the_guides() = siteAt(Route.Adaptive) {
        press(hasTestTag("Site_adaptiveGuides"))
        until("a guide") { address().startsWith("/docs/guides") }
    }

    private companion object {
        val MAIL = hasPantrixTag("ListItem", "mail1")
    }
}
