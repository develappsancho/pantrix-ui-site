package com.pantrixui.site.feature.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview
import com.pantrixui.site.core.designsystem.previewCode

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun AdaptiveScreenPreview() {
    AdaptiveScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun AdaptiveScreenDarkPreview() {
    AdaptiveScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun AdaptiveScreenPhonePreview() {
    AdaptiveScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun AdaptiveScreenPhoneDarkPreview() {
    AdaptiveScreenSample(dark = true)
}

/** The adaptive page with its code read, the simulator's inbox on a laptop. */
@Composable
private fun AdaptiveScreenSample(dark: Boolean) {
    SitePreview(dark) {
        AdaptiveScreen(
            state = AdaptiveUiState(
                code = LoadState.Loaded(
                    (listOf("window", "frame") + AdaptiveSay.Patterns.map { it.code }).associateWith { WINDOW_CODE }
                )
            ),
            onAction = {},
            onGuides = {}
        )
    }
}

/** The code each part of the page shows, as its content writes it. */
private val WINDOW_CODE =
    previewCode("val window = PantrixTheme.window\nval columns = responsive(compact = 1, medium = 2)")
