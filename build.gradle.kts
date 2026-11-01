plugins {
    // Declared once here so that every module's build shares one copy of each plugin's classes.
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlinter) apply false
}

// build-logic is a build of its own, so a task run by name here does not reach it. These make
// `./gradlew lintKotlin` and `./gradlew formatKotlin` cover its sources as well as every module's.
listOf("lintKotlin", "formatKotlin").forEach { name ->
    tasks.register(name) {
        group = "formatting"
        description = "Runs $name in build-logic, the build that holds the convention plugins."
        dependsOn(gradle.includedBuild("build-logic").task(":$name"))
    }
}
