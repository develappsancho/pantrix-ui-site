package com.pantrixui.site.core.model

/**
 * Where the site's desktop app is downloaded: the releases of the site's own repository on GitHub, which
 * `deploy/deploy.sh desktop` makes. The file of each platform keeps its name from one release to the next, so the
 * address of the latest one's never changes.
 */
object SiteDownloads {
    /** The site's repository on GitHub, which is open. */
    const val REPOSITORY = "https://github.com/develappsancho/pantrix-ui-site"

    /** Every release of the site, the newest first. */
    const val RELEASES = "$REPOSITORY/releases"

    /** The page of the latest release, with its notes. */
    const val LATEST = "$RELEASES/latest"

    /** The name of the macOS app's disk image in every release. */
    const val MAC_FILE = "PantrixUI-macOS.dmg"

    /** The macOS app's disk image in the latest release. */
    const val MAC_DOWNLOAD = "$LATEST/download/$MAC_FILE"

    /** What GitHub's API says of the latest release: its version, its day and its files with their sizes and hashes. */
    const val LATEST_API = "https://api.github.com/repos/develappsancho/pantrix-ui-site/releases/latest"
}

/**
 * The desktop app's latest release, as the download page shows it.
 *
 * @property version its version, the day it was made: `26.10.31`.
 * @property year the year GitHub published it in, in UTC.
 * @property month the month it was published in, from 1 for January.
 * @property day the day of the month it was published on.
 * @property notes the address of its page, with its notes.
 * @property macSize the macOS disk image's size in bytes, or null where the release has none.
 * @property macSha256 the macOS disk image's SHA-256 in small hexadecimal letters, or null where GitHub gives none.
 */
data class DesktopRelease(
    val version: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val notes: String,
    val macSize: Long?,
    val macSha256: String?
)
