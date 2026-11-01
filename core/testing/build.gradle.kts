plugins {
    id("site.kmp.library")
    id("site.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.base)
            api(projects.core.content)
            api(projects.core.routing)
            api(projects.core.designsystem)
            api(libs.kotlinx.coroutines.test)
            api(libs.pantrix.testing)
        }
    }
}
