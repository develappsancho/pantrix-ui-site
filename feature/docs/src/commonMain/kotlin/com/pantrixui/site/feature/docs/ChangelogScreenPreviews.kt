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
import com.pantrixui.site.core.model.ChangeEntry
import com.pantrixui.site.core.model.ChangeGroup
import com.pantrixui.site.core.model.ChangeKind
import com.pantrixui.site.core.model.ChangeSection
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Release

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun ChangelogScreenPreview() {
    ChangelogScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun ChangelogScreenDarkPreview() {
    ChangelogScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun ChangelogScreenPhonePreview() {
    ChangelogScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun ChangelogScreenPhoneDarkPreview() {
    ChangelogScreenSample(dark = true)
}

/** The changelog, read: a release with what it added and what it fixed, by module, one change breaking. */
@Composable
private fun ChangelogScreenSample(dark: Boolean) {
    SitePreview(dark) {
        ChangelogScreen(state = ChangelogUiState(changelog = LoadState.Loaded(CHANGELOG)), onAction = {})
    }
}

/** A changelog of one release. */
private val CHANGELOG = Changelog(
    language = Language.English,
    title = "Changelog",
    description = previewSpans("What changed in each release, for someone who uses the library."),
    releases = listOf(
        Release(
            version = "0.1.0-alpha03",
            date = "2026-10-30",
            sections = listOf(
                ChangeSection(
                    kind = ChangeKind.Added,
                    title = "Added",
                    groups = listOf(
                        ChangeGroup(
                            module = "Components",
                            key = "components",
                            entries = listOf(
                                change("PantrixTextArea takes text of several lines."),
                                change("PantrixListItem draws a row of a list.")
                            )
                        )
                    )
                ),
                ChangeSection(
                    kind = ChangeKind.Fixed,
                    title = "Fixed",
                    groups = listOf(
                        ChangeGroup(
                            module = "Foundation",
                            key = "foundation",
                            entries = listOf(
                                ChangeEntry(
                                    blocks = paragraph("PantrixTheme reads the system's theme."),
                                    breaking = true
                                )
                            )
                        )
                    )
                )
            )
        )
    ),
    markdown = ""
)

/** A change that [text] says. */
private fun change(text: String): ChangeEntry = ChangeEntry(blocks = paragraph(text))

/** [text] as the blocks of a change. */
private fun paragraph(text: String): List<Block> = listOf(Block.Paragraph(previewSpans(text)))
