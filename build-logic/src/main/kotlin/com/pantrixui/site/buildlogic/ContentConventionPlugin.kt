package com.pantrixui.site.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension

/**
 * The site's content, written at build time by `:tools:content` from the pages in the repository's `content`
 * folder, and given to the module as its Compose resources: one JSON file for each page in each language, read when
 * the page is opened.
 */
class ContentConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val tool = configurations.create("siteContentTool") {
            isCanBeConsumed = false
            isCanBeResolved = true
        }
        dependencies.add(tool.name, dependencies.siteProject(":tools:content"))
        // The library's documents: the guides and the changelog in both languages. The jar alone, not what it runs on.
        val documents = configurations.create("siteLibraryDocuments") {
            isCanBeConsumed = false
            isCanBeResolved = true
            isTransitive = false
        }
        dependencies.add(documents.name, libs.pantrix.mcp)

        val generate = tasks.register<GenerateSiteContent>("generateSiteContent") {
            group = "site"
            description = "Writes the site's pages as the JSON the app reads."
            classpath = tool
            mainClass.set(TOOL_MAIN)
            pagesDirectory.set(rootProject.layout.projectDirectory.dir("content"))
            libraryDocuments.from(documents)
            outputDirectory.set(layout.buildDirectory.dir("generated/siteContent"))
        }

        (extensions.getByType(ComposeExtension::class.java) as ExtensionAware)
            .extensions.configure<ResourcesExtension> {
                customDirectory("commonMain", generate.flatMap { it.outputDirectory })
            }
    }

    private companion object {
        const val TOOL_MAIN = "com.pantrixui.site.tools.content.MainKt"
    }
}

/**
 * Runs the content tool: the pages in [pagesDirectory] and the library's documents in [libraryDocuments] in, the
 * app's content in [outputDirectory] out.
 */
abstract class GenerateSiteContent : JavaExec() {
    /** The pages the site writes itself, in Markdown: `content/<language>/<route>.md`. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val pagesDirectory: DirectoryProperty

    /** The library's documents, the `com.pantrixui:mcp` jar: the guides and the changelog come from it. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val libraryDocuments: ConfigurableFileCollection

    /** Where the content goes: the Compose resources of the module, under `files/content`. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        argumentProviders.add(
            CommandLineArgumentProvider {
                listOf(
                    "--pages",
                    pagesDirectory.get().asFile.absolutePath,
                    "--docs",
                    libraryDocuments.singleFile.absolutePath,
                    "--out",
                    outputDirectory.get().asFile.absolutePath
                )
            }
        )
    }
}
