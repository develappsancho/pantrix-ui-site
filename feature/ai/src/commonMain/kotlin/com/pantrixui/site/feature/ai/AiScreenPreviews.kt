package com.pantrixui.site.feature.ai

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
private fun AiScreenPreview() {
    AiScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun AiScreenDarkPreview() {
    AiScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun AiScreenPhonePreview() {
    AiScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun AiScreenPhoneDarkPreview() {
    AiScreenSample(dark = true)
}

/** The page for coding assistants with the server's answer read, the conversation at its last step. */
@Composable
private fun AiScreenSample(dark: Boolean) {
    SitePreview(dark) {
        AiScreen(
            state = AiUiState(
                answers = LoadState.Loaded(listOf(ANSWER, ANSWER_TABLET, ANSWER_PHONE).associateWith { ANSWER_CODE })
            ),
            onAction = {}
        )
    }
}

/** What the server answers a question about a button, as the page's content writes it. */
private val ANSWER_CODE = previewCode("PantrixButton(\n    text = \"Save\",\n    onClick = { save() }\n)")
