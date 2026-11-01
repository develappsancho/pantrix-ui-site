plugins {
    id("site.jvm.tool")
}

application {
    mainClass = "com.pantrixui.site.tools.content.MainKt"
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.markdown)
    implementation(libs.kotlinx.serialization.json)
}
