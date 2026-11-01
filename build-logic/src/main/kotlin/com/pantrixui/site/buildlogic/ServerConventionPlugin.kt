package com.pantrixui.site.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * The site's server, a Spring Boot application on the JVM that runs beside the site on its host: Spring's classes
 * opened for its proxies (the `kotlin-spring` plugin), its versions from Spring Boot's own list, and a jar that runs
 * by itself (`bootJar`). The site's address, `site.url`, is written as a resource of its own, `site.properties`, which
 * `application.properties` imports: a resource written, not one filtered, so that the IDE builds the module as Gradle
 * does.
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
        val siteProperties = tasks.register<WriteSiteProperties>("writeSiteProperties") {
            group = "site"
            description = "Writes the site's address, site.url in gradle.properties, as the server's site.properties."
            address.set(siteUrl)
            outputDirectory.set(layout.buildDirectory.dir("generated/siteResources"))
        }
        extensions.configure<SourceSetContainer> {
            named("main") { resources.srcDir(siteProperties) }
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

/**
 * Writes `site.properties`, the server's resource that holds [address] as `pantrix.site.built-url`: the address the
 * site is built for, which the server allows where the stack gives none of its own.
 */
abstract class WriteSiteProperties : DefaultTask() {
    /** The site's address, with no slash at its end. */
    @get:Input
    abstract val address: Property<String>

    /** The resource folder the file is written in. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun write() {
        outputDirectory.get().asFile.resolve("site.properties").writeText(
            "# The address the site is built for, site.url in gradle.properties: written by the build.\n" +
                "pantrix.site.built-url=${address.get()}\n"
        )
    }
}
