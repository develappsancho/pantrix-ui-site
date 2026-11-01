package com.pantrixui.site.tools.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.inCapitals
import com.pantrixui.site.core.model.ShareImage
import com.pantrixui.site.core.model.SiteAddress

/**
 * The card a shared link of a page shows: the site's mark, what part of the site the page is in, and its title, in
 * the site's light theme and type. It fills [ShareImage.WIDTH] by [ShareImage.HEIGHT] at a density of one.
 */
@Composable
internal fun ShareCard(image: ShareImage) {
    val colors = PantrixTheme.colors
    val type = SiteTheme.type
    Column(Modifier.fillMaxSize().background(colors.background)) {
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 80.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    Modifier.size(56.dp).background(colors.primary.color, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    PantrixText(
                        text = "P",
                        color = Color.White,
                        style = type.nav.copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    )
                }
                PantrixText(
                    text = "Pantrix UI",
                    color = colors.textPrimary,
                    style = type.nav.copy(fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                PantrixText(
                    text = image.kicker.inCapitals(),
                    color = colors.primary.color,
                    style = type.overline.copy(fontSize = 24.sp, lineHeight = 30.sp)
                )
                PantrixText(
                    text = image.title,
                    color = colors.textPrimary,
                    style = type.pageTitle.copy(fontSize = 68.sp, lineHeight = 76.sp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            PantrixText(
                text = SiteAddress.host,
                color = colors.textSecondary,
                style = type.body.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold)
            )
        }
        Spacer(Modifier.fillMaxWidth().height(14.dp).background(colors.primary.color))
    }
}
