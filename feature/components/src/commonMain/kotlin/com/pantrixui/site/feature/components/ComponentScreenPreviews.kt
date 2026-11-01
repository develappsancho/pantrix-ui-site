package com.pantrixui.site.feature.components

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
import com.pantrixui.site.core.model.ComponentCard
import com.pantrixui.site.core.model.ComponentExample
import com.pantrixui.site.core.model.ComponentFeature
import com.pantrixui.site.core.model.ComponentForm
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.ComponentParameter
import com.pantrixui.site.core.model.Language

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun ComponentScreenPreview() {
    ComponentScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun ComponentScreenDarkPreview() {
    ComponentScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun ComponentScreenPhonePreview() {
    ComponentScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun ComponentScreenPhoneDarkPreview() {
    ComponentScreenSample(dark = true)
}

/** The button's page, read: what it is, what it does, an example with its code, and its parameters. */
@Composable
private fun ComponentScreenSample(dark: Boolean) {
    SitePreview(dark) {
        ComponentScreen(state = ComponentUiState(page = LoadState.Loaded(PAGE)), onAction = {})
    }
}

/** The page of the button, short. */
private val PAGE = ComponentPage(
    language = Language.English,
    card = ComponentCard(
        key = "button",
        name = "Button",
        function = "PantrixButton",
        summary = previewSpans("A button that runs an action."),
        group = "elements"
    ),
    packageName = "com.pantrixui.components.elements.button",
    lead = previewSpans("A button runs an action when it is pressed: save a form, send a message, open a page."),
    features = listOf(
        ComponentFeature(label = "Variants.", text = previewSpans("Solid, soft, outline, text and destructive.")),
        ComponentFeature(label = "Sizes.", text = previewSpans("Four sizes, each a step of the platform's own."))
    ),
    examples = listOf(
        ComponentExample(
            name = "variants",
            title = "Variants",
            code = previewCode("PantrixButton(text = \"Save\", onClick = {}, variant = ButtonVariant.Solid)")
        )
    ),
    forms = listOf(
        ComponentForm(
            label = "PantrixButton",
            parameters = listOf(
                ComponentParameter("text", "String", description = previewSpans("What the button says.")),
                ComponentParameter("onClick", "() -> Unit", description = previewSpans("What pressing it does.")),
                ComponentParameter(
                    "variant",
                    "ButtonVariant",
                    default = "ButtonVariant.Solid",
                    description = previewSpans("How much it stands out.")
                )
            )
        )
    )
)
