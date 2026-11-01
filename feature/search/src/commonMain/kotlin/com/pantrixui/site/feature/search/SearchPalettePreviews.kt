package com.pantrixui.site.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.SearchEntry
import com.pantrixui.site.core.model.SearchIndex
import com.pantrixui.site.core.model.SearchKind

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun SearchPalettePreview() {
    SearchPaletteSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun SearchPaletteDarkPreview() {
    SearchPaletteSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun SearchPalettePhonePreview() {
    SearchPaletteSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun SearchPalettePhoneDarkPreview() {
    SearchPaletteSample(dark = true)
}

/** The search as it opens over a page, its index read: its field, its filters by kind and what it holds. */
@Composable
private fun SearchPaletteSample(dark: Boolean) {
    SitePreview(dark) {
        SearchPalette(
            state = SearchUiState(index = LoadState.Loaded(INDEX)),
            onAction = {},
            onDismiss = {},
            onOpen = { _, _ -> }
        )
    }
}

/** A few of what the search finds: components, an API, a part of a page and a guide. */
private val INDEX = SearchIndex(
    language = Language.English,
    entries = listOf(
        SearchEntry(SearchKind.Component, "Button", "Elements", "/components/button", mark = "B"),
        SearchEntry(SearchKind.Component, "TextField", "Inputs", "/components/text-field", mark = "T"),
        SearchEntry(SearchKind.Api, "PantrixTheme", "Foundation", "/foundations", mark = "{}"),
        SearchEntry(SearchKind.Section, "Add the dependency", "Installation", "/docs/installation", "dependency", "#"),
        SearchEntry(SearchKind.Guide, "Adaptive layouts", "Guides", "/docs/guides/adaptive", mark = "G")
    )
)
