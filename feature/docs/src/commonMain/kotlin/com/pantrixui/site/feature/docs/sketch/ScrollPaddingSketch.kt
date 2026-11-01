package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.divider.PantrixHorizontalDivider
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.a11y.scrollPadding
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The scroll padding guide's sketch: an address form that scrolls under a bar at its foot, and a button that moves the
 * keyboard's focus on to the next field, as Tab does. Compose brings the field into the scroller's view, and the bar
 * is in that view: without `Modifier.scrollPadding` a field can land behind it.
 */
@Composable
internal fun ScrollPaddingSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    var current by remember { mutableIntStateOf(-1) }
    val requesters = remember { List(ScrollPaddingSay.Fields.size) { FocusRequester() } }
    val states = remember { List(ScrollPaddingSay.Fields.size) { TextFieldState() } }
    SketchFigure(caption = ScrollPaddingSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(
                app = ScrollPaddingSay.App.of(language),
                title = ScrollPaddingSay.Title.of(language),
                overlay = {
                    Column(
                        Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(PantrixTheme.colors.surface)
                    ) {
                        PantrixHorizontalDivider()
                        PantrixButton(
                            text = ScrollPaddingSay.Pay.of(language),
                            onClick = {},
                            fullWidth = true,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            tagKey = "sketch_pay"
                        )
                    }
                }
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .then(if (fix) Modifier.scrollPadding(PaddingValues(bottom = BAR)) else Modifier)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScrollPaddingSay.Fields.forEachIndexed { index, label ->
                        NoSystemKeyboard {
                            PantrixTextField(
                                state = states[index],
                                label = label.of(language),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(requesters[index])
                                    .onFocusChanged { if (it.hasFocus) current = index },
                                tagKey = "sketch_field_$index"
                            )
                        }
                    }
                    Box(Modifier.height(BAR))
                }
            }
        }
        SketchControls {
            PantrixButton(
                text = ScrollPaddingSay.Next.of(language),
                onClick = { requesters[(current + 1).mod(requesters.size)].requestFocus() },
                variant = ButtonVariant.Outline,
                tagKey = "sketch_nextField"
            )
            SketchSwitch(code = "Modifier.scrollPadding()", checked = fix, onToggle = {
                fix = !fix
            }, tag = "sketch_scrollPadding")
            SketchNote((if (fix) ScrollPaddingSay.On else ScrollPaddingSay.Off).of(language))
        }
    }
}

/** How tall the bar at the screen's foot is: what the scroller is told it covers. */
private val BAR = 64.dp

/** The sketch's words, in each language. */
private object ScrollPaddingSay {
    val App = Words("Shop", "Mağaza")
    val Title = Words("Where to send it", "Nereye gönderelim")
    val Pay = Words("Pay", "Öde")
    val Next = Words("Next field", "Sonraki alan")
    val Fields = listOf(
        Words("Name", "Ad"),
        Words("Street", "Sokak"),
        Words("Building", "Bina"),
        Words("City", "Şehir"),
        Words("Postcode", "Posta kodu"),
        Words("Phone", "Telefon")
    )
    val On = Words(
        "The scroller knows the bar covers its foot: each field it brings into view stands above the bar.",
        "Kaydırıcı, ayağını çubuğun örttüğünü biliyor: görünüre getirdiği her alan çubuğun üstünde duruyor."
    )
    val Off = Words(
        "The scroller counts the bar as room: a field with the focus can stand behind it, out of sight.",
        "Kaydırıcı çubuğu yer sayıyor: odaktaki alan onun arkasında, görünmeden durabilir."
    )
    val Caption = Words(
        "Pantrix’s own text fields and scroll padding, running on this page.",
        "Pantrix'in kendi metin alanları ve kaydırma payı, bu sayfada çalışıyor."
    )
}
