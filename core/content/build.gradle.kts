plugins {
    id("site.kmp.library")
    id("site.kmp.compose")
    // The site's pages, written as JSON by :tools:content, are this module's resources.
    id("site.content")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(libs.kotlinx.coroutines.core)
            implementation(projects.core.routing)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
