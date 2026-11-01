package com.pantrixui.site

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import com.pantrixui.testing.onPantrixNode
import kotlin.test.Test
import kotlin.test.fail

/**
 * The site's frame as a visitor uses it, drawn on the desktop's Skia at a phone's size, a tablet's and a desktop's: the
 * menu, the header's links and its tools, the language, the theme, the search, the components' filter and a
 * component's page.
 * Each test finds what it presses by the tag Pantrix UI gives a component, as the flows in `app/maestro` do on a
 * device; these run in every `jvmTest`.
 *
 * A page's motion never ends, so the test's clock is driven by hand: [until] gives the site real time to read its
 * content and the clock a step for its frames, until what the test waits for is there. Compose's own
 * `performScrollTo` asks a scrollable to scroll and looks again at once, which with such a clock it would do for
 * ever: [scrollTo] steps the clock while the page scrolls.
 */
@OptIn(ExperimentalTestApi::class)
class SiteAppTest {
    @Test
    fun the_menu_leads_to_a_page_and_its_own_button_closes_it() = phone(Route.Home) {
        onPantrixNode("IconButton", "openMenu").performClick()
        untilShown("the menu's Installation", INSTALLATION_ITEM)
        onNode(INSTALLATION_ITEM).performClick()
        until("the Installation page, the menu closed behind it") { has(INSTALLATION) && !has(MENU) }
        onPantrixNode("IconButton", "openMenu").performClick()
        until("the menu") { has(MENU) }
        untilShown("the menu's own button", hasPantrixTag("SheetClose", "menu"))
        onPantrixNode("SheetClose", "menu").performClick()
        until("the menu closed on the same page") { !has(MENU) && has(INSTALLATION) }
    }

    @Test
    fun the_brand_leads_home() = phone(Route.Components) {
        until("the components") { has(COMPONENT_FILTER) }
        onNode(hasTestTag("Site_home")).performClick()
        until("the home page") { has(hasPantrixTag("Button", "homeBrowse")) && !has(COMPONENT_FILTER) }
    }

    @Test
    fun the_language_menu_turns_the_words_into_turkish_and_back() = phone(Route.Home) {
        chooseLanguage("tr")
        onPantrixNode("IconButton", "search").assertContentDescriptionEquals("Belgelerde ara")
        onPantrixNode("IconButton", "openMenu").assertContentDescriptionEquals("Menüyü aç")
        chooseLanguage("en")
        onPantrixNode("IconButton", "search").assertContentDescriptionEquals("Search the docs")
    }

    @Test
    fun the_theme_button_turns_the_dark_theme_on_and_off_and_names_the_next() = phone(Route.Home) {
        val theme = onPantrixNode("IconButton", "theme")
        val light = brightness()
        theme.assertContentDescriptionEquals("Switch to the dark theme")
        theme.performClick()
        until("the dark theme") { brightness() < light / 2 }
        theme.assertContentDescriptionEquals("Switch to the light theme")
        theme.performClick()
        until("the light theme again") { brightness() == light }
        theme.assertContentDescriptionEquals("Switch to the dark theme")
    }

    @Test
    fun the_search_finds_a_component_keeps_the_components_and_opens_its_page() = phone(Route.Home) {
        onPantrixNode("IconButton", "search").performClick()
        untilShown("the search", SEARCH_FIELD)
        onNode(SEARCH_FIELD).performTextInput("date picker")
        until("the results") { has(result(0)) && has(result(3)) }
        untilShown("the components' filter", hasTestTag("Site_searchFilter_component"))
        onNode(hasTestTag("Site_searchFilter_component")).performClick()
        until("the three components alone") { has(result(2)) && !has(result(3)) }
        onNode(result(0)).performClick()
        until("Date Picker's page, the search closed") { has(current("Date Picker")) && !has(SEARCH) }
        // Cancel closes the search where it stands.
        onPantrixNode("IconButton", "search").performClick()
        untilShown("the search's Cancel", hasTestTag("Site_searchCancel"))
        onNode(hasTestTag("Site_searchCancel")).performClick()
        until("the search closed on the same page") { !has(SEARCH) && has(current("Date Picker")) }
    }

    @Test
    fun the_components_filter_keeps_the_components_it_names() = phone(Route.Components) {
        until("the components") { has(COMPONENT_FILTER) }
        onNode(COMPONENT_FILTER).performTextInput("date picker")
        until("two components") {
            has(hasPantrixTag("Button", "group_all") and hasContentDescription("All, 2"))
        }
    }

    @Test
    fun a_component_s_page_draws_its_examples_in_each_platform_s_preset_and_leads_to_its_api() =
        phone(Route.Component("button")) {
            val ios = hasPantrixTag("Segment", "ios", scope = "SegmentButton_exampleplatform")
            until("Button's examples") { has(ios) }
            // Every example has its own choice of platform: the first one's is pressed.
            scrollTo(ios)
            onAllNodes(ios).onFirst().performClick()
            until("the iOS preset chosen") { has(ios and SemanticsMatcher.expectValue(SELECTED, true)) }
            onAllNodes(ios).onFirst().assertIsSelected()
            scrollTo(hasPantrixTag("Button", "contents_api"))
            onPantrixNode("Button", "contents_api").performClick()
            val onClick = hasPantrixTag("Segment", "onclick", scope = "SegmentButton_forms")
            until("the API") { has(onClick) }
            scrollTo(onClick)
            onNode(onClick).performClick()
            until("the form of onClick chosen") { has(onClick and SemanticsMatcher.expectValue(SELECTED, true)) }
        }

    @Test
    fun the_header_s_links_lead_to_their_parts_on_a_desktop() = desktop(Route.Home) {
        untilShown("the header's links", hasPantrixTag("Button", "nav_components"))
        onPantrixNode("Button", "nav_components").performClick()
        until("the components") { has(COMPONENT_FILTER) }
        onPantrixNode("Button", "nav_references").performClick()
        until("the references") { has(hasText("Built with Pantrix UI")) }
        onNode(hasTestTag("Site_home")).performClick()
        until("the home page") { has(hasPantrixTag("Button", "homeBrowse")) }
    }

    @Test
    fun a_tablet_s_header_opens_the_search_from_its_box() = tablet(Route.Home) {
        untilShown("the header's search box", hasTestTag("Site_search"))
        onNode(hasTestTag("Site_search")).performClick()
        untilShown("the search", SEARCH_FIELD)
        onNode(SEARCH_FIELD).performTextInput("button")
        until("the results") { has(result(0)) }
    }

    /** Draws the site at [route] at a phone's size, in English and the light theme, and runs [check] on it. */
    private fun phone(
        route: Route,
        check: ComposeUiTest.() -> Unit
    ) = site(route, PHONE, check)

    /** Draws the site at [route] at a tablet's size, held upright, in English and the light theme, and runs [check]. */
    private fun tablet(
        route: Route,
        check: ComposeUiTest.() -> Unit
    ) = site(route, TABLET, check)

    /** Draws the site at [route] at a desktop's size, in English and the light theme, and runs [check] on it. */
    private fun desktop(
        route: Route,
        check: ComposeUiTest.() -> Unit
    ) = site(route, DESKTOP, check)

    private fun site(
        route: Route,
        size: Pair<Int, Int>,
        check: ComposeUiTest.() -> Unit
    ) = runDesktopComposeUiTest(size.first, size.second) {
        mainClock.autoAdvance = false
        setContent { PageAt(route, Language.English, dark = false) }
        until("the site's header") { has(hasPantrixTag("IconButton", "theme")) }
        check()
    }

    /** Chooses the language [code] in the header's menu, and waits until the header's button names it. */
    private fun ComposeUiTest.chooseLanguage(code: String) {
        onPantrixNode("Button", "language").performClick()
        val item = hasPantrixTag("MenuItem", "language_$code", scope = "Menu_language")
        untilShown("the language menu", item)
        onNode(item).performClick()
        val chosen = hasPantrixTag("Button", "language") and
            hasContentDescription(": ${code.uppercase()}, ", substring = true)
        until("the header in $code") { has(chosen) && !has(item) }
    }

    /** How bright the header's corner is, as the sum of its red, green and blue. */
    private fun ComposeUiTest.brightness(): Float {
        val pixel = onRoot().captureToImage().toPixelMap()[CORNER, CORNER]
        return pixel.red + pixel.green + pixel.blue
    }

    /** Waits until [done] holds: real time for what the site reads, then a step of the test's clock for its frames. */
    private fun ComposeUiTest.until(
        what: String,
        done: () -> Boolean
    ) {
        repeat(STEPS) {
            if (done()) return
            Thread.sleep(READ_TIME)
            mainClock.advanceTimeBy(FRAME_TIME)
        }
        if (!done()) fail("$what did not come: the screen's tags are ${tagsOnScreen()}")
    }

    /**
     * Waits until the node [matcher] finds is inside the window and has stopped moving: a menu, a sheet or a search
     * slides in, and a press is given where the node stands.
     */
    private fun ComposeUiTest.untilShown(
        what: String,
        matcher: SemanticsMatcher
    ) {
        var last: Rect? = null
        until(what) {
            val bounds = onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().firstOrNull()?.whole
            val settled = bounds != null && !bounds.isEmpty && bounds == last && window().contains(bounds)
            last = bounds
            settled
        }
    }

    /**
     * Scrolls the page until the first node [matcher] finds is inside its viewport: the outermost scrollable around it
     * that scrolls up and down, the page's own rather than an example's, is told the distance, and the clock is stepped
     * while it scrolls there.
     */
    private fun ComposeUiTest.scrollTo(matcher: SemanticsMatcher) {
        var seen = ""
        repeat(STEPS) {
            val node = onAllNodes(matcher, useUnmergedTree = true).onFirst().fetchSemanticsNode()
            val scrollable = generateSequence(node.parent) { it.parent }.lastOrNull { it.scrollsUpAndDown }
                ?: fail("Nothing around ${matcher.description} scrolls up and down")
            val viewport = scrollable.boundsInRoot
            val target = node.whole
            val distance = when {
                target.top < viewport.top -> target.top - viewport.top
                target.bottom > viewport.bottom -> minOf(target.bottom - viewport.bottom, target.top - viewport.top)
                else -> return
            }
            seen = "at ${target.top} in ${viewport.top}..${viewport.bottom}, scrolled by $distance"
            runOnUiThread { scrollable.config[SemanticsActions.ScrollBy].action?.invoke(0f, distance) }
            repeat(SCROLL_FRAMES) { mainClock.advanceTimeBy(FRAME_TIME) }
        }
        fail("${matcher.description} never came into view: $seen")
    }

    /** The node's place in the window, whole: its `boundsInRoot` is clipped, to nothing where it is out of view. */
    private val SemanticsNode.whole: Rect
        get() = Rect(positionInRoot, Size(size.width.toFloat(), size.height.toFloat()))

    private val SemanticsNode.scrollsUpAndDown: Boolean
        get() = SemanticsActions.ScrollBy in config && SemanticsProperties.VerticalScrollAxisRange in config

    /** The window's bounds: its first root's, a popup's being another. */
    private fun ComposeUiTest.window(): Rect = onAllNodes(isRoot()).fetchSemanticsNodes().first().boundsInRoot

    private fun Rect.contains(other: Rect): Boolean =
        other.left >= left && other.top >= top && other.right <= right && other.bottom <= bottom

    private fun ComposeUiTest.has(matcher: SemanticsMatcher): Boolean =
        onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun ComposeUiTest.tagsOnScreen(): List<String> =
        onAllNodes(SemanticsMatcher.keyIsDefined(TEST_TAG), useUnmergedTree = true).fetchSemanticsNodes()
            .mapNotNull { it.config.getOrElseNullable(TEST_TAG) { null } }
            .take(TAGS_SHOWN)

    private companion object {
        val PHONE = 390 to 844
        val TABLET = 834 to 1194
        val DESKTOP = 1440 to 900

        val MENU = hasPantrixTag("Sheet", "menu")
        val INSTALLATION_ITEM = hasTestTag("Site_nav_installation")
        val INSTALLATION = hasPantrixTag("Button", "codetab0")
        val COMPONENT_FILTER = hasPantrixTag("TextField", "componentFilter")
        val SEARCH = hasPantrixTag("Modal", "search")
        val SEARCH_FIELD = hasPantrixTag("TextField", "search")
        val SELECTED = SemanticsProperties.Selected
        val TEST_TAG = SemanticsProperties.TestTag

        /** The search's result at [index], in the order it lists them. */
        fun result(index: Int) = hasPantrixTag("ListItem", "searchresult_$index")

        /** The breadcrumb's last item, the page's own name, saying [name]. */
        fun current(name: String) = hasPantrixTag("Item", "current", scope = "Breadcrumb_page") and
            (hasText(name) or hasAnyDescendant(hasText(name)))

        /** A pixel of the header, inside its corner, that no word or icon covers. */
        const val CORNER = 2

        /** How many times the site is given real time and its clock a step, and how much of each. */
        const val STEPS = 150
        const val READ_TIME = 40L
        const val FRAME_TIME = 100L

        /** How many steps of the clock a scroll is given before the page is looked at again. */
        const val SCROLL_FRAMES = 10

        const val TAGS_SHOWN = 40

        init {
            startSiteForTests()
        }
    }
}
