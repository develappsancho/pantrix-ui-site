@file:Suppress("MagicNumber") // A sketch's sample values, its times, amounts and sizes, are what it shows.

package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.switchbutton.PantrixSwitchButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.OnPlatform
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The system bars guide's sketch: an iPhone in the system's light mode, and an app with a dark theme switch of its own.
 * With `followSystemBars` the status bar and the home indicator follow the app's choice; without it they stay in the
 * system's light mode, dark marks on a dark app that cannot be read.
 */
@Composable
internal fun SystemBarsSketch() {
    val language = LocalLanguage.current
    var follow by rememberSaveable { mutableStateOf(true) }
    var dark by rememberSaveable { mutableStateOf(true) }
    // The system is light: what its bars draw in, unless they follow the app.
    val barsDark = follow && dark
    val marks = if (barsDark) Color.White else Color.Black
    SketchFigure(caption = SystemBarsSay.Caption.of(language)) {
        PhoneFrame {
            OnPlatform(PlatformType.Ios) {
                PantrixTheme(darkTheme = dark, followSystemBars = false) {
                    Box(Modifier.fillMaxSize().background(PantrixTheme.colors.background)) {
                        Column(Modifier.fillMaxSize()) {
                            StatusBar(marks)
                            Column(
                                Modifier.padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PantrixText(
                                    text = SystemBarsSay.Title.of(language),
                                    style = SiteTheme.type.body.sized(16.sp).copy(fontWeight = FontWeight.ExtraBold),
                                    color = PantrixTheme.colors.textPrimary
                                )
                                PantrixSwitchButton(
                                    checked = dark,
                                    onCheckedChange = { dark = it },
                                    label = SystemBarsSay.Dark.of(language),
                                    tagKey = "sketch_dark"
                                )
                            }
                        }
                        // The home indicator, in the bars' tone.
                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .width(96.dp)
                                .height(4.dp)
                                .background(marks, CircleShape)
                        )
                    }
                }
            }
        }
        SketchControls {
            SketchSwitch(code = "followSystemBars", checked = follow, onToggle = {
                follow = !follow
            }, tag = "sketch_followSystemBars")
            SketchNote(
                when {
                    !dark -> SystemBarsSay.Light
                    follow -> SystemBarsSay.Followed
                    else -> SystemBarsSay.Lost
                }.of(language)
            )
        }
    }
}

/** The status line the system draws: the time, the signal and the battery, in [marks]. */
@Composable
private fun StatusBar(marks: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 14.dp)
            .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = "9:41",
            style = SiteTheme.type.body.sized(12.sp).copy(fontWeight = FontWeight.ExtraBold),
            color = marks
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
            listOf(4, 6, 8, 10).forEach { height ->
                Box(Modifier.width(3.dp).height(height.dp).background(marks, RoundedCornerShape(1.dp)))
            }
            Box(
                Modifier
                    .padding(start = 4.dp)
                    .size(width = 20.dp, height = 10.dp)
                    .background(marks, RoundedCornerShape(3.dp))
            )
        }
    }
}

/** The sketch's words, in each language. */
private object SystemBarsSay {
    val Title = Words("Appearance", "Görünüm")
    val Dark = Words("Dark theme", "Koyu tema")
    val Light = Words(
        "The app is light, as the system is: the bars read either way. Turn the app’s dark theme on.",
        "Uygulama sistem gibi açık: çubuklar her türlü okunuyor. Uygulamanın koyu temasını aç."
    )
    val Followed = Words(
        "The app is dark and the system light: the status bar and the home indicator follow the app.",
        "Uygulama koyu, sistem açık: durum çubuğu ve ana ekran çizgisi uygulamayı izliyor."
    )
    val Lost = Words(
        "The bars stay in the system’s light mode: dark marks on a dark app, that cannot be read.",
        "Çubuklar sistemin açık modunda kalıyor: koyu bir uygulamada okunmayan koyu işaretler."
    )
    val Caption = Words(
        "Pantrix’s own theme and switch, running on this page; the phone’s bars are drawn.",
        "Pantrix'in kendi teması ve anahtarı bu sayfada çalışıyor; telefonun çubukları çizildi."
    )
}
