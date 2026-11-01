package com.pantrixui.site.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * The site's server, for what a folder of files cannot give: today Pantrix UI's MCP server, at `/mcp` ([McpEndpoint]).
 * The site itself is served as files, beside it.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class SiteServer

/** Starts the server, on the port of `server.port`: the variable `PORT`, or 8080. */
@Suppress("SpreadOperator") // Spring takes the arguments as a vararg: they are copied once, as the server starts.
fun main(args: Array<String>) {
    runApplication<SiteServer>(*args)
}
