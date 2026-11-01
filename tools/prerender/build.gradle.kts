plugins {
    id("site.jvm.tool")
}

application {
    mainClass = "com.pantrixui.site.tools.prerender.MainKt"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:routing"))
    implementation(project(":core:i18n"))
    implementation(libs.kotlinx.serialization.json)
}
