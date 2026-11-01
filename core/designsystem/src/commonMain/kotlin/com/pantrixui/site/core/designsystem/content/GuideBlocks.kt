package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.CheckKind
import com.pantrixui.site.core.model.Span

/**
 * A guide's first section with the live sketch of its problem beside it: side by side on a desktop and a tablet,
 * the sketch under the text on a phone.
 */
@Composable
internal fun AsideView(
    aside: Block.Aside,
    onLink: (String) -> Unit,
    anchors: PageAnchors?
) {
    val sketch = LocalSketches.current[aside.sketch]
    if (SiteTheme.layout == SiteLayout.Phone) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            BlockColumn(aside.blocks, onLink, anchors, gap = 10.dp)
            if (sketch != null) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { sketch() }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            BlockColumn(aside.blocks, onLink, anchors, gap = 10.dp, modifier = Modifier.weight(1f))
            sketch?.invoke()
        }
    }
}

/** A sketch as wide as the page, in its middle: nothing where the page has no sketch of that key. */
@Composable
internal fun SketchView(sketch: Block.Sketch) {
    val view = LocalSketches.current[sketch.sketch] ?: return
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { view() }
}

/** Why a guide's problem is as it is: a quiet note with a question mark, its first bold words its lead. */
@Composable
internal fun WhyNote(
    callout: Block.Callout,
    onLink: (String) -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SiteTheme.colors.surfaceAlt)
            .border(1.dp, PantrixTheme.colors.border, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(24.dp).background(PantrixTheme.colors.primary.container, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            PantrixText(
                text = "?",
                style = SiteTheme.type.body.sized(13.sp).copy(fontWeight = FontWeight.ExtraBold),
                color = PantrixTheme.colors.primary.onContainer
            )
        }
        val spans = callout.blocks.filterIsInstance<Block.Paragraph>().flatMap { it.text }
        RichText(spans, onLink, Modifier.weight(1f), strongColor = PantrixTheme.colors.textPrimary)
    }
}

/**
 * A table of rows that each name a thing in bold: with a caption and column headings where it has them, the rows it
 * sets apart filled. On a phone a table with headings becomes a list, each row its name over what the rest says.
 */
@Composable
internal fun LabelsTable(
    table: Block.Table,
    onLink: (String) -> Unit
) {
    val header = table.header
    when {
        header == null -> NameTable(table, onLink)
        SiteTheme.layout == SiteLayout.Phone -> StackedTable(table, onLink)
        else -> GridTable(table, header, onLink)
    }
}

/**
 * A table of a name and what it is, without headings: the files of a guide, its tests, where it is used. The names
 * stand in one column as wide as the longest of them, as a browser lays out a table whose names do not wrap, and
 * what they are takes the rest, breaking anywhere a path is too long for it.
 */
@Composable
private fun NameTable(
    table: Block.Table,
    onLink: (String) -> Unit
) {
    val cell = SiteTheme.type.body.sized(14.sp)
    val name = cell.copy(fontWeight = FontWeight.Bold)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    TableFrame {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val widths = remember(table.rows, constraints.maxWidth, cell) {
                columnWidths(
                    available = constraints.maxWidth,
                    columns = 2,
                    intrinsics = { column ->
                        table.rows.map { cells ->
                            if (column == 0) {
                                val width = measurer.measure(cells.first().plainText(), name).size.width.toFloat()
                                width to width
                            } else {
                                val text = cells.drop(1).flatten().plainText()
                                0f to measurer.measure(text, cell).multiParagraph.maxIntrinsicWidth
                            }
                        }
                    },
                    padding = with(density) { (CELL_PADDING * 2).toPx() }
                )
            }
            Column(Modifier.fillMaxWidth()) {
                table.rows.forEachIndexed { index, cells ->
                    if (index > 0) Hairline()
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                        PantrixText(
                            text = richText(cells.first(), onLink),
                            style = name,
                            color = PantrixTheme.colors.textPrimary,
                            softWrap = false,
                            modifier = Modifier
                                .width(with(density) { widths[0].toDp() })
                                .padding(horizontal = CELL_PADDING)
                        )
                        RichText(
                            cells.drop(1).flatten(),
                            onLink,
                            Modifier.weight(1f).padding(horizontal = CELL_PADDING),
                            style = cell,
                            codeSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}

/** A table on a phone: each row its name in bold, and what its other cells say after it, one after another. */
@Composable
private fun StackedTable(
    table: Block.Table,
    onLink: (String) -> Unit
) {
    TableFrame {
        table.rows.forEachIndexed { index, cells ->
            if (index > 0) Hairline()
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        if (index in
                            table.highlighted
                        ) {
                            PantrixTheme.colors.success.container
                        } else {
                            Color.Transparent
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                RichText(
                    cells.first(),
                    onLink,
                    style = SiteTheme.type.body.sized(14.sp).copy(fontWeight = FontWeight.ExtraBold),
                    color = PantrixTheme.colors.textPrimary
                )
                // The other cells as one sentence: "The button takes it. It does its work…".
                val rest = cells.drop(1).flatMapIndexed { cellIndex, spans ->
                    if (cellIndex == 0) spans else listOf(Span.Text(". ")) + spans
                }
                RichText(rest, onLink, style = SiteTheme.type.bodySmall)
            }
        }
    }
}

/**
 * A table with a caption and column headings, its columns as wide as a browser lays them out: each as wide as its
 * widest line where the room allows, the room left shared out by how much each holds.
 */
@Composable
private fun GridTable(
    table: Block.Table,
    header: List<List<Span>>,
    onLink: (String) -> Unit
) {
    val cell = SiteTheme.type.body.sized(14.sp)
    val head = cell.sized(12.sp).copy(fontWeight = FontWeight.ExtraBold)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    TableFrame {
        table.caption?.let { caption ->
            PantrixText(
                text = caption,
                style = cell.copy(fontWeight = FontWeight.ExtraBold),
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SiteTheme.colors.surfaceAlt)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(PantrixTheme.colors.border))
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val rows = listOf(header) + table.rows
            // Measured as they are drawn, code in its own face: a path is wider in it than in the text's.
            val texts = rows.map { cells -> cells.map { richText(it, onLink) } }
            val widths = remember(texts, constraints.maxWidth, cell, head) {
                columnWidths(
                    available = constraints.maxWidth,
                    columns = header.size,
                    intrinsics = { column ->
                        texts.mapIndexed { index, cells ->
                            val style = when {
                                index == 0 -> head
                                column == 0 -> cell.copy(fontWeight = FontWeight.Bold)
                                else -> cell
                            }
                            val laid = measurer.measure(cells.getOrNull(column) ?: AnnotatedString(""), style)
                            laid.multiParagraph.minIntrinsicWidth to laid.multiParagraph.maxIntrinsicWidth
                        }
                    },
                    padding = with(density) { (CELL_PADDING * 2).toPx() }
                )
            }
            Column(Modifier.fillMaxWidth()) {
                rows.forEachIndexed { index, cells ->
                    val heading = index == 0
                    if (index == 1) Box(Modifier.fillMaxWidth().height(1.dp).background(PantrixTheme.colors.border))
                    if (index > 1) Hairline()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .background(
                                if (index - 1 in table.highlighted) {
                                    PantrixTheme.colors.success.container
                                } else {
                                    Color.Transparent
                                }
                            )
                    ) {
                        cells.forEachIndexed { column, spans ->
                            RichText(
                                spans = spans,
                                onLink = onLink,
                                style = when {
                                    heading -> head
                                    column == 0 -> cell.copy(fontWeight = FontWeight.Bold)
                                    else -> cell
                                },
                                color = when {
                                    heading -> SiteTheme.colors.muted
                                    column == 0 -> PantrixTheme.colors.textPrimary
                                    else -> PantrixTheme.colors.textSecondary
                                },
                                modifier = Modifier
                                    .width(with(density) { widths[column].toDp() })
                                    .padding(horizontal = CELL_PADDING, vertical = if (heading) 10.dp else 11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The widths of a table's [columns] in [available] pixels, as a browser's automatic layout gives them: every column
 * its widest line where they all fit, the room left shared by those widths; where they do not, each its longest word
 * and the rest shared by how much more each would take. Where even the longest words do not fit, the columns whose
 * longest word is short keep it, and the others share what is left by theirs: a name is not broken to give a path
 * room. [intrinsics] gives a column's cells' narrowest and widest widths, without their [padding].
 */
internal fun columnWidths(
    available: Int,
    columns: Int,
    intrinsics: (column: Int) -> List<Pair<Float, Float>>,
    padding: Float
): List<Int> {
    val measured = (0 until columns).map { column ->
        val cells = intrinsics(column)
        (cells.maxOf { it.first } + padding) to (cells.maxOf { it.second } + padding)
    }
    val least = measured.sumOf { it.first.toDouble() }.toFloat()
    val most = measured.sumOf { it.second.toDouble() }.toFloat()
    val room = available.toFloat()
    val widths = when {
        most <= room -> measured.map { (_, widest) -> widest + (room - most) * widest / most }

        least >= room -> narrowestFirst(measured.map { it.first }, room)

        else -> measured.map { (narrowest, widest) ->
            narrowest + (room - least) * (widest - narrowest) / (most - least)
        }
    }
    // Whole pixels that add up to the room, the last column taking what rounding left.
    val whole = widths.dropLast(1).map { it.toInt() }
    return whole + (available - whole.sum())
}

/**
 * [narrowest] widths in [room] that do not all fit: the short ones whole, from the shortest, while each is no more
 * than an even share of what is left; the others by their size in the rest.
 */
private fun narrowestFirst(
    narrowest: List<Float>,
    room: Float
): List<Float> {
    val widths = MutableList(narrowest.size) { 0f }
    val open = narrowest.indices.sortedBy { narrowest[it] }.toMutableList()
    var left = room
    while (open.isNotEmpty() && narrowest[open.first()] <= left / open.size) {
        val column = open.removeAt(0)
        widths[column] = narrowest[column]
        left -= narrowest[column]
    }
    val rest = open.sumOf { narrowest[it].toDouble() }.toFloat()
    open.forEach { widths[it] = narrowest[it] * left / rest }
    return widths
}

/** A line under a table's rows' text, smaller and quieter: where to find what was named, or what is not seen. */
@Composable
internal fun NoteView(
    note: Block.Note,
    onLink: (String) -> Unit
) {
    RichText(
        spans = note.text,
        onLink = onLink,
        style = SiteTheme.type.bodySmall,
        color = SiteTheme.colors.muted,
        codeSize = 12.5.sp
    )
}

/**
 * What a helper does not do: each point a bold line and a sentence under it, marked with a dash, in a bordered
 * list.
 */
@Composable
internal fun PointsView(
    points: Block.Points,
    onLink: (String) -> Unit
) {
    TableFrame {
        points.points.forEachIndexed { index, point ->
            if (index > 0) Hairline()
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Mark(text = "–", fill = SiteTheme.colors.hover, color = SiteTheme.colors.muted, size = 22.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    RichText(
                        spans = point.title.map { if (it is Span.Text) it.copy(strong = false) else it },
                        onLink = onLink,
                        style = SiteTheme.type.body.sized(14.5.sp).copy(fontWeight = FontWeight.ExtraBold),
                        color = PantrixTheme.colors.textPrimary
                    )
                    RichText(
                        point.text,
                        onLink,
                        style = SiteTheme.type.body.sized(14.sp),
                        codeSize = 12.5.sp
                    )
                }
            }
        }
    }
}

/** Boxes side by side, two on a desktop and a tablet, one under another on a phone: each a title and what it says. */
@Composable
internal fun BoxesView(
    boxes: Block.Boxes,
    onLink: (String) -> Unit
) {
    Grid(
        items = boxes.boxes,
        columns = if (SiteTheme.layout == SiteLayout.Phone) 1 else 2
    ) { box ->
        val shape = RoundedCornerShape(14.dp)
        val hasCode = box.blocks.any { it is Block.Code }
        Column(
            Modifier
                .fillMaxWidth()
                .border(1.dp, PantrixTheme.colors.border, shape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(if (hasCode) 10.dp else 8.dp)
        ) {
            RichText(
                spans = box.title,
                onLink = onLink,
                style = SiteTheme.type.body.copy(fontWeight = FontWeight.ExtraBold),
                color = PantrixTheme.colors.textPrimary
            )
            box.blocks.forEach { block ->
                when (block) {
                    is Block.Code -> CodeView(block, compact = true)

                    is Block.Paragraph -> RichText(
                        block.text,
                        onLink,
                        style = SiteTheme.type.body.sized(14.sp),
                        codeSize = 12.5.sp
                    )

                    else -> ContentBlocks(listOf(block), onLink, style = ContentStyle.Guide)
                }
            }
        }
    }
}

/**
 * What was checked and how: a column for what was seen, what was read in the sources and what was not checked, side
 * by side on a desktop, one under another narrower, each headed in its own colour and its lines marked with it.
 */
@Composable
internal fun ChecksView(
    checks: Block.Checks,
    onLink: (String) -> Unit
) {
    Grid(
        items = checks.groups,
        columns = if (SiteTheme.layout == SiteLayout.Desktop) 3 else 1
    ) { group ->
        val role = when (group.kind) {
            CheckKind.Seen -> PantrixTheme.colors.success
            CheckKind.Read -> PantrixTheme.colors.primary
            CheckKind.Unchecked -> PantrixTheme.colors.warning
        }
        val mark = when (group.kind) {
            CheckKind.Seen -> "✓"
            CheckKind.Read -> "§"
            CheckKind.Unchecked -> "–"
        }
        val shape = RoundedCornerShape(14.dp)
        val line = SiteTheme.type.bodySmall.copy(lineHeight = (13.5 * CHECK_LINE).sp)
        Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, PantrixTheme.colors.border, shape)) {
            PantrixText(
                text = group.title,
                style = SiteTheme.type.body.sized(13.sp).copy(fontWeight = FontWeight.ExtraBold),
                color = role.onContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(role.container)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
            Column(
                Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                group.items.forEach { item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PantrixText(
                            text = mark,
                            style = line.copy(fontWeight = FontWeight.ExtraBold),
                            color = role.onContainer
                        )
                        RichText(item, onLink, Modifier.weight(1f), style = line, codeSize = 12.5.sp)
                    }
                }
            }
        }
    }
}

/** [items] in rows of [columns], as wide as each other, [gap] apart, each row as tall as its tallest at its top. */
@Composable
private fun <T> Grid(
    items: List<T>,
    columns: Int,
    gap: Dp = 12.dp,
    content: @Composable (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { item -> Box(Modifier.weight(1f)) { content(item) } }
                repeat(columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

/** A round mark before a line: a dash, a tick. */
@Composable
private fun Mark(
    text: String,
    fill: Color,
    color: Color,
    size: Dp
) {
    Box(Modifier.size(size).background(fill, CircleShape), contentAlignment = Alignment.Center) {
        PantrixText(text = text, style = SiteTheme.type.body.copy(fontWeight = FontWeight.ExtraBold), color = color)
    }
}

/** The bordered frame of a guide's table or list: rounder than a page's. */
@Composable
private fun TableFrame(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, PantrixTheme.colors.border, shape)) { content() }
}

@Composable
private fun Hairline() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(SiteTheme.colors.hairline))
}

/** A run of text as it reads, without its formatting: what a table's column is measured by. */
private fun List<Span>.plainText(): String = joinToString("") { span ->
    when (span) {
        is Span.Text -> span.text
        is Span.Code -> span.text
        is Span.Link -> span.text.plainText()
    }
}

/** The room inside a table's cell, at its sides. */
private val CELL_PADDING = 14.dp

/** The line a check's sentence takes, as a share of its size. */
private const val CHECK_LINE = 1.55
