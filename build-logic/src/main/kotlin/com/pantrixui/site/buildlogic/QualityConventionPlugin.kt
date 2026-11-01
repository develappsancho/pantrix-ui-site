package com.pantrixui.site.buildlogic

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask
import java.io.File

/**
 * The checks every module of the site passes: detekt with its own rules and the rules Pantrix UI gives an app
 * (`com.pantrixui:detekt-rules`), and ktlint through Kotlinter. Both read the site's configuration, in
 * `config/detekt/detekt.yml` and `.editorconfig`.
 */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.alias(libs.plugins.detekt)
        pluginManager.alias(libs.plugins.kotlinter)

        extensions.configure<DetektExtension> {
            buildUponDefaultConfig = true
            config.setFrom(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
            basePath = rootProject.projectDir.absolutePath
            parallel = true
        }
        dependencies.addProvider("detektPlugins", libs.pantrix.detektRules)

        val buildDir = "${File.separator}build${File.separator}"
        tasks.withType<Detekt>().configureEach {
            // Generated sources (the resources' accessors) are not the site's to style.
            exclude { it.file.absolutePath.contains(buildDir) }
            reports {
                html.required.set(true)
                xml.required.set(false)
                txt.required.set(false)
                sarif.required.set(false)
            }
        }
        tasks.withType<ConfigurableKtLintTask>().configureEach {
            exclude { it.file.absolutePath.contains(buildDir) }
        }

        // In a multiplatform module the plain `detekt` task looks at an empty source set; the work is in the tasks of
        // each source set. Without this, `./gradlew detekt` would pass while checking nothing.
        tasks.named("detekt") {
            dependsOn(
                tasks.withType<Detekt>().matching {
                    it.name != "detekt" && !it.name.startsWith("detektBaseline")
                }
            )
        }
    }
}
