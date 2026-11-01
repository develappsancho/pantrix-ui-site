package com.pantrixui.site.core.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.pantrixui.foundation.a11y.focusRing
import com.pantrixui.foundation.testing.pantrixTestTag
import com.pantrixui.foundation.theme.PantrixTheme

/**
 * What a part of the site's own frame that is pressed is made of, where no Pantrix component draws it: a sidebar's
 * row, the search box that opens the search. It is pressed by a finger, a pointer and the keyboard, shows Pantrix's
 * focus ring, says to a screen reader what it is, and is tagged for automation by [tag], which never comes from the
 * text it shows.
 *
 * @param tag its automation tag: `Site_<tag>`.
 * @param role what it is to a screen reader.
 * @param shape the shape of its ring and of what its press is clipped to.
 * @param interactionSource where its hover, focus and press are told, for a caller that draws them.
 * @param onClick what a press does.
 */
@Composable
fun Modifier.siteClickable(
    tag: String,
    role: Role,
    shape: Shape,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit
): Modifier {
    val interactions = interactionSource ?: remember { MutableInteractionSource() }
    return this
        .focusRing(interactions, shape, PantrixTheme.colors.focusRing, 2.dp)
        .clip(shape)
        .clickable(interactionSource = interactions, indication = null, role = role, onClick = onClick)
        .pantrixTestTag("Site_$tag")
}
