plugins {
    id("site.kmp.library")
    id("site.kmp.compose")
    // The site's pages, written as JSON by :tools:content, are this module's resources.
    id("site.content")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(libs.kotlinx.coroutines.core)
            implementation(project(":core:routing"))
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
