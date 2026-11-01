package com.pantrixui.site.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * A Kotlin Multiplatform module of the site, on every target the site runs on: Android, iOS on a device and in the
 * simulator, the desktop (JVM) and the web. The web is Wasm alone: a browser too old for it is given the page's static
 * HTML instead.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.kotlinMultiplatform)
        pluginManager.alias(libs.plugins.androidKmpLibrary)
        pluginManager.apply(QualityConventionPlugin::class)

        extensions.configure<KotlinMultiplatformExtension> {
            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
                namespace = siteNamespace
                compileSdk = libs.versions.android.compileSdk.get().toInt()
                minSdk = libs.versions.android.minSdk.get().toInt()
                // Android's host tests are a source-set tree of their own: on the common tree, every commonTest
                // test would run a second time under plain JUnit, where a UI test has no Android to draw on. The
                // common tests run on the JVM, in the browser and on iOS; a test of Android's own goes here.
                withHostTestBuilder { sourceSetTreeName = ANDROID_HOST_TEST_TREE }
                    .configure { isIncludeAndroidResources = true }
            }
            iosArm64()
            iosSimulatorArm64()
            jvm()
            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser()
                // As the library's modules do: a browser test of Compose bundles Skiko with webpack only for an
                // executable (CMP-4906), and without it Compose's check stops the run.
                binaries.executable()
            }

            commonTest.dependencies {
                implementation(libs.kotlin.test)
            }
        }

        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    private companion object {
        const val ANDROID_HOST_TEST_TREE = "androidHost"
    }
}
