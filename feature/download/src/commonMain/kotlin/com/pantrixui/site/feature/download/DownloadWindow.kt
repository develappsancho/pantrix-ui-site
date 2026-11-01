package com.pantrixui.site.feature.download

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.landingHeading
import com.pantrixui.site.core.designsystem.inCapitals
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.ComponentGroups

/**
 * A picture of the desktop app's window at a component's page: its title bar, its sidebar where [sidebar], and the
 * page of a button with a few of its neighbours. It is a picture, drawn with boxes, that no screen reader reads and no
 * pointer works: the page's words say what the app is.
 *
 * @param modifier the picture's modifier.
 * @param height how tall the window is under its title bar, at the least.
 * @param sidebar whether the window shows its sidebar, as a desktop's and a tablet's pictures do.
 */
@Composable
internal fun AppWindow(
    modifier: Modifier = Modifier,
    height: Dp,
    sidebar: Boolean
) {
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clearAndSetSemantics {}
            .shadow(WINDOW_SHADOW, shape, clip = false)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
    ) {
        TitleBar()
        // As tall as the design draws it, and taller where a platform's type needs the room: the sidebar as tall as
        // the page beside it.
        Row(Modifier.fillMaxWidth().heightIn(min = height).height(IntrinsicSize.Min)) {
            if (sidebar) Sidebar()
            Column(
                Modifier.weight(1f).fillMaxHeight().background(colors.background).padding(22.dp, 22.dp, 22.dp, 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ButtonPage()
            }
        }
    }
}

/** Three dots and the app's name, over a hairline. */
@Composable
private fun TitleBar() {
    val site = SiteTheme.colors
    Row(
        Modifier.fillMaxWidth().height(38.dp).background(site.surfaceAlt).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(WINDOW_DOTS) { Box(Modifier.size(11.dp).background(PantrixTheme.colors.borderHover, CircleShape)) }
        PantrixText(
            text = APP_NAME,
            style = strong(12.sp, FontWeight.Bold).copy(textAlign = TextAlign.Center),
            color = site.muted,
            // The dots' room again at the end, so the name stands in the bar's middle.
            modifier = Modifier.weight(1f).padding(end = 48.dp)
        )
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(site.hairline))
}

/** The window's sidebar: the brand, and its links as bars, the first the page on show. */
@Composable
private fun Sidebar() {
    val colors = PantrixTheme.colors
    val site = SiteTheme.colors
    Row(Modifier.fillMaxHeight()) {
        Column(
            Modifier.width(
                SIDEBAR_WIDTH
            ).fillMaxHeight().background(site.surfaceAlt).padding(horizontal = 14.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(18.dp).background(BRAND_BLUE, RoundedCornerShape(6.dp)), Alignment.Center) {
                    PantrixText(text = "P", style = strong(10.sp), color = Color.White)
                }
                PantrixText(text = APP_NAME, style = strong(11.5.sp), color = colors.textPrimary)
            }
            LINKS.forEachIndexed { index, share ->
                Bar(share, if (index == 0) site.accentLine else colors.border, height = 8.dp)
            }
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(site.hairline))
    }
}

/** The page of a button, as the app shows it: its group, its name, its lines, and the button among its neighbours. */
@Composable
private fun ColumnScope.ButtonPage() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val site = SiteTheme.colors
    PantrixText(
        text = ComponentGroups.title("elements").of(language).inCapitals(),
        style = landingHeading(10.sp, line = 1.4f, spacing = 0.08f),
        color = colors.primary.onContainer
    )
    PantrixText(
        text = "Button",
        style = landingHeading(24.sp, line = 1.1f, spacing = -0.03f),
        color = colors.textPrimary
    )
    TEXT_LINES.forEach { share -> Bar(share, colors.border, height = 7.dp) }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(site.canvas)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(22.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
    ) {
        Pill(DownloadSay.WindowSave.of(language), filled = true)
        Pill(DownloadSay.WindowCancel.of(language), filled = false)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Tile("Switch") { SwitchMark() }
        Tile("Checkbox") { CheckboxMark() }
        Tile("Text Field") { FieldMark() }
    }
}

/** A rounded bar [share] of the room across, standing for a line of words. */
@Composable
private fun Bar(
    share: Float,
    color: Color,
    height: Dp
) {
    Box(Modifier.fillMaxWidth(share).height(height).background(color, RoundedCornerShape(4.dp)))
}

/** A small button of the picture: filled in the brand's colour where [filled], with a line around it where not. */
@Composable
private fun Pill(
    text: String,
    filled: Boolean
) {
    val primary = PantrixTheme.colors.primary
    Box(
        Modifier
            .height(30.dp)
            .clip(CircleShape)
            .background(if (filled) primary.color else Color.Transparent)
            .border(1.dp, primary.color, CircleShape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        PantrixText(
            text = text,
            style = strong(12.sp, FontWeight.Bold),
            color = if (filled) primary.onColor else primary.onContainer
        )
    }
}

/** One of the button's neighbours: its picture over its [name]. */
@Composable
private fun RowScope.Tile(
    name: String,
    mark: @Composable () -> Unit
) {
    val colors = PantrixTheme.colors
    Column(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().height(34.dp).background(SiteTheme.colors.canvas, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            mark()
        }
        PantrixText(text = name, style = strong(10.5.sp), color = colors.textPrimary, maxLines = 1)
    }
}

/** A switch that is on. */
@Composable
private fun SwitchMark() {
    val primary = PantrixTheme.colors.primary
    Box(
        Modifier.width(26.dp).height(15.dp).background(primary.color, CircleShape).padding(2.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(Modifier.size(11.dp).background(primary.onColor, CircleShape))
    }
}

/** A box that is ticked. */
@Composable
private fun CheckboxMark() {
    val primary = PantrixTheme.colors.primary
    Box(Modifier.size(15.dp).background(primary.color, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
        PantrixIcon(imageVector = PantrixIcons.Check, contentDescription = null, tint = primary.onColor, size = 11.dp)
    }
}

/** An empty field. */
@Composable
private fun FieldMark() {
    val colors = PantrixTheme.colors
    Box(
        Modifier
            .fillMaxWidth(FIELD_SHARE)
            .height(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colors.surface)
            .border(1.dp, colors.borderHover, RoundedCornerShape(4.dp))
    )
}

/** The name in the window's title bar and its sidebar, the same in every language. */
private const val APP_NAME = "Pantrix UI"

/** How far across the sidebar each of its links reaches, the first the page on show. */
private val LINKS = listOf(0.70f, 0.85f, 0.60f, 0.78f, 0.66f, 0.72f)

/** How far across the page its two lines of words under the name reach. */
private val TEXT_LINES = listOf(0.92f, 0.64f)

/** The dots at the start of a macOS window's title bar: close, minimise and zoom. */
private const val WINDOW_DOTS = 3

/** How far across its tile the field reaches. */
private const val FIELD_SHARE = 0.7f

private val SIDEBAR_WIDTH = 150.dp
private val WINDOW_SHADOW = 12.dp

/** The brand's own blue, the same in both themes, as the header's mark is. */
private val BRAND_BLUE = Color(0xFF2264FF)
