package com.pantrixui.site.core.designsystem.content

import kotlin.test.Test
import kotlin.test.assertEquals

class ColumnWidthsTest {
    private fun widths(
        room: Int,
        vararg columns: Pair<Float, Float>
    ) = columnWidths(room, columns.size, { listOf(columns[it]) }, padding = 0f)

    @Test
    fun columns_that_fit_their_widest_lines_share_what_is_left_by_those_widths() {
        assertEquals(listOf(200, 400), widths(600, 100f to 100f, 100f to 200f))
    }

    @Test
    fun columns_that_fit_only_their_longest_words_share_the_rest_by_how_much_more_each_would_take() {
        assertEquals(listOf(100, 300), widths(400, 100f to 100f, 100f to 900f))
    }

    @Test
    fun where_even_the_longest_words_do_not_fit_a_short_column_keeps_its_word_and_the_long_ones_share_the_rest() {
        // A platform's name beside two paths: the name is not broken to give the paths room.
        assertEquals(listOf(80, 160, 160), widths(400, 80f to 80f, 300f to 300f, 300f to 300f))
        assertEquals(listOf(80, 120, 200), widths(400, 80f to 80f, 240f to 240f, 400f to 400f))
    }
}
