package com.pantrixui.site

import androidx.compose.ui.test.hasContentDescription
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test

/** The components' page: the filter by name, the groups, the featured cards, the list and the live previews. */
class ComponentsPageTest {
    @Test
    fun the_filter_keeps_the_components_it_names() = siteAt(Route.Components) {
        type(FILTER, "date picker")
        until("two components") { has(hasPantrixTag("Button", "group_all") and hasContentDescription("All, 2")) }
        until("Date Picker's card alone of the elements") { !has(card("avatar")) }
    }

    @Test
    fun a_group_keeps_its_own_components() = siteAt(Route.Components) {
        until("the elements") { has(card("avatar")) }
        press(hasPantrixTag("Button", "group_chart"))
        until("the charts alone") { has(card("line_chart")) && !has(card("avatar")) }
        press(hasPantrixTag("Button", "group_all"))
        until("every group again") { has(card("avatar")) && has(card("line_chart")) }
    }

    @Test
    fun a_featured_card_and_a_listed_one_open_their_pages() = siteAt(Route.Components) {
        press(hasPantrixTag("Card", "featured_button"))
        untilAt("/components/button")
        back()
        untilAt("/components")
        press(card("avatar"))
        untilAt("/components/avatar")
    }

    @Test
    fun a_featured_card_s_preview_runs_live() = siteAt(Route.Components) {
        val day = hasPantrixTag("Day", "2026_10_15", scope = "DatePicker_preview_day")
        press(day)
        until("the day chosen") { chosen(day) }
    }

    @Test
    fun a_phone_lists_the_components_by_group_and_a_row_opens_its_page() = siteAt(Route.Components, SiteSize.Phone) {
        press(hasPantrixTag("ComponentRow", "badge"))
        untilAt("/components/badge")
    }

    private companion object {
        val FILTER = hasPantrixTag("TextField", "componentFilter")

        /** The card of the component whose page is [key], in the list of every component. */
        fun card(key: String) = hasPantrixTag("Card", "component_$key")
    }
}
