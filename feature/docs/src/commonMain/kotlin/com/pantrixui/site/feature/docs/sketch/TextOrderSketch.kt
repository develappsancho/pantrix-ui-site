package com.pantrixui.site.feature.docs.sketch

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
import com.pantrixui.components.elements.divider.PantrixHorizontalDivider
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.l10n.PantrixLocale
import com.pantrixui.foundation.l10n.rememberTextOrder
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The text order guide's sketch: a phone's list of Turkish names, sorted by `sorted()`, by the characters' codes, or
 * with `rememberTextOrder()` in Turkish, by the alphabet a reader knows: `Ç` after `C`, `ı` before `i`, `Ş` after
 * `S`.
 */
@Composable
internal fun TextOrderSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    val order = rememberTextOrder(PantrixLocale.Turkish)
    val names = if (fix) NAMES.sortedWith(order) else NAMES.sorted()
    SketchFigure(caption = OrderSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(app = OrderSay.App.of(language), title = OrderSay.Title.of(language)) {
                Column(Modifier.fillMaxWidth()) {
                    names.forEachIndexed { index, name ->
                        if (index > 0) PantrixHorizontalDivider()
                        PantrixText(
                            text = name,
                            style = PantrixTheme.typography.body,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp)
                        )
                    }
                }
            }
        }
        SketchControls {
            SketchSwitch(
                code = "rememberTextOrder()",
                checked = fix,
                onToggle = { fix = !fix },
                tag = "sketch_rememberTextOrder"
            )
            SketchNote((if (fix) OrderSay.Alphabet else OrderSay.Codes).of(language))
        }
    }
}

/** Names whose order a code's order gets wrong in Turkish. */
private val NAMES = listOf("Zeynep", "Şule", "Selin", "Ömer", "Oya", "İpek", "Işık", "Ilgaz", "Ceren", "Çağrı")

/** The sketch's words, in each language. */
private object OrderSay {
    val App = Words("Contacts", "Kişiler")
    val Title = Words("Friends", "Arkadaşlar")
    val Codes = Words(
        "sorted(): by the characters’ codes. Ç, Ö, İ and Ş come after Z.",
        "sorted(): karakterlerin kodlarına göre. Ç, Ö, İ ve Ş Z'den sonra geliyor."
    )
    val Alphabet = Words(
        "sortedWith(rememberTextOrder()): by the Turkish alphabet, as a reader looks for a name.",
        "sortedWith(rememberTextOrder()): Türk alfabesine göre, okurun bir adı aradığı gibi."
    )
    val Caption = Words(
        "Pantrix’s own text order, running on this page.",
        "Pantrix'in kendi sıralaması, bu sayfada çalışıyor."
    )
}
