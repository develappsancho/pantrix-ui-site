package com.pantrixui.site.buildlogic

import org.gradle.api.DefaultTask
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
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import javax.inject.Inject

/**
 * The web app: the site's app in the browser, as Wasm, and as JavaScript for a browser that cannot run the Wasm build.
 * The JavaScript build is copied beside the Wasm one, and the page's own `site.js` chooses: the Wasm build where the
 * browser has Wasm's garbage collection, the JavaScript one where it has not, or where the Wasm build fails before it
 * has drawn. A browser that runs neither, or no script, is given each page's static HTML, which the build writes beside
 * the app: after each of the two distributions, `:tools:prerender` writes every page's `index.html` in the folder of
 * its address, with the `404.html`, `sitemap.xml` and `robots.txt`. `:tools:render` draws the app on the JVM for what
 * the static pages cannot read from the content: the landing pages' words, and each page's share image. Those two run
 * only when what they read has changed. `loaderTest`, part of `check`, tests `site.js` in a browser made of stand-ins;
 * `loaderBrowserTest`, run by hand, tests it in Chrome on the production build.
 */
class WebApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.kotlinMultiplatform)
        pluginManager.alias(libs.plugins.composeMultiplatform)
        pluginManager.alias(libs.plugins.composeCompiler)
        pluginManager.apply(QualityConventionPlugin::class)
        pluginManager.apply(RelativeKlibPathsConventionPlugin::class)

        extensions.configure<KotlinMultiplatformExtension> {
            js {
                browser {
                    commonWebpackConfig {
                        outputFileName = JS_BUNDLE
                    }
                }
                binaries.executable()
            }
            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser {
                    commonWebpackConfig {
                        outputFileName = WASM_BUNDLE
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
        DISTRIBUTIONS.forEach { (distribution, folder, jsDistribution) ->
            val name = "prerender" + distribution.capitalized()
            val prerender = tasks.register<PrerenderSite>(name) {
                group = "site"
                description = "Writes the site's static pages beside the web app of $distribution."
                classpath = tool
                mainClass.set(PRERENDER_MAIN)
                dependsOn(":core:content:generateSiteContent")
                contentDirectory.set(rootProject.layout.projectDirectory.dir(CONTENT))
                template.set(layout.projectDirectory.file("src/webMain/resources/index.html"))
                site.set(siteUrl)
                landingWords.set(landing.flatMap { it.wordsFile })
                libraryDocuments.from(documents)
                shareImages.set(shareManifest)
                outputDirectory.set(layout.buildDirectory.dir("dist/wasmJs/$folder"))
            }
            val copyShare = tasks.register<Copy>("copyShareImages${name.removePrefix("prerender")}") {
                from(share)
                into(layout.buildDirectory.dir("dist/wasmJs/$folder"))
            }
            // The JavaScript build beside the Wasm one: its script and the Wasm of Skiko it draws with. Its page, styles
            // and resources are the Wasm build's own already. The JavaScript build is one the Wasm distribution depends
            // on, not one this copy does: Gradle cannot run a finalizer that waits on tasks with finalizers of their own.
            val copyJs = tasks.register<Copy>("copyJsApp${name.removePrefix("prerender")}") {
                mustRunAfter(jsDistribution)
                from(layout.buildDirectory.dir("dist/js/$folder")) {
                    // Skiko's Wasm by the name of its hash, the one the script asks for; `skiko.wasm` it asks for only
                    // where a page names its own place for it.
                    include(JS_BUNDLE, "*.wasm")
                    exclude("skiko.wasm")
                }
                into(layout.buildDirectory.dir("dist/wasmJs/$folder"))
            }
            share.configure { mustRunAfter(prerender) }
            prerender.configure { finalizedBy(copyShare) }
            copyJs.configure { mustRunAfter(prerender, copyShare) }
            tasks.matching { it.name == distribution }.configureEach {
                dependsOn(jsDistribution)
                finalizedBy(prerender, copyJs)
            }
        }

        val node = NodeJsPlugin.apply(this)
        val nodeSetup = with(node) { project.nodeJsSetupTaskProvider }
        val loaderTests = layout.projectDirectory.dir("src/loaderTest")
        val loaderTest = tasks.register<LoaderTest>("loaderTest") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Tests the page's loader, site.js, in a browser made of stand-ins."
            dependsOn(nodeSetup)
            this.node.set(node.executable)
            tests.from(loaderTests.file("site.test.mjs"))
            loader.from(layout.projectDirectory.file(LOADER))
            stamp.set(layout.buildDirectory.file("loaderTest/passed.txt"))
        }
        tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { dependsOn(loaderTest) }
        tasks.register<LoaderTest>("loaderBrowserTest") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Tests the page's loader in Chrome, on the site as its production build writes it."
            dependsOn(nodeSetup, PRODUCTION)
            // What the distribution's finalizers write beside it: the static pages and the JavaScript build.
            mustRunAfter(listOf("prerender", "copyShareImages", "copyJsApp").map { it + PRODUCTION.capitalized() })
            this.node.set(node.executable)
            tests.from(loaderTests.file("browser.test.mjs"))
            site.set(layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
        }
    }

    private fun String.capitalized(): String = replaceFirstChar { it.uppercase() }

    private companion object {
        /** The Wasm build's script, which `site.js`, the script `index.html` loads, starts where the browser runs it. */
        const val WASM_BUNDLE = "site-wasm.js"

        /** The JavaScript build's script, which `site.js` starts where the browser cannot run the Wasm build. */
        const val JS_BUNDLE = "site-js.js"

        /** The page's own script, which starts the build the browser can run. */
        const val LOADER = "src/webMain/resources/site.js"

        /** The Wasm build's production distribution, the one the site deploys. */
        const val PRODUCTION = "wasmJsBrowserDistribution"

        const val PRERENDER_MAIN = "com.pantrixui.site.tools.prerender.MainKt"
        const val LANDING_MAIN = "com.pantrixui.site.tools.render.LandingMainKt"
        const val SHARE_MAIN = "com.pantrixui.site.tools.render.ShareMainKt"

        /** What `:core:content`'s content tool writes: the pages, the components and the changelog. */
        const val CONTENT = "core/content/build/generated/siteContent"

        /**
         * The web app's two distributions: the Wasm build's, the folder each is written to, and the JavaScript build's
         * that is copied into it.
         */
        val DISTRIBUTIONS = listOf(
            Triple(PRODUCTION, "productionExecutable", "jsBrowserDistribution"),
            Triple(
                "wasmJsBrowserDevelopmentExecutableDistribution",
                "developmentExecutable",
                "jsBrowserDevelopmentExecutableDistribution"
            )
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

/**
 * Runs tests of the page's loader with Node's own test runner. With a [stamp] it runs again only when a test or the
 * loader has changed; without one, as for the tests that open the built [site] in Chrome, each time it is asked for.
 */
abstract class LoaderTest : DefaultTask() {
    /** Node, as Kotlin's Gradle plugin installs it for the web builds. */
    @get:Input
    abstract val node: Property<String>

    /** The test files, run together. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val tests: ConfigurableFileCollection

    /** The loader the tests read, `site.js`. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val loader: ConfigurableFileCollection

    /** The built site the tests in Chrome open, given to them as `SITE_DIST`. */
    @get:Internal
    abstract val site: DirectoryProperty

    /** Written once the tests have passed. */
    @get:OutputFile
    @get:Optional
    abstract val stamp: RegularFileProperty

    @get:Inject
    protected abstract val execOperations: ExecOperations

    @TaskAction
    fun run() {
        execOperations.exec {
            executable = node.get()
            args(listOf("--test") + tests.files.map { it.absolutePath })
            site.orNull?.let { environment("SITE_DIST", it.asFile.absolutePath) }
        }
        stamp.orNull?.asFile?.writeText("The loader's tests passed.\n")
    }
}
