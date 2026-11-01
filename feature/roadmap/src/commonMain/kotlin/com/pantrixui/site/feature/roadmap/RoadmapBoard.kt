package com.pantrixui.site.feature.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.landingGutter
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage

/**
 * The board: a filter for each stage over the columns of items, side by side on a desktop, two to a row on a tablet,
 * and one under another on a phone, which puts what is being built first.
 */
@Composable
internal fun Board(
    state: RoadmapUiState,
    onAction: (RoadmapAction) -> Unit,
    open: (Link) -> Unit
) {
    val layout = SiteTheme.layout
    val columns = if (layout ==
        SiteLayout.Phone
    ) {
        PHONE_ORDER.map { stage -> RoadmapSay.Columns.first { it.stage == stage } }
    } else {
        RoadmapSay.Columns
    }
    val shown = columns.filter { state.filter == null || it.stage == state.filter }
    val pick: (Stage?) -> Unit = { onAction(RoadmapAction.PickFilter(it)) }
    when (layout) {
        SiteLayout.Desktop, SiteLayout.Tablet -> {
            val desktop = layout == SiteLayout.Desktop
            LandingBand(
                PaddingValues(start = landingGutter, end = landingGutter, bottom = if (desktop) 64.dp else 48.dp),
                gap = if (desktop) 16.dp else 14.dp
            ) {
                Filters(columns, state.filter, pick)
                ColumnGrid(shown, columnsInRow = if (desktop) shown.size else 2, open)
            }
        }

        SiteLayout.Phone -> {
            Filters(columns, state.filter, pick, Modifier.padding(bottom = 16.dp))
            LandingBand(PaddingValues(start = landingGutter, end = landingGutter, bottom = 40.dp), gap = 20.dp) {
                shown.forEach { column -> PhoneColumn(column, open) }
            }
        }
    }
}

/**
 * A filter for all the items and one for each stage, each with its count: in a row that wraps, or that scrolls
 * sideways on a phone.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Filters(
    columns: List<Column>,
    filter: Stage?,
    onPick: (Stage?) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val all: @Composable () -> Unit = {
        FilterButton(
            RoadmapSay.All.of(language),
            PantrixTheme.colors.borderStrong,
            columns.sumOf { it.items.size },
            on = filter == null,
            tag = "filter_all"
        ) { onPick(null) }
    }
    val each: @Composable (Column) -> Unit = { column ->
        FilterButton(
            column.name.of(language),
            stageColor(column.stage),
            column.items.size,
            on = filter == column.stage,
            tag = "filter_${column.stage.name.lowercase()}"
        ) { onPick(column.stage) }
    }
    if (SiteTheme.layout == SiteLayout.Phone) {
        Row(
            modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = landingGutter),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            all()
            columns.forEach { each(it) }
        }
    } else {
        FlowRow(
            modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            all()
            columns.forEach { each(it) }
        }
    }
}

/** A filter: its dot, its name and its count, tinted when it is on. */
@Composable
private fun FilterButton(
    name: String,
    dot: Color,
    count: Int,
    on: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(10.dp)
    val content = if (on) colors.primary.onContainer else colors.textSecondary
    Row(
        Modifier
            .height(if (SiteTheme.layout == SiteLayout.Desktop) 36.dp else 44.dp)
            .background(if (on) colors.primary.container else colors.surface, shape)
            .border(1.dp, if (on) SiteTheme.colors.accentLine else colors.border, shape)
            .semantics { selected = on }
            .siteClickable(tag = tag, role = Role.Button, shape = shape, onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dot(dot)
        PantrixText(text = name, style = strong(13.5.sp, FontWeight.Bold), color = content, maxLines = 1)
        PantrixText(
            text = "$count",
            style = small(12.sp).copy(fontWeight = FontWeight.SemiBold),
            color = content,
            modifier = Modifier.alpha(COUNT_ALPHA)
        )
    }
}

/** The columns shown, [columnsInRow] to a row, each its items on a quiet ground. */
@Composable
private fun ColumnGrid(
    columns: List<Column>,
    columnsInRow: Int,
    open: (Link) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        columns.chunked(columnsInRow).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                row.forEach { column -> BoardColumn(column, open, Modifier.weight(1f)) }
                repeat(columnsInRow - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** A column of a desktop's or a tablet's board: its dot, name and count, a desktop's note, and its items. */
@Composable
private fun BoardColumn(
    column: Column,
    open: (Link) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .background(SiteTheme.colors.canvas, shape)
            .border(1.dp, SiteTheme.colors.hairline, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ColumnHead(column, Modifier.padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = if (desktop) 2.dp else 0.dp))
        if (desktop) {
            PantrixText(
                text = column.note.of(language),
                style = small(12.5.sp),
                color = SiteTheme.colors.muted,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
        column.items.forEachIndexed { index, item -> ItemCard(item, itemTag(column, index), open) }
    }
}

/** A column of a phone's list: its heading with its dot and count, then its items. */
@Composable
private fun PhoneColumn(
    column: Column,
    open: (Link) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ColumnHead(column, Modifier)
        column.items.forEachIndexed { index, item -> ItemCard(item, itemTag(column, index), open) }
    }
}

/** A column's dot, its name and how many items it holds; a heading on a phone, where the columns are its parts. */
@Composable
private fun ColumnHead(
    column: Column,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Dot(stageColor(column.stage), size = 10.dp)
        PantrixText(
            text = column.name.of(language),
            style = strong(if (phone) 18.sp else 16.sp),
            color = PantrixTheme.colors.textPrimary,
            modifier = if (phone) Modifier.semantics { heading() } else Modifier
        )
        PantrixText(
            text = "${column.items.size}",
            style = small(12.5.sp).copy(fontWeight = FontWeight.Bold),
            color = SiteTheme.colors.muted
        )
    }
}

/** An item: its area and version, what it is and does, and how far it has come; it leads where it has a link. */
@Composable
private fun ItemCard(
    item: Item,
    tag: String,
    open: (Link) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(14.dp)
    val link = item.link
    val title = item.title.of(layout).of(language)
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.border, shape)
            .then(
                if (link == null) {
                    Modifier
                } else {
                    Modifier.siteClickable(tag = tag, role = Role.Button, shape = shape) { open(link) }
                }
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val (ground, ink) = areaColors(item.area)
            Pill(RoadmapSay.areaName(item.area).of(language), ground, ink, size = 11.5.sp)
            Spacer(Modifier.weight(1f))
            PantrixText(
                text = item.version.of(language),
                style = small(12.sp).copy(fontWeight = FontWeight.SemiBold),
                color = SiteTheme.colors.muted
            )
        }
        PantrixText(
            text = title,
            style = strong(
                if (phone) 15.5.sp else 15.sp
            ).copy(lineHeight = ((if (phone) 15.5f else 15f) * TITLE_LINE).sp),
            color = colors.textPrimary
        )
        PantrixText(
            text = item.body.of(layout).of(language),
            style = small(if (phone) 14.sp else 13.5.sp),
            color = colors.textSecondary
        )
        item.progress?.let {
            ItemProgress(
                it,
                Modifier.padding(
                    top = if (layout ==
                        SiteLayout.Desktop
                    ) {
                        4.dp
                    } else {
                        0.dp
                    }
                )
            )
        }
    }
}

/** How far an item has come: a thin bar and what is done. */
@Composable
private fun ItemProgress(
    progress: Progress,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.fillMaxWidth().height(6.dp).background(SiteTheme.colors.hover, CircleShape)) {
            if (progress.percent > 0) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.percent / PERCENT)
                        .fillMaxHeight()
                        .background(PantrixTheme.colors.primary.color, CircleShape)
                )
            }
        }
        PantrixText(
            text = progress.label.of(language),
            style = small(12.sp).copy(fontWeight = FontWeight.SemiBold),
            color = SiteTheme.colors.muted
        )
    }
}

/** An item's automation tag, from its column and its place in it: `roadmap_next_0`. */
private fun itemTag(
    column: Column,
    index: Int
) = "roadmap_${column.stage.name.lowercase()}_$index"

/** The colour of a stage's dot. */
@Composable
internal fun stageColor(stage: Stage): Color {
    val colors = PantrixTheme.colors
    return when (stage) {
        Stage.Shipped -> colors.success.color
        Stage.Building -> colors.primary.color
        Stage.Next -> colors.warning.color
        Stage.Later -> SiteTheme.colors.violetMark
    }
}

/** The ground and the ink of an area's tag. */
@Composable
private fun areaColors(area: Area): Pair<Color, Color> {
    val colors = PantrixTheme.colors
    return when (area) {
        Area.Library -> colors.primary.container to colors.primary.onContainer
        Area.Tooling -> SiteTheme.colors.hover to colors.textSecondary
        Area.Docs -> colors.success.container to colors.success.onContainer
        Area.Native -> SiteTheme.colors.violet to SiteTheme.colors.onViolet
        Area.Components -> colors.warning.container to colors.warning.onContainer
    }
}

/** A phone's columns, what is being built first and what is shipped last. */
private val PHONE_ORDER = listOf(Stage.Building, Stage.Next, Stage.Later, Stage.Shipped)

private const val COUNT_ALPHA = 0.75f
private const val TITLE_LINE = 1.35f
private const val PERCENT = 100f
