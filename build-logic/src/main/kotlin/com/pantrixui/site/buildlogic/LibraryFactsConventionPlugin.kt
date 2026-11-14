package com.pantrixui.site.buildlogic

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.util.zip.ZipFile

/**
 * What the site says of the library, counted at build time from the library itself, the `com.pantrixui:mcp` jar of the
 * catalogue's version: how many components it has, as the jar lists them (the list the MCP server answers with), and
 * how many guides for apps it has in both languages, written as `BUILT_COMPONENT_COUNT` and `BUILT_GUIDE_COUNT` in the
 * module's common code. A release with more of either is counted again, and no page writes the number by hand.
 */
class LibraryFactsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        // The library's documents, the jar alone, not what it runs on.
        val documents = configurations.create("siteLibraryFacts") {
            isCanBeConsumed = false
            isCanBeResolved = true
            isTransitive = false
        }
        dependencies.add(documents.name, libs.pantrix.mcp)
        val write = tasks.register<WriteLibraryFacts>("writeLibraryFacts") {
            group = "site"
            description = "Counts the library's components and guides in its com.pantrixui:mcp jar, into the code."
            library.from(documents)
            packageName.set(siteNamespace)
            outputDirectory.set(layout.buildDirectory.dir("generated/libraryFacts"))
        }
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain").kotlin.srcDir(write.flatMap { it.outputDirectory })
            }
        }
    }
}

/**
 * Writes how many components and guides the library in [library] has as the constants `BUILT_COMPONENT_COUNT` and
 * `BUILT_GUIDE_COUNT` of [packageName], in a source folder of its own: the length of the `components` list of the
 * jar's `pantrix-ui/pantrix-ui.json`, and the guides of `pantrix-ui/guides/` that `pantrix-ui/tr/guides/` has too.
 */
abstract class WriteLibraryFacts : DefaultTask() {
    /** The `com.pantrixui:mcp` jar. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val library: ConfigurableFileCollection

    /** The package the constant is written in. */
    @get:Input
    abstract val packageName: Property<String>

    /** The source folder the file is written in. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun write() {
        val jar = library.singleFile
        val (text, names) = ZipFile(jar).use { zip ->
            val entry = checkNotNull(zip.getEntry(LIBRARY)) { "${jar.name} has no $LIBRARY" }
            zip.getInputStream(entry).readBytes().decodeToString() to
                zip.entries().asSequence().map { it.name }.toList()
        }
        val components = (JsonSlurper().parseText(text) as Map<*, *>)["components"] as? List<*>
        val count = checkNotNull(components) { "$LIBRARY in ${jar.name} lists no components" }.size
        check(count > 0) { "$LIBRARY in ${jar.name} lists no components" }

        fun guides(folder: String) = names.filter { it.startsWith(folder) && it.endsWith(".md") }
            .map { it.removePrefix(folder) }.filter { '/' !in it }.toSet()
        val guides = (guides(ENGLISH_GUIDES) intersect guides(TURKISH_GUIDES)).size
        check(guides > 0) { "${jar.name} has no guides in both languages" }
        val root = outputDirectory.get().asFile
        root.deleteRecursively()
        val folder = root.resolve(packageName.get().replace('.', '/')).apply { mkdirs() }
        folder.resolve("BuiltLibraryFacts.kt").writeText(
            """
            |package ${packageName.get()}
            |
            |/**
            | * How many components the library has: the list of `$LIBRARY` in the `com.pantrixui:mcp` jar of the
            | * catalogue's version, counted by the build, and not edited here.
            | */
            |internal const val BUILT_COMPONENT_COUNT: Int = $count
            |
            |/**
            | * How many guides for apps the library has in both languages: those of `$ENGLISH_GUIDES` that
            | * `$TURKISH_GUIDES` has too, in the same jar, counted by the build, and not edited here.
            | */
            |internal const val BUILT_GUIDE_COUNT: Int = $guides
            |
            """.trimMargin()
        )
    }

    private companion object {
        /** What the library is, its components among it, in the jar. */
        const val LIBRARY = "pantrix-ui/pantrix-ui.json"

        /** The folders of the library's guides for apps in the jar, in English and in Turkish. */
        const val ENGLISH_GUIDES = "pantrix-ui/guides/"
        const val TURKISH_GUIDES = "pantrix-ui/tr/guides/"
    }
}
