plugins {
    id("site.kmp.library")
    id("site.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:base"))
            api(project(":core:content"))
            api(project(":core:routing"))
            api(project(":core:designsystem"))
            api(libs.kotlinx.coroutines.test)
            api(libs.pantrix.testing)
        }
    }
}
