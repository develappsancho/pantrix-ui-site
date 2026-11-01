package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.checkbox.PantrixCheckbox
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.testing.TestScope
import com.pantrixui.foundation.testing.pantrixTestTagOf
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Words

/**
 * The testing guide's sketch: a checkout in a `TestScope`, in the language chosen, with each control's automation tag
 * shown under it. The words change with the language; the tags, built from each `tagKey`, do not, and a test or a
 * Maestro flow that finds a control by its tag finds it in every language.
 */
@Composable
internal fun TestingSketch() {
    val language = LocalLanguage.current
    var shown by rememberSaveable { mutableStateOf(Language.English) }
    var tags by rememberSaveable { mutableStateOf(true) }
    var agreed by rememberSaveable { mutableStateOf(false) }
    val card = remember { TextFieldState("4242 4242 4242 4242") }
    SketchFigure(caption = TestingSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(app = TestingSay.App.of(shown), title = TestingSay.Title.of(shown)) {
                TestScope(SCOPE) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PantrixTextField(
                            state = card,
                            label = TestingSay.Card.of(shown),
                            modifier = Modifier.fillMaxWidth(),
                            tagKey = "card_number"
                        )
                        if (tags) Tag(pantrixTestTagOf("TextField", "card_number", SCOPE))
                        PantrixCheckbox(
                            checked = agreed,
                            onCheckedChange = { agreed = it },
                            label = TestingSay.Terms.of(shown),
                            tagKey = "terms"
                        )
                        if (tags) Tag(pantrixTestTagOf("Checkbox", "terms", SCOPE))
                        PantrixButton(text = TestingSay.Pay.of(shown), onClick = {}, fullWidth = true, tagKey = "pay")
                        if (tags) Tag(pantrixTestTagOf("Button", "pay", SCOPE))
                    }
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = listOf(Language.English to "English", Language.Turkish to "Türkçe"),
                selected = shown,
                onSelect = { shown = it },
                tag = "sketch_language"
            )
            SketchSwitch(code = "testTag", checked = tags, onToggle = { tags = !tags }, tag = "sketch_tags")
            SketchNote(TestingSay.Note.of(language))
        }
    }
}

/** A control's tag, under it, as a test finds it. */
@Composable
private fun Tag(tag: String) {
    PantrixText(
        text = tag,
        style = SiteTheme.type.inlineCode.sized(10.5.sp),
        color = PantrixTheme.colors.info.onContainer,
        modifier = Modifier
            .background(PantrixTheme.colors.info.container, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

private const val SCOPE = "Checkout"

/** The sketch's words, in each language. */
private object TestingSay {
    val App = Words("Shop", "Mağaza")
    val Title = Words("Checkout", "Ödeme")
    val Card = Words("Card number", "Kart numarası")
    val Terms = Words("I accept the terms", "Koşulları kabul ediyorum")
    val Pay = Words("Pay", "Öde")
    val Note = Words(
        "Change the language: the words change, the tags do not. onPantrixNode(\"Button\", \"pay\") finds the " +
            "button in both.",
        "Dili değiştir: sözcükler değişiyor, etiketler değişmiyor. onPantrixNode(\"Button\", \"pay\") düğmeyi " +
            "ikisinde de buluyor."
    )
    val Caption = Words(
        "Pantrix’s own components and tags, running on this page.",
        "Pantrix'in kendi bileşenleri ve etiketleri, bu sayfada çalışıyor."
    )
}
