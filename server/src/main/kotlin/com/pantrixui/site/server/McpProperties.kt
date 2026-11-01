package com.pantrixui.site.server

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * How the MCP server is served, from `pantrix.mcp` in the application's properties (`application.properties`). It has
 * no defaults of its own: a default would give the class a second constructor, and Spring binds a class with one.
 *
 * @property origins the hosts whose web pages may call the server from a browser: the site's own. A request from a
 *   page of any other host is refused; an assistant's own process sends no origin, and is answered.
 */
@ConfigurationProperties("pantrix.mcp")
data class McpProperties(
    val origins: Set<String>
)
