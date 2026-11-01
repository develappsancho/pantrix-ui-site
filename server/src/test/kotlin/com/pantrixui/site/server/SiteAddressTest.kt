package com.pantrixui.site.server

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The server where the stack gives it the address it is served at, `SITE_URL`: the site on this machine. Its own pages
 * may call the MCP server, and the address the site is built for is then one of any other.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["pantrix.site.url=http://localhost:4004"]
)
class SiteAddressTest(
    @param:LocalServerPort private val port: Int
) {
    private val client = HttpClient.newHttpClient()

    @Test
    fun the_pages_of_the_address_the_site_is_served_at_may_call_the_server() {
        assertEquals(200, toolsFrom("http://localhost:4004"))
        assertEquals(200, toolsFrom("http://www.localhost:4004"))
        assertEquals(403, toolsFrom("https://pantrixui.com"))
    }

    private fun toolsFrom(origin: String): Int {
        val request = HttpRequest.newBuilder(URI.create("http://localhost:$port/mcp"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")
            .header("Origin", origin)
            .POST(HttpRequest.BodyPublishers.ofString("""{"jsonrpc":"2.0","id":1,"method":"tools/list"}"""))
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()
    }
}
