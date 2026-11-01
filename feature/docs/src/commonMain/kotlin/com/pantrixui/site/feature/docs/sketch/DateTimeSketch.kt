@file:Suppress("MagicNumber") // A sketch's sample values, its times, amounts and sizes, are what it shows.

package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.container.card.CardVariant
import com.pantrixui.components.container.card.PantrixCard
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.date.rememberToday
import com.pantrixui.foundation.l10n.PantrixLocale
import com.pantrixui.foundation.l10n.PantrixStrings
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The date and time guide's sketch: today, as this page's clock has it, and a delivery's hour, written by the strings
 * of the language chosen, by a clock of twelve hours or of twenty-four. No date library is behind it.
 */
@Composable
internal fun DateTimeSketch() {
    val language = LocalLanguage.current
    var is24Hour by rememberSaveable { mutableStateOf(true) }
    var locale by rememberSaveable { mutableStateOf("tr") }
    val strings = PantrixStrings.forLocale(LOCALES.getValue(locale))
    val today = rememberToday()
    SketchFigure(caption = DateSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(app = DateSay.App.of(language), title = DateSay.Title.of(language)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Line(DateSay.Today.of(language), strings.longDate(today.day, today.month, today.year))
                    Line(DateSay.Short.of(language), strings.date(today.day, today.month, today.year))
                    Line(
                        DateSay.Window.of(language),
                        "${strings.time(14, 5, is24Hour = is24Hour)} – " +
                            strings.time(15, 30, is24Hour = is24Hour)
                    )
                    Line(DateSay.Iso.of(language), today.toString())
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = listOf("en" to "English", "tr" to "Türkçe", "ar" to "العربية"),
                selected = locale,
                onSelect = { locale = it },
                tag = "sketch_language"
            )
            SketchSwitch(code = "is24Hour", checked = is24Hour, onToggle = { is24Hour = !is24Hour }, tag = "sketch_24h")
            SketchNote(DateSay.Note.of(language))
        }
    }
}

@Composable
private fun Line(
    label: String,
    value: String
) {
    PantrixCard(variant = CardVariant.Outlined, modifier = Modifier.fillMaxWidth()) {
        PantrixText(text = label, style = PantrixTheme.typography.footnote, color = PantrixTheme.colors.textSecondary)
        PantrixText(text = value, style = PantrixTheme.typography.body, modifier = Modifier.padding(top = 2.dp))
    }
}

private val LOCALES = mapOf("en" to PantrixLocale.English, "tr" to PantrixLocale.Turkish, "ar" to PantrixLocale.Arabic)

/** The sketch's words, in each language. */
private object DateSay {
    val App = Words("Orders", "Siparişler")
    val Title = Words("Delivery", "Teslimat")
    val Today = Words("Today", "Bugün")
    val Short = Words("On the receipt", "Fişte")
    val Window = Words("Arrives between", "Geliş aralığı")
    val Iso = Words("Sent to the server", "Sunucuya giden")
    val Note = Words(
        "Today is this page’s clock’s, read again at midnight. Each line is the language’s own; the server gets " +
            "ISO 8601.",
        "Bugün bu sayfanın saatinindir, gece yarısı yeniden okunur. Her satır dilin kendi yazımı; sunucu ISO 8601 alır."
    )
    val Caption = Words(
        "Pantrix’s own dates and times, running on this page.",
        "Pantrix'in kendi tarihleri ve saatleri, bu sayfada çalışıyor."
    )
}
