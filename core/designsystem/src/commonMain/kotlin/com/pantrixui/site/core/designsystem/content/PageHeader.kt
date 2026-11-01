package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.link.LinkUnderline
import com.pantrixui.components.elements.link.PantrixLink
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.navigation.breadcrumb.BreadcrumbColors
import com.pantrixui.components.navigation.breadcrumb.BreadcrumbItem
import com.pantrixui.components.navigation.breadcrumb.BreadcrumbStyle
import com.pantrixui.components.navigation.breadcrumb.PantrixBreadcrumb
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.model.ContentsEntry
import com.pantrixui.site.core.model.Span

/**
 * One step of a page's breadcrumb.
 *
 * @property label what it says.
 * @property href where it leads: a path of the site.
 */
data class Crumb(
    val label: String,
    val href: String
)

/**
 * The head of a page of the documentation: the trail above it, its title, the button that copies it as Markdown,
 * its lead and its tags. On a desktop and a tablet the button stands beside the title; on a phone it is as wide as
 * the page, and the trail keeps only the step above the page unless [fullTrail] says to keep all of them. Under them,
 * where there is no room beside the article for its "On this page" list, stands that list as a row of buttons.
 *
 * @param trail the pages above this one, the part of the site first.
 * @param title the page's title, the trail's last step.
 * @param lead the paragraph under the title, if the page has one.
 * @param copied whether the page was just copied: the button says so.
 * @param onCopy what the button does.
 * @param onLink what a step of the trail does, given its address.
 * @param modifier the head's modifier.
 * @param tags the tags under the lead.
 * @param contents the page's sections, for the row of buttons, or none where the list stands beside the article.
 * @param onContents what one of those buttons does, given its section's anchor.
 * @param style the kind of page it heads.
 * @param fullTrail whether a phone's trail keeps every step, the page's own too: the guides' does.
 * @param leadText the lead as a run of text, in place of [lead], where it holds bold words or code.
 */
@Composable
fun PageHeader(
    trail: List<Crumb>,
    title: String,
    lead: String?,
    copied: Boolean,
    onCopy: () -> Unit,
    onLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    tags: List<PageTag> = emptyList(),
    contents: List<ContentsEntry> = emptyList(),
    onContents: (String) -> Unit = {},
    style: ContentStyle = ContentStyle.Doc,
    fullTrail: Boolean = false,
    leadText: List<Span> = emptyList()
) {
    val leadView: (@Composable () -> Unit)? = when {
        leadText.isNotEmpty() -> (
            {
                RichText(
                    leadText,
                    onLink,
                    style = SiteTheme.type.lead,
                    color = PantrixTheme.colors.textSecondary,
                    strongColor = PantrixTheme.colors.textPrimary
                )
            }
        )

        lead != null -> ({ Lead(lead) })

        else -> null
    }
    val layout = SiteTheme.layout
    val titleStyle = if (style == ContentStyle.Guide) SiteTheme.type.guideTitle else SiteTheme.type.pageTitle
    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Desktop) 14.dp else 12.dp)
    ) {
        if (layout == SiteLayout.Phone) {
            if (fullTrail) {
                Trail(trail, title, onLink)
            } else {
                trail.lastOrNull()?.let { above -> StepAbove(above, onLink) }
            }
            PantrixText(text = title, style = titleStyle, color = PantrixTheme.colors.textPrimary)
            leadView?.invoke()
            if (tags.isNotEmpty()) PageTags(tags)
            CopyPageButton(copied, onCopy, Modifier.fillMaxWidth())
        } else {
            if (trail.isNotEmpty()) Trail(trail, title, onLink)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                PantrixText(
                    text = title,
                    style = titleStyle,
                    color = PantrixTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                CopyPageButton(copied, onCopy)
            }
            leadView?.invoke()
            if (tags.isNotEmpty()) PageTags(tags)
        }
        if (contents.isNotEmpty()) ContentsChips(contents, onContents)
    }
}

/** The step above a page, alone: a phone's trail of a page of the documentation, whose title is the step itself. */
@Composable
private fun StepAbove(
    above: Crumb,
    onLink: (String) -> Unit
) {
    PantrixLink(
        text = above.label,
        onClick = { onLink(above.href) },
        style = SiteTheme.type.breadcrumb,
        color = SiteTheme.colors.muted,
        tagKey = "breadcrumb",
        underline = LinkUnderline.OnHover
    )
}

@Composable
private fun Trail(
    trail: List<Crumb>,
    title: String,
    onLink: (String) -> Unit
) {
    val muted = SiteTheme.colors.muted
    PantrixBreadcrumb(
        items = trail.mapIndexed { index, crumb -> BreadcrumbItem(key = "$index", label = crumb.label) } +
            BreadcrumbItem(key = CURRENT, label = title),
        onSelect = { key -> trail.getOrNull(key.toIntOrNull() ?: -1)?.let { onLink(it.href) } },
        textStyle = SiteTheme.type.breadcrumb,
        // As tall as its line of text, and its links underlined under a pointer, as the design's trail is.
        style = BreadcrumbStyle(
            itemGap = 6.dp,
            separatorSize = 14.dp,
            minHeight = 0.dp,
            linkUnderline = LinkUnderline.OnHover
        ),
        colors = BreadcrumbColors(link = muted, current = PantrixTheme.colors.textPrimary, separator = muted),
        tagKey = "page"
    )
}

@Composable
private fun Lead(text: String) {
    PantrixText(text = text, style = SiteTheme.type.lead, color = PantrixTheme.colors.textSecondary)
}

/** The button that copies the page as Markdown: 36 tall beside a desktop's title, 44 where a finger presses it. */
@Composable
private fun CopyPageButton(
    copied: Boolean,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalSiteStrings.current
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    val label = if (copied) strings.copied else strings.copyPage
    PantrixButton(
        onClick = onCopy,
        contentDescription = label,
        modifier = modifier,
        variant = ButtonVariant.Outline,
        size = ButtonSize.Medium,
        style = ButtonStyle(
            medium = ButtonSizeStyle(
                height = if (desktop) 36.dp else 44.dp,
                horizontalPadding = if (desktop) 12.dp else 14.dp
            )
        ),
        shape = RoundedCornerShape(if (desktop) 10.dp else 12.dp),
        colors = ButtonColors(
            container = PantrixTheme.colors.surface,
            content = PantrixTheme.colors.textPrimary,
            border = PantrixTheme.colors.borderHover
        ),
        tagKey = "copyPage"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixIcon(
                imageVector = if (copied) PantrixIcons.Check else PantrixIcons.Copy,
                contentDescription = null,
                size = if (desktop) 15.dp else 16.dp
            )
            PantrixText(text = label, style = SiteTheme.type.button)
        }
    }
}

/** The key of the trail's last step, the page on show. */
private const val CURRENT = "current"
