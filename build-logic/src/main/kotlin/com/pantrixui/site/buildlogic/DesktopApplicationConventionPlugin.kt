package com.pantrixui.site.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * The desktop app: a window that draws the site's app, packaged for macOS, Windows and Linux. A release gives its
 * version as `-Psite.version=26.10.31` (`deploy/deploy.sh desktop`): three numbers, the first above 0, as macOS and an
 * MSI both take them. A build that is no release is `1.0.0`. `smokeTest`, run by hand, opens the app as it is
 * packaged and lets it run.
 */
class DesktopApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.kotlinJvm)
        pluginManager.alias(libs.plugins.composeMultiplatform)
        pluginManager.alias(libs.plugins.composeCompiler)
        pluginManager.apply(QualityConventionPlugin::class)

        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        }

        dependencies {
            add("implementation", project(":app:shared"))
            add("implementation", composeDesktopCurrentOs)
            add("implementation", libs.kotlinx.coroutines.swing)
        }

        (extensions.getByType(ComposeExtension::class.java) as ExtensionAware)
            .extensions.configure<DesktopExtension> {
                application {
                    mainClass = "$SITE_PACKAGE.MainKt"
                    nativeDistributions {
                        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                        packageName = "Pantrix UI"
                        packageVersion = target.providers.gradleProperty(SITE_VERSION_PROPERTY).getOrElse("1.0.0")
                        description = "Pantrix UI's website as a desktop app"
                        vendor = "Pantrix UI"
                        // The site's logo, three layers, on each platform; Linux's is the window's icon too.
                        macOS {
                            iconFile.set(target.file("icons/PantrixUI.icns"))
                            bundleID = SITE_APPLICATION_ID
                            appCategory = "public.app-category.developer-tools"
                            minimumSystemVersion = "13.0"
                        }
                        windows { iconFile.set(target.file("icons/PantrixUI.ico")) }
                        linux { iconFile.set(target.file("src/main/resources/PantrixUI.png")) }
                    }
                }
            }

        tasks.register<DesktopSmokeTest>("smokeTest") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Opens the desktop app as createDistributable packages it, and lets it run."
            dependsOn(CREATE_DISTRIBUTABLE)
            distribution.set(layout.buildDirectory.dir(DISTRIBUTION))
            seconds.set(SMOKE_SECONDS)
        }
    }
}

/** The task that packages the desktop app, as the release's disk image takes it (`deploy/desktop/macos-dmg.sh`). */
private const val CREATE_DISTRIBUTABLE = "createDistributable"

/** Where [CREATE_DISTRIBUTABLE] writes the app. */
private const val DISTRIBUTION = "compose/binaries/main/app"

/** How long the smoke test lets the app run: past its first page, which it has drawn in about two seconds. */
private const val SMOKE_SECONDS = 15

/**
 * Opens the desktop app as it is packaged, the one in [distribution], and lets it run for [seconds]: it passes where
 * the app is still running then and has written no error, and the app is closed after. What it tries is what a
 * package can break and the tests on the JVM cannot: the runtime the package carries, its resources and Skiko's native
 * library. It opens a window on the screen while it runs.
 */
@DisableCachingByDefault(because = "It runs the app")
abstract class DesktopSmokeTest : DefaultTask() {
    /** The folder the packaged app is in: macOS's `.app`, or Linux's and Windows's folder of the app. */
    @get:Internal
    abstract val distribution: DirectoryProperty

    /** How long the app is let run. */
    @get:Input
    abstract val seconds: Property<Int>

    @TaskAction
    fun run() {
        val app = launcherIn(distribution.get().asFile)
        val output = temporaryDir.resolve("output.txt")
        val process = ProcessBuilder(app.absolutePath).redirectErrorStream(true).redirectOutput(output).start()
        val closed = process.waitFor(seconds.get().toLong(), TimeUnit.SECONDS)
        if (!closed) {
            process.descendants().forEach { it.destroy() }
            process.destroy()
            if (!process.waitFor(CLOSE_SECONDS, TimeUnit.SECONDS)) process.destroyForcibly()
        }
        val said = output.readText()
        if (closed) throw GradleException("The desktop app closed by itself, with ${process.exitValue()}:\n$said")
        if (ERROR.containsMatchIn(said)) throw GradleException("The desktop app wrote an error:\n$said")
        logger.lifecycle("The desktop app ran for ${seconds.get()} seconds without an error: $app")
    }

    /** The app's launcher: macOS's inside its `.app`, Linux's in `bin`, Windows's `.exe`. */
    private fun launcherIn(folder: File): File =
        folder.walkTopDown()
            .filter { it.isFile && it.canExecute() }
            .firstOrNull { it.parentFile.name == "MacOS" || it.parentFile.name == "bin" || it.extension == "exe" }
            ?: throw GradleException("No packaged app in $folder: run createDistributable")

    private companion object {
        /** How long a closed app is given to end before it is ended by force. */
        const val CLOSE_SECONDS = 5L

        /** A line of an error the app writes: an exception's, or the JVM's. */
        val ERROR = Regex("""Exception|Error:|FATAL""")
    }
}

/** The Gradle property a release gives the desktop app's version in. */
private const val SITE_VERSION_PROPERTY = "site.version"
