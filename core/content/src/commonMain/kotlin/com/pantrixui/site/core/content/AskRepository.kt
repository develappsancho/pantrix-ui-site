package com.pantrixui.site.core.content

import com.pantrixui.site.core.model.AskComponent
import com.pantrixui.site.core.model.ComponentHits
import com.pantrixui.site.core.model.DeclarationDetail
import com.pantrixui.site.core.model.DeclarationHits
import com.pantrixui.site.core.model.DocumentHits
import com.pantrixui.site.core.model.ExampleList
import com.pantrixui.site.core.model.IconHits
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.SiteAddress
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Where the page that asks the library's MCP server gets its answers: the server's tools, called one at a time, each
 * answer read as the data the tool gives. Every call is in [Language]: a search reads both of the documents'
 * languages whatever it is, and the answer says things in it.
 *
 * A call throws [AskFailure] where the server cannot be reached, does not answer in time, or answers with an error:
 * the page then says the server did not answer.
 */
interface AskRepository {
    /** The components that match [query], the best first, at most [limit]. */
    suspend fun searchComponents(
        query: String,
        language: Language,
        limit: Int
    ): ComponentHits

    /** The component whose composable is [function], as a card shows it. */
    suspend fun component(
        function: String,
        language: Language
    ): AskComponent

    /** The examples of the page of the component whose composable is [function]. */
    suspend fun examples(
        function: String,
        language: Language
    ): ExampleList

    /** The declarations that match [query], the best first, at most [limit]. */
    suspend fun searchApi(
        query: String,
        language: Language,
        limit: Int
    ): DeclarationHits

    /** The declarations named [title], as a reader calls them, in full; a member's type for a member. */
    suspend fun declaration(
        title: String,
        language: Language
    ): List<DeclarationDetail>

    /** The icons that match [query], at most [limit]. */
    suspend fun searchIcons(
        query: String,
        language: Language,
        limit: Int
    ): IconHits

    /** The pages, the guides and the skill's references that match [query], the best first, at most [limit]. */
    suspend fun searchDocumentation(
        query: String,
        language: Language,
        limit: Int
    ): DocumentHits
}

/** A call to the server that came to nothing: no network, no answer in time, or an answer that is an error. */
class AskFailure(message: String) : Exception(message)

/**
 * The library's MCP server at [address], called over Streamable HTTP as any client calls it: a `tools/call` a request,
 * each on its own, as the server answers a request with no session before it. Its tools give their answers as
 * structured content, which is read here; their Markdown is for an assistant.
 *
 * @param address the server's address: the site's own `/mcp`.
 * @param timeout how long a call waits for its answer before it gives up.
 * @param post what posts a request's JSON and reads the answer's text: the platform's own client, or a test's.
 */
class McpAskRepository(
    private val address: String = SiteAddress.mcp,
    private val timeout: Duration = 10.seconds,
    private val post: suspend (url: String, body: String, headers: Map<String, String>) -> String? = ::postJson
) : AskRepository {
    private var id = 0

    override suspend fun searchComponents(
        query: String,
        language: Language,
        limit: Int
    ): ComponentHits = call("search_components", ComponentHits.serializer(), language) {
        put("query", query)
        put("limit", limit)
    }

    override suspend fun component(
        function: String,
        language: Language
    ): AskComponent = call("get_component_metadata", AskComponent.serializer(), language) {
        put("name", function)
    }

    override suspend fun examples(
        function: String,
        language: Language
    ): ExampleList = call("list_examples", ExampleList.serializer(), language) {
        put("component", function)
    }

    override suspend fun searchApi(
        query: String,
        language: Language,
        limit: Int
    ): DeclarationHits = call("search_api", DeclarationHits.serializer(), language) {
        put("query", query)
        put("limit", limit)
    }

    override suspend fun declaration(
        title: String,
        language: Language
    ): List<DeclarationDetail> {
        val given = data("get_api", language) { put("name", title) }
        return given["declarations"]?.jsonArray.orEmpty().map {
            READER.decodeFromJsonElement(DeclarationDetail.serializer(), it)
        }
    }

    override suspend fun searchIcons(
        query: String,
        language: Language,
        limit: Int
    ): IconHits = call("search_icons", IconHits.serializer(), language) {
        put("query", query)
        put("limit", limit)
    }

    override suspend fun searchDocumentation(
        query: String,
        language: Language,
        limit: Int
    ): DocumentHits = call("search_documentation", DocumentHits.serializer(), language) {
        put("query", query)
        put("limit", limit)
    }

    private suspend fun <T> call(
        tool: String,
        shape: DeserializationStrategy<T>,
        language: Language,
        arguments: JsonObjectBuilder.() -> Unit
    ): T = READER.decodeFromJsonElement(shape, data(tool, language, arguments))

    /** What [tool] answers [arguments] with, as data, in [language]. */
    private suspend fun data(
        tool: String,
        language: Language,
        arguments: JsonObjectBuilder.() -> Unit
    ): JsonObject {
        val body = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", ++id)
            put("method", "tools/call")
            putJsonObject("params") {
                put("name", tool)
                putJsonObject("arguments") {
                    arguments()
                    put("language", language.code)
                }
            }
        }
        val text = withTimeoutOrNull(timeout) { post(address, body.toString(), HEADERS) }
        val answer = text?.let { READER.parseToJsonElement(it).jsonObject }
        return answer?.structured() ?: throw AskFailure("$tool: ${answer?.fault() ?: "the server did not answer"}")
    }

    private companion object {
        /** What a client of Streamable HTTP sends with each message, the version of the protocol among it. */
        val HEADERS = mapOf(
            "Accept" to "application/json, text/event-stream",
            "MCP-Protocol-Version" to "2025-11-25"
        )

        /** What reads an answer: a field the page has no use for is left alone, as the server may add more. */
        val READER = Json { ignoreUnknownKeys = true }
    }
}

/** The data this answer holds, or null where it holds none: an error, or a call that failed. */
private fun JsonObject.structured(): JsonObject? = this["result"]
    ?.jsonObject
    ?.takeUnless { it["isError"]?.jsonPrimitive?.booleanOrNull == true }
    ?.get("structuredContent")
    ?.jsonObject

/** Why this answer holds no data, in the server's own words where it gives some. */
private fun JsonObject.fault(): String {
    val result = this["result"]?.jsonObject
    return when {
        "error" in this -> {
            this["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull ?: "an error"
        }

        result == null -> {
            "the answer has no result"
        }

        result["isError"]?.jsonPrimitive?.booleanOrNull == true -> {
            result["content"]?.jsonArray?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
                ?: "the call failed"
        }

        else -> {
            "the answer holds no data"
        }
    }
}

/** [body] posted to [url] as JSON with [headers], and the answer's text; null where it cannot be read or fails. */
internal expect suspend fun postJson(
    url: String,
    body: String,
    headers: Map<String, String>
): String?

/** The type of a body of JSON. */
internal const val JSON = "application/json"
