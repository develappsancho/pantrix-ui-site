package com.pantrixui.site

import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The desktop app's page: its disk image, its release notes, the releases to watch, and its hash copied. */
class DownloadPageTest {
    @Test
    fun the_hash_is_copied() = siteAt(Route.Download) {
        press(hasPantrixTag("Button", "copyHash"))
        until("the copy") { copied() != null }
        assertEquals(SCREEN_RELEASE.macSha256, copied())
    }

    @Test
    fun the_disk_image_the_notes_and_the_releases_open_outside_the_site() = siteAt(Route.Download) {
        press(hasPantrixTag("Button", "downloadMac"))
        until("the disk image") { services.opened.size == 1 }
        assertTrue(services.opened.last().endsWith(".dmg"), services.opened.last())
        press(hasPantrixTag("Button", "releaseNotes"))
        until("the notes") { services.opened.size == 2 }
        assertEquals(SCREEN_RELEASE.notes, services.opened.last())
        press(hasPantrixTag("Button", "download_macos"))
        press(hasPantrixTag("Button", "releases_windows"))
        press(hasPantrixTag("Button", "releases_linux"))
        until("the releases") { services.opened.size == 5 }
        assertTrue(services.opened.all { it.startsWith("https://github.com/") }, "${services.opened}")
        assertEquals("/download", address())
    }
}
