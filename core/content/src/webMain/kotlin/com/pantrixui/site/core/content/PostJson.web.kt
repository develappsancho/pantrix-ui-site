// The browser's own fetch, its promises awaited as Kotlin's.
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.pantrixui.site.core.content

import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.await
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsException
import kotlin.js.JsString
import kotlin.js.toJsString

/**
 * [body] posted to the address as JSON by the browser's fetch, and the answer's text: the site's own server answers
 * a page of the site's address. A rejected fetch is a [JsException], as for [fetchText].
 */
internal actual suspend fun postJson(
    url: String,
    body: String,
    headers: Map<String, String>
): String? = try {
    val sent = Headers()
    sent.append("Content-Type", JSON)
    headers.forEach { (name, value) -> sent.append(name, value) }
    val response = window.fetch(url, postOf(sent, body.toJsString())).await<Response>()
    if (response.ok) {
        // A JavaScript string in the Wasm build, which is Kotlin's own in the JavaScript build.
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        response.text().await<JsString>().toString()
    } else {
        null
    }
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (@Suppress("SwallowedException") failure: JsException) {
    // No network, or the server cannot be reached: the browser rejects the fetch.
    null
}

/**
 * A POST's options with [headers] and [body] alone. `RequestInit(method = …, headers = …, body = …)` gives each option
 * it is not given as null, and a browser refuses a null `cache`, `mode` or `credentials` before it sends anything.
 */
@Suppress("UnusedParameter") // The JavaScript body reads them by their names.
private fun postOf(
    headers: Headers,
    body: JsString
): RequestInit = js("({ method: 'POST', headers: headers, body: body })")
