package com.pantrixui.site.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/** The desktop app: a window that draws the site's app, packaged for macOS, Windows and Linux. */
class DesktopApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.kotlinJvm)
        pluginManager.alias(libs.plugins.composeMultiplatform)
        pluginManager.alias(libs.plugins.composeCompiler)
        pluginManager.apply(QualityConventionPlugin::class)

        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        }

        dependencies {
            add("implementation", project(":app:shared"))
            add("implementation", composeDesktopCurrentOs)
            add("implementation", libs.kotlinx.coroutines.swing)
        }

        (extensions.getByType(ComposeExtension::class.java) as ExtensionAware)
            .extensions.configure<DesktopExtension> {
                application {
                    mainClass = "$SITE_PACKAGE.MainKt"
                    nativeDistributions {
                        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                        packageName = "Pantrix UI"
                        packageVersion = "1.0.0"
                    }
                }
            }
    }
}
