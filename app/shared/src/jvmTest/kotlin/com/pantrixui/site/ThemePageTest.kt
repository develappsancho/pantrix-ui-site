package com.pantrixui.site

import androidx.compose.ui.test.hasTestTag
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The theme builder: a colour picked or typed, the corners, the density, the preview's platform and mode, the preset's
 * name, and what it gives: its code copied, its file saved, and the start again.
 */
class ThemePageTest {
    @Test
    fun a_picked_colour_is_the_one_its_hex_says_and_a_typed_one_picks_its_swatch() = siteAt(Route.Theme) {
        press(swatch("0F7B7D"))
        until("teal picked") { chosen(swatch("0F7B7D")) && "0F7B7D" in said(HEX).uppercase() }
        replace(HEX, "6D4AFF")
        until("violet picked by its hex") { chosen(swatch("6D4AFF")) && !chosen(swatch("0F7B7D")) }
    }

    @Test
    fun the_corners_the_density_and_the_preview_are_chosen() = siteAt(Route.Theme) {
        listOf(
            hasPantrixTag("Segment", "sharp", scope = "SegmentButton_presetcorners"),
            hasPantrixTag("Segment", "compact", scope = "SegmentButton_presetdensity"),
            hasPantrixTag("Segment", "ios", scope = "SegmentButton_previewplatform"),
            hasPantrixTag("Segment", "dark", scope = "SegmentButton_previewmode"),
            hasTestTag("Site_face_own")
        ).forEach { choice ->
            press(choice)
            until("${choice.description} chosen") { chosen(choice) }
        }
    }

    @Test
    fun its_code_is_copied_with_the_preset_s_name_and_colour() = siteAt(Route.Theme) {
        replace(hasPantrixTag("TextField", "presetName"), "Acme")
        press(swatch("C42A55"))
        until("raspberry picked") { chosen(swatch("C42A55")) }
        press(hasPantrixTag("Button", "themeCopy"))
        until("the copy") { copied() != null }
        val code = copied().orEmpty()
        assertTrue("Acme" in code && "C42A55" in code.uppercase(), code)
    }

    @Test
    fun its_file_is_saved() = siteAt(Route.Theme) {
        press(swatch("E5761F"))
        press(hasPantrixTag("Button", "themeDownload"))
        until("a file saved") { services.saver.files.isNotEmpty() }
        val file = services.saver.files.single()
        assertEquals("PantrixPreset.kt", file.name)
        assertTrue("E5761F" in runBlocking { file.readBytes() }.decodeToString().uppercase())
    }

    @Test
    fun it_starts_again_from_pantrix() = siteAt(Route.Theme) {
        press(swatch("101322"))
        until("ink picked") { chosen(swatch("101322")) }
        press(hasPantrixTag("Button", "themeReset"))
        until("Pantrix blue again") { chosen(swatch("2264FF")) && "2264FF" in said(HEX).uppercase() }
    }

    @Test
    fun its_preview_s_controls_work() = siteAt(Route.Theme) {
        val neighbour = hasPantrixTag("SwitchButton", "previewNeighbour")
        val was = on(neighbour)
        press(neighbour)
        until("the switch turned") { on(neighbour) != was }
        val gift = hasPantrixTag("Chip", "previewGift")
        val giftWas = chosen(gift)
        press(gift)
        until("the chip turned") { chosen(gift) != giftWas }
        val place = hasPantrixTag("Segment", "place2", scope = "SegmentButton_previewplace")
        press(place)
        until("the third place chosen") { chosen(place) }
    }

    private companion object {
        val HEX = hasPantrixTag("TextField", "presetHex")

        /** The swatch of the colour whose hex is [hex]. */
        fun swatch(hex: String) = hasTestTag("Site_swatch_$hex")
    }
}
