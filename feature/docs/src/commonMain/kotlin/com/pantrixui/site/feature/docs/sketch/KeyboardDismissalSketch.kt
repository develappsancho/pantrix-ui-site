package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.inputs.textarea.PantrixTextArea
import com.pantrixui.foundation.keyboard.clearFocusOnTap
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The keyboard guide's sketch: a phone whose screen is Pantrix's own, a text area and a button under it, and
 * `clearFocusOnTap()` on its root, turned on and off by the switch under it. A press in the field gives it the focus
 * and opens a keyboard drawn over the button; a tap on empty room puts it away only while the modifier is on.
 */
@Composable
internal fun KeyboardDismissalSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    var note by remember { mutableStateOf<Words?>(null) }
    val field = remember(language) { TextFieldState(DismissalSay.FieldText.of(language)) }
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    // Read when a press lands, before a frame has passed: a tap right after the field's own may come first.
    val fieldFocus = remember { FocusFlag() }
    LaunchedEffect(focused) { if (focused) note = DismissalSay.FieldTook }
    SketchFigure(caption = DismissalSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(
                app = DismissalSay.Delivery.of(language),
                title = DismissalSay.Title.of(language),
                modifier = Modifier
                    .then(if (fix) Modifier.clearFocusOnTap() else Modifier)
                    .roomTaps(fix, fieldFocus) { wasFocused ->
                        note = when {
                            !wasFocused -> DismissalSay.AlreadyAway
                            fix -> DismissalSay.Cleared
                            else -> DismissalSay.Stays
                        }
                    },
                overlay = {
                    // Where the keyboard, once open, covers it but for its top edge: the button cannot be reached.
                    PantrixButton(
                        text = DismissalSay.Send.of(language),
                        onClick = { note = DismissalSay.Sent },
                        fullWidth = true,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 14.dp, end = 14.dp, bottom = SEND_ABOVE_THE_EDGE),
                        tagKey = "sketch_send"
                    )
                    if (focused) {
                        DrawnKeyboard(LETTERS, onKey = { it.typeInto(field) }, Modifier.align(Alignment.BottomCenter))
                    }
                }
            ) {
                NoSystemKeyboard {
                    PantrixTextArea(
                        state = field,
                        label = DismissalSay.Label.of(language),
                        minLines = 3,
                        maxLines = 3,
                        resizable = false,
                        showCharacterCount = false,
                        interactionSource = interactions,
                        modifier = Modifier.onFocusChanged { fieldFocus.focused = it.hasFocus },
                        tagKey = "sketch_note"
                    )
                }
            }
        }
        SketchControls {
            SketchSwitch(
                code = "clearFocusOnTap()",
                checked = fix,
                onToggle = {
                    fix = !fix
                    note = if (fix) DismissalSay.On else DismissalSay.Off
                },
                tag = "sketch_clearFocusOnTap"
            )
            SketchNote((note ?: if (focused) DismissalSay.Open else DismissalSay.Closed).of(language))
        }
    }
}

/**
 * Hears a tap on empty room: a press that nothing under it took and that ends where it began. It stands inside the
 * modifier and hears the tap first, without taking it; without the modifier it takes it, so that nothing above the
 * sketch clears the focus in the modifier's place.
 */
@Composable
private fun Modifier.roomTaps(
    fix: Boolean,
    fieldFocus: FocusFlag,
    onRoomTap: (wasFocused: Boolean) -> Unit
): Modifier {
    val currentFix by rememberUpdatedState(fix)
    val currentTap by rememberUpdatedState(onRoomTap)
    return pointerInput(fieldFocus) {
        awaitEachGesture {
            awaitFirstDown()
            val wasFocused = fieldFocus.focused
            val up = waitForUpOrCancellation() ?: return@awaitEachGesture
            currentTap(wasFocused)
            if (!currentFix) up.consume()
        }
    }
}

/** What pressing a key of the letters' keyboard does to [field]: return starts a new line, as an iPhone's does. */
private fun KeyCap.typeInto(field: TextFieldState) {
    field.edit {
        when (label) {
            SPACE -> append(" ")
            RETURN -> append("\n")
            DELETE -> if (length > 0) replace(length - 1, length, "")
            SHIFT, NUMBERS -> Unit
            else -> append(label)
        }
    }
}

/** How far above the screen's bottom edge the button stands: the keyboard's height, less a sliver of the button. */
private val SEND_ABOVE_THE_EDGE = 128.dp

private const val SHIFT = "⇧"
private const val DELETE = "⌫"
private const val NUMBERS = "123"
private const val SPACE = "space"
private const val RETURN = "return"

/** An iPhone's letters, as the design draws them. */
private val LETTERS: List<List<KeyCap>> = listOf(
    keys("qwertyuiop"),
    keys("asdfghjkl"),
    listOf(KeyCap(SHIFT, 1.4f, letter = false)) + keys("zxcvbnm") + KeyCap(DELETE, 1.4f, letter = false),
    listOf(KeyCap(NUMBERS, 1.3f, letter = false), KeyCap(SPACE, 4.2f), KeyCap(RETURN, 1.9f, letter = false))
)

/** The sketch's words, in each language. */
private object DismissalSay {
    val Delivery = Words("Delivery", "Teslimat")
    val Title = Words("A note for the courier", "Kuryeye bir not")
    val Label = Words("Note to the courier", "Kuryeye not")
    val FieldText = Words("Leave it at the door, please.", "Lütfen kapıya bırakın.")
    val Send = Words("Send", "Gönder")
    val Open = Words(
        "The keyboard is open over Send. Tap the empty room above the field.",
        "Klavye Gönder'in üstünde açık. Alanın üstündeki boş yere dokun."
    )
    val Closed = Words("Tap the field, then the empty room above it.", "Alana, sonra üstündeki boş yere dokun.")
    val FieldTook = Words(
        "The field took the tap: the keyboard opens, and its bottom-right key starts a new line.",
        "Dokunuşu alan aldı: klavye açılır, sağ alttaki tuşu yeni satır açar."
    )
    val AlreadyAway = Words(
        "The keyboard is already away. Tap the field first.",
        "Klavye zaten kapalı. Önce alana dokun."
    )
    val Cleared = Words(
        "Nothing took the tap: clearFocus() ran, and the keyboard went.",
        "Dokunuşu hiçbir şey almadı: clearFocus() çalıştı, klavye gitti."
    )
    val Stays = Words(
        "Nothing took the tap, and nothing clears the focus: the keyboard stays over Send.",
        "Dokunuşu hiçbir şey almadı, odağı bırakan da yok: klavye Gönder'in üstünde kalıyor."
    )
    val Sent = Words("Send took the tap, and did its work.", "Dokunuşu Gönder aldı ve işini yaptı.")
    val On = Words("The modifier is on the screen’s root.", "Değiştirici ekranın kökünde.")
    val Off = Words("The screen has no modifier at its root.", "Ekranın kökünde değiştirici yok.")
    val Caption = Words(
        "Pantrix’s own text area and modifier, running on this page.",
        "Pantrix'in kendi metin alanı ve değiştiricisi, bu sayfada çalışıyor."
    )
}
