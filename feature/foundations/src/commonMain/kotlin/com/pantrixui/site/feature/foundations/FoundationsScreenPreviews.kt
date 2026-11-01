package com.pantrixui.site.feature.foundations

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun FoundationsScreenPreview() {
    FoundationsScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun FoundationsScreenDarkPreview() {
    FoundationsScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun FoundationsScreenPhonePreview() {
    FoundationsScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun FoundationsScreenPhoneDarkPreview() {
    FoundationsScreenSample(dark = true)
}

/** The foundations page as it opens: its samples in the light theme, still. */
@Composable
private fun FoundationsScreenSample(dark: Boolean) {
    SitePreview(dark) { FoundationsScreen(state = FoundationsUiState(), onAction = {}) }
}
