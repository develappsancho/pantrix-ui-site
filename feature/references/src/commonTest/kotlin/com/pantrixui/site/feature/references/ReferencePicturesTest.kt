package com.pantrixui.site.feature.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The pictures of the apps' cards: found by the app's key, one for each theme. */
class ReferencePicturesTest {
    @Test
    fun formatico_has_its_screen_in_each_theme() {
        assertNotNull(ReferencePictures.of("formatico", dark = false))
        assertNotNull(ReferencePictures.of("formatico", dark = true))
        assertTrue(setOf("reference_formatico", "reference_formatico_dark").all { it in ReferencePictures.names })
    }

    @Test
    fun an_app_without_a_picture_has_its_mark() {
        assertNull(ReferencePictures.of("pantrix-ui-demo", dark = false))
        assertNull(ReferencePictures.of("pantrix-ui-demo", dark = true))
    }

    /** A picture is made in both themes: a dark page with a light screen on it, or the other way, would glare. */
    @Test
    fun every_picture_has_one_for_the_other_theme() {
        val names = ReferencePictures.names
        val (dark, light) = names.partition { it.endsWith(ReferencePictures.DARK) }
        assertEquals(light.map { it + ReferencePictures.DARK }.toSet(), dark.toSet())
    }
}
