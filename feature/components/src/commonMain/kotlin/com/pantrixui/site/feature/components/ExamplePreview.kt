package com.pantrixui.site.feature.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.foundation.a11y.PantrixPlatform
import com.pantrixui.foundation.l10n.PantrixLocale
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.ShopeersPreset
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.dotted
import com.pantrixui.site.core.designsystem.content.siteName
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * An example the site has running: the component as the example's code draws it, on this page.
 *
 * @property content the example itself.
 */
internal class LiveExample(val content: @Composable () -> Unit)

/**
 * A live example in its frame: a choice of the platform whose conventions it is drawn in, the dark theme and a
 * right-to-left language over it, and the example under them on the page's canvas, in the site's own theme for the
 * platform chosen.
 *
 * @param example what is drawn.
 * @param modifier the frame's modifier.
 */
@Composable
internal fun ExampleFrame(
    example: LiveExample,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val host = PantrixTheme.platform
    var platform by rememberSaveable { mutableStateOf(host.type) }
    var dark by rememberSaveable { mutableStateOf(false) }
    var rtl by rememberSaveable { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    val phone = SiteTheme.layout == SiteLayout.Phone
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, PantrixTheme.colors.border, shape)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(SiteTheme.colors.surfaceAlt)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PantrixSegmentButton(
                options = PlatformType.entries.map { SegmentOption(it, it.siteName, tagKey = it.name.lowercase()) },
                selected = platform,
                onSelect = { platform = it },
                modifier = if (phone) Modifier.weight(1f) else Modifier,
                fullWidth = phone,
                tagKey = "examplePlatform"
            )
            if (!phone) {
                Box(Modifier.weight(1f))
                Toggle(FrameSay.Dark.of(language), dark, "exampleDark") { dark = !dark }
                Toggle(FrameSay.ArabicRtl.of(language), rtl, "exampleRtl") { rtl = !rtl }
            }
        }
        val colors = remember(dark) { ShopeersPreset.colors(dark) }
        PantrixTheme(
            preset = ShopeersPreset,
            colors = colors,
            platform = PantrixPlatform.forType(platform, isTouch = host.isTouch),
            locale = if (rtl) PantrixLocale.Arabic else PantrixTheme.locale,
            followSystemBars = false
        ) {
            val canvas = PantrixTheme.colors.background
            val dots = PantrixTheme.colors.border
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = CANVAS_HEIGHT)
                    .background(canvas)
                    .dotted(dots)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(Modifier.widthIn(max = CONTENT_WIDTH).fillMaxWidth()) { example.content() }
            }
        }
    }
}

@Composable
private fun Toggle(
    label: String,
    on: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    PantrixButton(
        text = label,
        onClick = onClick,
        variant = if (on) ButtonVariant.Soft else ButtonVariant.Text,
        size = ButtonSize.Small,
        tagKey = tag
    )
}

/** The frame's words, in each language. */
private object FrameSay {
    val Dark = Words("Dark", "Koyu")
    val ArabicRtl = Words("Arabic, RTL", "Arapça, RTL")
}

private val CANVAS_HEIGHT = 180.dp
private val CONTENT_WIDTH = 520.dp
