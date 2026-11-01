plugins {
    id("site.jvm.tool")
}

application {
    mainClass = "com.pantrixui.site.tools.prerender.MainKt"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.routing)
    implementation(projects.core.i18n)
    implementation(libs.kotlinx.serialization.json)
}
