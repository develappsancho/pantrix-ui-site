package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.container.card.CardVariant
import com.pantrixui.components.container.card.PantrixCard
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.l10n.PantrixLocale
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The language guide's sketch: an order's card in the language its reader chooses. The date and the list of names
 * are the library's words, from the theme's `PantrixStrings`; the title, the line and the button are the app's own,
 * chosen by the same locale, so that no part of the screen is left in another language. Arabic is read from the
 * right, the whole screen with it.
 */
@Composable
internal fun LanguageSketch() {
    val language = LocalLanguage.current
    var reader by rememberSaveable { mutableStateOf(Reader.English) }
    SketchFigure(caption = LanguageSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(app = LanguageSay.App.of(language), title = null) {
                PantrixTheme(locale = reader.locale) {
                    val strings = PantrixTheme.strings
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PantrixText(text = reader.title, style = PantrixTheme.typography.title)
                        PantrixCard(variant = CardVariant.Outlined, modifier = Modifier.fillMaxWidth()) {
                            PantrixText(
                                text = strings.fullDate(DAY, MONTH, YEAR),
                                style = PantrixTheme.typography.footnote
                            )
                            PantrixText(text = reader.sharedWith)
                            PantrixText(
                                text = strings.list(listOf("Ada", "Grace", "Alan")),
                                color = PantrixTheme.colors.textSecondary
                            )
                        }
                        PantrixButton(text = reader.button, onClick = {}, fullWidth = true, tagKey = "sketch_reorder")
                    }
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = Reader.entries.map { it to it.label },
                selected = reader,
                onSelect = { reader = it },
                tag = "sketch_language"
            )
            SketchNote(LanguageSay.Note.of(language))
        }
    }
}

/** A language the sketch's reader can choose: its locale, and the app's own words in it. */
private enum class Reader(
    val label: String,
    val locale: PantrixLocale,
    val title: String,
    val sharedWith: String,
    val button: String
) {
    English("English", PantrixLocale.English, "Order #1042", "Shared with", "Order again"),
    Turkish("Türkçe", PantrixLocale.Turkish, "Sipariş #1042", "Paylaşılanlar", "Yeniden sipariş ver"),
    Arabic("العربية", PantrixLocale.Arabic, "الطلب #1042", "تمت المشاركة مع", "اطلب مرة أخرى")
}

private object LanguageSay {
    val App = Words("Shop", "Mağaza")
    val Caption = Words(
        "The library's words and the app's own, in one language.",
        "Kütüphanenin sözleri ve uygulamanın kendi sözleri, tek bir dilde."
    )
    val Note = Words(
        "The date and the names are the library's; the title, the line and the button are the app's.",
        "Tarih ve adlar kütüphanenin; başlık, satır ve düğme uygulamanın."
    )
}

private const val DAY = 11
private const val MONTH = 11
private const val YEAR = 2026
