plugins {
    id("site.kmp.library")
    id("site.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
        }
    }
}
