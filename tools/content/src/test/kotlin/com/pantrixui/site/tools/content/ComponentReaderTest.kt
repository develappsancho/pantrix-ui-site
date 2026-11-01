package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The components as the site reads them from the library's documents: a page of the demo, and the list of them. */
class ComponentReaderTest {
    private val english =
        """
        # Text Area

        Text of several lines.

        - `PantrixTextArea` (`com.pantrixui.components.inputs.textarea`, `com.pantrixui:components`): A text input.

        ## Features

        - **Lines.** `minLines` says how many lines it has
          at the least.
        - **The count and a screen reader.** A screen reader is told the count once typing stops.

        ## Examples

        ### A comment that grows

        It grows a line at a time.

        ```kotlin
        import com.pantrixui.components.inputs.textarea.PantrixTextArea

        PantrixTextArea(state = comment, tagKey = "comment")
        ```

        ### In the app's colours

        ```kotlin
        PantrixTextArea(state = note, colors = ownColors())
        ```

        What the code calls of the demo app itself, which an app writes for itself:

        - `ownColors`: The examples' own colours.

        ## API

        ### PantrixTextArea

        `import com.pantrixui.components.inputs.textarea.PantrixTextArea`, from `com.pantrixui:components`

        ```kotlin
        @Composable
        fun PantrixTextArea(
            state: TextFieldState,
            onSubmit: (() -> Unit)? = null,
            style: TextFieldStyle? = null,
            colors: TextFieldColors? = null,
            tagKey: String? = null
        )
        ```

        A text area: a field for text of several lines.

        Parameters:

        - `state: TextFieldState`: the text.
        - `onSubmit: (() -> Unit)?`: what Ctrl and Enter does.
        - `style: TextFieldStyle?`: its style.
        - `colors: TextFieldColors?`: its colours.
        - `tagKey: String?`: its key: the field is `TextArea_<tagKey>`, its clear button `TextAreaClear_<tagKey>`.

        ### TextFieldStyle

        ```kotlin
        class TextFieldStyle(public val height: Dp = Dp.Unspecified)
        ```

        A field's box.

        Parameters:

        - `height: Dp`: how tall it is.

        ### TextFieldColors

        ```kotlin
        data class TextFieldColors(public val container: Color = Color.Unspecified)
        ```

        What a field is painted with.

        Parameters:

        - `container: Color`: The box's fill. The theme's `surface`.
        """.trimIndent()

    private val json =
        """
        {
          "pages": [{ "key": "text-area", "title": "Text Area" }],
          "components": [{
            "name": "TextArea", "function": "PantrixTextArea", "category": "inputs",
            "description": "A text input of several lines.", "package": "com.pantrixui.components.inputs.textarea",
            "page": "text-area", "tags": ["TextArea", "TextAreaClear"]
          }]
        }
        """.trimIndent()

    private val reader = ComponentReader(
        json = json,
        pages = mapOf(
            Language.English to mapOf("text-area" to LibraryPage.read(english)),
            Language.Turkish to emptyMap()
        )
    )

    @Test
    fun a_page_of_the_documents_has_its_features_its_examples_and_its_api() {
        val page = LibraryPage.read(english)
        assertEquals("Text Area", page.title)
        assertEquals("**Lines.** `minLines` says how many lines it has\nat the least.", page.features.first())
        val (comment, coloured) = page.examples
        assertEquals("A comment that grows", comment.title)
        assertEquals(listOf("com.pantrixui.components.inputs.textarea.PantrixTextArea"), comment.imports)
        assertEquals("PantrixTextArea(state = comment, tagKey = \"comment\")", comment.code)
        assertFalse(comment.callsTheApp)
        assertTrue(coloured.callsTheApp, "an example that names what it calls of the app")
        val area = page.declarations.first { it.name == "PantrixTextArea" }
        assertEquals(listOf("state", "onSubmit", "style", "colors", "tagKey"), area.parameters.map { it.name })
        assertNull(area.parameters.first().default, "a parameter that has to be given")
        assertEquals("null", area.parameters[1].default)
        assertEquals("(() -> Unit)?", area.parameters[1].type)
    }

    @Test
    fun a_default_is_read_past_the_arrows_and_the_brackets_of_its_type() {
        val defaults = LibraryPage.defaultsOf(
            "fun Chart(\n    label: (Int) -> String = { \"Day \$it\" },\n    size: Pair<Int, Int> = 1 to 2,\n" +
                "    mode: Mode = Mode.A\n)"
        )
        assertEquals("{ \"Day \$it\" }", defaults["label"])
        assertEquals("1 to 2", defaults["size"])
        assertEquals("Mode.A", defaults["mode"])
    }

    @Test
    fun a_component_s_page_has_its_forms_its_style_its_colours_its_tags_and_what_is_about_accessibility() {
        val page = reader.pages(Language.English).single()
        assertEquals("text-area", page.card.key)
        assertEquals("Text Area", page.card.name)
        assertEquals(listOf("state"), page.forms.map { it.label })
        assertEquals("TextFieldStyle", page.style?.name)
        assertEquals("surface", page.colors?.fields?.single()?.role)
        assertEquals(listOf("Field", "Clear button"), page.tags.map { it.label })
        assertEquals("comment", page.tagKey)
        assertEquals(listOf("Lines"), page.features.map { it.label })
        assertEquals(listOf("The count and a screen reader"), page.accessibility.map { it.label })
        assertEquals(listOf("a-comment-that-grows", "in-the-app-s-colours"), page.examples.map { it.name })
        assertEquals(listOf("TextFieldStyle", "TextFieldColors"), page.types)
    }

    @Test
    fun the_site_runs_an_example_that_calls_nothing_of_the_app() {
        assertEquals(listOf("a-comment-that-grows"), reader.runnable(Language.English).map { it.name })
    }

    @Test
    fun the_list_has_each_group_in_the_site_s_order() {
        val index = reader.index(Language.English)
        assertEquals("elements", index.groups.first().key)
        assertEquals(listOf("text-area"), index.groups.single { it.key == "inputs" }.components.map { it.key })
    }

    @Test
    fun an_example_s_body_is_apart_from_what_of_its_file_it_calls_in_an_order_that_compiles() {
        val (body, declarations) = LiveExampleWriter.split(
            "val x = 1\n\nPantrixText(text = \"\$x\")\n\nprivate val DAILY = WEEK.size\n\nprivate val WEEK = listOf(1)"
        )
        assertEquals("val x = 1\n\nPantrixText(text = \"\$x\")", body)
        assertEquals(
            "private val WEEK = listOf(1)\n\nprivate val DAILY = WEEK.size",
            LiveExampleWriter.ordered(declarations)
        )
        assertEquals("x2two", LiveExampleWriter.segment("2-two"), "a name that starts with a digit")
        assertEquals("inx", LiveExampleWriter.segment("in"))
    }
}
