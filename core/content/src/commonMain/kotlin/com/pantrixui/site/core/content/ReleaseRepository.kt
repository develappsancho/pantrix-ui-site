package com.pantrixui.site.core.content

import com.pantrixui.site.core.model.DesktopRelease
import com.pantrixui.site.core.model.SiteDownloads
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/** Where the download page learns of the desktop app's latest release. */
fun interface ReleaseRepository {
    /** The latest release; it throws where the release cannot be learnt, as with no network or no release yet. */
    suspend fun latest(): DesktopRelease
}

/**
 * The desktop app's latest release as GitHub's API gives it, asked when the download page first wants it and kept
 * for as long as the app runs. GitHub answers a browser's question from any address, and the release's files are
 * downloaded from GitHub anyway; the app needs no key, as the repository is open.
 *
 * @param fetch what reads an address's text: the platform's own client, or a test's.
 */
class GitHubReleaseRepository(
    private val fetch: suspend (String) -> String? = ::fetchText
) : ReleaseRepository {
    private val lock = Mutex()
    private var kept: DesktopRelease? = null

    override suspend fun latest(): DesktopRelease = lock.withLock {
        kept ?: run {
            val text = checkNotNull(fetch(SiteDownloads.LATEST_API)) { "GitHub gave no answer" }
            releaseOf(text).also { kept = it }
        }
    }
}

/**
 * The release GitHub's API describes in [json]: its tag less its `v` as the version, the day it was published, and
 * the macOS disk image's size and SHA-256 where the release has one.
 */
fun releaseOf(json: String): DesktopRelease {
    val release = Json.parseToJsonElement(json).jsonObject
    val tag = checkNotNull(release.text("tag_name")) { "The release has no tag" }
    val published = checkNotNull(release.text("published_at")) { "The release is not published" }
    val (year, month, day) = published.substringBefore('T').split('-').map { it.toInt() }
    val mac = release["assets"]?.jsonArray.orEmpty().map { it.jsonObject }.firstOrNull {
        it.text("name") == SiteDownloads.MAC_FILE
    }
    return DesktopRelease(
        version = tag.removePrefix("v"),
        year = year,
        month = month,
        day = day,
        notes = release.text("html_url") ?: SiteDownloads.LATEST,
        macSize = mac?.get("size")?.jsonPrimitive?.longOrNull,
        // GitHub gives a file's hash as its kind and its digits: `sha256:0e6b…`.
        macSha256 = mac?.text("digest")?.takeIf { it.startsWith(SHA256) }?.removePrefix(SHA256)
    )
}

/** The text of an address, or null where it cannot be read or does not answer with success. */
internal expect suspend fun fetchText(url: String): String?

private fun JsonObject.text(key: String): String? = get(key)?.jsonPrimitive?.contentOrNull

private const val SHA256 = "sha256:"
