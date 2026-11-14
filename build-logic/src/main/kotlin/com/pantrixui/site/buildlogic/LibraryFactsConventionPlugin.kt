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
 * What the site says of the library, counted at build time from the library itself: how many components it has, as
 * the `com.pantrixui:mcp` jar of the catalogue's version lists them (the list the MCP server answers with), written
 * as `BUILT_COMPONENT_COUNT` in the module's common code. A release with more components is counted again, and no
 * page writes the number by hand.
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
            description = "Counts the library's components in its com.pantrixui:mcp jar, into the module's code."
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
 * Writes how many components the library in [library] has as the constant `BUILT_COMPONENT_COUNT` of [packageName],
 * in a source folder of its own: the length of the `components` list of the jar's `pantrix-ui/pantrix-ui.json`.
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
        val text = ZipFile(jar).use { zip ->
            val entry = checkNotNull(zip.getEntry(LIBRARY)) { "${jar.name} has no $LIBRARY" }
            zip.getInputStream(entry).readBytes().decodeToString()
        }
        val components = (JsonSlurper().parseText(text) as Map<*, *>)["components"] as? List<*>
        val count = checkNotNull(components) { "$LIBRARY in ${jar.name} lists no components" }.size
        check(count > 0) { "$LIBRARY in ${jar.name} lists no components" }
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
            """.trimMargin()
        )
    }

    private companion object {
        /** What the library is, its components among it, in the jar. */
        const val LIBRARY = "pantrix-ui/pantrix-ui.json"
    }
}
