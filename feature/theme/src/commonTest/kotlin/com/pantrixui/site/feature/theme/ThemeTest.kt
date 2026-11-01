package com.pantrixui.site.feature.theme

import androidx.compose.ui.graphics.Color
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.token.color.contrastRatio
import com.pantrixui.foundation.token.density.PantrixDensity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun a_colour_s_code_is_read_whole_or_not_at_all() {
        assertEquals(0xFF2264FF, parseHex("#2264ff"))
        assertEquals(0xFF2264FF, parseHex(" 2264FF "))
        assertNull(parseHex("#2264F"), "half typed")
        assertNull(parseHex("#2264FG"))
        assertEquals("#6D4AFF", hexOf(0xFF6D4AFF))
    }

    @Test
    fun colours_are_mixed_channel_by_channel_as_the_design_mixes_them() {
        assertEquals(Color(0xFF808080), mix(Color.Black, Color.White, 0.5f), "half of 255, rounded up")
        assertEquals(Color.White, mix(Color(0xFF2264FF), Color.White, 1f))
        assertEquals(Color(0xFF2264FF), mix(Color(0xFF2264FF), Color.White, 0f))
    }

    @Test
    fun the_brand_s_colour_makes_a_role_whose_words_read_in_both_themes() {
        listOf(0xFF2264FF, 0xFF6D4AFF, 0xFF0F7B7D, 0xFFE5761F, 0xFFC42A55, 0xFF101322, 0xFFFFE066).forEach { argb ->
            listOf(false, true).forEach { dark ->
                val role = primaryRole(Color(argb), dark)
                val onIt = contrastRatio(role.onColor, role.color)
                // White or near black, whichever reads better: one of the two always reaches large text's 3:1.
                assertTrue(onIt >= 3f, "${hexOf(argb)} dark=$dark reads $onIt")
            }
            // A link in a light theme is darkened until it reads on white, as far as eight steps take it.
            assertTrue(contrastRatio(textShade(Color(argb), dark = false), Color.White) >= 4.5f, hexOf(argb))
        }
        assertEquals(Color.White, primaryRole(Color(0xFF2264FF), dark = false).onColor)
        assertEquals(NEAR_BLACK, primaryRole(Color(0xFFFFE066), dark = false).onColor)
    }

    @Test
    fun the_kotlin_is_a_whole_preset_named_after_the_brand() {
        val state = ThemeUiState(name = "Acme & Co.", primary = 0xFF6D4AFF, corners = Corners.Soft, face = Face.Own)
        assertEquals("AcmeCoPreset", state.objectName)
        assertEquals("AcmeCoPreset.kt", state.fileName)
        assertEquals("BrandPreset", ThemeUiState(name = "  ").objectName)
        val code = presetText(state)
        assertTrue(code.startsWith("object AcmeCoPreset : DesignPreset {"), code)
        assertTrue("override val name = \"Acme & Co.\"" in code, code)
        assertTrue("xl = RoundedCornerShape(28.dp)" in code, code)
        assertTrue("PantrixFont(Res.font.brand_bold, FontWeight.Bold)" in code, code)
        assertTrue("color = Color(0xFF6D4AFF)" in code, "the light primary is the brand's own")
        assertTrue("private val AcmeCoPresetDark = PantrixColors(" in code, code)
        // Every role of the colours is written out: the file compiles as it is copied.
        listOf("success", "scrim", "chart = listOf(", "textInverse").forEach { assertTrue(it in code, it) }
    }

    @Test
    fun the_model_keeps_a_half_typed_code_and_starts_again_where_the_preview_stands() = runTest(dispatcher) {
        val model = ThemeViewModel()
        model.onAction(ThemeAction.Hex("#6D4"))
        assertEquals("#6D4", model.state.value.hexInput)
        assertEquals(DEFAULT_PRIMARY, model.state.value.primary, "a code half typed changes nothing yet")
        model.onAction(ThemeAction.Hex("#6D4AFF"))
        assertEquals(0xFF6D4AFF, model.state.value.primary)
        model.onAction(ThemeAction.PickColour(0xFF0F7B7D))
        assertEquals("#0F7B7D", model.state.value.hexInput)
        model.onAction(ThemeAction.PickDensity(PantrixDensity.Compact))
        model.onAction(ThemeAction.PickPlatform(PlatformType.Ios))
        model.onAction(ThemeAction.ShowDark(true))
        model.onAction(ThemeAction.Reset)
        val reset = model.state.value
        assertEquals(DEFAULT_PRIMARY, reset.primary)
        assertEquals(PantrixDensity.Comfortable, reset.density)
        assertEquals(PlatformType.Ios, reset.platform, "the preview's platform and theme stay")
        assertTrue(reset.dark)
        model.onAction(ThemeAction.CopyKotlin)
        val copy = model.effects.first()
        assertIs<ThemeEffect.Copy>(copy)
        assertTrue(copy.text.startsWith("object PantrixPreset : DesignPreset {"))
    }

    @Test
    fun a_download_hands_over_the_preset_s_file_by_its_name() = runTest(dispatcher) {
        val model = ThemeViewModel()
        model.onAction(ThemeAction.Name("Acme"))
        model.onAction(ThemeAction.Download)
        val save = model.effects.first()
        assertIs<ThemeEffect.Save>(save)
        assertEquals("AcmePreset.kt", save.name)
        assertEquals(presetText(model.state.value), save.text, "the file is the code shown")
    }
}
