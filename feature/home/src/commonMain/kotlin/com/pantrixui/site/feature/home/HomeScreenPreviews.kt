package com.pantrixui.site.feature.home

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
private fun HomeScreenPreview() {
    HomeScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun HomeScreenDarkPreview() {
    HomeScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun HomeScreenPhonePreview() {
    HomeScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun HomeScreenPhoneDarkPreview() {
    HomeScreenSample(dark = true)
}

/** The home page with the code of its form read, the form on iOS. */
@Composable
private fun HomeScreenSample(dark: Boolean) {
    SitePreview(dark) {
        HomeScreen(
            state = HomeUiState(code = LoadState.Loaded(HomeCode(FORM, FORM, FORM, PRESET))),
            onAction = {}
        )
    }
}

/** The code of the page's sign-in form, and of the preset it is drawn in. */
private val FORM = previewCode(
    "PantrixTextField(state = email, label = \"Email\")\nPantrixButton(text = \"Sign in\", onClick = ::signIn)"
)

private val PRESET = previewCode("PantrixTheme(preset = PantrixPreset.Default) {\n    SignIn()\n}")
