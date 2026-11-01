package com.pantrixui.site

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

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
    fun references() = capture("references", Route.References)

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
            startSiteForTests()
        }
    }
}
