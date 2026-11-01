package com.pantrixui.site

import androidx.compose.ui.test.hasTestTag
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The page that asks the library's MCP server, here the test's own ([TEST_ASK_SERVER]): a question asked and answered,
 * a question tried from its suggestions, what it searches, its tabs, and the way back.
 */
class AskPageTest {
    @Test
    fun a_question_is_answered_with_the_component_and_its_page() = siteAt(Route.Ask) {
        type(QUESTION, "toast")
        press(hasPantrixTag("Button", "askSubmit"))
        until("the answer") { has(OPEN_PAGE) }
        assertTrue(TEST_ASK_SERVER.calls.any { it.startsWith("search_components(toast") }, "${TEST_ASK_SERVER.calls}")
        press(OPEN_PAGE)
        untilAt("/components/snackbar")
    }

    @Test
    fun a_suggestion_asks_its_question() = siteAt(Route.Ask) {
        val asked = TEST_ASK_SERVER.calls.size
        val suggestion = hasPantrixTag("Chip", "askTry_0")
        scrollTo(suggestion)
        val question = said(suggestion)
        press(suggestion)
        until("the suggestion asked") { said(QUESTION) == question && TEST_ASK_SERVER.calls.size > asked }
    }

    @Test
    fun what_it_searches_is_chosen() = siteAt(Route.Ask) {
        val api = hasPantrixTag("Chip", "askScope_api")
        press(api)
        until("the API chosen") { chosen(api) }
        val all = hasPantrixTag("Chip", "askScope_all")
        press(all)
        until("everything chosen") { chosen(all) }
    }

    @Test
    fun its_tabs_show_each_way_of_asking() = siteAt(Route.Ask) {
        listOf("assistant", "key", "server").forEach { key ->
            val tab = hasTestTag("Site_askTab_$key")
            press(tab)
            until("the $key tab chosen") { chosen(tab) }
        }
    }

    @Test
    fun it_leads_back_to_the_page_for_assistants() = siteAt(Route.Ask) {
        press(hasPantrixTag("Link", "askBack"))
        untilAt("/ai")
    }

    private companion object {
        val QUESTION = hasPantrixTag("TextField", "askQuestion")

        /** The answer's way to the component's page: a link on a desktop, a button on a phone. */
        val OPEN_PAGE = hasPantrixTag("Link", "askOpenPage") or hasPantrixTag("Button", "askOpenPage")
    }
}
