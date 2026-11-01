package com.pantrixui.site.core.content

import com.pantrixui.site.core.model.DesktopRelease
import com.pantrixui.site.core.model.SiteDownloads
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GitHubReleaseRepositoryTest {
    @Test
    fun a_release_is_its_tag_its_day_and_its_disk_image_s_size_and_hash() {
        assertEquals(
            DesktopRelease(
                version = "26.10.31",
                year = 2026,
                month = 10,
                day = 31,
                notes = "${SiteDownloads.RELEASES}/tag/v26.10.31",
                macSize = 92_143_171,
                macSha256 = HASH
            ),
            releaseOf(RELEASE)
        )
    }

    @Test
    fun a_release_s_day_is_the_one_its_version_names_not_the_utc_day_github_gives() {
        // Made at 00:50 in Istanbul on the 31st: GitHub says 21:50 on the 30th.
        val late = releaseOf(RELEASE.replace("2026-10-31T09:12:44Z", "2026-10-30T21:50:31Z"))
        assertEquals(Triple(2026, 10, 31), Triple(late.year, late.month, late.day))
        // A tag that names no day takes GitHub's.
        val named = releaseOf(
            RELEASE.replace("v26.10.31", "v1.2.0").replace("2026-10-31T09:12:44Z", "2026-11-02T08:00:00Z")
        )
        assertEquals(Triple(2026, 11, 2), Triple(named.year, named.month, named.day))
    }

    @Test
    fun a_release_without_the_disk_image_or_its_hash_has_neither() {
        val bare = releaseOf("""{"tag_name": "v26.11.2", "published_at": "2026-11-02T08:00:00Z", "assets": []}""")
        assertEquals("26.11.2", bare.version)
        assertEquals(Triple(2026, 11, 2), Triple(bare.year, bare.month, bare.day))
        assertEquals(SiteDownloads.LATEST, bare.notes, "a release with no page of its own leads to the latest's")
        assertNull(bare.macSize)
        val unhashed = releaseOf(RELEASE.replace("\"digest\": \"sha256:$HASH\",", ""))
        assertEquals(92_143_171, unhashed.macSize)
        assertNull(unhashed.macSha256)
    }

    @Test
    fun the_latest_release_is_asked_of_github_once_and_kept() = runTest {
        val asked = mutableListOf<String>()
        val repository = GitHubReleaseRepository { url -> RELEASE.also { asked += url } }
        repository.latest()
        val again = repository.latest()
        assertEquals("26.10.31", again.version)
        assertEquals(listOf(SiteDownloads.LATEST_API), asked)
    }

    @Test
    fun a_release_that_cannot_be_read_fails_and_is_asked_for_again_the_next_time() = runTest {
        var answer: String? = null
        val repository = GitHubReleaseRepository { answer }
        assertFailsWith<IllegalStateException> { repository.latest() }
        answer = "<html>No release here</html>"
        assertFailsWith<IllegalArgumentException> { repository.latest() }
        answer = RELEASE
        assertEquals("26.10.31", repository.latest().version)
    }

    private companion object {
        const val HASH = "0e6bb0079ca77d507192d5b8530f5897002a62ae4ab8d02dde71863f766707a5"

        /** The latest release as GitHub's API answers, less what the page does not read. */
        val RELEASE =
            """
            {
              "html_url": "https://github.com/develappsancho/pantrix-ui-site/releases/tag/v26.10.31",
              "tag_name": "v26.10.31",
              "name": "26.10.31",
              "draft": false,
              "prerelease": false,
              "published_at": "2026-10-31T09:12:44Z",
              "assets": [
                {
                  "name": "notes.txt",
                  "size": 12
                },
                {
                  "name": "PantrixUI-macOS.dmg",
                  "content_type": "application/x-apple-diskimage",
                  "size": 92143171,
                  "digest": "sha256:$HASH",
                  "browser_download_url": "${SiteDownloads.LATEST}/download/PantrixUI-macOS.dmg"
                }
              ]
            }
            """.trimIndent()
    }
}
