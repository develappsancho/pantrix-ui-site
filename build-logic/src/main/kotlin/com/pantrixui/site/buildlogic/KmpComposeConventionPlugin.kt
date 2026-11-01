package com.pantrixui.site.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.js.testing.karma.KotlinKarma

/**
 * A module that draws: Compose Multiplatform, its compiler and its resources, with the tests that draw what they
 * check.
 */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.composeMultiplatform)
        pluginManager.alias(libs.plugins.composeCompiler)

        extensions.configure<KotlinMultiplatformExtension> {
            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
                androidResources.enable = true
            }
            commonMain.dependencies {
                api(libs.compose.runtime)
                api(libs.compose.ui)
                api(libs.compose.foundation)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
            }
            commonTest.dependencies {
                implementation(libs.compose.uiTest)
            }
            // runComposeUiTest draws for real: the test JVM needs Skiko's native binary for this machine.
            jvmTest.dependencies {
                implementation(composeDesktopCurrentOs)
            }
        }

        // The resources' accessors live in the module's own package, not in one derived from the build's name.
        (extensions.getByType(ComposeExtension::class.java) as ExtensionAware)
            .extensions.configure<ResourcesExtension> {
                packageOfResClass = "$siteNamespace.generated.resources"
            }

        // What Android Studio draws a preview with, on the module's own runtime classpath only.
        dependencies {
            add("androidRuntimeClasspath", libs.compose.uiTooling)
        }

        configureBrowserTests()
    }

    /**
     * The Karma setup of the Wasm browser tests, in `config/karma/wasm`: a Compose UI test drawn by Skiko in a browser
     * outgrows Mocha's two seconds with nothing wrong.
     */
    private fun Project.configureBrowserTests() {
        val karmaConfig = rootProject.layout.projectDirectory.dir("config/karma/wasm")
        tasks.withType<KotlinJsTest>().configureEach {
            if (compilation.target.platformType != KotlinPlatformType.wasm) return@configureEach
            inputs.dir(karmaConfig)
                .withPropertyName("karmaConfig")
                .withPathSensitivity(PathSensitivity.RELATIVE)
            (testFramework as? KotlinKarma)?.useConfigDirectory(karmaConfig.asFile)
            onTestFrameworkSet { (this as? KotlinKarma)?.useConfigDirectory(karmaConfig.asFile) }
        }
    }
}
