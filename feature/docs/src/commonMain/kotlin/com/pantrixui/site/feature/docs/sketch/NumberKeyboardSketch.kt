package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.keyboard.clearFocusOnTap
import com.pantrixui.foundation.platform.numberKeyboardOptions
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The number keyboard guide's sketch: a field of an amount on an iPhone and the keyboard it asks for, drawn. Without
 * `numberKeyboardOptions` it is the number pad, ten digits and a key that takes one back, and what was typed cannot
 * be sent; with it, the keyboard of numbers and punctuation, whose return key does the field's action.
 */
@Composable
internal fun NumberKeyboardSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    var note by remember { mutableStateOf<Words?>(null) }
    val field = remember { TextFieldState("12") }
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    val focus = LocalFocusManager.current
    LaunchedEffect(focused, fix) { if (focused) note = if (fix) NumberPadSay.Punctuation else NumberPadSay.Pad }
    SketchFigure(caption = NumberPadSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(
                app = NumberPadSay.App.of(language),
                title = NumberPadSay.Title.of(language),
                modifier = Modifier.clearFocusOnTap(),
                overlay = {
                    if (focused) {
                        DrawnKeyboard(
                            rows = if (fix) NUMBERS_AND_PUNCTUATION else NUMBER_PAD,
                            onKey = { key ->
                                if (key.typeInto(field)) {
                                    note = NumberPadSay.Sent
                                    focus.clearFocus()
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomCenter),
                            keyHeight = if (fix) 30 else 38
                        )
                    }
                }
            ) {
                Column {
                    NoSystemKeyboard {
                        PantrixTextField(
                            state = field,
                            label = NumberPadSay.Label.of(language),
                            keyboardOptions = if (fix) {
                                numberKeyboardOptions(imeAction = ImeAction.Done, decimal = true)
                            } else {
                                KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                            },
                            interactionSource = interactions,
                            modifier = Modifier.fillMaxWidth(),
                            tagKey = "sketch_amount"
                        )
                    }
                }
            }
        }
        SketchControls {
            SketchSwitch(
                code = "numberKeyboardOptions()",
                checked = fix,
                onToggle = {
                    fix = !fix
                    note = if (fix) NumberPadSay.On else NumberPadSay.Off
                },
                tag = "sketch_numberKeyboardOptions"
            )
            SketchNote((note ?: NumberPadSay.Start).of(language))
        }
    }
}

/**
 * What pressing this key does to [field]: a digit or the separator is typed, delete takes one back, and the rest is
 * kept out, as the field keeps out what is no number. Says whether it was the action key, which sends the amount.
 */
private fun KeyCap.typeInto(field: TextFieldState): Boolean {
    when (label) {
        DONE -> return true
        DELETE -> field.edit { if (length > 0) replace(length - 1, length, "") }
        else -> if (label.all { it.isDigit() || it == '.' }) field.edit { append(label) }
    }
    return false
}

private const val DONE = "done"
private const val DELETE = "⌫"

/** An iPhone's number pad: ten digits, a separator and a key that takes one back, and no key for the action. */
private val NUMBER_PAD: List<List<KeyCap>> = listOf(
    keys("123"),
    keys("456"),
    keys("789"),
    listOf(KeyCap("."), KeyCap("0"), KeyCap(DELETE, letter = false))
)

/** The keyboard of numbers and punctuation: it opens on its digits, and its return key shows the action. */
private val NUMBERS_AND_PUNCTUATION: List<List<KeyCap>> = listOf(
    keys("1234567890"),
    keys("-/:;()$&@\""),
    listOf(KeyCap("#+=", 1.4f, letter = false)) + keys(".,?!'") + KeyCap(DELETE, 1.4f, letter = false),
    listOf(KeyCap("ABC", 1.3f, letter = false), KeyCap("space", 4.2f), KeyCap(DONE, 1.9f, letter = false))
)

/** The sketch's words, in each language. */
private object NumberPadSay {
    val App = Words("Courier", "Kurye")
    val Title = Words("A tip for the courier", "Kuryeye bahşiş")
    val Label = Words("Amount", "Tutar")
    val Start = Words("Tap the amount to type it.", "Yazmak için tutara dokun.")
    val Pad = Words(
        "The number pad: ten digits and a key that takes one back. Nothing sends the amount.",
        "Sayı tuş takımı: on rakam ve birini geri alan bir tuş. Tutarı hiçbir şey göndermiyor."
    )
    val Punctuation = Words(
        "The keyboard of numbers and punctuation: its return key is the field's action, done.",
        "Sayı ve noktalama klavyesi: dönüş tuşu alanın eylemi, bitti."
    )
    val Sent = Words("The action key sent the amount.", "Eylem tuşu tutarı gönderdi.")
    val On = Words(
        "The field asks for its keyboard with numberKeyboardOptions().",
        "Alan klavyesini numberKeyboardOptions() ile istiyor."
    )
    val Off = Words(
        "The field asks for KeyboardType.Decimal with ImeAction.Done, as Compose has it.",
        "Alan, Compose'un verdiği gibi ImeAction.Done ile KeyboardType.Decimal istiyor."
    )
    val Caption = Words(
        "Pantrix’s own text field and keyboard options, running on this page.",
        "Pantrix'in kendi metin alanı ve klavye seçenekleri, bu sayfada çalışıyor."
    )
}
