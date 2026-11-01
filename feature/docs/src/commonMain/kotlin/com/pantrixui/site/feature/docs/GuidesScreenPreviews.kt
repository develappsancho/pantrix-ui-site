package com.pantrixui.site.feature.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview
import com.pantrixui.site.core.designsystem.previewSpans
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.GuideCatalogue
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun GuidesScreenPreview() {
    GuidesScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun GuidesScreenDarkPreview() {
    GuidesScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun GuidesScreenPhonePreview() {
    GuidesScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun GuidesScreenPhoneDarkPreview() {
    GuidesScreenSample(dark = true)
}

/** The guides' page, read: every guide of the catalogue by its topic, each with how long it takes to read. */
@Composable
private fun GuidesScreenSample(dark: Boolean) {
    SitePreview(dark) {
        GuidesScreen(
            state = GuidesUiState(
                page = LoadState.Loaded(PAGE),
                minutes = GuideCatalogue.entries.associate { it.key to MINUTES }
            ),
            onAction = {},
            onNavigate = {},
            onSearch = {}
        )
    }
}

/** The guides' page: its head alone, since the guides come from the catalogue. */
private val PAGE = Page(
    path = "/docs/guides",
    language = Language.English,
    title = "Guides",
    description = "What every Compose Multiplatform app meets, and how Pantrix answers it.",
    blocks = listOf(Block.Paragraph(previewSpans("Each guide shows a problem, why it happens and the fix.")))
)

/** How long each guide takes to read. */
private const val MINUTES = 6
