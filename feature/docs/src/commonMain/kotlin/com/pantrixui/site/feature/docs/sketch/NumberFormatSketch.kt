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
 * The number format guide's sketch: an order's total, its share of a budget and a count of items, written by
 * `toString()` or by the strings of the language chosen, English, Turkish or Arabic, in each one's separators and
 * digits.
 */
@Composable
internal fun NumberFormatSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    var locale by rememberSaveable { mutableStateOf("tr") }
    val strings = PantrixStrings.forLocale(LOCALES.getValue(locale))
    SketchFigure(caption = FormatSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(app = FormatSay.App.of(language), title = FormatSay.Title.of(language)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Line(FormatSay.Total.of(language), if (fix) strings.decimal(TOTAL) else TOTAL.toString())
                    Line(FormatSay.Share.of(language), if (fix) strings.percent(SHARE) else "${SHARE * 100}%")
                    Line(
                        FormatSay.Items.of(language),
                        if (fix) strings.decimal(ITEMS, fractionDigits = 0) else "$ITEMS"
                    )
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
            SketchSwitch(code = "strings.decimal()", checked = fix, onToggle = { fix = !fix }, tag = "sketch_decimal")
            SketchNote((if (fix) FormatSay.Fixed else FormatSay.Plain).of(language))
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
        PantrixText(text = value, style = PantrixTheme.typography.title, modifier = Modifier.padding(top = 2.dp))
    }
}

private const val TOTAL = 1234567.891
private const val SHARE = 0.264
private const val ITEMS = 12480

private val LOCALES = mapOf("en" to PantrixLocale.English, "tr" to PantrixLocale.Turkish, "ar" to PantrixLocale.Arabic)

/** The sketch's words, in each language. */
private object FormatSay {
    val App = Words("Shop", "Mağaza")
    val Title = Words("This month", "Bu ay")
    val Total = Words("Orders", "Siparişler")
    val Share = Words("Of the budget", "Bütçeden")
    val Items = Words("Items sent", "Gönderilen ürün")
    val Plain = Words(
        "toString(): the same in every language, with no group separator and every digit of the fraction.",
        "toString(): her dilde aynı, binlik ayırıcısı yok ve kesrin her basamağı orada."
    )
    val Fixed = Words(
        "The language’s own separators, its digits and the place of its percent sign, on every platform.",
        "Dilin kendi ayırıcıları, rakamları ve yüzde işaretinin yeri, her platformda."
    )
    val Caption = Words(
        "Pantrix’s own number strings, running on this page.",
        "Pantrix'in kendi sayı yazımı, bu sayfada çalışıyor."
    )
}
