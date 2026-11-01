plugins {
    id("site.server")
}

dependencies {
    implementation(libs.pantrix.mcp) {
        // The MCP SDK brings Ktor for transports of its own, which this server does not use: Spring serves HTTP, and
        // PantrixMcp.http() answers it. The SDK reaches Ktor elsewhere only to match a resource template, and Pantrix
        // UI's server has none. McpEndpointTest asks for each kind of message without it.
        exclude(group = "io.ktor")
    }
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.kotlinx.coroutines.reactor)
    testImplementation(libs.kotlinx.serialization.json)
}
