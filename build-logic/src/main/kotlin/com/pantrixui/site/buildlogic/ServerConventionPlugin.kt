package com.pantrixui.site.buildlogic

import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.filter
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * The site's server, a Spring Boot application on the JVM that runs beside the site on its host: Spring's classes
 * opened for its proxies (the `kotlin-spring` plugin), its versions from Spring Boot's own list, and a jar that runs
 * by itself (`bootJar`). The site's address, `site.url`, is written in its `application.properties` for `@siteUrl@`.
 */
class ServerConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.kotlinJvm)
        pluginManager.alias(libs.plugins.kotlinSpring)
        pluginManager.alias(libs.plugins.springBoot)
        pluginManager.apply(QualityConventionPlugin::class)

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        }
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
        tasks.named<ProcessResources>("processResources") {
            val address = siteUrl
            inputs.property("siteUrl", address)
            filesMatching("application.properties") {
                filter<ReplaceTokens>("tokens" to mapOf("siteUrl" to address.get()))
            }
        }

        dependencies {
            add("implementation", platform(libs.spring.boot.dependencies))
            // Spring reads a Kotlin class's constructor and its parameters' defaults through Kotlin's reflection.
            add("implementation", libs.kotlin.reflect)
            add("testImplementation", libs.spring.boot.starter.test)
            add("testImplementation", libs.kotlin.test)
        }
    }
}
