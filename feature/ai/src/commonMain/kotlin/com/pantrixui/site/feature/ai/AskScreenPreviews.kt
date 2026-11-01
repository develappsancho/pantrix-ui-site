package com.pantrixui.site.feature.ai

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_HEIGHT
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview
import com.pantrixui.site.core.model.AskComponent
import com.pantrixui.site.core.model.ComponentHit
import com.pantrixui.site.core.model.ComponentSnippet
import com.pantrixui.site.core.model.DocumentHit
import com.pantrixui.site.core.model.DocumentHits
import com.pantrixui.site.core.model.ExampleList
import com.pantrixui.site.core.model.ExampleNamed

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun AskScreenPreview() {
    AskScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = PREVIEW_DESKTOP_HEIGHT)
@Composable
private fun AskScreenDarkPreview() {
    AskScreenSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun AskScreenPhonePreview() {
    AskScreenSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = PREVIEW_PHONE_HEIGHT)
@Composable
private fun AskScreenPhoneDarkPreview() {
    AskScreenSample(dark = true)
}

/** The page with a question answered: the tools it called, a component's card, and the others it found. */
@Composable
private fun AskScreenSample(dark: Boolean) {
    SitePreview(dark) {
        AskScreen(state = SAMPLE, onAction = {})
    }
}

private val TEXT_AREA = ComponentHit(
    name = "TextArea",
    function = "PantrixTextArea",
    category = "inputs",
    page = "text-area",
    description = "A text input of several lines that grows with its text and then scrolls.",
    keywords = listOf("textarea", "multiline input", "comment box", "message box"),
    import = "com.pantrixui.components.inputs.textarea.PantrixTextArea",
    artifact = "com.pantrixui:components"
)

/** What the server answers "a comment box with several lines" with, as it answered it. */
private val SAMPLE = AskUiState(
    asked = "a comment box with several lines",
    phase = AskPhase.Answered,
    steps = listOf(
        AskStep("search_components", "a comment box with several lines", AskResult.Components(30, "TextArea")),
        AskStep(
            "search_documentation",
            "a comment box with several lines",
            AskResult.Documents(3, listOf("Text Area"))
        ),
        AskStep("get_component_metadata", "PantrixTextArea", AskResult.Card),
        AskStep("list_examples", "PantrixTextArea", AskResult.Examples(11))
    ),
    answer = AskAnswer.Component(
        hits = listOf(
            TEXT_AREA,
            TEXT_AREA.copy(name = "FilledTextArea", function = "PantrixFilledTextArea", page = "filled-text-area"),
            TEXT_AREA.copy(name = "TextField", function = "PantrixTextField", page = "text-field")
        ),
        shown = "PantrixTextArea",
        card = AskComponent(
            name = "TextArea",
            function = "PantrixTextArea",
            category = "inputs",
            description = TEXT_AREA.description,
            keywords = TEXT_AREA.keywords,
            import = TEXT_AREA.import,
            artifact = TEXT_AREA.artifact,
            page = "text-area",
            snippets = listOf(
                ComponentSnippet(
                    title = "A comment that grows with its text",
                    code = "val comment = rememberTextFieldState()\nPantrixTextArea(\n    state = comment,\n" +
                        "    label = stringResource(Res.string.comment),\n    maxLength = 500,\n" +
                        "    tagKey = \"comment\"\n)"
                )
            )
        ),
        examples = ExampleList(examples = List(11) { ExampleNamed("Example$it", "Example $it") }),
        documents = DocumentHits(1, listOf(DocumentHit("text-area", "page", "Text Area", "Text of several lines.")))
    )
)
