package com.pantrixui.site.feature.download

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview
import com.pantrixui.site.core.model.DesktopRelease
import com.pantrixui.site.core.model.SiteDownloads

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun DownloadScreenPreview() {
    DownloadScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun DownloadScreenDarkPreview() {
    DownloadScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun DownloadScreenPhonePreview() {
    DownloadScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun DownloadScreenPhoneDarkPreview() {
    DownloadScreenSample(dark = true)
}

/** The download page with a release read: its version, its day, and its file's size and hash. */
@Composable
private fun DownloadScreenSample(dark: Boolean) {
    SitePreview(dark) {
        DownloadScreen(state = DownloadUiState(release = LoadState.Loaded(SAMPLE_RELEASE)), onAction = {})
    }
}

/** A release as GitHub's API gives one. */
private val SAMPLE_RELEASE = DesktopRelease(
    version = "26.10.31",
    year = 2026,
    month = 10,
    day = 31,
    notes = "${SiteDownloads.RELEASES}/tag/v26.10.31",
    macSize = 92_143_171,
    macSha256 = "0e6bb0079ca77d507192d5b8530f5897002a62ae4ab8d02dde71863f766707a5"
)
