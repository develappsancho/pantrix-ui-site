plugins {
    id("site.kmp.library")
    id("site.address")
    // How many components the library has, counted in its jar.
    id("site.library-facts")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
        }
    }
}
