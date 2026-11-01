package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.container.alert.AlertColors
import com.pantrixui.components.container.alert.AlertStyle
import com.pantrixui.components.container.alert.PantrixAlert
import com.pantrixui.components.container.card.CardColors
import com.pantrixui.components.container.card.CardVariant
import com.pantrixui.components.container.card.PantrixCard
import com.pantrixui.components.elements.icon.IconStyle
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.shape.ShapeSpec
import com.pantrixui.foundation.token.style.ProvidePantrixStyles
import com.pantrixui.foundation.token.style.pantrixStyles
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Card
import com.pantrixui.site.core.model.Fact
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Step
import com.pantrixui.site.core.model.TableStyle
import com.pantrixui.site.core.model.Tone
import kotlin.math.roundToInt
import com.pantrixui.site.core.model.Row as ContentRow

/**
 * How a page's content is spaced and headed: a page of the documentation's, or a guide's, whose sections stand a
 * little further apart under larger headings.
 */
enum class ContentStyle {
    /** A page of the documentation: Installation, Introduction. */
    Doc,

    /** One of the guides. */
    Guide
}

/** The live sketches a page's content shows beside its text, by their keys: a guide's own. */
val LocalSketches: ProvidableCompositionLocal<Map<String, @Composable () -> Unit>> =
    staticCompositionLocalOf { emptyMap() }

/**
 * A page's content, block by block, laid out as the site's design lays it: a section a step apart from the one
 * before, the parts of a section close together.
 *
 * @param blocks what the page says.
 * @param onLink what a link does, given its address.
 * @param modifier the column's modifier.
 * @param anchors where to record the parts the page's contents name, if it has contents.
 * @param style how the content is spaced and headed.
 */
@Composable
fun ContentBlocks(
    blocks: List<Block>,
    onLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    anchors: PageAnchors? = null,
    style: ContentStyle = ContentStyle.Doc
) {
    val layout = SiteTheme.layout
    val sectionGap = sectionGap(style)
    val blockGap = if (style == ContentStyle.Guide && layout != SiteLayout.Phone) 14.dp else 12.dp
    Column(modifier) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) {
                val opensSection = (block is Block.Heading && block.level == 2) || block is Block.Steps
                Spacer(Modifier.height(if (opensSection) sectionGap else blockGap))
            }
            BlockView(block, onLink, anchors, style)
        }
    }
}

/** The room between a page's sections, and between a guide's last section and its foot. */
@Composable
fun sectionGap(style: ContentStyle): Dp = when (SiteTheme.layout) {
    SiteLayout.Desktop -> if (style == ContentStyle.Guide) 44.dp else 40.dp
    SiteLayout.Tablet -> 36.dp
    SiteLayout.Phone -> 30.dp
}

/** [blocks] one under another, [gap] apart: what a part of a guide holds, closer together than a page's blocks. */
@Composable
internal fun BlockColumn(
    blocks: List<Block>,
    onLink: (String) -> Unit,
    anchors: PageAnchors?,
    gap: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        blocks.forEach { BlockView(it, onLink, anchors, ContentStyle.Guide) }
    }
}

@Composable
private fun BlockView(
    block: Block,
    onLink: (String) -> Unit,
    anchors: PageAnchors?,
    style: ContentStyle
) {
    when (block) {
        is Block.Heading -> RichText(
            spans = block.text,
            onLink = onLink,
            style = when {
                style == ContentStyle.Guide && block.level <= 2 -> SiteTheme.type.guideHeading
                style == ContentStyle.Guide -> SiteTheme.type.guideSubheading
                block.level <= 2 -> SiteTheme.type.sectionTitle
                else -> SiteTheme.type.stepTitle
            },
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.pageAnchor(anchors, block.anchor)
        )

        is Block.Paragraph -> RichText(block.text, onLink)

        is Block.Code -> CodeView(block, fileHeaderHeight = if (style == ContentStyle.Guide) 40.dp else null)

        is Block.ItemList -> ItemListView(block, onLink)

        is Block.Table -> if (block.style == TableStyle.Labels) LabelsTable(block, onLink) else TableView(block, onLink)

        is Block.Callout -> if (block.tone == Tone.Why) WhyNote(block, onLink) else CalloutView(block, onLink)

        is Block.Tabs -> TabsView(block, onLink, anchors)

        is Block.Steps -> StepsView(block.steps, onLink, anchors)

        is Block.Facts -> FactsView(block.facts)

        is Block.Cards -> CardsView(block.cards, onLink)

        is Block.Rows -> RowsView(block.rows, onLink, style)

        is Block.Aside -> AsideView(block, onLink, anchors)

        is Block.Sketch -> SketchView(block)

        is Block.Note -> NoteView(block, onLink)

        is Block.Points -> PointsView(block, onLink)

        is Block.Boxes -> BoxesView(block, onLink)

        is Block.Checks -> ChecksView(block, onLink)
    }
}

@Composable
private fun ItemListView(
    list: Block.ItemList,
    onLink: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        list.items.forEachIndexed { index, item ->
            Row {
                PantrixText(
                    text = if (list.ordered) "${index + 1}." else "•",
                    style = SiteTheme.type.body,
                    color = PantrixTheme.colors.textSecondary,
                    modifier = Modifier.width(20.dp)
                )
                ContentBlocks(item, onLink, Modifier.weight(1f))
            }
        }
    }
}

/** Views of one thing: a file in two repositories shows its tabs in the code's header, and what each holds under it. */
@Composable
private fun TabsView(
    tabs: Block.Tabs,
    onLink: (String) -> Unit,
    anchors: PageAnchors?
) {
    var selected by rememberSaveable(tabs) { mutableIntStateOf(0) }
    val tab = tabs.tabs[selected.coerceIn(tabs.tabs.indices)]
    val code = tab.blocks.firstOrNull() as? Block.Code
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (code != null) {
            CodeView(
                code = code,
                tabs = tabs.tabs.map { it.label },
                selectedTab = selected,
                onSelectTab = { selected = it },
                label = tabs.label
            )
            if (tab.blocks.size > 1) ContentBlocks(tab.blocks.drop(1), onLink, anchors = anchors)
        } else {
            ContentBlocks(tab.blocks, onLink, anchors = anchors)
        }
    }
}

@Composable
private fun StepsView(
    steps: List<Step>,
    onLink: (String) -> Unit,
    anchors: PageAnchors?
) {
    val layout = SiteTheme.layout
    Column {
        steps.forEachIndexed { index, step ->
            val last = index == steps.lastIndex
            if (layout == SiteLayout.Phone) {
                Column(
                    Modifier.padding(bottom = if (last) 0.dp else 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StepNumber(index + 1, step.optional, size = 30.dp)
                        StepTitle(step, Modifier.pageAnchor(anchors, step.anchor))
                    }
                    ContentBlocks(step.blocks, onLink, anchors = anchors)
                }
            } else {
                val numberSize = if (layout == SiteLayout.Tablet) 34.dp else 32.dp
                val line = PantrixTheme.colors.border
                // The line from one step's number to the next is drawn behind the row, as tall as the step: a code
                // block in it cannot be asked its height in advance, so the row is not sized by what it asks for.
                Row(
                    Modifier.drawBehind {
                        if (!last) {
                            val x = numberSize.toPx() / 2
                            val width = STEP_LINE.toPx()
                            val gap = STEP_LINE_GAP.toPx()
                            drawLine(line, Offset(x, numberSize.toPx() + gap), Offset(x, size.height - gap), width)
                        }
                    },
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    StepNumber(index + 1, step.optional, size = numberSize)
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(bottom = if (last) 0.dp else 36.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StepTitle(step, Modifier.padding(top = 4.dp).pageAnchor(anchors, step.anchor))
                        ContentBlocks(step.blocks, onLink, anchors = anchors)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepNumber(
    number: Int,
    optional: Boolean,
    size: Dp
) {
    val primary = PantrixTheme.colors.primary
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (optional) PantrixTheme.colors.surface else primary.color)
            .then(if (optional) Modifier.border(2.dp, primary.color, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        PantrixText(
            text = "$number",
            style = SiteTheme.type.button.copy(fontWeight = FontWeight.ExtraBold),
            color = if (optional) primary.onContainer else primary.onColor
        )
    }
}

/**
 * A step's title, and the word that says the step may be left out after it, as the design sets the word in the
 * title's line: on the title's last line and its baseline where there is room, at the start of a line of its own under
 * it where there is not, and never broken. Beside the title as a part of a row, the word was left no room by a title
 * that wraps, and stood one letter to a line. In the title's own text, its smaller letters would have been given the
 * title's line on a baseline of their own, and the title would have stood taller.
 */
@Composable
private fun StepTitle(
    step: Step,
    modifier: Modifier
) {
    val style = SiteTheme.type.stepTitle
    val color = PantrixTheme.colors.textPrimary
    if (!step.optional) {
        PantrixText(text = step.title, modifier = modifier, style = style, color = color)
        return
    }
    val lines = remember { TitleLines() }
    Layout(
        content = {
            PantrixText(text = step.title, style = style, color = color, onTextLayout = { lines.laid = it })
            PantrixText(
                text = LocalSiteStrings.current.optional,
                style = SiteTheme.type.button,
                color = SiteTheme.colors.muted,
                maxLines = 1,
                softWrap = false
            )
        },
        modifier = modifier
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val title = measurables[0].measure(loose)
        val word = measurables[1].measure(loose)
        val text = checkNotNull(lines.laid)
        val last = text.lineCount - 1
        val gap = OPTIONAL_GAP.roundToPx()
        val rtl = layoutDirection == LayoutDirection.Rtl
        val lineEnd = if (rtl) {
            title.width - text.getLineLeft(last).roundToInt()
        } else {
            text.getLineRight(last).roundToInt()
        }
        val besideIt = lineEnd + gap + word.width <= constraints.maxWidth
        val baseline = text.getLineBaseline(last).roundToInt()
        val line = text.getLineBottom(last).roundToInt() - text.getLineTop(last).roundToInt()
        val start = if (besideIt) lineEnd + gap else 0
        val top = (if (besideIt) baseline else baseline + line) - word[FirstBaseline]
        val width = maxOf(title.width, start + word.width)
        layout(width, maxOf(title.height, top + word.height)) {
            title.place(if (rtl) width - title.width else 0, 0)
            word.place(if (rtl) width - start - word.width else start, top)
        }
    }
}

/** A step's title's lines, as it was measured last: read in the same pass of the layout, after the title's measure. */
private class TitleLines {
    var laid: TextLayoutResult? = null
}

@Composable
private fun FactsView(facts: List<Fact>) {
    val layout = SiteTheme.layout
    val gap = if (layout == SiteLayout.Phone) 8.dp else 10.dp
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = when (layout) {
            SiteLayout.Desktop -> ((maxWidth + gap) / (FACT_WIDTH + gap)).toInt().coerceAtLeast(1)
            SiteLayout.Tablet -> TABLET_FACT_COLUMNS
            SiteLayout.Phone -> PHONE_FACT_COLUMNS
        }
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            facts.chunked(columns).forEach { row ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    // On a phone an odd last fact takes the whole row, as the design lays it.
                    val span = if (layout == SiteLayout.Phone && row.size == 1) columns else 1
                    row.forEach { fact -> FactBox(fact, Modifier.weight(span.toFloat()).fillMaxHeight()) }
                    repeat(columns - row.size * span) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun FactBox(
    fact: Fact,
    modifier: Modifier
) {
    val phone = SiteTheme.layout == SiteLayout.Phone
    Column(
        modifier
            .border(1.dp, PantrixTheme.colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = if (phone) 12.dp else 14.dp, vertical = if (phone) 10.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        PantrixText(text = fact.label, style = SiteTheme.type.factLabel, color = SiteTheme.colors.muted)
        PantrixText(text = fact.value, style = SiteTheme.type.factValue, color = PantrixTheme.colors.textPrimary)
    }
}

@Composable
private fun CardsView(
    cards: List<Card>,
    onLink: (String) -> Unit
) {
    val layout = SiteTheme.layout
    if (layout == SiteLayout.Phone) {
        BorderedList {
            cards.forEachIndexed { index, card ->
                if (index > 0) Hairline()
                PantrixCard(
                    onClick = { onLink(card.href) },
                    variant = CardVariant.Outlined,
                    shape = RoundedCornerShape(0.dp),
                    colors = CardColors(border = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    tagKey = "next$index"
                ) {
                    Row(Modifier.heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            PantrixText(
                                text = card.title,
                                style = SiteTheme.type.cardTitle,
                                color = PantrixTheme.colors.textPrimary
                            )
                            PantrixText(
                                text = card.text,
                                style = SiteTheme.type.bodySmall,
                                color = PantrixTheme.colors.textSecondary
                            )
                        }
                        PantrixIcon(
                            imageVector = PantrixIcons.ChevronForward,
                            contentDescription = null,
                            tint = SiteTheme.colors.muted
                        )
                    }
                }
            }
        }
        return
    }
    val gap = 12.dp
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = when (layout) {
            SiteLayout.Desktop -> ((maxWidth + gap) / (220.dp + gap)).toInt().coerceAtLeast(1)
            else -> 2
        }
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            cards.chunked(columns).forEachIndexed { rowIndex, row ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    row.forEachIndexed { index, card ->
                        PantrixCard(
                            onClick = { onLink(card.href) },
                            variant = CardVariant.Outlined,
                            shape = RoundedCornerShape(14.dp),
                            colors = CardColors(border = PantrixTheme.colors.border),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            tagKey = "next${rowIndex * columns + index}"
                        ) {
                            // One child: the card's own room between its children is not put between the two lines.
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PantrixText(
                                    text = card.title,
                                    style = SiteTheme.type.cardTitle,
                                    color = PantrixTheme.colors.textPrimary
                                )
                                PantrixText(
                                    text = card.text,
                                    style = SiteTheme.type.bodySmall,
                                    color = PantrixTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun RowsView(
    rows: List<ContentRow>,
    onLink: (String) -> Unit,
    style: ContentStyle
) {
    val layout = SiteTheme.layout
    if (style == ContentStyle.Guide) {
        GuideRows(rows, onLink)
        return
    }
    BorderedList {
        rows.forEachIndexed { index, row ->
            if (index > 0) Hairline()
            if (layout == SiteLayout.Phone) {
                RichText(
                    spans = listOf(Span.Text(row.label, strong = true), Span.Text(" · ")) + row.text,
                    onLink = onLink,
                    style = SiteTheme.type.bodySmall.copy(fontSize = SiteTheme.type.sidebarItem.fontSize),
                    strongColor = PantrixTheme.colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            } else {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PantrixText(
                        text = row.label,
                        style = SiteTheme.type.body.copy(fontWeight = FontWeight.ExtraBold),
                        color = PantrixTheme.colors.textPrimary,
                        modifier = Modifier.width(if (layout == SiteLayout.Tablet) 80.dp else 90.dp)
                    )
                    RichText(row.text, onLink, Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * A guide's rows: a name and what it says, the name in a column of its own on a desktop and a tablet and over its
 * sentence on a phone.
 */
@Composable
private fun GuideRows(
    rows: List<ContentRow>,
    onLink: (String) -> Unit
) {
    val phone = SiteTheme.layout == SiteLayout.Phone
    BorderedList {
        rows.forEachIndexed { index, row ->
            if (index > 0) Hairline()
            if (phone) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp)) {
                    PantrixText(
                        text = row.label,
                        style = SiteTheme.type.body.copy(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold),
                        color = PantrixTheme.colors.textPrimary
                    )
                    RichText(row.text, onLink, style = SiteTheme.type.bodySmall)
                }
            } else {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PantrixText(
                        text = row.label,
                        style = SiteTheme.type.body.copy(fontWeight = FontWeight.ExtraBold),
                        color = PantrixTheme.colors.textPrimary,
                        modifier = Modifier.width(GUIDE_ROW_LABEL)
                    )
                    RichText(row.text, onLink, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TableView(
    table: Block.Table,
    onLink: (String) -> Unit
) {
    val layout = SiteTheme.layout
    BorderedList(shape = RoundedCornerShape(12.dp)) {
        table.caption?.let { caption ->
            PantrixText(
                text = caption,
                style = SiteTheme.type.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SiteTheme.colors.surfaceAlt)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(PantrixTheme.colors.border))
        }
        table.rows.forEachIndexed { index, cells ->
            if (index > 0) Hairline()
            if (layout == SiteLayout.Phone || cells.size != 2) {
                Column(
                    Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    cells.forEachIndexed { cellIndex, cell ->
                        RichText(
                            spans = cell,
                            onLink = onLink,
                            style = SiteTheme.type.bodySmall,
                            color = if (cellIndex ==
                                0
                            ) {
                                PantrixTheme.colors.textPrimary
                            } else {
                                PantrixTheme.colors.textSecondary
                            }
                        )
                    }
                }
            } else {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    PantrixText(
                        text = richText(cells[0], onLink),
                        style = SiteTheme.type.inlineCode.copy(
                            fontSize = SiteTheme.type.factLabel.fontSize,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = PantrixTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        softWrap = false
                    )
                    RichText(cells[1], onLink, Modifier.weight(1f), style = SiteTheme.type.bodySmall)
                }
            }
        }
    }
}

/**
 * A note set apart from the text. Its first bold words are written in the note's own colour, as the design writes a
 * warning's lead. It is there before the reader comes, so it is not announced.
 */
@Composable
private fun CalloutView(
    callout: Block.Callout,
    onLink: (String) -> Unit
) {
    val role = when (callout.tone) {
        Tone.Info, Tone.Why -> PantrixTheme.colors.info
        Tone.Success -> PantrixTheme.colors.success
        Tone.Warning -> PantrixTheme.colors.warning
        Tone.Danger -> PantrixTheme.colors.danger
    }
    val edge = if (callout.tone == Tone.Warning) SiteTheme.colors.warningLine else role.container
    val spans = (callout.blocks.firstOrNull() as? Block.Paragraph)?.text.orEmpty()
    ProvidePantrixStyles(CALLOUT_ICON) {
        PantrixAlert(
            message = richText(spans, onLink, strongColor = role.onContainer),
            modifier = Modifier.fillMaxWidth(),
            icon = if (callout.tone == Tone.Warning) SiteIcons.Lock else PantrixIcons.Info,
            announce = false,
            style = CALLOUT_STYLE,
            colors = AlertColors(
                container = role.container,
                content = PantrixTheme.colors.textSecondary,
                accent = role.onContainer,
                border = edge
            ),
            textStyle = SiteTheme.type.bodySmall.copy(fontSize = SiteTheme.type.sidebarItem.fontSize)
        )
    }
}

/** A callout's corners, room and edge, as the design draws them. */
private val CALLOUT_STYLE = AlertStyle(
    shape = ShapeSpec.Fixed(RoundedCornerShape(12.dp)),
    horizontalPadding = 14.dp,
    verticalPadding = 12.dp,
    contentGap = 12.dp,
    borderWidth = 1.dp
)

/** A callout's icon, smaller than the theme's. */
private val CALLOUT_ICON = pantrixStyles(IconStyle.override(all = IconStyle(size = 18.dp)))

@Composable
private fun BorderedList(
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    content: @Composable () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, PantrixTheme.colors.border, shape)
    ) {
        content()
    }
}

@Composable
private fun Hairline() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(SiteTheme.colors.hairline))
}

/** How wide a guide's row's name is on a desktop and a tablet. */
private val GUIDE_ROW_LABEL = 120.dp

/** The width of the line from one step's number to the next. */
private val STEP_LINE = 2.dp

/** The room between that line and the numbers at its ends. */
private val STEP_LINE_GAP = 6.dp

/** The room between a step's title and its "optional" on the title's last line. */
private val OPTIONAL_GAP = 8.dp

/** The narrowest a desktop's fact is: as many stand in a row as are this wide. */
private val FACT_WIDTH = 170.dp

/** How many facts stand in a row on a tablet. */
private const val TABLET_FACT_COLUMNS = 3

/** How many facts stand in a row on a phone. */
private const val PHONE_FACT_COLUMNS = 2
