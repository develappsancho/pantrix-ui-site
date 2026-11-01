@file:Suppress("MagicNumber") // A preview's sample values, its rows, amounts and sizes, are what it shows.

package com.pantrixui.site.feature.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pantrixui.components.chart.cartesian.ChartValue
import com.pantrixui.components.chart.common.ChartDash
import com.pantrixui.components.chart.common.ChartMarker
import com.pantrixui.components.chart.line.PantrixLineChart
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.date.datepicker.PantrixDatePicker
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textarea.PantrixTextArea
import com.pantrixui.foundation.date.PantrixDate
import com.pantrixui.foundation.date.rememberToday
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.model.Words

/**
 * A component the components page starts with, the one most apps start with, drawn live.
 *
 * @property key the key of its page.
 * @property name its name.
 * @property line what the page says of it under its name.
 * @property scale how much smaller than itself it is drawn in its card.
 * @property preview the component itself, with values of its own.
 */
internal class Featured(
    val key: String,
    val name: String,
    val line: Words,
    val scale: Float,
    val preview: @Composable () -> Unit
)

/** The components most apps start with, as the page shows them first. */
internal val FEATURED: List<Featured> = listOf(
    Featured(
        key = "button",
        name = "Button",
        line = Words("Six variants, five sizes and a loading state.", "Altı tür, beş boy ve bir yükleniyor hâli."),
        scale = 1f
    ) { ButtonPreview() },
    Featured(
        key = "text-area",
        name = "Text Area",
        line = Words("Several lines that grow, then scroll.", "Uzayan, sonra kayan birkaç satır."),
        scale = 0.8f
    ) { TextAreaPreview() },
    Featured(
        key = "date-picker",
        name = "Date Picker",
        line = Words(
            "A day or a stretch of days, with no date library.",
            "Tarih kütüphanesi olmadan bir gün ya da günler."
        ),
        scale = 0.42f
    ) { DatePickerPreview() },
    Featured(
        key = "data-table",
        name = "Data Table",
        line = Words("Sorting, selection, rows that open.", "Sıralama, seçim, açılan satırlar."),
        scale = 0.5f
    ) { DataTablePreview() },
    Featured(
        key = "line-chart",
        name = "Line Chart",
        line = Words(
            "Lines from rows, with zoom and export.",
            "Satırlardan çizgiler; yakınlaştırma ve dışa aktarmayla."
        ),
        scale = 0.7f
    ) { LineChartPreview() },
    Featured(
        key = "command-palette",
        name = "Command Palette",
        line = Words("Every command, found as it is typed.", "Her komut, yazıldıkça bulunur."),
        scale = 0.85f
    ) { CommandPalettePreview() }
)

/**
 * [content] drawn at [scale] of its own size in the middle of where it stands, which it may be wider and taller than
 * itself: a component in a preview's room, as it is and only smaller.
 */
@Composable
internal fun Miniature(
    scale: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .wrapContentSize(unbounded = true)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) { content() }
    }
}

@Composable
private fun ButtonPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        PantrixButton(text = "Save", onClick = {}, tagKey = "preview_save")
        PantrixButton(text = "Cancel", onClick = {}, variant = ButtonVariant.Outline, tagKey = "preview_cancel")
    }
}

@Composable
private fun TextAreaPreview() {
    val comment = rememberTextFieldState("Softer than I expected, and the size guide was right.")
    PantrixTextArea(
        state = comment,
        modifier = Modifier.width(260.dp),
        label = "Comment",
        minLines = 2,
        maxLines = 2,
        resizable = false,
        maxLength = 500,
        tagKey = "preview_comment"
    )
}

@Composable
private fun DatePickerPreview() {
    val today = rememberToday()
    var day by remember { mutableStateOf<PantrixDate?>(today) }
    PantrixDatePicker(selected = day, onSelect = { day = it }, tagKey = "preview_day")
}

/** An order of the data table's preview. */
private class PreviewOrder(
    val number: String,
    val paid: Boolean,
    val total: String
)

@Composable
private fun DataTablePreview() {
    val orders = remember {
        listOf(
            PreviewOrder("PX-2048", paid = true, total = "€84"),
            PreviewOrder("PX-2049", paid = false, total = "€26"),
            PreviewOrder("PX-2050", paid = true, total = "€142")
        )
    }
    val columns = remember {
        listOf(
            DataTableColumn.text<PreviewOrder>(key = "order", header = "Order") { it.number },
            DataTableColumn<PreviewOrder>(key = "status", header = "Status") { order ->
                PantrixTag(
                    text = if (order.paid) "Paid" else "Open",
                    tone = if (order.paid) TagTone.Success else TagTone.Warning
                )
            },
            DataTableColumn.text(key = "total", header = "Total", alignment = Alignment.End) { it.total }
        )
    }
    PantrixDataTable(rows = orders, columns = columns, modifier = Modifier.width(560.dp), tagKey = "preview_orders")
}

/** A week of the line chart's preview: what was sold, and what was expected. */
private class PreviewWeek(
    val week: String,
    val sold: Float,
    val expected: Float
)

@Composable
private fun LineChartPreview() {
    val weeks = remember {
        listOf(
            PreviewWeek("1", 12f, 10f),
            PreviewWeek("2", 18f, 13f),
            PreviewWeek("3", 15f, 16f),
            PreviewWeek("4", 24f, 19f),
            PreviewWeek("5", 21f, 22f),
            PreviewWeek("6", 30f, 25f)
        )
    }
    PantrixLineChart(
        data = weeks,
        x = { it.week },
        values = listOf(
            ChartValue("sold", "Sold", marker = ChartMarker.Circle) { it.sold },
            ChartValue("expected", "Expected", dash = ChartDash.Dashed) { it.expected }
        ),
        modifier = Modifier.width(360.dp).height(200.dp),
        xAxis = null,
        yAxis = null,
        legend = null,
        tooltip = null,
        tagKey = "preview_sales"
    )
}

/**
 * The command palette as it is drawn open, its field and two of its commands: the palette itself is a layer over a
 * screen, which a card has no room for.
 */
@Composable
private fun CommandPalettePreview() {
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier
            .width(260.dp)
            .background(colors.surface, shape)
            .border(1.dp, colors.border, shape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PantrixText(text = "new ord|", style = SiteTheme.type.bodySmall, color = colors.textPrimary)
        listOf("New order" to "⌘N", "Reorder last" to "⌘R").forEachIndexed { index, (label, keys) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (index == 0) colors.primary.container else colors.surface, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PantrixText(
                    text = label,
                    style = SiteTheme.type.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (index == 0) colors.primary.onContainer else colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                PantrixText(text = keys, style = SiteTheme.type.inlineCode, color = SiteTheme.colors.muted)
            }
        }
    }
}
