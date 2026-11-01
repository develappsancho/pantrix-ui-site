package com.pantrixui.site.core.designsystem.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.overlay.sheet.PantrixSheet
import com.pantrixui.components.overlay.sheet.SheetColors
import com.pantrixui.components.overlay.sheet.SheetEdge
import com.pantrixui.components.overlay.sheet.SheetStyle
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.siteClickable

/**
 * The column beside a desktop's page: the documentation's links in their groups, scrolled on its own, and under them
 * [footer], a card that leads on.
 *
 * @param sections the groups, in order.
 * @param onSelect what choosing a link does, given its address.
 * @param modifier the column's modifier.
 * @param footer what stands under the groups, if anything.
 */
@Composable
fun SiteSidebar(
    sections: List<NavSection>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null
) {
    Box(
        modifier
            .width(SIDEBAR_WIDTH)
            .fillMaxHeight()
            .background(SiteTheme.colors.surfaceAlt)
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .pantrixVerticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            NavList(sections, onSelect)
            footer?.invoke()
        }
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .width(1.dp)
                .fillMaxHeight()
                .background(SiteTheme.colors.hairline)
        )
    }
}

/**
 * The card under the sidebar's links: a count and a line, leading to a page.
 *
 * @param title its first line: "98 components".
 * @param text the line under it.
 * @param onClick what pressing it does.
 * @param tag its automation tag.
 */
@Composable
fun SidebarCard(
    title: String,
    text: String,
    onClick: () -> Unit,
    tag: String
) {
    val shape = RoundedCornerShape(12.dp)
    val interactions = remember { MutableInteractionSource() }
    val hovered by interactions.collectIsHoveredAsState()
    Column(
        Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .siteClickable(tag, Role.Button, shape, interactions, onClick)
            .background(PantrixTheme.colors.surface, shape)
            .border(1.dp, if (hovered) PantrixTheme.colors.borderHover else PantrixTheme.colors.border, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        PantrixText(
            text = title,
            style = SiteTheme.type.cardTitle.copy(fontSize = 13.5.sp),
            color = PantrixTheme.colors.textPrimary
        )
        PantrixText(
            text = text,
            style = SiteTheme.type.factLabel.copy(fontWeight = FontWeight.Medium),
            color = SiteTheme.colors.muted
        )
    }
}

/**
 * The menu a tablet's or a phone's header opens: the site's links in their groups, in a sheet along the window's start
 * edge, its title the part of the site on show.
 *
 * @param visible whether it is open.
 * @param onDismissRequest what closing it does: its button, a press beside it, Back and Escape.
 * @param title what it is called: "Docs".
 * @param sections the groups, in order.
 * @param onSelect what choosing a link does, given its address; the menu closes as it does.
 */
@Composable
fun SiteMenu(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    sections: List<NavSection>,
    onSelect: (String) -> Unit
) {
    PantrixSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = title,
        edge = SheetEdge.Start,
        style = SheetStyle(sideWidth = if (SiteTheme.layout == SiteLayout.Phone) 312.dp else 340.dp),
        colors = SheetColors(container = PantrixTheme.colors.surface),
        tagKey = "menu"
    ) {
        NavList(
            sections = sections,
            onSelect = { href ->
                onDismissRequest()
                onSelect(href)
            },
            modifier = Modifier.padding(
                horizontal = if (SiteTheme.layout == SiteLayout.Phone) 8.dp else 12.dp,
                vertical = 14.dp
            )
        )
    }
}

/** The width of the sidebar. */
private val SIDEBAR_WIDTH = 232.dp
