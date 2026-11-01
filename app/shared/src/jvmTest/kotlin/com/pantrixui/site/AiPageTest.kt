package com.pantrixui.site

import androidx.compose.ui.test.hasTestTag
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertTrue

/** The page for coding assistants: its clients, its files, its conversation played again, and the way to ask. */
class AiPageTest {
    @Test
    fun each_client_s_setup_is_shown_in_turn() = siteAt(Route.Ai) {
        (0..3).forEach { index ->
            val client = hasTestTag("Site_client_$index")
            press(client)
            until("client $index shown") { chosen(client) }
        }
    }

    @Test
    fun its_files_and_llms_txt_open_outside_the_site() = siteAt(Route.Ai) {
        press(hasPantrixTag("Button", "aiReadLlms"))
        until("llms.txt opened") { services.opened.any { it.endsWith("llms.txt") } }
        press(hasPantrixTag("Button", "file_0"))
        press(hasPantrixTag("Button", "file_2"))
        until("two files opened") { services.opened.size == 3 }
        assertTrue(services.opened.all { it.startsWith("http") }, "${services.opened}")
        assertTrue(address() == "/ai")
    }

    @Test
    fun the_conversation_is_played_again() = siteAt(Route.Ai) {
        val replay = hasPantrixTag("Button", "aiReplay")
        scrollTo(replay)
        val shown = words()
        press(replay)
        until("the conversation from its start") { words() != shown }
        until("the conversation to its end") { words() == shown }
    }

    @Test
    fun its_ways_to_ask_lead_to_the_page_that_asks_the_server() = siteAt(Route.Ai) {
        press(hasTestTag("Site_aiOwnQuestion"))
        untilAt("/ai/ask")
        back()
        untilAt("/ai")
        press(hasPantrixTag("Link", "aiAsk"))
        untilAt("/ai/ask")
    }
}
