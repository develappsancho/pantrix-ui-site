package com.pantrixui.site.feature.roadmap

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun RoadmapScreenPreview() {
    RoadmapScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun RoadmapScreenDarkPreview() {
    RoadmapScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun RoadmapScreenPhonePreview() {
    RoadmapScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun RoadmapScreenPhoneDarkPreview() {
    RoadmapScreenSample(dark = true)
}

/** The roadmap as it opens: every stage, and the campaign that the server's sample draws. */
@Composable
private fun RoadmapScreenSample(dark: Boolean) {
    SitePreview(dark) { RoadmapScreen(state = RoadmapUiState(), onAction = {}) }
}
