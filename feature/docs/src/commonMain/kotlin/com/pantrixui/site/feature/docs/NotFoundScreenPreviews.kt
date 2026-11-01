package com.pantrixui.site.feature.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun NotFoundScreenPreview() {
    NotFoundScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun NotFoundScreenDarkPreview() {
    NotFoundScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun NotFoundScreenPhonePreview() {
    NotFoundScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun NotFoundScreenPhoneDarkPreview() {
    NotFoundScreenSample(dark = true)
}

/** The page a wrong address opens: what it says, and the way back. */
@Composable
private fun NotFoundScreenSample(dark: Boolean) {
    SitePreview(dark) { NotFoundScreen(path = "/no-such-page", onNavigate = {}, onSearch = {}) }
}
