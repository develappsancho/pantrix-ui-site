package com.pantrixui.site.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

/**
 * Writes the paths of a module's source files into its klibs relative to the repository's root.
 *
 * A klib keeps the path of each source file it was compiled from, and the site's Wasm carries the paths of its own
 * modules' klibs, where anyone can read them: written in full, they are the build machine's, its user's name and its
 * folders. Relative to the root, they name the file alone. The web (JS and Wasm) and the native (iOS) compilations
 * write klibs; the JVM's and Android's do not. `deploy/check.sh` looks for such a path in the Wasm. Pantrix UI's own
 * klibs do the same from its 0.1.1 on.
 */
class RelativeKlibPathsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val relativePaths = "-Xklib-relative-path-base=${rootDir.absolutePath}"
        tasks.withType<Kotlin2JsCompile>().configureEach { compilerOptions.freeCompilerArgs.add(relativePaths) }
        tasks.withType<KotlinNativeCompile>().configureEach { compilerOptions.freeCompilerArgs.add(relativePaths) }
    }
}
