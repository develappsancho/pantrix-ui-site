package com.pantrixui.site.feature.references

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.PlatformKind
import com.pantrixui.site.core.model.Reference
import com.pantrixui.site.core.model.ReferencePlatform
import com.pantrixui.site.core.model.ReferencePoint

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun ReferencesScreenPreview() {
    ReferencesScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun ReferencesScreenDarkPreview() {
    ReferencesScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun ReferencesScreenPhonePreview() {
    ReferencesScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun ReferencesScreenPhoneDarkPreview() {
    ReferencesScreenSample(dark = true)
}

/** The references page with the site alone, as it opens today. */
@Composable
private fun ReferencesScreenSample(dark: Boolean) {
    SitePreview(dark) {
        ReferencesScreen(state = ReferencesUiState(projects = LoadState.Loaded(listOf(SAMPLE_SITE))), onAction = {})
    }
}

/** The site, as `content/references.json` writes it in English. */
private val SAMPLE_SITE = Reference(
    key = "pantrix-ui-site",
    name = "Pantrix UI's site",
    address = "pantrixui.com",
    line = "The library's documentation site and its apps for Android, iOS and macOS, from one Kotlin codebase.",
    about = "The library's own documentation site and its apps: the guides, a page for each of the " +
        "${LibraryFacts.COMPONENTS} components with its examples running live, the theme builder, the window " +
        "simulator and a page that asks the MCP server.",
    url = "https://pantrixui.com",
    source = "https://github.com/develappsancho/pantrix-ui-site",
    download = "/download",
    platforms = listOf(
        ReferencePlatform(PlatformKind.Web, "Web", "Wasm, and a JavaScript build for older browsers"),
        ReferencePlatform(PlatformKind.Android, "Android", "Phones, tablets and foldables"),
        ReferencePlatform(PlatformKind.Ios, "iOS", "iPhone and iPad"),
        ReferencePlatform(PlatformKind.Desktop, "macOS", "A signed and notarized app")
    ),
    languages = listOf("en", "tr"),
    components = listOf("PantrixButton", "PantrixCard", "PantrixCodeBlock", "PantrixSidebar", "PantrixTextField"),
    made = listOf(
        ReferencePoint("Compose Multiplatform, with no Material", "Pantrix's components on Compose Foundation alone."),
        ReferencePoint("A view model for each page", "MVI: a page's state, the actions taken on it and its effects.")
    )
)
