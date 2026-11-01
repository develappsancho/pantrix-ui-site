package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.i18n.LocalSiteStrings

/** What a page says when its content could not be read, with the button that reads it again. */
@Composable
fun PageFailed(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalSiteStrings.current
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PantrixText(
                text = strings.pageNotOpened,
                style = SiteTheme.type.lead,
                color = PantrixTheme.colors.textPrimary
            )
            PantrixButton(text = strings.tryAgain, onClick = onRetry, variant = ButtonVariant.Outline, tagKey = "retry")
        }
    }
}
