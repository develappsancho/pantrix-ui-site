package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.sized

/**
 * A chip that keeps a page's list to one part of it, with how many of the list's lines that part has: a topic of the
 * guides, a module of the changelog, a group of the components.
 *
 * @param label what it says.
 * @param count how many lines it keeps.
 * @param selected whether it is the one chosen.
 * @param tag its automation tag.
 * @param onClick what choosing it does.
 */
@Composable
fun FilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val colors = PantrixTheme.colors
    val content = if (selected) colors.primary.onContainer else colors.textSecondary
    PantrixButton(
        onClick = onClick,
        contentDescription = "$label, $count",
        modifier = Modifier.semantics { this.selected = selected },
        variant = ButtonVariant.Outline,
        size = ButtonSize.Medium,
        style = ButtonStyle(
            medium = ButtonSizeStyle(
                height = if (SiteTheme.layout == SiteLayout.Desktop) 34.dp else 40.dp,
                horizontalPadding = 12.dp
            )
        ),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonColors(
            container = if (selected) colors.primary.container else colors.surface,
            content = content,
            border = if (selected) SiteTheme.colors.accentLine else colors.border
        ),
        tagKey = tag
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixText(text = label, style = SiteTheme.type.button.sized(13.5.sp), maxLines = 1, softWrap = false)
            PantrixText(
                text = "$count",
                style = SiteTheme.type.button.sized(12.sp).copy(fontWeight = FontWeight.ExtraBold),
                color = content.copy(alpha = COUNT_ALPHA)
            )
        }
    }
}

/** How strong a chip's count is beside its label. */
private const val COUNT_ALPHA = 0.75f
