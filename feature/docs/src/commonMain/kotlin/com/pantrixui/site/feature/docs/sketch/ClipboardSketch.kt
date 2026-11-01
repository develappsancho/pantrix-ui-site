package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.container.card.CardVariant
import com.pantrixui.components.container.card.PantrixCard
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
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
 * The clipboard guide's sketch: a coupon and its copy button, on the platform chosen. With the answer `copyText()`
 * gives, the button says what happened there: copied, nothing where the system shows the copy itself, or that it
 * could not be copied on a page that is not secure. Without it, the button says "copied" whatever happened.
 */
@Composable
internal fun ClipboardSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    var platform by rememberSaveable { mutableStateOf(Where.Android13) }
    var pressed by remember { mutableStateOf(false) }
    val said = when {
        !pressed -> null
        !fix -> ClipboardSay.Copied
        !platform.writable -> ClipboardSay.Failed
        platform.announced -> null
        else -> ClipboardSay.Copied
    }
    SketchFigure(caption = ClipboardSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(
                app = ClipboardSay.App.of(language),
                title = ClipboardSay.Title.of(language),
                overlay = {
                    // What the system shows by itself, on Android 13 and later, whatever the app says.
                    if (pressed && platform.announced && platform.writable) {
                        PantrixText(
                            text = ClipboardSay.SystemPreview.of(language),
                            style = PantrixTheme.typography.footnote,
                            color = PantrixTheme.colors.textInverse,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 18.dp)
                                .background(PantrixTheme.colors.textPrimary, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PantrixCard(variant = CardVariant.Outlined, modifier = Modifier.fillMaxWidth()) {
                        PantrixText(
                            text = ClipboardSay.Coupon.of(language),
                            style = PantrixTheme.typography.footnote,
                            color = PantrixTheme.colors.textSecondary
                        )
                        PantrixText(text = "SPRING-24", style = PantrixTheme.typography.title)
                    }
                    PantrixButton(
                        text = ClipboardSay.Copy.of(language),
                        onClick = { pressed = true },
                        variant = ButtonVariant.Outline,
                        fullWidth = true,
                        tagKey = "sketch_copy"
                    )
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        said?.let { words ->
                            PantrixText(
                                text = words.of(language),
                                style = PantrixTheme.typography.label,
                                color = if (words == ClipboardSay.Failed) {
                                    PantrixTheme.colors.danger.color
                                } else {
                                    PantrixTheme.colors.success.color
                                }
                            )
                        }
                    }
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = Where.entries.map { it to it.label.of(language) },
                selected = platform,
                onSelect = {
                    platform = it
                    pressed = false
                },
                tag = "sketch_platform"
            )
            SketchSwitch(
                code = "copyText()",
                checked = fix,
                onToggle = {
                    fix = !fix
                    pressed = false
                },
                tag = "sketch_copyText"
            )
            SketchNote(
                when {
                    !pressed -> ClipboardSay.Start
                    !fix && !platform.writable -> ClipboardSay.Lied
                    !fix && platform.announced -> ClipboardSay.Twice
                    !platform.writable -> ClipboardSay.Refused
                    platform.announced -> ClipboardSay.Quiet
                    else -> ClipboardSay.Done
                }.of(language)
            )
        }
    }
}

/** Where the sketch's copy happens, and what that platform's clipboard does. */
private enum class Where(
    val label: Words,
    val writable: Boolean,
    val announced: Boolean
) {
    Android12(Words("Android 12", "Android 12"), writable = true, announced = false),
    Android13(Words("Android 13+", "Android 13+"), writable = true, announced = true),
    Http(Words("Web, http", "Web, http"), writable = false, announced = false)
}

/** The sketch's words, in each language. */
private object ClipboardSay {
    val App = Words("Wallet", "Cüzdan")
    val Title = Words("Your coupon", "Kuponunuz")
    val Coupon = Words("15% off, until Sunday", "Pazara dek %15 indirim")
    val Copy = Words("Copy the code", "Kodu kopyala")
    val Copied = Words("Copied", "Kopyalandı")
    val Failed = Words("Could not be copied", "Kopyalanamadı")
    val SystemPreview = Words("Copied to clipboard", "Panoya kopyalandı")
    val Start = Words("Press the button to copy the code.", "Kodu kopyalamak için düğmeye bas.")
    val Done = Words("The copy got there, and the button says so.", "Kopya yerine ulaştı ve düğme bunu söylüyor.")
    val Quiet = Words(
        "Android 13 shows the copy itself: the button says nothing, or the reader is told twice.",
        "Android 13 kopyayı kendisi gösterir: düğme bir şey demez, yoksa okura iki kez söylenir."
    )
    val Refused = Words(
        "A page that is not secure has no clipboard: copyText() answers false, and the button says so.",
        "Güvenli olmayan bir sayfanın panosu yoktur: copyText() false der, düğme de bunu söyler."
    )
    val Lied = Words(
        "Nothing was copied, and the button says it was.",
        "Hiçbir şey kopyalanmadı, düğme ise kopyalandı diyor."
    )
    val Twice = Words(
        "The system says it once, and the button a second time.",
        "Sistem bir kez söylüyor, düğme de ikinci kez."
    )
    val Caption = Words(
        "Pantrix’s own rule of what a copy button says, running on this page.",
        "Pantrix'in bir kopyalama düğmesinin ne diyeceğine dair kuralı, bu sayfada çalışıyor."
    )
}
