// The browser's own fetch, its promises awaited as Kotlin's.
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.pantrixui.site.core.content

import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsException
import kotlin.js.JsString

/**
 * The address's text by the browser's fetch: another site's must allow the page's address, as GitHub's API does.
 *
 * A rejected fetch is a [JsException], which in the JavaScript build is any [Throwable]: a cancelled wait goes on up
 * before it, so the coroutine that asked stops as it should.
 */
internal actual suspend fun fetchText(url: String): String? = try {
    // The request's options given, as JavaScript's declaration of fetch has no default for them.
    val response = window.fetch(url, RequestInit()).await<Response>()
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
    // No network, or an address that does not allow the page's: the browser rejects the fetch.
    null
}
