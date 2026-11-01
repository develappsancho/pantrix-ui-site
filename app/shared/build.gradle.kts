plugins {
    id("site.kmp.library")
    id("site.kmp.compose")
    id("site.screenshots")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // What the iOS app in app/iosApp links: the whole site, as one framework.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.docs)
            implementation(projects.feature.components)
            implementation(projects.feature.home)
            implementation(projects.feature.foundations)
            implementation(projects.feature.theme)
            implementation(projects.feature.adaptive)
            implementation(projects.feature.ai)
            implementation(projects.feature.roadmap)
            implementation(projects.feature.download)
            implementation(projects.feature.search)
            implementation(projects.core.base)
            implementation(projects.core.navigation)
            implementation(projects.core.model)
            implementation(projects.core.routing)
            implementation(projects.core.i18n)
            implementation(projects.core.designsystem)
            implementation(projects.core.content)
            implementation(libs.pantrix.components)
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            // The graph's modules are the shared module's own API: an entry point or a tool may give its own.
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
        }
    }
}
