@file:Suppress("MagicNumber") // A sketch is drawn in the design's own measures: its rows, its shares and its counts.

package com.pantrixui.site.feature.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.i18n.LocalLanguage

/**
 * A pattern's sketches: small pictures of the windows it is drawn in, each with what it shows under it. They are
 * pictures only, and a screen reader is told nothing of them: the pattern's words say it.
 */
@Composable
internal fun PatternSketches(
    pattern: Pattern,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .background(SiteTheme.colors.canvas, RoundedCornerShape(14.dp))
            .padding(16.dp)
            .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        when (pattern.code) {
            "scaffold" -> {
                Sketched(pattern, 0) { Device(92.dp, 176.dp, 18.dp) { PhoneWithBar() } }
                Sketched(pattern, 1) { Device(150.dp, 200.dp, 14.dp) { TabletWithRail() } }
                Sketched(pattern, 2) { Device(230.dp, 150.dp, 10.dp) { LaptopWithSidebar() } }
            }

            "list-detail" -> {
                Sketched(pattern, 0) {
                    Device(92.dp, 176.dp, 18.dp) { Rows(Modifier.padding(8.dp), chosenFirst = true) }
                }
                Sketched(pattern, 1) { Device(230.dp, 150.dp, 10.dp) { Panes(listWidth = 72.dp, extra = false) } }
                Sketched(pattern, 2) { Device(230.dp, 150.dp, 10.dp) { Panes(listWidth = 56.dp, extra = true) } }
            }

            "supporting" -> {
                Sketched(pattern, 0) { Device(92.dp, 176.dp, 18.dp) { PhoneWithSheet() } }
                Sketched(pattern, 1) { Device(260.dp, 150.dp, 10.dp) { Beside() } }
            }

            "tabletop" -> {
                Sketched(pattern, 0) { Device(210.dp, 150.dp, 12.dp) { BookPanes() } }
                Sketched(pattern, 1) { Device(120.dp, 170.dp, 12.dp, ground = TABLE_GROUND) { Table() } }
            }

            "columns" -> {
                Columns()
            }

            else -> {
                OwnBox()
            }
        }
    }
}

/** A sketch with what it shows under it. */
@Composable
private fun Sketched(
    pattern: Pattern,
    index: Int,
    sketch: @Composable () -> Unit
) {
    val language = LocalLanguage.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sketch()
        PantrixText(
            text = pattern.sketches.getOrNull(index)?.of(language).orEmpty(),
            style = SiteTheme.type.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            color = SiteTheme.colors.muted
        )
    }
}

/** A device's outline and screen, [width] by [height]. */
@Composable
private fun Device(
    width: Dp,
    height: Dp,
    corner: Dp,
    ground: Color = PantrixTheme.colors.surface,
    screen: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(corner)
    Box(
        Modifier.size(width, height).clip(shape).background(ground).border(4.dp, OUTLINE, shape).padding(4.dp),
        content = screen
    )
}

/** A bar of writing, [fraction] of its room wide. */
@Composable
private fun Skel(
    fraction: Float = 1f,
    height: Dp = 8.dp,
    color: Color = SiteTheme.colors.hover
) {
    Box(Modifier.fillMaxWidth(fraction).height(height).background(color, RoundedCornerShape(4.dp)))
}

/** A list's rows, the first chosen where [chosenFirst]. */
@Composable
private fun Rows(
    modifier: Modifier,
    chosenFirst: Boolean,
    rows: Int = 3,
    height: Dp = 20.dp
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Skel(0.5f, color = PantrixTheme.colors.borderHover)
        repeat(rows) { index -> ListRow(index == 0 && chosenFirst, height) }
    }
}

@Composable
private fun ListRow(
    chosen: Boolean,
    height: Dp
) {
    val shape = RoundedCornerShape(5.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .background(if (chosen) PantrixTheme.colors.primary.container else SiteTheme.colors.hover, shape)
            .then(if (chosen) Modifier.border(1.dp, SiteTheme.colors.accentLine, shape) else Modifier)
    )
}

/** A navigation's dots: the first, the place on show, in the brand's colour. */
@Composable
private fun Dots(vertical: Boolean) {
    val dots: @Composable () -> Unit = {
        Box(Modifier.size(width = 12.dp, height = 8.dp).background(PantrixTheme.colors.primary.color, CircleShape))
        repeat(2) { Box(Modifier.size(8.dp).background(PantrixTheme.colors.borderHover, CircleShape)) }
    }
    if (vertical) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            dots()
        }
    } else {
        Row(horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) { dots() }
    }
}

@Composable
private fun PhoneWithBar() {
    Column(Modifier.fillMaxSize()) {
        Rows(Modifier.weight(1f).padding(4.dp), chosenFirst = false, height = 22.dp)
        Row(
            Modifier.fillMaxWidth().height(20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(width = 14.dp, height = 8.dp).background(PantrixTheme.colors.primary.color, CircleShape))
            repeat(2) { Box(Modifier.size(8.dp).background(PantrixTheme.colors.borderHover, CircleShape)) }
        }
    }
}

@Composable
private fun TabletWithRail() {
    Row(Modifier.fillMaxSize()) {
        Box(Modifier.width(22.dp).fillMaxHeight().padding(top = 10.dp), contentAlignment = Alignment.TopCenter) {
            Dots(vertical = true)
        }
        Rows(Modifier.weight(1f).padding(8.dp), chosenFirst = false, height = 24.dp)
    }
}

@Composable
private fun LaptopWithSidebar() {
    Row(Modifier.fillMaxSize()) {
        Column(
            Modifier.width(58.dp).fillMaxHeight().padding(horizontal = 6.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Skel(color = PantrixTheme.colors.primary.color)
            repeat(3) { Skel(color = PantrixTheme.colors.borderHover) }
        }
        Rows(Modifier.weight(1f).padding(8.dp), chosenFirst = false, rows = 2)
    }
}

/** A list beside its detail, and an extra pane at the end where [extra]. */
@Composable
private fun Panes(
    listWidth: Dp,
    extra: Boolean
) {
    Row(Modifier.fillMaxSize().padding(2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Rows(Modifier.width(listWidth), chosenFirst = true, rows = if (extra) 2 else 3, height = 18.dp)
        Pane(Modifier.weight(1f)) {
            Skel(0.6f)
            Skel(height = 6.dp)
            Skel(0.8f, height = 6.dp)
        }
        if (extra) Pane(Modifier.width(52.dp)) {}
    }
}

@Composable
private fun Pane(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier.fillMaxHeight().background(
            SiteTheme.colors.surfaceAlt,
            shape
        ).border(1.dp, SiteTheme.colors.hairline, shape).padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        content = content
    )
}

@Composable
private fun PhoneWithSheet() {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Skel(0.6f, color = PantrixTheme.colors.borderHover)
            Skel(height = 6.dp)
            Skel(height = 6.dp)
            Skel(0.7f, height = 6.dp)
        }
        Box(Modifier.fillMaxSize().background(PantrixTheme.colors.scrim))
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(88.dp)
                .background(PantrixTheme.colors.surface, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(width = 22.dp, height = 3.dp).background(PantrixTheme.colors.borderStrong, CircleShape))
            Skel(0.5f, color = PantrixTheme.colors.borderHover)
            Skel(height = 14.dp)
            Skel(height = 14.dp)
        }
    }
}

@Composable
private fun Beside() {
    Row(Modifier.fillMaxSize().padding(2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Column(Modifier.weight(1f).padding(4.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Skel(0.4f, color = PantrixTheme.colors.borderHover)
            Skel(height = 6.dp)
            Skel(height = 6.dp)
            Skel(0.7f, height = 6.dp)
            Skel(height = 48.dp)
        }
        Pane(Modifier.width(78.dp)) {
            Skel(0.7f, height = 7.dp, color = PantrixTheme.colors.borderHover)
            Skel(height = 16.dp, color = PantrixTheme.colors.primary.container)
            Skel(height = 16.dp)
        }
    }
}

@Composable
private fun BookPanes() {
    Box(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize()) {
            Rows(
                Modifier.weight(1f).padding(start = 2.dp, end = 10.dp, top = 2.dp),
                chosenFirst = true,
                rows = 3,
                height = 18.dp
            )
            Pane(Modifier.weight(1f).padding(start = 10.dp)) {}
        }
        Box(Modifier.align(Alignment.Center).width(8.dp).fillMaxHeight().background(FOLD_SHADE))
    }
}

@Composable
private fun Table() {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Box(Modifier.size(30.dp).background(TABLE_FACE, CircleShape))
        }
        Box(Modifier.fillMaxWidth().height(8.dp).background(FOLD_SHADE))
        Row(
            Modifier.fillMaxWidth().weight(1f).background(TABLE_NIGHT),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { Box(Modifier.size(18.dp).background(TABLE_BUTTON, CircleShape)) }
            Box(Modifier.size(18.dp).background(TABLE_LEAVE, CircleShape))
        }
    }
}

/** Four, eight and twelve columns, the twelve spanned eight and four, and what keeps the content's width. */
@Composable
private fun Columns() {
    val language = LocalLanguage.current
    val muted = SiteTheme.colors.muted
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("Compact · 4" to 4, "Medium · 8" to 8, "Expanded · 12" to 12).forEach { (name, count) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PantrixText(
                    text = name,
                    style = SiteTheme.type.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = muted,
                    modifier = Modifier.width(88.dp)
                )
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (count == 12) {
                        Box(Modifier.weight(8f).height(28.dp).background(COLUMN_STRONG, RoundedCornerShape(4.dp)))
                        Box(Modifier.weight(4f).height(28.dp).background(COLUMN_WARM, RoundedCornerShape(4.dp)))
                    } else {
                        repeat(count) {
                            Box(Modifier.weight(1f).height(28.dp).background(COLUMN, RoundedCornerShape(4.dp)))
                        }
                    }
                }
            }
        }
        PantrixText(
            text = AdaptiveSay.ColumnsNote.of(language) + "Modifier.maxContentWidth()",
            style = SiteTheme.type.bodySmall.copy(fontSize = 12.5.sp),
            color = muted
        )
    }
}

/** The same orders as a table in a wide box and a list in a narrow one. */
@Composable
private fun OwnBox() {
    val language = LocalLanguage.current
    val small = SiteTheme.type.bodySmall.copy(fontSize = 10.5.sp, lineHeight = 14.sp)
    val shape = RoundedCornerShape(10.dp)
    val panel = Modifier.background(
        PantrixTheme.colors.surface,
        shape
    ).border(1.dp, SiteTheme.colors.hairline, shape).padding(10.dp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(panel.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                AdaptiveSay.TableHeads.forEach {
                    PantrixText(
                        it.of(language),
                        style = small.copy(fontWeight = FontWeight.Bold),
                        color = SiteTheme.colors.muted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            listOf(Triple("PX-2048", true, "€84"), Triple("PX-2049", false, "€26")).forEach { (number, paid, total) ->
                Row {
                    PantrixText(number, style = small, modifier = Modifier.weight(1f))
                    PantrixText("26 Oct", style = small, modifier = Modifier.weight(1f))
                    PantrixText(
                        (if (paid) AdaptiveSay.Paid else AdaptiveSay.Open).of(language),
                        style = small.copy(fontWeight = FontWeight.Bold),
                        color = if (paid) PantrixTheme.colors.success.color else PantrixTheme.colors.warning.color,
                        modifier = Modifier.weight(1f)
                    )
                    PantrixText(total, style = small, modifier = Modifier.weight(1f))
                }
            }
            PantrixText(
                AdaptiveSay.InTheMainArea.of(language),
                style = small.copy(fontWeight = FontWeight.Bold),
                color = PantrixTheme.colors.primary.onContainer
            )
        }
        Column(panel.width(120.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(Triple("PX-2048", true, "€84"), Triple("PX-2049", false, "€26")).forEach { (number, paid, total) ->
                Column {
                    PantrixText(number, style = small.copy(fontWeight = FontWeight.Bold))
                    PantrixText(
                        (if (paid) AdaptiveSay.Paid else AdaptiveSay.Open).of(language) + " · " + total,
                        style = small.copy(fontWeight = FontWeight.Bold),
                        color = if (paid) PantrixTheme.colors.success.color else PantrixTheme.colors.warning.color
                    )
                }
            }
            PantrixText(
                AdaptiveSay.InASidePanel.of(language),
                style = small.copy(fontWeight = FontWeight.Bold),
                color = PantrixTheme.colors.primary.onContainer
            )
        }
    }
}

private val OUTLINE = Color(0xFF181D2B)
private val FOLD_SHADE = Color(0x4D05070A)
private val TABLE_GROUND = Color(0xFF1D3362)
private val TABLE_FACE = Color(0x995386FF)
private val TABLE_NIGHT = Color(0xFF0B0E15)
private val TABLE_BUTTON = Color(0xFF333B4E)
private val TABLE_LEAVE = Color(0xFFC42A55)
private val COLUMN = Color(0x292264FF)
private val COLUMN_STRONG = Color(0x522264FF)
private val COLUMN_WARM = Color(0x4DE5761F)
