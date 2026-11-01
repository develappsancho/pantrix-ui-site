package com.pantrixui.site.buildlogic

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/** The version catalogue, as a build script reads it. */
internal val Project.libs: LibrariesForLibs
    get() = extensions.getByName("libs") as LibrariesForLibs

/** Applies the plugin a catalogue entry names. */
internal fun PluginManager.alias(plugin: Provider<PluginDependency>) {
    apply(plugin.get().pluginId)
}

/**
 * The package a module's code, Android namespace and generated resources live in, from its Gradle path, so that a new
 * module needs nothing of its own for them: `:core:base` is `com.pantrixui.site.core.base`.
 */
internal val Project.siteNamespace: String
    get() = SITE_PACKAGE + path.replace(':', '.').replace("-", "")

internal const val SITE_PACKAGE = "com.pantrixui.site"

/** The site's application id on Android and its bundle id on iOS. */
internal const val SITE_APPLICATION_ID = "com.pantrixui.site"

/** The modules every feature builds on. A feature never depends on another feature. */
internal val FEATURE_FOUNDATIONS = listOf(
    ":core:base",
    ":core:navigation",
    ":core:model",
    ":core:routing",
    ":core:i18n",
    ":core:designsystem",
    ":core:content"
)

internal val KotlinMultiplatformExtension.commonMain: KotlinSourceSet
    get() = sourceSets.getByName("commonMain")

internal val KotlinMultiplatformExtension.commonTest: KotlinSourceSet
    get() = sourceSets.getByName("commonTest")

internal val KotlinMultiplatformExtension.jvmTest: KotlinSourceSet
    get() = sourceSets.getByName("jvmTest")

/** Compose's desktop runtime for the machine the build runs on: what a JVM that draws needs. */
internal val Project.composeDesktopCurrentOs: String
    get() = extensions.getByType(ComposeExtension::class.java).dependencies.desktop.currentOs

internal fun DependencyHandler.siteProject(path: String) = project(mapOf("path" to path))
