plugins {
    id("site.jvm.tool")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Programs that draw the site with Compose on the JVM at build time: the landing pages' words as the app shows them,
// and each page's share image.
application {
    mainClass = "com.pantrixui.site.tools.render.LandingMainKt"
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.core.model)
    implementation(projects.core.content)
    implementation(projects.core.navigation)
    implementation(projects.core.routing)
    implementation(projects.core.i18n)
    implementation(projects.core.designsystem)
    implementation(libs.pantrix.components)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.uiTest)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization.json)
}
