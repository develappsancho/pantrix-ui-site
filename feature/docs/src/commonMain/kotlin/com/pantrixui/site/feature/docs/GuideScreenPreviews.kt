package com.pantrixui.site.feature.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview
import com.pantrixui.site.core.designsystem.previewCode
import com.pantrixui.site.core.designsystem.previewSpans
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ContentsEntry
import com.pantrixui.site.core.model.GuideCatalogue
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Tone

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun GuideScreenPreview() {
    GuideScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun GuideScreenDarkPreview() {
    GuideScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun GuideScreenPhonePreview() {
    GuideScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun GuideScreenPhoneDarkPreview() {
    GuideScreenSample(dark = true)
}

/** A guide, read: its head with its tags, the problem it solves with a note on why, and the code of the fix. */
@Composable
private fun GuideScreenSample(dark: Boolean) {
    SitePreview(dark) {
        DocScreen(
            state = DocUiState(page = LoadState.Loaded(PAGE)),
            path = PAGE.path,
            onAction = {},
            onNavigate = {},
            onSearch = {},
            guide = GuideCatalogue.entries.first()
        )
    }
}

/** The first guide of the catalogue, short. */
private val PAGE = Page(
    path = "/docs/guides/${GuideCatalogue.entries.first().key}",
    language = Language.English,
    title = GuideCatalogue.entries.first().title.of(Language.English),
    description = GuideCatalogue.entries.first().summary.of(Language.English),
    section = "Guides",
    blocks = listOf(
        Block.Heading(level = 2, anchor = "problem", text = previewSpans("The problem")),
        Block.Paragraph(previewSpans("A layout drawn for a phone stretches on a tablet and folds badly on a desktop.")),
        Block.Callout(
            Tone.Why,
            listOf(Block.Paragraph(previewSpans("Compose gives a window its size, not its class.")))
        ),
        Block.Heading(level = 2, anchor = "fix", text = previewSpans("The fix")),
        previewCode("val columns = responsive(compact = 1, medium = 2, expanded = 3)")
    ),
    contents = listOf(
        ContentsEntry(anchor = "problem", title = "The problem"),
        ContentsEntry(anchor = "fix", title = "The fix")
    ),
    minutes = 6
)
