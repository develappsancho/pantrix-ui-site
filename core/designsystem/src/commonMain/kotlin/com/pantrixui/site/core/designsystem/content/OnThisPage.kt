package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.model.ContentsEntry

/**
 * A page's "On this page" list, beside it on a wide desktop: the headings a reader jumps to, the one being read
 * marked by the line along its side.
 *
 * @param entries the headings, in the page's order.
 * @param current the anchor of the heading being read, or null above the first.
 * @param onSelect what choosing a heading does, given its anchor.
 * @param modifier the list's modifier.
 * @param title what the list is called: "On this page", or "Versions" on the changelog.
 * @param footer what follows the headings: links of the page's own, under headings of their own.
 */
@Composable
fun OnThisPage(
    entries: List<ContentsEntry>,
    current: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = LocalSiteStrings.current.onThisPage,
    footer: @Composable ColumnScope.() -> Unit = {}
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ContentsHeading(title)
        entries.forEach { entry ->
            val muted = SiteTheme.colors.muted
            val label = buildAnnotatedString {
                append(entry.title)
                entry.note?.let { note ->
                    withStyle(SpanStyle(color = muted, fontWeight = FontWeight.SemiBold)) { append(" · $note") }
                }
            }
            ContentsLink(label, entry.anchor == current, "toc_${entry.anchor}") { onSelect(entry.anchor) }
        }
        footer()
    }
}

/**
 * A heading of the list beside a page: "On this page", or one of its own further down.
 *
 * @param text what it says, set in capitals.
 * @param modifier its modifier.
 */
@Composable
fun ContentsHeading(
    text: String,
    modifier: Modifier = Modifier
) {
    PantrixText(
        text = text.uppercase(),
        style = SiteTheme.type.overline,
        color = SiteTheme.colors.muted,
        modifier = modifier.padding(bottom = 6.dp).semantics { heading() }
    )
}

/**
 * A line of the list beside a page, with the line along its side that marks the one being read.
 *
 * @param label what it says.
 * @param current whether it is the one being read.
 * @param tag its automation tag.
 * @param onClick what choosing it does.
 */
@Composable
fun ContentsLink(
    label: AnnotatedString,
    current: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val hovered by interactions.collectIsHoveredAsState()
    val primary = PantrixTheme.colors.primary
    val line = if (current) primary.color else PantrixTheme.colors.border
    val text = when {
        current -> primary.onContainer
        hovered -> PantrixTheme.colors.textPrimary
        else -> PantrixTheme.colors.textSecondary
    }
    PantrixText(
        text = label,
        style = SiteTheme.type.bodySmall.copy(
            fontSize = 13.5.sp,
            fontWeight = if (current) FontWeight.Bold else FontWeight.SemiBold
        ),
        color = text,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { selected = current }
            .siteClickable(tag, Role.Button, RoundedCornerShape(2.dp), interactions, onClick)
            .drawBehind {
                val width = LINE_WIDTH.toPx()
                drawLine(line, Offset(width / 2, 0f), Offset(width / 2, size.height), width)
            }
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
    )
}

/** The width of the line along a heading's side. */
private val LINE_WIDTH = 2.dp
