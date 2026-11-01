package com.pantrixui.site

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import com.pantrixui.site.core.model.Route
import com.pantrixui.testing.hasPantrixTag
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The foundations: the colours light and dark, the motion played, the three densities, and the contents. */
class FoundationsPageTest {
    @Test
    fun the_colours_are_shown_light_and_dark() = siteAt(Route.Foundations) {
        val dark = hasPantrixTag("Segment", "dark", scope = "SegmentButton_foundationscolours")
        val light = hasPantrixTag("Segment", "light", scope = "SegmentButton_foundationscolours")
        until("the colours") { has(light) }
        val lightWords = words()
        press(dark)
        until("the dark colours") { chosen(dark) && words() != lightWords }
        press(light)
        until("the light colours again") { chosen(light) && words() == lightWords }
    }

    @Test
    fun the_motion_is_played_and_played_back() = siteAt(Route.Foundations) {
        val play = hasPantrixTag("Button", "playMotion")
        val slow = hasText("slow ·", substring = true)
        scrollTo(slow)
        val still = track(slow)
        press(play)
        settle()
        scrollTo(slow)
        val moved = track(slow)
        assertFalse(still.contentEquals(moved), "the slow dot went along its track")
        press(play)
        settle()
        scrollTo(slow)
        assertTrue(still.contentEquals(track(slow)), "the slow dot came back")
    }

    /** The window's pixels in the band of the line [label] finds: a track's name, its track and its dot. */
    private fun SiteScreen.track(label: SemanticsMatcher): IntArray {
        val line = onAllNodes(label, useUnmergedTree = true).onFirst().fetchSemanticsNode().boundsInRoot
        val image = onAllNodes(isRoot()).onFirst().captureToImage().toPixelMap()
        val rows = line.top.toInt().coerceAtLeast(0) until line.bottom.toInt().coerceAtMost(image.height)
        return rows.flatMap { y -> (0 until image.width).map { x -> image[x, y].hashCode() } }.toIntArray()
    }

    @Test
    fun the_densities_draw_one_button_at_three_heights() = siteAt(Route.Foundations) {
        val compact = hasPantrixTag("Button", "density_compact")
        scrollTo(compact)
        val heights = listOf("compact", "comfortable", "spacious").map { density ->
            onAllNodes(hasPantrixTag("Button", "density_$density"), useUnmergedTree = true)
                .fetchSemanticsNodes().single().size.height
        }
        assertTrue(heights[0] < heights[1] && heights[1] < heights[2], "$heights")
    }

    @Test
    fun its_contents_bring_a_section_into_view() = siteAt(Route.Foundations) {
        press(hasTestTag("Site_toc_density"))
        untilShown("the densities in view", hasPantrixTag("Button", "density_comfortable"))
        press(hasTestTag("Site_toc_colour"))
        untilShown(
            "the colours in view",
            hasPantrixTag("SegmentButton", "foundationsColours") and !hasAnyAncestor(hasTestTag("Site_toc_colour"))
        )
        assertTrue(has(hasText("Colour")))
    }
}
