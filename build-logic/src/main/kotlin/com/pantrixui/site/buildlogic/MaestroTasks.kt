package com.pantrixui.site.buildlogic

import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * What both of the app's Maestro tasks are told: the flows, which of them run, the device, the program and where the
 * report goes. A run on a device is never up to date: the device is not an input Gradle can see.
 */
@DisableCachingByDefault(because = "It runs flows on a device")
abstract class MaestroTask : DefaultTask() {
    /** The flows' directory, with its `config.yaml`. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val flows: DirectoryProperty

    /**
     * The flows that run in place of all of them: files or folders of [flows], by their paths in it, such as
     * `flows/form` or `flows/form/checkbox.yaml`. A folder brings every flow under it. Empty runs the whole
     * workspace, as its `config.yaml` lists it.
     */
    @get:Input
    abstract val only: ListProperty<String>

    /** The device the flows run on, or none for the one Maestro finds. */
    @get:Input
    @get:Optional
    abstract val device: Property<String>

    /** The Maestro program. */
    @get:Input
    abstract val executable: Property<String>

    /**
     * Where a run writes what it found, emptied before it starts: the JUnit report, `report.xml`, and in `debug` each
     * flow's commands and logs, with the screenshot and the view hierarchy of the screen where a flow failed, in
     * `debug/<flow>/screenshots` and `debug/<flow>/screen-hierarchy`.
     */
    @get:Internal
    abstract val reports: DirectoryProperty

    @get:Inject
    protected abstract val exec: ExecOperations

    init {
        outputs.upToDateWhen { false }
    }

    /**
     * Runs the flows on [device] for the app [appId], with the environment [variables] beside the build's own and the
     * flows' own [values] beside `APP_ID`. The report names its suite [platform].
     */
    protected fun runFlows(
        device: String?,
        appId: String,
        platform: String,
        variables: Map<String, String> = emptyMap(),
        values: Map<String, String> = emptyMap()
    ) {
        val reports = reports.get().asFile.apply { deleteRecursively() }
        val chosen = chosenFlows()
        val run = exec.exec {
            executable(this@MaestroTask.executable.get())
            args(
                buildList {
                    add("test")
                    if (device != null) addAll(listOf("--device", device))
                    addAll(listOf("-e", "$APP_ID=$appId"))
                    values.forEach { (name, value) -> addAll(listOf("-e", "$name=$value")) }
                    addAll(listOf("--format", JUNIT, "--output", reports.resolve(REPORT).absolutePath))
                    addAll(listOf("--test-suite-name", platform))
                    addAll(listOf("--debug-output", reports.resolve(DEBUG).absolutePath, "--flatten-debug-output"))
                    addAll(chosen.map { it.absolutePath })
                }
            )
            environment(variables)
            isIgnoreExitValue = true
        }
        if (run.exitValue != 0) {
            throw GradleException(
                "Maestro's flows failed on $platform: the report is ${reports.resolve(REPORT)}, and the screen where " +
                    "each flow failed is in ${reports.resolve(DEBUG)}/<flow>/screenshots"
            )
        }
    }

    /** The workspace, or the flows [only] names: a folder is opened into the flows under it, in their paths' order. */
    private fun chosenFlows(): List<File> {
        val root = flows.get().asFile
        if (only.get().isEmpty()) return listOf(root)
        return only.get().flatMap { path ->
            val named = root.resolve(path)
            when {
                named.isDirectory -> named.walkTopDown().filter { it.isFile && it.extension == YAML }.sorted().toList()
                named.isFile -> listOf(named)
                else -> throw GradleException("No flow or folder of flows at $named")
            }
        }.distinct().ifEmpty { throw GradleException("No flows in ${only.get()}") }
    }

    private companion object {
        const val APP_ID = "APP_ID"
        const val JUNIT = "JUNIT"
        const val REPORT = "report.xml"
        const val DEBUG = "debug"
        const val YAML = "yaml"
    }
}

/**
 * Installs the app's debug build on an Android device or emulator, and runs the app's Maestro flows there.
 *
 * The build goes on [device] alone, or on the one device connected where none is named. The Android plugin's own
 * `installDebug` installs on every device connected: with two, the app on the other is installed again, and closed, in
 * the middle of whatever runs there.
 */
abstract class MaestroTestTask : MaestroTask() {
    /** The debug build's id, its `applicationId`. */
    @get:Input
    abstract val appId: Property<String>

    /** The folder the debug build's APK is packaged into. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apks: DirectoryProperty

    /** Reads which APK [apks] holds. */
    @get:Internal
    abstract val apkLoader: Property<BuiltArtifactsLoader>

    /** The SDK's `adb`. */
    @get:Internal
    abstract val adb: RegularFileProperty

    @TaskAction
    fun run() {
        install(device.orNull)
        runFlows(device.orNull, appId.get(), PLATFORM)
    }

    /** Installs the APK on [device], or on the one device connected. */
    private fun install(device: String?) {
        val apk = apkLoader.get().load(apks.get())?.elements?.singleOrNull()?.outputFile
            ?: throw GradleException("No single APK was built in ${apks.get()}")
        val said = ByteArrayOutputStream()
        val run = exec.exec {
            commandLine(
                buildList {
                    add(adb.get().asFile.absolutePath)
                    if (device != null) addAll(listOf("-s", device))
                    addAll(listOf("install", "-r", apk))
                }
            )
            errorOutput = said
            isIgnoreExitValue = true
        }
        if (run.exitValue != 0) {
            val where = device?.let { " on $it" } ?: ""
            val hint = if (device == null) ". Name the device with -Pmaestro.device=<serial>" else ""
            throw GradleException("adb could not install $apk$where: ${said.toString(Charsets.UTF_8).trim()}$hint")
        }
    }

    private companion object {
        const val PLATFORM = "Android"
    }
}

/**
 * Builds the iOS app with Xcode for a simulator, installs it there and runs the app's Maestro flows on it.
 *
 * The app is built by its project's target, not by a scheme: the project shares none, and the scheme Xcode makes for
 * itself is in each user's own data, which git leaves out.
 *
 * The simulator is [device], or the first one booted. Xcode's build runs the shared module's own Gradle task, which
 * puts the Kotlin framework into the app, so this task leaves that to it. The app's id is read from what Xcode built,
 * its `Info.plist`, as it carries the team's id that the Xcode project's configuration adds.
 *
 * Maestro starts its driver on a simulator the first time it is used there, which takes longer than its default wait:
 * the wait is [DRIVER_STARTUP_TIMEOUT_MILLIS] unless `MAESTRO_DRIVER_STARTUP_TIMEOUT` says otherwise.
 */
abstract class MaestroIosTestTask : MaestroTask() {
    /** The iOS app's Xcode project. */
    @get:Internal
    abstract val xcodeProject: DirectoryProperty

    /** The project's target that builds the app. */
    @get:Input
    abstract val target: Property<String>

    /** Where Xcode builds the app, apart from the IDE's own builds. */
    @get:Internal
    abstract val products: DirectoryProperty

    @TaskAction
    fun run() {
        val simulator = device.orNull ?: bootedSimulator()
        val products = products.get().asFile
        val build = listOf(
            listOf("xcodebuild"),
            listOf("-project", xcodeProject.get().asFile.absolutePath),
            listOf("-target", target.get()),
            listOf("-configuration", CONFIGURATION),
            listOf("-sdk", SIMULATOR_SDK),
            listOf("-arch", SIMULATOR_ARCH),
            listOf("SYMROOT=${products.absolutePath}", "OBJROOT=${products.resolve(INTERMEDIATES).absolutePath}"),
            listOf("build")
        ).flatten()
        exec.exec { commandLine(build) }
        val app = products.resolve(BUILT_APPS).listFiles { file -> file.extension == APP }?.singleOrNull()
            ?: throw GradleException("Xcode built no single app in ${products.resolve(BUILT_APPS)}")
        exec.exec { commandLine("xcrun", "simctl", "install", simulator, app.absolutePath) }
        val timeout = System.getenv(DRIVER_TIMEOUT) ?: DRIVER_STARTUP_TIMEOUT_MILLIS.toString()
        runFlows(simulator, bundleIdOf(app), PLATFORM, mapOf(DRIVER_TIMEOUT to timeout))
    }

    /** The first simulator booted, by its UDID. */
    private fun bootedSimulator(): String {
        val listed = output("xcrun", "simctl", "list", "devices", "booted")
        return BOOTED.find(listed)?.groupValues?.get(1)
            ?: throw GradleException("No simulator is booted: boot one, or name it with -Pmaestro.device=<UDID>")
    }

    /** The bundle id of the [app] Xcode built. */
    private fun bundleIdOf(app: File): String =
        output(PLIST_BUDDY, "-c", "Print :CFBundleIdentifier", app.resolve(INFO_PLIST).absolutePath).trim()

    /** What [command] writes, once it has run. */
    private fun output(vararg command: String): String {
        val written = ByteArrayOutputStream()
        exec.exec {
            commandLine(*command)
            standardOutput = written
        }
        return written.toString(Charsets.UTF_8)
    }

    private companion object {
        const val PLATFORM = "iOS"
        const val CONFIGURATION = "Debug"
        const val BUILT_APPS = "Debug-iphonesimulator"
        const val INTERMEDIATES = "Intermediates"
        const val SIMULATOR_SDK = "iphonesimulator"
        const val SIMULATOR_ARCH = "arm64"
        const val APP = "app"
        const val INFO_PLIST = "Info.plist"
        const val PLIST_BUDDY = "/usr/libexec/PlistBuddy"
        const val DRIVER_TIMEOUT = "MAESTRO_DRIVER_STARTUP_TIMEOUT"
        const val DRIVER_STARTUP_TIMEOUT_MILLIS = 240_000

        /** A booted simulator's line in `simctl list`: `iPhone 18 Pro Max (50036CB7-…) (Booted)`. */
        val BOOTED = Regex("""\(([0-9A-F-]{36})\) \(Booted\)""")
    }
}
