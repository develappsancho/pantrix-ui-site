package com.pantrixui.site.buildlogic

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * The app's Maestro flows on a device, in two tasks that `check` does not run: a flow needs a device, and Maestro is a
 * program of its own, installed apart from the build.
 *
 * - On the Android app, `maestroTest` installs the debug build on the device or emulator, that one alone, and runs
 *   the flows there.
 * - On the shared module, `maestroIosTest` builds the iOS app with Xcode for a booted simulator, installs it and runs
 *   the flows there.
 *
 * Both run `maestro test` on the flows in `app/maestro`, told the app's id as `APP_ID`, and write a JUnit report with
 * what a failing flow left, its screenshot among it, to the module's `build/maestro/report`. Properties choose:
 * - `-Pmaestro.flows=<path>,…`: the flows to run, files or folders of `app/maestro`, such as `flows/search.yaml`; all
 *   of them by default.
 * - `-Pmaestro.device=<id>`: the device where more than one is there, a serial on Android, a simulator's UDID on iOS.
 * - `-Pmaestro.executable`: the program, where it is neither in `~/.maestro/bin`, where Maestro's installer puts it,
 *   nor on the path.
 */
class MaestroConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.withPlugin(ANDROID_APPLICATION) { registerAndroidTask() }
        pluginManager.withPlugin(KOTLIN_MULTIPLATFORM) { registerIosTask() }
    }

    private fun Project.registerAndroidTask() {
        val components = extensions.getByType<ApplicationAndroidComponentsExtension>()
        val task = tasks.register<MaestroTestTask>("maestroTest") {
            group = GROUP
            description = "Installs the app's debug build on an Android device and runs its Maestro flows there"
            adb.set(components.sdkComponents.adb)
            configureMaestro(this@registerAndroidTask)
        }
        components.onVariants(components.selector().withName(DEBUG)) { variant ->
            task.configure {
                apks.set(variant.artifacts.get(SingleArtifact.APK))
                apkLoader.set(variant.artifacts.getBuiltArtifactsLoader())
                appId.set(variant.applicationId)
            }
        }
    }

    private fun Project.registerIosTask() {
        tasks.register<MaestroIosTestTask>("maestroIosTest") {
            group = GROUP
            description = "Builds the app's iOS app for a booted simulator and runs its Maestro flows there"
            xcodeProject.set(rootProject.layout.projectDirectory.dir(XCODE_PROJECT))
            target.set(TARGET)
            products.set(layout.buildDirectory.dir(PRODUCTS))
            configureMaestro(this@registerIosTask)
        }
    }

    private fun MaestroTask.configureMaestro(project: Project) {
        val providers = project.providers
        flows.set(project.rootProject.layout.projectDirectory.dir(FLOWS))
        only.set(providers.gradleProperty(ONLY).map(::listed).orElse(emptyList()))
        device.set(providers.gradleProperty(DEVICE))
        executable.set(providers.gradleProperty(EXECUTABLE).orElse(project.provider { installedMaestro() }))
        reports.set(project.layout.buildDirectory.dir(REPORTS))
    }

    /** The values of a property that lists them with commas, `a,b`, without spaces or empty ones. */
    private fun listed(value: String): List<String> = value.split(',').map(String::trim).filter(String::isNotEmpty)

    /** Where Maestro's installer puts the program, or its bare name, for the path to find. */
    private fun installedMaestro(): String =
        File(System.getProperty(USER_HOME), INSTALLED).takeIf { it.canExecute() }?.path ?: MAESTRO

    private companion object {
        const val GROUP = "verification"
        const val ANDROID_APPLICATION = "com.android.application"
        const val KOTLIN_MULTIPLATFORM = "org.jetbrains.kotlin.multiplatform"
        const val DEBUG = "debug"
        const val FLOWS = "app/maestro"
        const val XCODE_PROJECT = "app/iosApp/iosApp.xcodeproj"
        const val TARGET = "iosApp"
        const val PRODUCTS = "maestro/xcode"
        const val ONLY = "maestro.flows"
        const val REPORTS = "maestro/report"
        const val DEVICE = "maestro.device"
        const val EXECUTABLE = "maestro.executable"
        const val USER_HOME = "user.home"
        const val INSTALLED = ".maestro/bin/maestro"
        const val MAESTRO = "maestro"
    }
}
