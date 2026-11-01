package com.pantrixui.site.core.designsystem.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.LANDING_WIDTH
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.i18n.ownName
import com.pantrixui.site.core.model.Language

/**
 * What choosing the site's language does, from a page: the frame's own choice, which it gives the pages it draws. A
 * page drawn alone, in a test, chooses nothing.
 */
val LocalChooseLanguage: ProvidableCompositionLocal<(Language) -> Unit> = staticCompositionLocalOf { {} }

/**
 * The foot of a page that has no other: the licence and the site's languages on a band across the window, clear of
 * the system's bottom bar.
 *
 * @param modifier the band's modifier.
 */
@Composable
fun SiteFoot(modifier: Modifier = Modifier) {
    val gutter = if (SiteTheme.layout == SiteLayout.Phone) 16.dp else 24.dp
    FootBand(modifier) {
        FootLine(Modifier.padding(horizontal = gutter, vertical = 28.dp).widthIn(max = LANDING_WIDTH).fillMaxWidth())
    }
}

/**
 * A band of the site's foot across the window: the softer ground, a hairline over it, [content] in its middle, and
 * room for the system's bottom bar under it.
 *
 * @param modifier the band's modifier.
 * @param content what it holds.
 */
@Composable
fun FootBand(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val line = SiteTheme.colors.hairline
    Box(
        modifier
            .fillMaxWidth()
            .background(SiteTheme.colors.surfaceAlt)
            .drawBehind { drawLine(line, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
        contentAlignment = Alignment.TopCenter
    ) {
        content()
    }
}

/**
 * The foot's last line: the library's licence at its start, and at its end the site's languages, the one on show as
 * words and each other as a link that turns the site to it.
 *
 * @param modifier the line's modifier.
 */
@Composable
fun FootLine(modifier: Modifier = Modifier) {
    val language = LocalLanguage.current
    val choose = LocalChooseLanguage.current
    val muted = SiteTheme.colors.muted
    val style = SiteTheme.type.breadcrumb.copy(fontWeight = SiteTheme.type.body.fontWeight)
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        PantrixText(text = LICENCE, style = style, color = muted, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Language.entries.forEachIndexed { index, each ->
                if (index > 0) PantrixText(text = "·", style = style, color = muted)
                if (each == language) {
                    PantrixText(text = each.ownName, style = style, color = muted)
                } else {
                    PantrixText(
                        text = each.ownName,
                        style = style,
                        color = PantrixTheme.colors.primary.onContainer,
                        modifier = Modifier.siteClickable(
                            tag = "footLanguage_${each.code}",
                            role = Role.Button,
                            shape = RoundedCornerShape(4.dp),
                            onClick = { choose(each) }
                        )
                    )
                }
            }
        }
    }
}

/** The licence Pantrix UI is under. */
private const val LICENCE = "Apache License 2.0"
