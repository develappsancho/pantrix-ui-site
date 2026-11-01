package com.pantrixui.site.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * The site's address in a module's code: `site.url` in `gradle.properties`, written at build time as
 * `BUILT_SITE_ADDRESS` in the module's common code. An app, which is opened at no address, gives this one; the web
 * app gives the address it was opened at.
 */
class SiteAddressConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val write = tasks.register<WriteSiteAddress>("writeSiteAddress") {
            group = "site"
            description = "Writes the site's address, site.url in gradle.properties, into the module's code."
            address.set(siteUrl)
            packageName.set(siteNamespace)
            outputDirectory.set(layout.buildDirectory.dir("generated/siteAddress"))
        }
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain").kotlin.srcDir(write.flatMap { it.outputDirectory })
            }
        }
    }
}

/** Writes [address] as the constant `BUILT_SITE_ADDRESS` of [packageName], in a source folder of its own. */
abstract class WriteSiteAddress : DefaultTask() {
    /** The site's address, with no slash at its end. */
    @get:Input
    abstract val address: Property<String>

    /** The package the constant is written in. */
    @get:Input
    abstract val packageName: Property<String>

    /** The source folder the file is written in. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun write() {
        val root = outputDirectory.get().asFile
        root.deleteRecursively()
        val folder = root.resolve(packageName.get().replace('.', '/')).apply { mkdirs() }
        folder.resolve("BuiltSiteAddress.kt").writeText(
            """
            |package ${packageName.get()}
            |
            |/** The site's address, `site.url` in `gradle.properties`: written by the build, and not edited here. */
            |internal const val BUILT_SITE_ADDRESS: String = "${address.get()}"
            |
            """.trimMargin()
        )
    }
}
