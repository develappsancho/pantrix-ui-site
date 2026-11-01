package com.pantrixui.site.buildlogic

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * The pages' screenshots with Roborazzi, drawn by `jvmTest` on the desktop's Skia, in `screenshots/jvm/`:
 * `./gradlew :app:shared:recordRoborazziJvm` writes them, and `verifyRoborazziJvm` compares the pages with them.
 *
 * A test class whose name ends in `ScreenshotTest` runs only for Roborazzi, in its tasks or with its `roborazzi.test.*`
 * properties: it draws every page at three sizes, which takes minutes, and a plain `jvmTest` would draw them for
 * nothing, since only Roborazzi looks at them.
 */
class ScreenshotConventionPlugin : Plugin<Project> {
    @OptIn(ExperimentalRoborazziApi::class)
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.roborazzi)

        extensions.configure<RoborazziExtension> {
            outputDir.set(layout.projectDirectory.dir(SCREENSHOTS_DIR))
            // One folder for each platform's task, as in the library: the JVM's alone today.
            separateOutputDirs.set(true)
        }
        extensions.configure<KotlinMultiplatformExtension> {
            jvmTest.dependencies {
                implementation(libs.roborazzi.composeDesktop)
                // The site's lifecycle runs on the main dispatcher, which the desktop takes from Swing, as its app does.
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        // A Roborazzi task, or a test task told to record, verify or compare as the library's guide runs one.
        val roborazzi = gradle.startParameter.taskNames.any { ROBORAZZI in it } ||
            MODES.any { providers.gradleProperty("roborazzi.test.$it").isPresent }
        tasks.withType<Test>().configureEach {
            // A file name given to captureRoboImage lands in the output directory, not beside build.gradle.kts.
            systemProperty(FILE_PATH_STRATEGY, FILE_PATH_STRATEGY_OUTPUT_DIR)
            if (!roborazzi) exclude(SCREENSHOT_TESTS)
        }
    }

    private companion object {
        const val SCREENSHOTS_DIR = "screenshots"
        const val ROBORAZZI = "Roborazzi"
        const val SCREENSHOT_TESTS = "**/*ScreenshotTest*"
        val MODES = listOf("record", "verify", "compare")
        const val FILE_PATH_STRATEGY = "roborazzi.record.filePathStrategy"
        const val FILE_PATH_STRATEGY_OUTPUT_DIR = "relativePathFromRoborazziContextOutputDirectory"
    }
}
