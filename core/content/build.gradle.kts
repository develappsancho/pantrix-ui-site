plugins {
    id("site.kmp.library")
    id("site.kmp.compose")
    // The site's pages, written as JSON by :tools:content, are this module's resources.
    id("site.content")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(libs.kotlinx.coroutines.core)
            implementation(projects.core.routing)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    // The library's MCP server, run in the JVM's tests, which the page's calls are read against. It needs no Ktor, as
    // the site's server's does not (server/build.gradle.kts).
    "jvmTestImplementation"(libs.pantrix.mcp) {
        exclude(group = "io.ktor")
    }
}
