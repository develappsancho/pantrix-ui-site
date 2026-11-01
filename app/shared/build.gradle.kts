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
            implementation(project(":feature:docs"))
            implementation(project(":feature:components"))
            implementation(project(":feature:home"))
            implementation(project(":feature:foundations"))
            implementation(project(":feature:theme"))
            implementation(project(":feature:adaptive"))
            implementation(project(":feature:ai"))
            implementation(project(":feature:roadmap"))
            implementation(project(":feature:search"))
            implementation(project(":core:base"))
            implementation(project(":core:navigation"))
            implementation(project(":core:model"))
            implementation(project(":core:routing"))
            implementation(project(":core:i18n"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:content"))
            implementation(libs.pantrix.components)
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}
