package com.pantrixui.site.core.content

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

/**
 * [body] posted to the address as JSON over Android's own HTTP connection, and the answer's text, off the main thread:
 * the app asks for the internet in its manifest.
 */
@Suppress("InjectDispatcher") // An actual takes no dispatcher to be given: the network waits on the platform's threads.
internal actual suspend fun postJson(
    url: String,
    body: String,
    headers: Map<String, String>
): String? = withContext(Dispatchers.IO) {
    try {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.connectTimeout = POST_TIMEOUT_MILLISECONDS
        connection.readTimeout = POST_TIMEOUT_MILLISECONDS
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", JSON)
        headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
        try {
            connection.outputStream.use { it.write(body.encodeToByteArray()) }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } finally {
            connection.disconnect()
        }
    } catch (@Suppress("SwallowedException") failure: IOException) {
        // No network, or no answer in time: the address cannot be read, which the caller is told by null.
        null
    }
}

/** How long a connection and a read each wait before they give up. */
private const val POST_TIMEOUT_MILLISECONDS = 10_000
