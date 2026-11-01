package com.pantrixui.site.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * The web app: the site's app in the browser, as Wasm. A browser without Wasm is given each page's static HTML, which
 * the build writes beside the app: after each of the two distributions, `:tools:prerender` writes every page's
 * `index.html` in the folder of its address, with the `404.html`, `sitemap.xml` and `robots.txt`. `:tools:render`
 * draws the app on the JVM for what the static pages cannot read from the content: the landing pages' words, and each
 * page's share image. Those two run only when what they read has changed.
 */
class WebApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.kotlinMultiplatform)
        pluginManager.alias(libs.plugins.composeMultiplatform)
        pluginManager.alias(libs.plugins.composeCompiler)
        pluginManager.apply(QualityConventionPlugin::class)

        extensions.configure<KotlinMultiplatformExtension> {
            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser {
                    commonWebpackConfig {
                        outputFileName = WEB_BUNDLE
                    }
                }
                binaries.executable()
            }
            commonMain.dependencies {
                implementation(project(":app:shared"))
                implementation(libs.compose.ui)
            }
        }

        val tool = configurations.create("sitePrerenderTool") {
            isCanBeConsumed = false
            isCanBeResolved = true
        }
        dependencies.add(tool.name, dependencies.siteProject(":tools:prerender"))
        // The library's documents for coding assistants: the jar alone, not what it runs on.
        val documents = configurations.create("sitePrerenderDocuments") {
            isCanBeConsumed = false
            isCanBeResolved = true
            isTransitive = false
        }
        dependencies.add(documents.name, libs.pantrix.mcp)
        val render = configurations.create("siteRenderTool") {
            isCanBeConsumed = false
            isCanBeResolved = true
        }
        dependencies.add(render.name, dependencies.siteProject(":tools:render"))
        val built = layout.buildDirectory.dir("site")
        val landing = tasks.register<ReadLandingWords>("readLandingWords") {
            group = "site"
            description = "Reads the landing pages' words from the app, for their static pages."
            classpath = render
            mainClass.set(LANDING_MAIN)
            wordsFile.set(built.map { it.file("landing.json") })
        }
        val shareManifest = built.map { it.file("share.json") }
        val share = tasks.register<DrawShareImages>("drawShareImages") {
            group = "site"
            description = "Draws the image a shared link of each page shows."
            classpath = render
            mainClass.set(SHARE_MAIN)
            manifest.set(shareManifest)
            outputDirectory.set(built.map { it.dir("share") })
        }
        DISTRIBUTIONS.forEach { (distribution, folder) ->
            val name = "prerender" + distribution.replaceFirstChar { it.uppercase() }
            val prerender = tasks.register<PrerenderSite>(name) {
                group = "site"
                description = "Writes the site's static pages beside the web app of $distribution."
                classpath = tool
                mainClass.set(PRERENDER_MAIN)
                dependsOn(":core:content:generateSiteContent")
                contentDirectory.set(rootProject.layout.projectDirectory.dir(CONTENT))
                template.set(layout.projectDirectory.file("src/wasmJsMain/resources/index.html"))
                site.set(SITE)
                landingWords.set(landing.flatMap { it.wordsFile })
                libraryDocuments.from(documents)
                shareImages.set(shareManifest)
                outputDirectory.set(layout.buildDirectory.dir("dist/wasmJs/$folder"))
            }
            val copyShare = tasks.register<Copy>("copyShareImages${name.removePrefix("prerender")}") {
                from(share)
                into(layout.buildDirectory.dir("dist/wasmJs/$folder"))
            }
            share.configure { mustRunAfter(prerender) }
            prerender.configure { finalizedBy(copyShare) }
            tasks.matching { it.name == distribution }.configureEach { finalizedBy(prerender) }
        }
    }

    private companion object {
        /** The script `index.html` loads. */
        const val WEB_BUNDLE = "site.js"

        /** The site's address, which its pages' links, its sitemap and what a shared link shows are written with. */
        const val SITE = "https://pantrixui.com"

        const val PRERENDER_MAIN = "com.pantrixui.site.tools.prerender.MainKt"
        const val LANDING_MAIN = "com.pantrixui.site.tools.render.LandingMainKt"
        const val SHARE_MAIN = "com.pantrixui.site.tools.render.ShareMainKt"

        /** What `:core:content`'s content tool writes: the pages, the components and the changelog. */
        const val CONTENT = "core/content/build/generated/siteContent"

        /** The web app's two distributions, each with the folder it is written to. */
        val DISTRIBUTIONS = listOf(
            "wasmJsBrowserDistribution" to "productionExecutable",
            "wasmJsBrowserDevelopmentExecutableDistribution" to "developmentExecutable"
        )
    }
}

/**
 * Runs the prerender tool: the content tool's output in [contentDirectory] and the app's [template] in, the static
 * pages in [outputDirectory], the web app's folder, out. They are written into the distribution's own folder after
 * it, so the task has no outputs of its own and runs each time the distribution does.
 */
abstract class PrerenderSite : JavaExec() {
    /** What the content tool wrote: the pages, the components and the changelog, in both languages. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val contentDirectory: DirectoryProperty

    /** The app's `index.html`, each page's file is written from. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val template: RegularFileProperty

    /** The site's address, with no slash at its end. */
    @get:Input
    abstract val site: Property<String>

    /** The landing pages' words, as `readLandingWords` read them from the app. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val landingWords: RegularFileProperty

    /** The library's documents, the `com.pantrixui:mcp` jar: `llms.txt` and what it leads to come from it. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val libraryDocuments: ConfigurableFileCollection

    /** Where the list of the pages' share images goes, for `drawShareImages`. */
    @get:Internal
    abstract val shareImages: RegularFileProperty

    /** The web app's folder, where the pages go beside it. */
    @get:Internal
    abstract val outputDirectory: DirectoryProperty

    init {
        argumentProviders.add(
            CommandLineArgumentProvider {
                listOf(
                    "--content",
                    contentDirectory.get().asFile.absolutePath,
                    "--template",
                    template.get().asFile.absolutePath,
                    "--site",
                    site.get(),
                    "--out",
                    outputDirectory.get().asFile.absolutePath
                ) + landingWords.orNull?.let { listOf("--landing", it.asFile.absolutePath) }.orEmpty() +
                    shareImages.orNull?.let { listOf("--share", it.asFile.absolutePath) }.orEmpty() +
                    libraryDocuments.singleOrNull()?.let { listOf("--docs", it.absolutePath) }.orEmpty()
            }
        )
    }
}

/** Runs `:tools:render`'s landing reader: the app's code in, the landing pages' words in [wordsFile] out. */
abstract class ReadLandingWords : JavaExec() {
    /** Where the words go: JSON of each language's code to each page's path to its words in HTML. */
    @get:OutputFile
    abstract val wordsFile: RegularFileProperty

    init {
        argumentProviders.add(CommandLineArgumentProvider { listOf("--out", wordsFile.get().asFile.absolutePath) })
    }
}

/** Runs `:tools:render`'s share image drawer: the list in [manifest] in, the images in [outputDirectory] out. */
abstract class DrawShareImages : JavaExec() {
    /** The share images to draw, as the prerender tool lists them. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val manifest: RegularFileProperty

    /** Where the images go, each at its own path: `og/en/index.png`. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        argumentProviders.add(
            CommandLineArgumentProvider {
                listOf(
                    "--manifest",
                    manifest.get().asFile.absolutePath,
                    "--out",
                    outputDirectory.get().asFile.absolutePath
                )
            }
        )
    }
}
