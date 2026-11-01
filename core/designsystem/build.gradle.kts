plugins {
    id("site.kmp.library")
    id("site.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.pantrix.components)
            api(project(":core:model"))
            api(project(":core:i18n"))
        }
        commonTest.dependencies {
            implementation(libs.pantrix.testing)
        }
    }
}
