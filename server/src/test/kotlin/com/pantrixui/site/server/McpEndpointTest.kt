package com.pantrixui.site.server

import com.pantrixui.mcp.PantrixMcp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The server as an assistant reaches it: over HTTP, at `/mcp`, one POST for each message, with no session between two
 * of them. Every kind of message the library's server answers is asked, since the server runs without the Ktor the
 * MCP SDK brings.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpEndpointTest(
    @param:LocalServerPort private val port: Int,
    @param:Autowired private val site: SiteProperties
) {
    private val client = HttpClient.newHttpClient()

    @Test
    fun every_kind_of_message_is_answered_over_http() {
        val initialized = result(post(request(1, "initialize", INITIALIZE)))
        assertEquals("pantrix-ui", initialized.getValue("serverInfo").jsonObject.getValue("name").jsonPrimitive.content)
        assertEquals(202, post(notification("notifications/initialized")).statusCode())
        val tools = result(post(request(2, "tools/list"))).getValue("tools").jsonArray
        assertEquals(PantrixMcp.tools, tools.map { it.jsonObject.getValue("name").jsonPrimitive.content })
        val called = result(post(call(3, "search_components", "query", "toast")))
        assertTrue("**Snackbar**" in called.toString(), called.toString().take(200))
        val resources = result(post(request(4, "resources/list"))).getValue("resources").jsonArray
        assertTrue(resources.any { it.jsonObject.getValue("uri").jsonPrimitive.content == "pantrix://llms.txt" })
        val read = result(post(request(5, "resources/read", buildJsonObject { put("uri", "pantrix://llms.txt") })))
        val index = read.getValue("contents").jsonArray.first().jsonObject.getValue("text").jsonPrimitive.content
        assertTrue(index.startsWith("# Pantrix UI\n"))
        // A resource it does not have looks for a template too, which is where the SDK would reach Ktor.
        val missing = post(request(6, "resources/read", buildJsonObject { put("uri", "pantrix://nothing") }))
        assertNotNull(Json.parseToJsonElement(missing.body()).jsonObject["error"], missing.body())
        val prompts = result(post(request(7, "prompts/list"))).getValue("prompts").jsonArray
        assertEquals(3, prompts.size)
        val prompt = result(
            post(
                request(
                    8,
                    "prompts/get",
                    buildJsonObject {
                        put("name", "find_component_for_use_case")
                        putJsonObject("arguments") { put("use_case", "a toast that offers undo") }
                    }
                )
            )
        )
        assertTrue("**Snackbar**" in prompt.toString())
    }

    @Test
    fun what_is_not_a_message_from_an_assistant_or_the_site_is_refused() {
        val get = send(HttpRequest.newBuilder(endpoint).header("Accept", "text/event-stream").GET())
        assertEquals(405, get.statusCode())
        assertEquals("POST", get.headers().firstValue("Allow").orElse(null))
        assertEquals(403, post(request(1, "tools/list"), "Origin" to "https://elsewhere.example").statusCode())
        // With no SITE_URL, the site is served at the address it is built for, and its pages may call the server.
        val host = site.address.host
        assertEquals(200, post(request(1, "tools/list"), "Origin" to "https://$host").statusCode())
        assertEquals(200, post(request(1, "tools/list"), "Origin" to "https://www.$host").statusCode())
        assertEquals(415, post(request(1, "tools/list"), "Content-Type" to "text/plain").statusCode())
    }

    @Test
    fun the_server_runs_without_ktor() {
        assertFailsWith<ClassNotFoundException> { Class.forName("io.ktor.http.HttpMethod") }
    }

    private val endpoint: URI get() = URI.create("http://localhost:$port/mcp")

    private fun post(
        body: String,
        vararg headers: Pair<String, String>
    ): HttpResponse<String> {
        val given = mapOf("Content-Type" to "application/json", "Accept" to "application/json, text/event-stream") +
            headers
        val builder = HttpRequest.newBuilder(endpoint).POST(HttpRequest.BodyPublishers.ofString(body))
        given.forEach { (name, value) -> builder.header(name, value) }
        return send(builder)
    }

    private fun send(builder: HttpRequest.Builder): HttpResponse<String> =
        client.send(builder.build(), HttpResponse.BodyHandlers.ofString())

    private fun result(response: HttpResponse<String>): JsonObject {
        assertEquals(200, response.statusCode(), response.body())
        assertEquals("application/json", response.headers().firstValue("Content-Type").orElse(null))
        return Json.parseToJsonElement(response.body()).jsonObject.getValue("result").jsonObject
    }

    private companion object {
        val INITIALIZE = buildJsonObject {
            put("protocolVersion", "2025-11-25")
            putJsonObject("capabilities") {}
            putJsonObject("clientInfo") {
                put("name", "test")
                put("version", "1")
            }
        }

        fun request(
            id: Int,
            method: String,
            params: JsonObject? = null
        ): String = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", id)
            put("method", method)
            params?.let { put("params", it) }
        }.toString()

        fun notification(method: String): String = buildJsonObject {
            put("jsonrpc", "2.0")
            put("method", method)
        }.toString()

        fun call(
            id: Int,
            tool: String,
            argument: String,
            value: String
        ): String = request(
            id,
            "tools/call",
            buildJsonObject {
                put("name", tool)
                putJsonObject("arguments") { put(argument, value) }
            }
        )
    }
}
