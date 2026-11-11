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
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun DocScreenPreview() {
    DocScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun DocScreenDarkPreview() {
    DocScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun DocScreenPhonePreview() {
    DocScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun DocScreenPhoneDarkPreview() {
    DocScreenSample(dark = true)
}

/** A page of the docs, read: its head, two parts with their code, and its contents beside it. */
@Composable
private fun DocScreenSample(dark: Boolean) {
    SitePreview(dark) {
        DocScreen(
            state = DocUiState(page = LoadState.Loaded(PAGE)),
            path = PAGE.path,
            onAction = {},
            onNavigate = {},
            onSearch = {}
        )
    }
}

/** The installation page, short. */
private val PAGE = Page(
    path = "/docs/installation",
    language = Language.English,
    title = "Installation",
    description = "Add Pantrix to a Compose Multiplatform project and draw its first screen.",
    section = "Getting started",
    blocks = listOf(
        Block.Heading(level = 2, anchor = "dependency", text = previewSpans("Add the dependency")),
        Block.Paragraph(previewSpans("Pantrix is one dependency in the shared module's common source set.")),
        previewCode("implementation(\"com.pantrixui:components:0.2.1\")"),
        Block.Heading(level = 2, anchor = "first-screen", text = previewSpans("Your first screen")),
        Block.Paragraph(previewSpans("Put the app's content in the theme, and draw a component in it.")),
        previewCode("PantrixTheme {\n    PantrixButton(text = \"Hello\", onClick = {})\n}", file = "App.kt")
    ),
    contents = listOf(
        ContentsEntry(anchor = "dependency", title = "Add the dependency"),
        ContentsEntry(anchor = "first-screen", title = "Your first screen")
    )
)
