package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.container.card.CardColors
import com.pantrixui.components.container.card.CardVariant
import com.pantrixui.components.container.card.PantrixCard
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagColors
import com.pantrixui.components.data.tag.TagStyle
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.link.LinkUnderline
import com.pantrixui.components.elements.link.PantrixLink
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.shape.ShapeRole
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.model.ContentsEntry

/** The colour a page's tag is drawn in: what kind of thing it says. */
enum class PageTagTone {
    /** A plain fact: how long the page takes to read. */
    Plain,

    /** Who the page is for: every Compose Multiplatform app. */
    Accent,

    /** Where what it says was seen. */
    Success
}

/**
 * One of the tags under a page's lead.
 *
 * @property label what it says.
 * @property tone the colour it is drawn in.
 */
data class PageTag(
    val label: String,
    val tone: PageTagTone = PageTagTone.Plain
)

/** A page's tags, in a row that wraps: Pantrix's tags, round at the ends, in the colour of what each says. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PageTags(
    tags: List<PageTag>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            val colors = PantrixTheme.colors
            val (container, content) = when (tag.tone) {
                PageTagTone.Plain -> SiteTheme.colors.hover to colors.textSecondary
                PageTagTone.Accent -> colors.primary.container to colors.primary.onContainer
                PageTagTone.Success -> colors.success.container to colors.success.onContainer
            }
            RoundTag(tag.label, container, content)
        }
    }
}

/**
 * A Pantrix tag round at its ends, in colours of the site's own: the marks of a page, a card and a version.
 *
 * @param text what it says.
 * @param container its fill.
 * @param content the colour of its words.
 * @param textStyle the type of its words.
 * @param horizontalPadding the room at each end.
 * @param verticalPadding the room above and below its words: it is as tall as a line of [textStyle] and this.
 */
@Composable
fun RoundTag(
    text: String,
    container: Color,
    content: Color,
    textStyle: TextStyle = SiteTheme.type.tag,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 3.dp
) {
    val height = with(LocalDensity.current) { textStyle.lineHeight.toDp() } + verticalPadding * 2
    PantrixTag(
        text = text,
        style = TagStyle(shape = ShapeRole.Full, height = height, horizontalPadding = horizontalPadding),
        colors = TagColors(container = container, content = content),
        textStyle = textStyle
    )
}

/**
 * A page's "On this page" list on a tablet and a phone, where there is no room beside the article: a row of buttons
 * that scrolls sideways, reaching the window's edges past the page's own room.
 *
 * @param entries the page's sections.
 * @param onSelect what a button does, given its section's anchor.
 */
@Composable
internal fun ContentsChips(
    entries: List<ContentsEntry>,
    onSelect: (String) -> Unit
) {
    Row(
        Modifier.edgeToEdge(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entries.forEach { entry ->
            PantrixButton(
                onClick = { onSelect(entry.anchor) },
                contentDescription = entry.title,
                variant = ButtonVariant.Outline,
                size = ButtonSize.Medium,
                style = ButtonStyle(medium = ButtonSizeStyle(height = 36.dp, horizontalPadding = 12.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonColors(
                    container = PantrixTheme.colors.surface,
                    content = PantrixTheme.colors.textSecondary,
                    border = PantrixTheme.colors.border
                ),
                tagKey = "contents_${entry.anchor}"
            ) {
                PantrixText(
                    text = entry.title,
                    style = SiteTheme.type.button.sized(13.sp),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * A row that scrolls sideways from one edge of the window to the other on a tablet and a phone: it reaches past the
 * page's own room around the article, and starts and ends its content where the article does.
 */
@Composable
fun Modifier.edgeToEdge(): Modifier {
    val edge = if (SiteTheme.layout == SiteLayout.Phone) 16.dp else 24.dp
    return bleed(edge)
        .horizontalScroll(rememberScrollState())
        .padding(start = edge, end = edge, bottom = 2.dp)
}

/** As wide as the room it is given and [edge] more on each side, into the page's own room around the article. */
private fun Modifier.bleed(edge: Dp): Modifier = layout { measurable, constraints ->
    if (!constraints.hasBoundedWidth) {
        val placeable = measurable.measure(constraints)
        return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
    val extra = edge.roundToPx() * 2
    val placeable = measurable.measure(
        constraints.copy(minWidth = constraints.maxWidth + extra, maxWidth = constraints.maxWidth + extra)
    )
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
}

/**
 * A page that leads to another from its foot.
 *
 * @property title what the page is called.
 * @property href its address.
 */
data class Neighbour(
    val title: String,
    val href: String
)

/**
 * The foot of a guide: the link to the text it is written from, what it was checked with, and the cards that lead to
 * the guides before and after it, side by side, or one over the other on a phone.
 *
 * @param editLabel what the link to the text says.
 * @param editHref where it leads.
 * @param checkedWith what the guide says it was checked with.
 * @param previousLabel the label of the card that leads back.
 * @param previous the guide before, if there is one.
 * @param nextLabel the label of the card that leads on.
 * @param next the guide after, if there is one.
 * @param onLink what a link does, given its address.
 */
@Composable
fun PageFoot(
    editLabel: String,
    editHref: String,
    checkedWith: String,
    previousLabel: String,
    previous: Neighbour?,
    nextLabel: String,
    next: Neighbour?,
    onLink: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(sectionGap(ContentStyle.Guide))) {
        FootLine(editLabel, editHref, checkedWith, onLink)
        if (previous != null || next != null) {
            val cards: List<@Composable (Modifier) -> Unit> = listOf(
                { modifier -> NeighbourCard(previousLabel, previous, onLink, TextAlign.Start, "previous", modifier) },
                { modifier -> NeighbourCard(nextLabel, next, onLink, TextAlign.End, "next", modifier) }
            )
            if (SiteTheme.layout == SiteLayout.Phone) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (previous != null) cards[0](Modifier.fillMaxWidth())
                    if (next != null) cards[1](Modifier.fillMaxWidth())
                }
            } else {
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // A card that is not there keeps its column, so the one that is stays on its side.
                    if (previous != null) cards[0](Modifier.weight(1f).fillMaxHeight()) else Spacer(Modifier.weight(1f))
                    if (next != null) cards[1](Modifier.weight(1f).fillMaxHeight()) else Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FootLine(
    editLabel: String,
    editHref: String,
    checkedWith: String,
    onLink: (String) -> Unit
) {
    val text = SiteTheme.type.bodySmall
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PantrixLink(
            text = editLabel,
            onClick = { onLink(editHref) },
            style = text.copy(fontWeight = FontWeight.Bold),
            underline = LinkUnderline.OnHover,
            tagKey = "editPage"
        )
        PantrixText(text = checkedWith, style = text, color = SiteTheme.colors.muted)
    }
}

/**
 * A card that leads somewhere: a hairline around it, and around it a stronger line and a shadow under a pointer.
 *
 * @param onClick what pressing it does.
 * @param tagKey its automation tag.
 * @param modifier the card's modifier.
 * @param contentPadding the room inside it.
 * @param container its fill.
 * @param shape its corners.
 * @param content what it says.
 */
@Composable
fun LinkCard(
    onClick: () -> Unit,
    tagKey: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    container: Color = PantrixTheme.colors.surface,
    shape: Shape = RoundedCornerShape(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val hovered by interactions.collectIsHoveredAsState()
    PantrixCard(
        onClick = onClick,
        variant = CardVariant.Outlined,
        shape = shape,
        colors = CardColors(
            container = container,
            border = if (hovered) PantrixTheme.colors.borderHover else PantrixTheme.colors.border
        ),
        contentPadding = contentPadding,
        interactionSource = interactions,
        modifier = modifier.shadow(if (hovered) 8.dp else 0.dp, shape, clip = false),
        tagKey = tagKey,
        content = content
    )
}

@Composable
private fun NeighbourCard(
    label: String,
    neighbour: Neighbour?,
    onLink: (String) -> Unit,
    align: TextAlign,
    tag: String,
    modifier: Modifier
) {
    neighbour ?: return
    LinkCard(
        onClick = { onLink(neighbour.href) },
        tagKey = tag,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        val alignment = if (align == TextAlign.End) Alignment.End else Alignment.Start
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = alignment,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PantrixText(
                text = label,
                style = SiteTheme.type.tag,
                color = SiteTheme.colors.muted,
                textAlign = align
            )
            PantrixText(
                text = neighbour.title,
                style = SiteTheme.type.body.copy(fontWeight = FontWeight.ExtraBold),
                color = PantrixTheme.colors.textPrimary,
                textAlign = align
            )
        }
    }
}
