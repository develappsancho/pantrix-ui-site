package com.pantrixui.site.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.HostTestBuilder
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/** The Android app: an activity that draws the site's app, and nothing else. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.androidApplication)
        pluginManager.alias(libs.plugins.composeCompiler)
        pluginManager.apply(QualityConventionPlugin::class)

        extensions.configure<ApplicationExtension> {
            namespace = SITE_APPLICATION_ID
            compileSdk = libs.versions.android.compileSdk.get().toInt()
            defaultConfig {
                applicationId = SITE_APPLICATION_ID
                minSdk = libs.versions.android.minSdk.get().toInt()
                targetSdk = libs.versions.android.targetSdk.get().toInt()
                versionCode = 1
                versionName = "1.0"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
            buildFeatures {
                compose = true
            }
            // AGP's Compose Preview task (generate<Variant>ComposePreviewRunfiles), made for every variant with Compose,
            // draws the previews with a unit test's compiled resources: it fails as it is created, and the IDE's sync
            // with it, where the unit tests leave them out.
            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
        }

        // The same task needs the variant's unit tests, which AGP turns off for every build type but the tested one,
        // release among them.
        extensions.configure<ApplicationAndroidComponentsExtension> {
            beforeVariants { variant -> variant.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable = true }
        }

        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        }

        dependencies {
            add("implementation", project(":app:shared"))
            add("implementation", libs.androidx.activity.compose)
            add("debugImplementation", libs.compose.uiTooling)
        }
    }
}
