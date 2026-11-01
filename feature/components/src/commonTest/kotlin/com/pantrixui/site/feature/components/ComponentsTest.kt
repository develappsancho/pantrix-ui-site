package com.pantrixui.site.feature.components

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ComponentCard
import com.pantrixui.site.core.model.ComponentExample
import com.pantrixui.site.core.model.ComponentForm
import com.pantrixui.site.core.model.ComponentGroup
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.ComponentParameter
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.testing.FakeContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ComponentsTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun card(
        key: String,
        name: String,
        group: String,
        summary: String
    ) = ComponentCard(key, name, "Pantrix${name.replace(" ", "")}", listOf(Span.Text(summary)), group)

    private val index = ComponentIndex(
        language = Language.English,
        groups = listOf(
            ComponentGroup(
                "elements",
                listOf(card("text", "Text", "elements", "Words."), card("button", "Button", "elements", "A press."))
            ),
            ComponentGroup("inputs", listOf(card("text-area", "Text Area", "inputs", "Several lines.")))
        )
    )

    private val textArea = ComponentPage(
        language = Language.English,
        card = index.groups.last().components.single(),
        packageName = "com.pantrixui.components.inputs.textarea",
        lead = listOf(Span.Text("A field of several lines.")),
        examples = listOf(
            ComponentExample("a-comment", "A comment", code = Block.Code("kotlin", lines = emptyList()))
        ),
        forms = listOf(
            ComponentForm("state", parameters = listOf(ComponentParameter("state", "TextFieldState"))),
            ComponentForm("value", parameters = listOf(ComponentParameter("value", "String")))
        )
    )

    @Test
    fun the_filter_keeps_the_components_whose_name_function_or_summary_holds_it_and_the_chips_count_them() {
        val state = ComponentsUiState(index = LoadState.Loaded(index), query = "lines")
        assertEquals(listOf("inputs"), state.shown.map { it.key })
        assertEquals(1, state.count(null))
        assertEquals(0, state.count("elements"))
        val all = state.copy(query = "")
        assertEquals(listOf("button", "text"), all.shown.first().components.map { it.key }, "by name")
        assertEquals(listOf("inputs"), all.copy(group = "inputs").shown.map { it.key })
    }

    @Test
    fun the_list_is_read_in_the_page_s_language_and_a_chosen_group_is_kept() = runTest(dispatcher) {
        val model = ComponentsViewModel(Language.English, FakeContentRepository(indexes = listOf(index)))
        advanceUntilIdle()
        assertEquals(index, model.state.value.index.valueOrNull)
        model.onAction(ComponentsAction.PickGroup("inputs"))
        model.onAction(ComponentsAction.Filter("area"))
        assertEquals("inputs", model.state.value.group)
        assertEquals(listOf("text-area"), model.state.value.shown.single().components.map { it.key })
    }

    @Test
    fun a_component_s_page_is_read_and_its_form_chosen_and_one_the_library_has_not_is_null() = runTest(dispatcher) {
        val content = FakeContentRepository(components = listOf(textArea))
        val model = ComponentViewModel("text-area", Language.English, content)
        advanceUntilIdle()
        assertEquals(textArea, model.state.value.page.valueOrNull)
        model.onAction(ComponentAction.PickForm(1))
        model.onAction(ComponentAction.ToggleParameters)
        assertEquals(1, model.state.value.form)
        assertTrue(model.state.value.allParameters)
        val missing = ComponentViewModel("nothing", Language.English, content)
        advanceUntilIdle()
        assertTrue(missing.state.value.page is LoadState.Loaded)
        assertNull(missing.state.value.page.valueOrNull)
    }

    @Test
    fun a_page_copied_is_its_name_its_lead_its_import_its_examples_and_its_forms() {
        val markdown = ComponentMarkdown.of(textArea)
        assertTrue(markdown.startsWith("# Text Area\n\nA field of several lines.\n"), markdown)
        assertTrue("import com.pantrixui.components.inputs.textarea.PantrixTextArea" in markdown, markdown)
        assertTrue("## A comment" in markdown && "- `state: TextFieldState`" in markdown, markdown)
    }

    @Test
    fun a_style_is_read_from_what_it_says_of_itself_a_style_inside_it_by_its_own_fields() {
        val fields = StyleValues.of(
            "ButtonStyle(shape=Medium, height=48.0.dp, padding=Dp.Unspecified, " +
                "medium=ButtonSizeStyle(height=40.0.dp, text=14.0.sp), textRole=Body)"
        )
        assertEquals("Medium", fields["shape"])
        assertEquals("48dp", fields["height"])
        assertEquals(StyleValues.NONE, fields["padding"])
        assertEquals("40dp", fields["medium.height"])
        assertEquals("14sp", fields["medium.text"])
        assertEquals("Body", fields["textRole"])
    }

    @Test
    fun the_site_knows_every_style_of_the_library_and_reads_each_on_every_platform() {
        STYLE_KEYS.keys.forEach { name ->
            val values = presetValues(name)
            assertTrue(values != null && values.values.all { it.isNotEmpty() }, "$name has no fields the page reads")
        }
    }
}
