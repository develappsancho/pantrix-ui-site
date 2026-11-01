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

/**
 * The desktop app: a window that draws the site's app, packaged for macOS, Windows and Linux. A release gives its
 * version as `-Psite.version=26.10.31` (`deploy/deploy.sh desktop`): three numbers, the first above 0, as macOS and an
 * MSI both take them. A build that is no release is `1.0.0`.
 */
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
                        packageVersion = target.providers.gradleProperty(SITE_VERSION_PROPERTY).getOrElse("1.0.0")
                        description = "Pantrix UI's website as a desktop app"
                        vendor = "Pantrix UI"
                        // The site's logo, three layers, on each platform; Linux's is the window's icon too.
                        macOS {
                            iconFile.set(target.file("icons/PantrixUI.icns"))
                            bundleID = SITE_APPLICATION_ID
                            appCategory = "public.app-category.developer-tools"
                            minimumSystemVersion = "13.0"
                        }
                        windows { iconFile.set(target.file("icons/PantrixUI.ico")) }
                        linux { iconFile.set(target.file("src/main/resources/PantrixUI.png")) }
                    }
                }
            }
    }
}

/** The Gradle property a release gives the desktop app's version in. */
private const val SITE_VERSION_PROPERTY = "site.version"
