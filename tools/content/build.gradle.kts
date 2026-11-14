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
    // The library's documents, which the tests read the library's components from: the jar alone.
    testRuntimeOnly(libs.pantrix.mcp) {
        isTransitive = false
    }
}
