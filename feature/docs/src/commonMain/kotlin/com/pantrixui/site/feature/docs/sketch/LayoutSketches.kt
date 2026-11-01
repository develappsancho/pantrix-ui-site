@file:Suppress("MagicNumber") // A sketch's sample values, its times, amounts and sizes, are what it shows.

package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.container.card.CardVariant
import com.pantrixui.components.container.card.PantrixCard
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.securetextfield.PantrixSecureTextField
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.adaptive.PantrixWindow
import com.pantrixui.foundation.adaptive.byWidth
import com.pantrixui.foundation.adaptive.isNarrow
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.WindowFrame
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Words

/**
 * The adaptive guide's sketch: a window of the size chosen, told to its content as `PantrixTheme.window`, and a list
 * of orders laid out by its class: one column on a phone, more as the window widens, by `window.byWidth(…)`.
 */
@Composable
internal fun AdaptiveSketch() {
    val language = LocalLanguage.current
    var size by rememberSaveable { mutableStateOf(SketchWidth.Tablet) }
    SketchFigure(caption = LayoutSay.WindowCaption.of(language)) {
        WindowFrame(
            title = "${size.width.value.toInt()} × ${size.height.value.toInt()}",
            modifier = Modifier.widthIn(max = size.width).fillMaxWidth(),
            height = null
        ) {
            ScaledWindow(size.width, size.height) { window -> Orders(window, language) }
        }
        SketchControls {
            SketchChoice(
                options = SketchWidth.entries.map { it to it.label.of(language) },
                selected = size,
                onSelect = { size = it },
                tag = "sketch_window"
            )
            SketchNote(LayoutSay.ClassNote.of(language))
        }
    }
}

@Composable
private fun Orders(
    window: PantrixWindow,
    language: Language
) {
    SketchPlatform(if (window.isNarrow) PlatformType.Ios else PlatformType.Desktop) {
        val columns = window.byWidth(compact = 1, medium = 2, expanded = 3, large = 4)
        Column(
            Modifier.fillMaxSize().background(PantrixTheme.colors.background).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PantrixText(
                text = "widthClass = ${window.widthClass.name} · columns = $columns",
                style = PantrixTheme.typography.code,
                color = PantrixTheme.colors.primary.onContainer
            )
            ORDERS.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { number ->
                        PantrixCard(variant = CardVariant.Outlined, modifier = Modifier.weight(1f)) {
                            PantrixText(
                                text = "${LayoutSay.Order.of(language)} #$number",
                                style = PantrixTheme.typography.title
                            )
                            PantrixText(
                                text = LayoutSay.OrderLine.of(language),
                                color = PantrixTheme.colors.textSecondary
                            )
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/**
 * The adaptive usage guide's sketch: a sign-in screen, a screen of its own, that asks the window it is drawn in
 * whether it is narrow. A narrow window has the form alone; a wide one has the brand's panel beside it.
 */
@Composable
internal fun AdaptiveUsageSketch() {
    val language = LocalLanguage.current
    var size by rememberSaveable { mutableStateOf(SketchWidth.Tablet) }
    SketchFigure(caption = LayoutSay.WindowCaption.of(language)) {
        WindowFrame(
            title = "${size.width.value.toInt()} × ${size.height.value.toInt()}",
            modifier = Modifier.widthIn(max = size.width).fillMaxWidth(),
            height = null
        ) {
            ScaledWindow(size.width, size.height) { window -> SignIn(window, language) }
        }
        SketchControls {
            SketchChoice(
                options = SketchWidth.entries.map { it to it.label.of(language) },
                selected = size,
                onSelect = { size = it },
                tag = "sketch_window"
            )
            SketchNote(LayoutSay.NarrowNote.of(language))
        }
    }
}

@Composable
private fun SignIn(
    window: PantrixWindow,
    language: Language
) {
    SketchPlatform(if (window.isNarrow) PlatformType.Ios else PlatformType.Desktop) {
        Row(Modifier.fillMaxSize().background(PantrixTheme.colors.background)) {
            if (!window.isNarrow) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(PantrixTheme.colors.primary.container)
                        .padding(40.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    PantrixText(
                        text = LayoutSay.Brand.of(language),
                        style = PantrixTheme.typography.display,
                        color = PantrixTheme.colors.primary.onContainer
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(Modifier.widthIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PantrixText(text = LayoutSay.SignIn.of(language), style = PantrixTheme.typography.headline)
                    PantrixTextField(
                        state = rememberTextFieldState(),
                        label = LayoutSay.Email.of(language),
                        modifier = Modifier.fillMaxWidth(),
                        tagKey = "sketch_email"
                    )
                    PantrixSecureTextField(
                        state = rememberTextFieldState(),
                        label = LayoutSay.Password.of(language),
                        modifier = Modifier.fillMaxWidth(),
                        tagKey = "sketch_password"
                    )
                    PantrixButton(
                        text = LayoutSay.SignIn.of(language),
                        onClick = {},
                        fullWidth = true,
                        tagKey = "sketch_signIn"
                    )
                }
            }
        }
    }
}

/** The orders the sketch lists, by their numbers. */
private val ORDERS = (1..8).toList()

/** What each width's choice says. */
private val SketchWidth.label: Words
    get() = when (this) {
        SketchWidth.Phone -> Words("Phone", "Telefon")
        SketchWidth.Foldable -> Words("Foldable", "Katlanabilir")
        SketchWidth.Tablet -> Words("Tablet", "Tablet")
        SketchWidth.Desktop -> Words("Desktop", "Masaüstü")
    }

/** The layout sketches' words, in each language. */
private object LayoutSay {
    val Order = Words("Order", "Sipariş")
    val OrderLine = Words("3 items · on its way", "3 ürün · yolda")
    val ClassNote = Words(
        "The content asks the window it is drawn in, not the device: its class picks the columns.",
        "İçerik cihaza değil çizildiği pencereye soruyor: sınıfı sütunları seçiyor."
    )
    val NarrowNote = Words(
        "The screen asks window.isNarrow: the brand’s panel stands beside the form only where there is room.",
        "Ekran window.isNarrow'a soruyor: markanın paneli formun yanında yalnızca yer olduğunda duruyor."
    )
    val Brand = Words("Harbor", "Harbor")
    val SignIn = Words("Sign in", "Giriş yap")
    val Email = Words("E-mail", "E-posta")
    val Password = Words("Password", "Parola")
    val WindowCaption = Words(
        "Pantrix’s own window and components, running on this page in a window of the size chosen.",
        "Pantrix'in kendi penceresi ve bileşenleri, seçilen boyutta bir pencerede bu sayfada çalışıyor."
    )
}
