import org.gradle.initialization.DependenciesAccessors
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask

plugins {
    `kotlin-dsl`
    // This build's own sources are linted like every module's. A build cannot apply the convention plugins it
    // builds, so kotlinter is applied here by hand.
    alias(libs.plugins.kotlinter)
}

tasks.withType<ConfigurableKtLintTask>().configureEach {
    val buildDirSegment = "${File.separator}build${File.separator}"
    exclude { it.file.absolutePath.contains(buildDirSegment) }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

/** The artifact of a plugin's marker: what puts the plugin's classes on this build's classpath. */
fun marker(plugin: Provider<PluginDependency>): Provider<String> =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

dependencies {
    // The version catalogue's generated accessors, so that the plugins read `libs` as a build script does.
    gradle.serviceOf<DependenciesAccessors>().classes.asFiles.forEach {
        compileOnly(files(it.absolutePath))
    }

    implementation(marker(libs.plugins.androidApplication))
    implementation(marker(libs.plugins.androidKmpLibrary))
    implementation(marker(libs.plugins.kotlinMultiplatform))
    implementation(marker(libs.plugins.kotlinJvm))
    implementation(marker(libs.plugins.kotlinSerialization))
    implementation(marker(libs.plugins.composeMultiplatform))
    implementation(marker(libs.plugins.composeCompiler))
    implementation(marker(libs.plugins.roborazzi))
    implementation(marker(libs.plugins.detekt))
    implementation(marker(libs.plugins.kotlinter))
    implementation(marker(libs.plugins.springBoot))
    implementation(marker(libs.plugins.kotlinSpring))
}

gradlePlugin {
    plugins {
        register("siteKmpLibrary") {
            id = "site.kmp.library"
            implementationClass = "com.pantrixui.site.buildlogic.KmpLibraryConventionPlugin"
        }
        register("siteKmpCompose") {
            id = "site.kmp.compose"
            implementationClass = "com.pantrixui.site.buildlogic.KmpComposeConventionPlugin"
        }
        register("siteFeature") {
            id = "site.feature"
            implementationClass = "com.pantrixui.site.buildlogic.FeatureConventionPlugin"
        }
        register("siteQuality") {
            id = "site.quality"
            implementationClass = "com.pantrixui.site.buildlogic.QualityConventionPlugin"
        }
        register("siteAppAndroid") {
            id = "site.app.android"
            implementationClass = "com.pantrixui.site.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("siteAppDesktop") {
            id = "site.app.desktop"
            implementationClass = "com.pantrixui.site.buildlogic.DesktopApplicationConventionPlugin"
        }
        register("siteAppWeb") {
            id = "site.app.web"
            implementationClass = "com.pantrixui.site.buildlogic.WebApplicationConventionPlugin"
        }
        register("siteJvmTool") {
            id = "site.jvm.tool"
            implementationClass = "com.pantrixui.site.buildlogic.JvmToolConventionPlugin"
        }
        register("siteScreenshots") {
            id = "site.screenshots"
            implementationClass = "com.pantrixui.site.buildlogic.ScreenshotConventionPlugin"
        }
        register("siteServer") {
            id = "site.server"
            implementationClass = "com.pantrixui.site.buildlogic.ServerConventionPlugin"
        }
        register("siteAddress") {
            id = "site.address"
            implementationClass = "com.pantrixui.site.buildlogic.SiteAddressConventionPlugin"
        }
        register("siteContent") {
            id = "site.content"
            implementationClass = "com.pantrixui.site.buildlogic.ContentConventionPlugin"
        }
        register("siteLiveExamples") {
            id = "site.live-examples"
            implementationClass = "com.pantrixui.site.buildlogic.LiveExamplesConventionPlugin"
        }
        register("siteMaestro") {
            id = "site.maestro"
            implementationClass = "com.pantrixui.site.buildlogic.MaestroConventionPlugin"
        }
    }
}
