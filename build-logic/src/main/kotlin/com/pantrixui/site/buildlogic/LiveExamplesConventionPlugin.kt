package com.pantrixui.site.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * The examples the module runs, written at build time by `:tools:content` as Kotlin, from the library's documents and
 * the site's own examples in `content/examples`, and compiled with the module's own: the code a component's page
 * shows under each one it draws.
 */
class LiveExamplesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val tool = configurations.create("liveExamplesTool") {
            isCanBeConsumed = false
            isCanBeResolved = true
        }
        dependencies.add(tool.name, dependencies.siteProject(":tools:content"))
        val documents = configurations.create("liveExamplesDocuments") {
            isCanBeConsumed = false
            isCanBeResolved = true
            isTransitive = false
        }
        dependencies.add(documents.name, libs.pantrix.mcp)

        val generate = tasks.register<GenerateLiveExamples>("generateLiveExamples") {
            group = "site"
            description = "Writes the examples the site runs as Kotlin."
            classpath = tool
            mainClass.set(TOOL_MAIN)
            libraryDocuments.from(documents)
            examplesDirectory.set(rootProject.layout.projectDirectory.dir("content/examples"))
            outputDirectory.set(layout.buildDirectory.dir("generated/liveExamples"))
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").kotlin.srcDir(generate.flatMap { it.outputDirectory })
        }
    }

    private companion object {
        const val TOOL_MAIN = "com.pantrixui.site.tools.content.LiveMainKt"
    }
}

/** Runs the content tool's writer of the examples: the library's documents in, Kotlin in [outputDirectory] out. */
abstract class GenerateLiveExamples : JavaExec() {
    /** The library's documents, the `com.pantrixui:mcp` jar. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val libraryDocuments: ConfigurableFileCollection

    /** The site's own examples, `content/examples/<language>/<key>.md`. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val examplesDirectory: DirectoryProperty

    /** Where the Kotlin goes. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        argumentProviders.add(
            CommandLineArgumentProvider {
                listOf(
                    "--docs",
                    libraryDocuments.singleFile.absolutePath,
                    "--examples",
                    examplesDirectory.get().asFile.absolutePath,
                    "--out",
                    outputDirectory.get().asFile.absolutePath
                )
            }
        )
    }
}
