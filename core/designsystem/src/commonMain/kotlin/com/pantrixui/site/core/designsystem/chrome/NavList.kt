package com.pantrixui.site.core.designsystem.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.inCapitals
import com.pantrixui.site.core.designsystem.siteClickable

/**
 * A group of a [NavList]: its title and its links.
 *
 * @property title what the group is called, set in capitals over its links.
 * @property items its links.
 */
data class NavSection(
    val title: String,
    val items: List<NavItem>
)

/**
 * A link of a [NavList].
 *
 * @property key its automation key, the same in every language.
 * @property label what it says.
 * @property href where it leads: a path of the site or a web address.
 * @property current whether it is the page on show.
 * @property count how many pages it holds, written at its end: a group of components.
 * @property nested whether it is a link inside the one above it, set in from it: a component of its group.
 */
data class NavItem(
    val key: String,
    val label: String,
    val href: String,
    val current: Boolean = false,
    val count: Int? = null,
    val nested: Boolean = false
)

/**
 * The documentation's links in their groups: the sidebar beside a page on a desktop, the menu's on a tablet and a
 * phone, where its rows are as tall as a finger needs.
 *
 * @param sections the groups, in order.
 * @param onSelect what choosing a link does, given its address.
 * @param modifier the list's modifier.
 */
@Composable
fun NavList(
    sections: List<NavSection>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val layout = SiteTheme.layout
    val desktop = layout == SiteLayout.Desktop
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (desktop) 22.dp else 2.dp)) {
        sections.forEachIndexed { index, section ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                PantrixText(
                    text = section.title.inCapitals(),
                    style = SiteTheme.type.overline,
                    color = SiteTheme.colors.muted,
                    modifier = Modifier.padding(
                        start = if (desktop) 10.dp else 12.dp,
                        end = 10.dp,
                        top = if (desktop || index == 0) 0.dp else 14.dp,
                        bottom = 6.dp
                    )
                )
                section.items.forEach { item -> NavRow(item, onSelect) }
            }
        }
    }
}

@Composable
private fun NavRow(
    item: NavItem,
    onSelect: (String) -> Unit
) {
    val layout = SiteTheme.layout
    val desktop = layout == SiteLayout.Desktop
    val shape = RoundedCornerShape(if (desktop) 8.dp else 10.dp)
    val interactions = remember { MutableInteractionSource() }
    val hovered by interactions.collectIsHoveredAsState()
    val primary = PantrixTheme.colors.primary
    val fill = when {
        item.current -> primary.container
        hovered -> SiteTheme.colors.hover
        else -> Color.Transparent
    }
    val side = if (desktop) 10.dp else 12.dp
    Row(
        Modifier
            .padding(start = if (item.nested) NESTED else 0.dp)
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { selected = item.current }
            .siteClickable("nav_${item.key}", Role.Button, shape, interactions) { onSelect(item.href) }
            .background(fill, shape)
            .heightIn(
                min = if (desktop) {
                    0.dp
                } else if (layout == SiteLayout.Tablet) {
                    44.dp
                } else {
                    48.dp
                }
            )
            .padding(horizontal = side, vertical = if (desktop) 6.dp else 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = item.label,
            style = SiteTheme.type.sidebarItem.copy(
                fontWeight = if (item.current) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (item.current) primary.onContainer else PantrixTheme.colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        item.count?.let { count ->
            PantrixText(
                text = "$count",
                style = SiteTheme.type.sidebarItem.copy(fontWeight = FontWeight.SemiBold),
                color = SiteTheme.colors.muted
            )
        }
    }
}

/** How far a link inside another is set in from it. */
private val NESTED = 12.dp
