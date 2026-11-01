package com.pantrixui.site.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * One kind of page: its screens, its ViewModels and its part of the dependency graph. A feature builds on the core
 * modules ([FEATURE_FOUNDATIONS]) and never on another feature; it reaches another page by a route.
 */
class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(KmpLibraryConventionPlugin::class)
        pluginManager.apply(KmpComposeConventionPlugin::class)
        pluginManager.alias(libs.plugins.kotlinSerialization)

        extensions.configure<KotlinMultiplatformExtension> {
            commonMain.dependencies {
                FEATURE_FOUNDATIONS.forEach { implementation(project(it)) }
                implementation(libs.pantrix.components)
                implementation(libs.koin.compose)
                implementation(libs.koin.composeViewmodel)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.kotlinx.serialization.json)
            }
            commonTest.dependencies {
                implementation(project(":core:testing"))
                implementation(libs.pantrix.testing)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
