package com.pantrixui.site

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every screen of the site has a test that presses through it: each `…Screen` the features declare, `HomeScreen`, has
 * a `…PageTest` beside this one, `HomePageTest`, as every screen has its previews (`SitePreviewsTest`). A new screen
 * fails this until its test is written.
 */
class PageTestsTest {
    @Test
    fun every_screen_has_a_test_that_presses_through_it() {
        val screens = File(ROOT, "feature").listFiles().orEmpty()
            .flatMap { File(it, MAIN).walk().filter { file -> file.extension == "kt" }.toList() }
            .flatMap { file -> SCREEN.findAll(file.readText()).map { it.groupValues[1] } }
            .distinct()
        assertTrue(screens.size >= SCREENS, "${screens.size} screens")
        val missing = screens.map { it.removeSuffix("Screen") + "PageTest" }.filter { !File(TESTS, "$it.kt").isFile }
        assertEquals(emptyList(), missing)
    }

    private companion object {
        /** The repository's root, from the module the test runs in. */
        val ROOT = File("../..")
        val TESTS = File("src/jvmTest/kotlin/com/pantrixui/site")
        const val MAIN = "src/commonMain/kotlin"

        /** The site's screens: 15 on 2026-11-02. */
        const val SCREENS = 15

        /** A screen: a public composable whose name ends in `Screen`, as `SitePreviewsTest` finds them. */
        val SCREEN = Regex("""^fun (\w+Screen)\(""", RegexOption.MULTILINE)
    }
}
