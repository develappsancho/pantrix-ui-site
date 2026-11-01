rootProject.name = "PantrixUISite"

@Suppress("UnstableApiUsage")
pluginManagement {
    // The convention plugins every module applies: Kotlin classes in build-logic, one for each kind of module.
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        // Pantrix UI comes from the local Maven repository until the site is deployed, and nothing else does.
        mavenLocal {
            content { includeGroupAndSubgroups("com.pantrixui") }
        }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Each platform's entry point, nothing but what starts the app there, and the composition root they all draw
// (:app:shared): the dependency graph, the shell and the navigation. The iOS app is the Xcode project in app/iosApp.
include(":app:androidApp")
include(":app:desktopApp")
include(":app:webApp")
include(":app:shared")

// What the features share. A feature depends on these, never on another feature.
include(":core:base")
include(":core:navigation")
include(":core:model")
include(":core:routing")
include(":core:i18n")
include(":core:designsystem")
include(":core:content")
include(":core:testing")

// One module for each kind of page.
include(":feature:docs")
include(":feature:components")
include(":feature:home")
include(":feature:foundations")
include(":feature:theme")
include(":feature:adaptive")
include(":feature:ai")
include(":feature:roadmap")
include(":feature:search")

// Programs that run at build time only.
include(":tools:content")
include(":tools:prerender")
include(":tools:render")
