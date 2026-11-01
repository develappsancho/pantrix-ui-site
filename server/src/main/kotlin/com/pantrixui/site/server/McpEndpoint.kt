package com.pantrixui.site.server

import com.pantrixui.mcp.PantrixMcp
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Pantrix UI's MCP server at `/mcp`, over Streamable HTTP: an assistant is given the address, as
 * `claude mcp add --transport http pantrix-ui https://pantrixui.com/mcp`, and asks the library's documents for what it
 * needs, with no Java and no jar on its machine.
 *
 * The protocol is the library's and the HTTP is Spring's: each request goes to [PantrixMcp.http] as it came, whatever
 * its method, and its answer goes back as it is.
 */
@RestController
class McpEndpoint(
    site: SiteProperties
) {
    private val mcp = PantrixMcp.http(origins = site.hosts)

    /** Answers a request made to `/mcp`, with the request's [body] where it has one. */
    @RequestMapping("/mcp")
    suspend fun answer(
        request: ServerHttpRequest,
        @RequestBody(required = false) body: String?
    ): ResponseEntity<String> {
        val answer = mcp.answer(request.method.name(), request.headers.toSingleValueMap(), body.orEmpty())
        val response = ResponseEntity.status(answer.status)
            .headers { headers -> answer.headers.forEach { (name, value) -> headers.set(name, value) } }
        return if (answer.body.isEmpty()) response.build() else response.body(answer.body)
    }
}
