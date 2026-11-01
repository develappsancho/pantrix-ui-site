package com.pantrixui.site

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.SystemTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.pantrixui.foundation.date.LocalPantrixClock
import com.pantrixui.foundation.date.PantrixClock
import com.pantrixui.site.core.content.ReleaseRepository
import com.pantrixui.site.core.model.DesktopRelease
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.navigation.NavigationHistory
import com.pantrixui.site.core.navigation.Navigator
import io.github.takahirom.roborazzi.captureRoboImage
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * One page of each kind the site has, as the desktop's Skia draws it: the first screen at a desktop's, a tablet's and a
 * phone's size, in the light theme and the dark one. Each is `<page>_<width>.png` and `<page>_<width>_dark.png`, the
 * Turkish's with `_tr` before the width, in `screenshots/jvm/`: `./gradlew :app:shared:recordRoborazziJvm` writes them,
 * and `verifyRoborazziJvm` compares the pages with them.
 *
 * The test's clock is driven by hand and a page is drawn after a fixed number of its steps, so a motion that never
 * ends, such as a showcase that turns, is drawn at the same moment each time. The screen's clock is a fixed day, so a
 * calendar marks the same today whenever the pages are drawn.
 */
@OptIn(ExperimentalTestApi::class)
class PageScreenshotTest {
    @Test
    fun home() = capture("home", Route.Home, turkishToo = true)

    /**
     * The home page in a window too narrow for its hero's two columns and too wide for a tablet's layout: a foldable
     * open on its side, the iPhone Duo's 951 by 669. The hero's words take the tablet's single column.
     */
    @Test
    fun home_between_a_tablet_and_a_desktop() = capture("home", Route.Home, sizes = listOf(FOLDABLE_ON_ITS_SIDE))

    @Test
    fun components() = capture("components", Route.Components)

    @Test
    fun component() = capture("component_button", Route.Component("button"))

    @Test
    fun component_with_the_site_s_own_examples() = capture("component_data_table", Route.Component("data-table"))

    @Test
    fun foundations() = capture("foundations", Route.Foundations)

    @Test
    fun theme() = capture("theme", Route.Theme)

    @Test
    fun adaptive() = capture("adaptive", Route.Adaptive)

    @Test
    fun ai() = capture("ai", Route.Ai)

    @Test
    fun ask() = capture("ask", Route.Ask)

    @Test
    fun roadmap() = capture("roadmap", Route.Roadmap)

    @Test
    fun download() = capture("download", Route.Download)

    @Test
    fun changelog() = capture("changelog", Route.Changelog)

    @Test
    fun doc() = capture("installation", Route.Doc("installation"), turkishToo = true)

    @Test
    fun guides() = capture("guides", Route.Guides)

    @Test
    fun guide() = capture("guide_testing", Route.Guide("testing"))

    @Test
    fun not_found() = capture("not_found", Route.NotFound("/no-such-page"))

    /** Draws [route] at each of [sizes] and in both themes, in English and, with [turkishToo], in Turkish as well. */
    private fun capture(
        name: String,
        route: Route,
        turkishToo: Boolean = false,
        sizes: List<Pair<Int, Int>> = SIZES
    ) {
        val languages = if (turkishToo) listOf(Language.English, Language.Turkish) else listOf(Language.English)
        languages.forEach { language ->
            val tongue = if (language == Language.English) "" else "_${language.code}"
            sizes.forEach { (width, height) ->
                draw("${name}${tongue}_$width.png", route, language, width, height, dark = false)
                draw("${name}${tongue}_${width}_dark.png", route, language, width, height, dark = true)
            }
        }
    }

    private fun draw(
        file: String,
        route: Route,
        language: Language,
        width: Int,
        height: Int,
        dark: Boolean
    ) = runDesktopComposeUiTest(width, height) {
        // A page's motion never ends, and a clock that waited for it would not either.
        mainClock.autoAdvance = false
        setContent { PageAt(route, language, dark) }
        repeat(FRAMES) {
            // Real time for what the page reads from its files, then a step of the test's clock for its frames.
            Thread.sleep(READ_TIME)
            mainClock.advanceTimeBy(FRAME_TIME)
        }
        onRoot().captureRoboImage(file)
    }

    private companion object {
        /** A desktop's first screen, a tablet's held upright, and a phone's. */
        val SIZES = listOf(1440 to 900, 834 to 1194, 390 to 844)

        /** A foldable's open screen on its side: the iPhone Duo's. */
        val FOLDABLE_ON_ITS_SIDE = 951 to 669

        /** How many times a page is given real time to read its content, and the test's clock a step for its frames. */
        const val FRAMES = 12
        const val READ_TIME = 250L
        const val FRAME_TIME = 400L

        init {
            // The site's dependencies, once for every page: Koin keeps one graph for the whole JVM. The desktop app's
            // release is a fixed one, so the download page is drawn the same whatever the network answers.
            if (GlobalContext.getOrNull() == null) {
                startSiteKoin(module { single<ReleaseRepository> { ReleaseRepository { SCREEN_RELEASE } } })
            }
        }
    }
}

/**
 * The site opened at [route] in [language], with the system's theme set to [dark] or light, and the screen's clock on a
 * fixed day: on the desktop the system's theme is the machine's and the day is today, and a picture must change with
 * neither.
 */
@Suppress("DEPRECATION") // Compose's own local for the system's theme, the one way to set it on the desktop.
@OptIn(InternalComposeUiApi::class)
@Composable
private fun PageAt(
    route: Route,
    language: Language,
    dark: Boolean
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
        SiteApp(history = remember { StartAt(route) }, startLanguage = language)
    }
}

/** The desktop app's release the download page shows: a day's version, its disk image's size and hash. */
private val SCREEN_RELEASE = DesktopRelease(
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

/** A history that opens the site at [route] and keeps nothing more. */
private class StartAt(
    private val route: Route
) : NavigationHistory<Route> {
    override fun startStack(default: List<Route>): List<Route> = listOf(route)

    @Composable
    override fun Bind(
        navigator: Navigator<Route>,
        pathOf: (Route) -> String
    ) = Unit
}
