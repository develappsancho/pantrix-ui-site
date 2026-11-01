package com.pantrixui.site.core.content

import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dataUsingEncoding
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import kotlin.coroutines.resume

/** [body] posted to the address as JSON over the system's own URL session, and the answer's text. */
@OptIn(BetaInteropApi::class)
internal actual suspend fun postJson(
    url: String,
    body: String,
    headers: Map<String, String>
): String? = suspendCancellableCoroutine { continuation ->
    val address = NSURL.URLWithString(url)
    if (address == null) {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }
    val request = NSMutableURLRequest.requestWithURL(address, NSURLRequestReloadIgnoringLocalCacheData, POST_TIMEOUT)
    request.setHTTPMethod("POST")
    request.setValue(JSON, forHTTPHeaderField = "Content-Type")
    headers.forEach { (name, value) -> request.setValue(value, forHTTPHeaderField = name) }
    @Suppress("CAST_NEVER_SUCCEEDS") // A Kotlin string is an NSString to Objective-C.
    request.setHTTPBody((body as NSString).dataUsingEncoding(NSUTF8StringEncoding))
    val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
        val status = (response as? NSHTTPURLResponse)?.statusCode
        val text = if (error == null && status == POST_OK && data != null) {
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
private const val POST_TIMEOUT = 10.0

/** The status of an answer that succeeded. */
private const val POST_OK = 200L
