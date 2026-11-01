package com.pantrixui.site.core.content

import com.pantrixui.site.core.model.Language
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

/** A call to the server that comes to nothing is a failure the page tells the reader of, whatever the reason. */
class McpAskFailureTest {
    @Test
    fun no_network_no_answer_in_time_and_an_error_are_each_a_failure() = runTest {
        val unreachable = McpAskRepository(address = "/mcp") { _, _, _ -> null }
        assertFailsWith<AskFailure> { unreachable.searchIcons("close", Language.English, 5) }
        val slow = McpAskRepository(address = "/mcp", timeout = 10.seconds) { _, _, _ ->
            delay(11.seconds)
            ANSWER
        }
        assertFailsWith<AskFailure> { slow.searchIcons("close", Language.English, 5) }
        val refused = McpAskRepository(address = "/mcp") { _, _, _ ->
            """{"jsonrpc":"2.0","id":1,"error":{"code":-32602,"message":"Unknown tool"}}"""
        }
        assertFailsWith<AskFailure> { refused.searchIcons("close", Language.English, 5) }
    }

    @Test
    fun a_call_names_its_tool_its_arguments_and_the_language_asked_for() = runTest {
        var sent = ""
        var sentHeaders = emptyMap<String, String>()
        val repository = McpAskRepository(address = "/mcp") { url, body, headers ->
            assertEquals("/mcp", url)
            sent = body
            sentHeaders = headers
            ANSWER
        }
        val icons = repository.searchIcons("close", Language.Turkish, 3)
        assertEquals("Close", icons.icons.single().name)
        assertEquals(
            """{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"search_icons",""" +
                """"arguments":{"query":"close","limit":3,"language":"tr"}}}""",
            sent
        )
        assertEquals("application/json, text/event-stream", sentHeaders["Accept"])
    }

    private companion object {
        val ANSWER =
            """{"jsonrpc":"2.0","id":1,"result":{"content":[{"type":"text","text":"- `PantrixIcons.Close`"}],""" +
                """"structuredContent":{"language":"tr","total":1,"icons":[{"name":"Close",""" +
                """"reference":"PantrixIcons.Close","description":"Kapatmak.","mirrored":false}]}}}"""
    }
}
