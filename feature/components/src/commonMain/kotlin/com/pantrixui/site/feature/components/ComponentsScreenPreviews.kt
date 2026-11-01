package com.pantrixui.site.feature.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview
import com.pantrixui.site.core.designsystem.previewSpans
import com.pantrixui.site.core.model.ComponentCard
import com.pantrixui.site.core.model.ComponentGroup
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.Language

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun ComponentsScreenPreview() {
    ComponentsScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun ComponentsScreenDarkPreview() {
    ComponentsScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun ComponentsScreenPhonePreview() {
    ComponentsScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun ComponentsScreenPhoneDarkPreview() {
    ComponentsScreenSample(dark = true)
}

/** The list of the components with a few read, in two groups, nothing filtered. */
@Composable
private fun ComponentsScreenSample(dark: Boolean) {
    SitePreview(dark) {
        ComponentsScreen(state = ComponentsUiState(index = LoadState.Loaded(INDEX)), onAction = {})
    }
}

/** A few components of two groups, with what each is for. */
private val INDEX = ComponentIndex(
    language = Language.English,
    groups = listOf(
        ComponentGroup(
            key = "elements",
            components = listOf(
                card("button", "Button", "A button that runs an action.", "elements"),
                card("chip", "Chip", "A compact action, filter or value.", "elements"),
                card("badge", "Badge", "A dot or a count on what it marks.", "elements")
            )
        ),
        ComponentGroup(
            key = "inputs",
            components = listOf(
                card("text-field", "TextField", "A box that takes a line of text.", "inputs"),
                card("text-area", "TextArea", "A box that takes several lines.", "inputs")
            )
        )
    )
)

/** The card of the component [name], its page at [key], in [group], and [summary] under its name. */
private fun card(
    key: String,
    name: String,
    summary: String,
    group: String
): ComponentCard = ComponentCard(key, name, "Pantrix$name", previewSpans(summary), group)
