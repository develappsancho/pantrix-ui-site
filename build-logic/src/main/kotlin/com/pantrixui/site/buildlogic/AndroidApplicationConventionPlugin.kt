package com.pantrixui.site.buildlogic

import com.android.build.api.dsl.ApplicationExtension
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
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
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
