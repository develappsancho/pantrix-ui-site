package com.pantrixui.site

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.SystemTheme
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.pantrixui.foundation.date.LocalPantrixClock
import com.pantrixui.foundation.date.PantrixClock
import com.pantrixui.foundation.files.LocalFileSaver
import com.pantrixui.foundation.platform.LocalClipboardTraits
import com.pantrixui.site.core.content.AskRepository
import com.pantrixui.site.core.content.ReleaseRepository
import com.pantrixui.site.core.model.DesktopRelease
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.navigation.NavigationHistory
import com.pantrixui.site.core.navigation.Navigator
import com.pantrixui.testing.TestClipboard
import com.pantrixui.testing.TestFileSaver
import com.pantrixui.testing.hasPantrixTag
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import kotlin.test.fail
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Starts the site's dependencies for the tests that draw it, once for the JVM: Koin keeps one graph for the whole
 * process. The desktop app's release is a fixed one, so the download page is drawn the same whatever the network
 * answers, and the library's MCP server is one of the test's own, [TEST_ASK_SERVER], so that no test asks the network.
 */
internal fun startSiteForTests() {
    if (GlobalContext.getOrNull() == null) {
        startSiteKoin(
            module {
                single<ReleaseRepository> { ReleaseRepository { SCREEN_RELEASE } }
                single<AskRepository> { TEST_ASK_SERVER }
            }
        )
    }
}

/**
 * The site opened at [route] in [language], with the system's theme set to [dark] or light, and the screen's clock on a
 * fixed day: on the desktop the system's theme is the machine's and the day is today, and a picture must change with
 * neither. With [services], the site copies, saves and opens links into them rather than the machine's, and they keep
 * where the site is.
 */
@Suppress("DEPRECATION") // Compose's own local for the system's theme, the one way to set it on the desktop.
@OptIn(InternalComposeUiApi::class)
@Composable
internal fun PageAt(
    route: Route,
    language: Language,
    dark: Boolean,
    services: SiteServices? = null
) {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides owner,
        LocalSystemTheme provides if (dark) SystemTheme.Dark else SystemTheme.Light,
        LocalPantrixClock provides SCREEN_CLOCK
    ) {
        val history = remember { StartAt(route, services) }
        if (services == null) {
            SiteApp(history = history, startLanguage = language)
        } else {
            CompositionLocalProvider(
                LocalClipboard provides services.clipboard,
                LocalClipboardTraits provides services.clipboard.traits,
                LocalFileSaver provides services.saver,
                LocalUriHandler provides services.links
            ) {
                SiteApp(history = history, startLanguage = language)
            }
        }
    }
}

/**
 * What a test gives the site in the place of the machine's: a clipboard, a saver of files and a handler of the links
 * it opens outside itself, each of which keeps what it was given, and the site's navigator, which says where it is.
 */
internal class SiteServices {
    val clipboard = TestClipboard()
    val saver = TestFileSaver()

    /** The addresses the site opened outside itself, in order. */
    val opened = mutableListOf<String>()

    val links = object : UriHandler {
        override fun openUri(uri: String) {
            opened += uri
        }
    }

    var navigator: Navigator<Route>? = null
    var pathOf: (Route) -> String = { "" }
}

/** The desktop app's release the download page shows: a day's version, its disk image's size and hash. */
internal val SCREEN_RELEASE = DesktopRelease(
    version = "26.10.31",
    year = 2026,
    month = 10,
    day = 31,
    notes = "https://github.com/develappsancho/pantrix-ui-site/releases/tag/v26.10.31",
    macSize = 92_143_171,
    macSha256 = "0e6bb0079ca77d507192d5b8530f5897002a62ae4ab8d02dde71863f766707a5"
)

/** The day the pages are drawn on, noon in UTC: a calendar marks it as today. */
private val SCREEN_CLOCK = PantrixClock(
    clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-10-30T12:00:00Z")
    },
    utcOffset = { Duration.ZERO }
)

/** A history that opens the site at [route] and keeps nothing more, but hands [services] the site's navigator. */
private class StartAt(
    private val route: Route,
    private val services: SiteServices?
) : NavigationHistory<Route> {
    override fun startStack(default: List<Route>): List<Route> = listOf(route)

    @Composable
    override fun Bind(
        navigator: Navigator<Route>,
        pathOf: (Route) -> String
    ) {
        services?.navigator = navigator
        services?.pathOf = pathOf
    }
}

/** The windows a test draws the site in: a phone's, a tablet's held upright and a desktop's. */
internal enum class SiteSize(
    val width: Int,
    val height: Int
) {
    Phone(390, 844),
    Tablet(834, 1194),
    Desktop(1440, 900)
}

/**
 * Draws the site at [route] in a window of [size], in [language] and the light theme, with the test's own
 * [SiteServices], and runs [check] on it once its header is there.
 */
@OptIn(ExperimentalTestApi::class)
internal fun siteAt(
    route: Route,
    size: SiteSize = SiteSize.Desktop,
    language: Language = Language.English,
    check: SiteScreen.() -> Unit
) = runDesktopComposeUiTest(size.width, size.height) {
    startSiteForTests()
    // A page's motion never ends, and a clock that waited for it would not either.
    mainClock.autoAdvance = false
    val services = SiteServices()
    setContent { PageAt(route, language, dark = false, services = services) }
    val screen = SiteScreen(this, services)
    screen.until("the site's header") { screen.has(hasPantrixTag("IconButton", "theme")) }
    screen.check()
}

/**
 * The site on a test's screen, with the services it was given, and what a test does with it. A page's motion never
 * ends, so the test's clock is driven by hand: [until] gives the site real time to read its content and the clock a
 * step for its frames, until what the test waits for is there. Compose's own `performScrollTo` asks a scrollable to
 * scroll and looks again at once, which with such a clock it would do for ever: [scrollTo] steps the clock while the
 * page scrolls.
 */
@OptIn(ExperimentalTestApi::class)
internal class SiteScreen(
    test: ComposeUiTest,
    val services: SiteServices
) : ComposeUiTest by test {
    /** Waits until [done] holds: real time for what the site reads, then a step of the test's clock for its frames. */
    fun until(
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

    /** Steps the clock a while, for what a press sets moving to come to rest. */
    fun settle() = repeat(SCROLL_FRAMES) { mainClock.advanceTimeBy(FRAME_TIME) }

    /** Whether a node [matcher] finds is on the screen's tree, in view or not. */
    fun has(matcher: SemanticsMatcher): Boolean =
        onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    /** How many nodes [matcher] finds on the screen's tree. */
    fun count(matcher: SemanticsMatcher): Int = onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().size

    /**
     * Waits until the node [matcher] finds is inside the window and has stopped moving: a menu, a sheet or a search
     * slides in, and a press is given where the node stands. A node out of view is scrolled to again: while a page
     * goes, the one before it is still on the screen, and its node is the one found at first.
     */
    fun untilShown(
        what: String,
        matcher: SemanticsMatcher
    ) {
        var last: Rect? = null
        until(what) {
            if (placed(matcher)?.whole?.let { window().contains(it) } == false) scrollTo(matcher)
            val bounds = placed(matcher)?.whole
            val settled = bounds != null && !bounds.isEmpty && bounds == last && window().contains(bounds)
            last = bounds
            settled
        }
    }

    /**
     * Scrolls the page until the first node [matcher] finds is inside its viewport: the outermost scrollable around it
     * that scrolls up and down, the page's own rather than an example's, is told the distance, and then the nearest
     * that scrolls sideways, as a row of chips does; the clock is stepped while each scrolls there.
     */
    fun scrollTo(matcher: SemanticsMatcher) {
        until("${matcher.description} in reach") { has(matcher) }
        bring(matcher, vertical = true)
        bring(matcher, vertical = false)
    }

    private fun bring(
        matcher: SemanticsMatcher,
        vertical: Boolean
    ) {
        repeat(STEPS) {
            val (scrollable, distance) = stepTo(matcher, vertical) ?: return
            runOnUiThread {
                val action = scrollable.config[SemanticsActions.ScrollBy].action
                if (vertical) action?.invoke(0f, distance) else action?.invoke(distance, 0f)
            }
            repeat(SCROLL_FRAMES) { mainClock.advanceTimeBy(FRAME_TIME) }
        }
        fail("${matcher.description} never came into view")
    }

    /**
     * The scrollable that brings the node [matcher] finds into view, up and down or sideways, and how far it scrolls:
     * none where the node is in view already, or nothing around it scrolls that way.
     */
    private fun stepTo(
        matcher: SemanticsMatcher,
        vertical: Boolean
    ): Pair<SemanticsNode, Float>? {
        val node = placed(matcher)
        val around = generateSequence(node?.parent) { it.parent }
        val scrollable = if (vertical) {
            around.lastOrNull {
                it.scrollsUpAndDown
            }
        } else {
            around.firstOrNull { it.scrollsSideways }
        }
        if (node == null || scrollable == null) return null
        val viewport = scrollable.boundsInRoot
        val target = node.whole
        val start = if (vertical) target.top - viewport.top else target.left - viewport.left
        val end = if (vertical) target.bottom - viewport.bottom else target.right - viewport.right
        val distance = when {
            start < 0 -> start
            end > 0 -> minOf(end, start)
            else -> 0f
        }
        return (scrollable to distance).takeIf { distance != 0f }
    }

    /** Brings the first node [matcher] finds into view, waits for it to stand still and presses it. */
    fun press(matcher: SemanticsMatcher) {
        scrollTo(matcher)
        untilShown("${matcher.description} to press", matcher)
        val nodes = onAllNodes(matcher, useUnmergedTree = true)
        val target = placed(matcher)?.id
        nodes[nodes.fetchSemanticsNodes().indexOfFirst { it.id == target }].performClick()
    }

    /**
     * The first node [matcher] finds that is placed on the screen with a size: a component that measures what it
     * shows before it places it, as a breadcrumb that folds by its width does, holds copies of its parts that are
     * never placed.
     */
    private fun placed(matcher: SemanticsMatcher): SemanticsNode? {
        val nodes = onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes()
        return nodes.firstOrNull { it.layoutInfo.isPlaced && it.size.width > 0 && it.size.height > 0 }
            ?: nodes.firstOrNull()
    }

    /** Brings the field [matcher] finds into view and types [text] into it, after what it already holds. */
    fun type(
        matcher: SemanticsMatcher,
        text: String
    ) {
        scrollTo(matcher)
        onAllNodes(matcher).onFirst().performTextInput(text)
    }

    /** Brings the field [matcher] finds into view and puts [text] in it in place of what it holds. */
    fun replace(
        matcher: SemanticsMatcher,
        text: String
    ) {
        scrollTo(matcher)
        onAllNodes(matcher).onFirst().performTextReplacement(text)
    }

    /** The site's address now, as its pages are written: `/docs/installation`. */
    fun address(): String {
        var path = ""
        runOnUiThread { path = services.navigator?.current?.let(services.pathOf).orEmpty() }
        return path
    }

    /** Waits until the site is at [path], and its page has come in. */
    fun untilAt(path: String) {
        until("the page at $path") { address() == path }
        settle()
    }

    /** Goes back, as the system's Back does. */
    fun back() = runOnUiThread { services.navigator?.back() }

    /** What the site put on the clipboard last, or null where it put nothing. */
    fun copied(): String? = runBlocking { services.clipboard.text() }

    /**
     * What the node [matcher] finds says: its words, or its name where it has none, with what it holds merged into it,
     * as a screen reader reads it.
     */
    fun said(matcher: SemanticsMatcher): String {
        val merged = onAllNodes(matcher).fetchSemanticsNodes().firstOrNull()
        val config = (merged ?: onAllNodes(matcher, useUnmergedTree = true).onFirst().fetchSemanticsNode()).config
        return config.getOrNull(SemanticsProperties.EditableText)?.text
            ?: config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }
            ?: config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString().orEmpty()
    }

    /** Whether the first node [matcher] finds is chosen: a segment, a tab, a radio button. */
    fun chosen(matcher: SemanticsMatcher): Boolean =
        onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().firstOrNull()
            ?.config?.getOrNull(SemanticsProperties.Selected) == true

    /** Whether the first node [matcher] finds is on: a switch, a checkbox or a chip that is ticked. */
    fun on(matcher: SemanticsMatcher): Boolean =
        onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().firstOrNull()
            ?.config?.getOrNull(SemanticsProperties.ToggleableState) == ToggleableState.On

    /** Every word the screen says, in the order of its tree. */
    fun words(): List<String> =
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text].map { it.text } }

    /** How bright the window's corner is, as the sum of its red, green and blue. */
    fun brightness(): Float {
        val pixel = onRoot().captureToImage().toPixelMap()[CORNER, CORNER]
        return pixel.red + pixel.green + pixel.blue
    }

    /** The window's bounds: its first root's, a popup's being another. */
    fun window(): Rect = onAllNodes(isRoot()).fetchSemanticsNodes().first().boundsInRoot

    private fun tagsOnScreen(): List<String> =
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.TestTag), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .mapNotNull { it.config.getOrNull(SemanticsProperties.TestTag) }
            .take(TAGS_SHOWN)

    private companion object {
        /** A pixel of the header, inside its corner, that no word or icon covers. */
        const val CORNER = 2

        /** How many times the site is given real time and its clock a step, and how much of each. */
        const val STEPS = 150
        const val READ_TIME = 40L
        const val FRAME_TIME = 100L

        /** How many steps of the clock a scroll, or a press, is given before the page is looked at again. */
        const val SCROLL_FRAMES = 10

        const val TAGS_SHOWN = 40
    }
}

/** The node's place in the window, whole: its `boundsInRoot` is clipped, to nothing where it is out of view. */
private val SemanticsNode.whole: Rect
    get() = Rect(positionInRoot, Size(size.width.toFloat(), size.height.toFloat()))

private val SemanticsNode.scrollsUpAndDown: Boolean
    get() = SemanticsActions.ScrollBy in config && SemanticsProperties.VerticalScrollAxisRange in config

private val SemanticsNode.scrollsSideways: Boolean
    get() = SemanticsActions.ScrollBy in config && SemanticsProperties.HorizontalScrollAxisRange in config

private fun Rect.contains(other: Rect): Boolean =
    other.left >= left && other.top >= top && other.right <= right && other.bottom <= bottom
