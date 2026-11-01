package com.pantrixui.site

import com.pantrixui.site.core.model.AskComponent
import com.pantrixui.site.core.model.ComponentHit
import com.pantrixui.site.core.model.ComponentHits
import com.pantrixui.site.core.model.ComponentSnippet
import com.pantrixui.site.core.model.DeclarationHit
import com.pantrixui.site.core.model.DeclarationHits
import com.pantrixui.site.core.model.DocumentHit
import com.pantrixui.site.core.model.DocumentHits
import com.pantrixui.site.core.model.ExampleList
import com.pantrixui.site.core.model.ExampleNamed
import com.pantrixui.site.core.model.IconHit
import com.pantrixui.site.core.model.IconHits
import com.pantrixui.site.core.testing.FakeAskRepository

/**
 * The library's MCP server as the tests that draw the site have it: every question finds the snackbar and the alert,
 * the snackbar first, with their cards, two examples, two declarations, an icon and a document. It keeps each call.
 */
internal val TEST_ASK_SERVER = FakeAskRepository(
    components = ComponentHits(
        2,
        listOf(SNACKBAR, SNACKBAR.copy(name = "Alert", function = "PantrixAlert", page = "alert"))
    ),
    cards = mapOf(
        "PantrixSnackbarHost" to SNACKBAR_CARD,
        "PantrixAlert" to SNACKBAR_CARD.copy(name = "Alert", function = "PantrixAlert", page = "alert")
    ),
    examples = mapOf(
        "PantrixSnackbarHost" to
            ExampleList(examples = listOf(ExampleNamed("Undo", "Undo"), ExampleNamed("Wait", "Wait")))
    ),
    declarations = DeclarationHits(
        2,
        listOf(
            DeclarationHit(
                "SnackbarDuration",
                "SnackbarDuration",
                "enum",
                "components",
                "com.pantrixui:components",
                "com.pantrixui.components.overlay.snackbar",
                "com.pantrixui.components.overlay.snackbar.SnackbarDuration",
                "How long a snackbar stays."
            ),
            DeclarationHit(
                "SnackbarResult",
                "SnackbarResult",
                "enum",
                "components",
                "com.pantrixui:components",
                "com.pantrixui.components.overlay.snackbar",
                "com.pantrixui.components.overlay.snackbar.SnackbarResult",
                "How a snackbar left."
            )
        )
    ),
    icons = IconHits(1, listOf(IconHit("ArrowBack", "PantrixIcons.ArrowBack", "Back.", mirrored = true))),
    documents = DocumentHits(1, listOf(DocumentHit("snackbar", "page", "Snackbar", "A short message.")))
)

private val SNACKBAR: ComponentHit
    get() = ComponentHit(
        name = "Snackbar",
        function = "PantrixSnackbarHost",
        category = "overlay",
        page = "snackbar",
        description = "A short message.",
        keywords = listOf("toast"),
        import = "com.pantrixui.components.overlay.snackbar.PantrixSnackbarHost",
        artifact = "com.pantrixui:components"
    )

private val SNACKBAR_CARD: AskComponent
    get() = AskComponent(
        name = "Snackbar",
        function = "PantrixSnackbarHost",
        category = "overlay",
        description = "A short message.",
        import = SNACKBAR.import,
        artifact = SNACKBAR.artifact,
        page = "snackbar",
        snippets = listOf(ComponentSnippet("Undo", "PantrixSnackbarHost(snackbars)"))
    )
