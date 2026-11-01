@file:Suppress("MagicNumber") // A sketch's sample values, its times, amounts and sizes, are what it shows.

package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The typography guide's sketch: Pantrix's type scale on a phone, each role in its own style with its size, and the
 * system's font size chosen under it. The scale is in sp: every role grows with it, and a text given no style is
 * `label`.
 */
@Composable
internal fun TypographySketch() {
    val language = LocalLanguage.current
    var fontScale by rememberSaveable { mutableFloatStateOf(1f) }
    SketchFigure(caption = TypeSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(app = TypeSay.App.of(language), title = null) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    val type = PantrixTheme.typography
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Role("display", type.display, "36", density)
                        Role("headline", type.headline, "28", density)
                        Role("title", type.title, "22", density)
                        Role("callout", type.callout, "18", density)
                        Role("body", type.body, "16", density)
                        Role("label", type.label, "14", density)
                        Role("footnote", type.footnote, "12", density)
                        Role("caption", type.caption, "11", density)
                    }
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = listOf(1f to "100%", 1.3f to "130%", 2f to "200%"),
                selected = fontScale,
                onSelect = { fontScale = it },
                tag = "sketch_fontScale"
            )
            SketchNote(TypeSay.Note.of(language))
        }
    }
}

/**
 * A role's sample in its style, at the font size chosen, and its name and size beside it at the page's own: the name
 * is the sketch's caption, not the app's text, and leaves the sample its room.
 */
@Composable
private fun Role(
    name: String,
    style: TextStyle,
    size: String,
    pageDensity: Density
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        PantrixText(
            text = "Aa",
            style = style,
            color = PantrixTheme.colors.textPrimary,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.weight(1f)
        )
        CompositionLocalProvider(LocalDensity provides pageDensity) {
            PantrixText(
                text = "$name · $size",
                style = SiteTheme.type.inlineCode,
                color = SiteTheme.colors.muted
            )
        }
    }
}

/** The sketch's words, in each language. */
private object TypeSay {
    val App = Words("Reader", "Okuyucu")
    val Note = Words(
        "The system’s font size, as a reader sets it: every role grows with it, since the scale is in sp.",
        "Sistemin yazı boyutu, okurun ayarladığı gibi: ölçek sp olduğu için her rol onunla büyür."
    )
    val Caption = Words(
        "Pantrix’s own type scale, running on this page.",
        "Pantrix'in kendi yazı ölçeği, bu sayfada çalışıyor."
    )
}
