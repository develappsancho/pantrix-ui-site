package com.pantrixui.site.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * The web app: the site's app in the browser, as Wasm. A browser without Wasm is given each page's static HTML, which
 * the build writes beside the app.
 */
class WebApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.kotlinMultiplatform)
        pluginManager.alias(libs.plugins.composeMultiplatform)
        pluginManager.alias(libs.plugins.composeCompiler)
        pluginManager.apply(QualityConventionPlugin::class)

        extensions.configure<KotlinMultiplatformExtension> {
            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser {
                    commonWebpackConfig {
                        outputFileName = WEB_BUNDLE
                    }
                }
                binaries.executable()
            }
            commonMain.dependencies {
                implementation(project(":app:shared"))
                implementation(libs.compose.ui)
            }
        }
    }

    private companion object {
        /** The script `index.html` loads. */
        const val WEB_BUNDLE = "site.js"
    }
}
