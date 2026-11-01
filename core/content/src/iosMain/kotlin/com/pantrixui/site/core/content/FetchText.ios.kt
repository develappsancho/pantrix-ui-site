package com.pantrixui.site.core.content

import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLRequestUseProtocolCachePolicy
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import kotlin.coroutines.resume

/** The address's text over the system's own URL session, which answers on a thread of its own. */
@OptIn(BetaInteropApi::class)
internal actual suspend fun fetchText(url: String): String? = suspendCancellableCoroutine { continuation ->
    val address = NSURL.URLWithString(url)
    if (address == null) {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }
    val request = NSURLRequest.requestWithURL(address, NSURLRequestUseProtocolCachePolicy, TIMEOUT_SECONDS)
    val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
        val status = (response as? NSHTTPURLResponse)?.statusCode
        val text = if (error == null && status == HTTP_OK && data != null) {
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        } else {
            null
        }
        continuation.resume(text)
    }
    continuation.invokeOnCancellation { task.cancel() }
    task.resume()
}

/** How long the request waits before it gives up. */
private const val TIMEOUT_SECONDS = 10.0

/** The status of an answer that succeeded. */
private const val HTTP_OK = 200L
