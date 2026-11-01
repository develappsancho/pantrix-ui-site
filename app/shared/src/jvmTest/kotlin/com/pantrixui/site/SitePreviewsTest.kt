package com.pantrixui.site

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import java.io.File
import java.lang.reflect.Method
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The previews of the site's screens and of its design system's parts, as the IDE draws them: each one found among
 * the modules' sources, called as the IDE calls a preview, through the function the Compose compiler made of it, at
 * the size its annotation gives, and drawn without failing. Every screen has four, in a `…Previews.kt` beside it: the
 * desktop's page and the phone's, each in the light theme and the dark one.
 */
@OptIn(ExperimentalTestApi::class)
class SitePreviewsTest {
    /** The sources of every module that has previews: the features' and the design system's. */
    private val sources: List<File> = File(ROOT, "feature").listFiles().orEmpty().map { File(it, MAIN) } +
        File(ROOT, "core/designsystem/$MAIN")

    /** Each preview: the class its file compiles to, its name, and the size its annotation gives. */
    private val previews: List<Found> = sources.flatMap { it.walk() }
        .filter { it.name.endsWith("Previews.kt") }
        .flatMap { file ->
            val text = file.readText()
            val owner = "${PACKAGE.find(text)!!.groupValues[1]}.${file.nameWithoutExtension}Kt"
            val constants = SIZES + CONSTANT.findAll(text).associate { it.groupValues[1] to it.groupValues[2].toInt() }
            PREVIEW.findAll(text).map { match ->
                val (width, height, name) = match.destructured
                Found(owner, name, constants.getValue(width), constants.getValue(height))
            }
        }
        .toList()

    @Test
    fun every_screen_has_the_desktop_s_and_the_phone_s_page_in_both_themes() {
        val screens = sources.flatMap { it.walk() }.filter { it.extension == "kt" }
            .flatMap { file -> SCREEN.findAll(file.readText()).map { it.groupValues[1] } }
            .toList()
        assertTrue(screens.size >= SCREENS, "${screens.size} screens")
        val names = previews.map { it.name }.toSet()
        val missing = screens.flatMap { screen -> KINDS.map { "$screen${it}Preview" } }.filter { it !in names }
        assertEquals(emptyList(), missing)
    }

    @Test
    fun every_preview_draws() {
        assertTrue(previews.size >= SCREENS * KINDS.size, "${previews.size} previews")
        previews.forEach { preview ->
            val method = Class.forName(preview.owner)
                .getDeclaredMethod(preview.name, Composer::class.java, Int::class.javaPrimitiveType)
                .apply { isAccessible = true }
            runDesktopComposeUiTest(preview.width, preview.height) {
                // A page's motion never ends, and a clock that waited for it would not either.
                mainClock.autoAdvance = false
                setContent { Call(method) }
                repeat(FRAMES) { mainClock.advanceTimeBy(FRAME_TIME) }
            }
        }
    }

    /** A preview: the function [name] of the class [owner], drawn [width] by [height]. */
    private data class Found(
        val owner: String,
        val name: String,
        val width: Int,
        val height: Int
    )

    private companion object {
        /** The repository's root, from the shared module the test runs in, and where a module's own code is. */
        val ROOT = File("../..")
        const val MAIN = "src/commonMain/kotlin"

        /** The screens the site has at least, and the four previews each one has. */
        const val SCREENS = 13
        val KINDS = listOf("", "Dark", "Phone", "PhoneDark")

        /** The sizes the design system names, which a preview's annotation gives by their names. */
        val SIZES = mapOf(
            "PREVIEW_DESKTOP_WIDTH" to 1440,
            "PREVIEW_DESKTOP_HEIGHT" to 900,
            "PREVIEW_PHONE_WIDTH" to 390,
            "PREVIEW_PHONE_HEIGHT" to 844
        )

        /** How many steps of the test's clock a preview is given to settle, and how long each is. */
        const val FRAMES = 4
        const val FRAME_TIME = 100L

        val PACKAGE = Regex("""^package ([\w.]+)""", RegexOption.MULTILINE)
        val PREVIEW = Regex("""@Preview\(widthDp = (\w+), heightDp = (\w+)\)\s+@Composable\s+private fun (\w+)\(\)""")
        val CONSTANT = Regex("""^private const val (\w+) = (\d+)$""", RegexOption.MULTILINE)
        val SCREEN = Regex("""^fun (\w+Screen)\(""", RegexOption.MULTILINE)
    }
}

/** Calls [method], a preview the Compose compiler made, with this composition's composer: what the IDE does. */
@Composable
private fun Call(method: Method) {
    val preview = remember { method }
    preview.invoke(null, currentComposer, 0)
}
