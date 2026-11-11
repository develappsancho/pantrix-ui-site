package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixPreviewTheme
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words
import kotlin.math.roundToInt

/**
 * The previews guide's sketch: one screen drawn in `PantrixPreviewTheme` on the platform chosen, in the light or
 * the dark. The same code draws a taller button where a finger presses it than where a pointer does: the note says
 * how tall it was measured, as a preview of each platform would show it.
 */
@Composable
internal fun PreviewsSketch() {
    val language = LocalLanguage.current
    var platform by rememberSaveable { mutableStateOf(PlatformType.Android) }
    var dark by rememberSaveable { mutableStateOf(false) }
    var buttonPx by remember { mutableIntStateOf(0) }
    val buttonDp = with(LocalDensity.current) { buttonPx.toDp().value.roundToInt() }
    SketchFigure(caption = PreviewsSay.Caption.of(language)) {
        PhoneFrame {
            PantrixPreviewTheme(platform = platform, dark = dark) {
                Box(Modifier.fillMaxSize().background(PantrixTheme.colors.background).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PantrixText(text = PreviewsSay.Title.of(language), style = PantrixTheme.typography.title)
                        PantrixText(text = PreviewsSay.Line.of(language), color = PantrixTheme.colors.textSecondary)
                        PantrixButton(
                            text = PreviewsSay.Button.of(language),
                            onClick = {},
                            fullWidth = true,
                            modifier = Modifier.onSizeChanged { buttonPx = it.height },
                            tagKey = "sketch_preview_button"
                        )
                    }
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = PREVIEWED.map { (type, label) -> type to label.of(language) },
                selected = platform,
                onSelect = { platform = it },
                tag = "sketch_preview_platform"
            )
            SketchSwitch(code = "dark = true", checked = dark, onToggle = { dark = !dark }, tag = "sketch_preview_dark")
            val shown = PREVIEWED.first { it.first == platform }.second.of(language)
            SketchNote(PreviewsSay.measured(shown, buttonDp).of(language))
        }
    }
}

/** The platforms the sketch draws its screen on. */
private val PREVIEWED = listOf(
    PlatformType.Android to Words("Android", "Android"),
    PlatformType.Ios to Words("iOS", "iOS"),
    PlatformType.Desktop to Words("Desktop", "Masaüstü")
)

private object PreviewsSay {
    val Title = Words("Checkout", "Ödeme")
    val Line = Words("Three items, delivered tomorrow.", "Üç ürün, yarın teslim.")
    val Button = Words("Pay now", "Şimdi öde")
    val Caption = Words(
        "One preview, drawn as the platform and the theme would draw it.",
        "Tek bir önizleme, platformun ve temanın çizeceği gibi."
    )

    fun measured(
        platform: String,
        height: Int
    ) = Words(
        "On $platform the button is $height dp tall.",
        "Düğmenin yüksekliği, $platform: $height dp."
    )
}
