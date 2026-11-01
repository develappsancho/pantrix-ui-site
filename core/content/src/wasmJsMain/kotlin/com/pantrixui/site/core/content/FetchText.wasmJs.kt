// The browser's own fetch, its promises awaited as Kotlin's.
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.pantrixui.site.core.content

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.Response
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsException
import kotlin.js.JsString

/** The address's text by the browser's fetch: another site's must allow the page's address, as GitHub's API does. */
internal actual suspend fun fetchText(url: String): String? = try {
    val response = window.fetch(url).await<Response>()
    if (response.ok) response.text().await<JsString>().toString() else null
} catch (@Suppress("SwallowedException") failure: JsException) {
    // No network, or an address that does not allow the page's: the browser rejects the fetch.
    null
}
