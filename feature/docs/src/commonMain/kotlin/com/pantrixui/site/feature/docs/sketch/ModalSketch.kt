package com.pantrixui.site.feature.docs.sketch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.pantrixui.components.container.surface.PantrixSurface
import com.pantrixui.components.data.listitem.ListItemStyle
import com.pantrixui.components.data.listitem.PantrixListItem
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.components.overlay.modal.ModalPosition
import com.pantrixui.components.overlay.modal.PantrixModal
import com.pantrixui.foundation.adaptive.BoxWithPantrixWindow
import com.pantrixui.foundation.adaptive.PantrixWindowFrame
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
 * The modal guide's sketch: a shop's search, opened over the phone's screen. With `PantrixModal` it stands at the
 * screen's top or in its middle over a scrim, and a press on the scrim or on a result closes it. With a plain `Popup`
 * it stands over the screen with no scrim, and none of the rest of a modal. The phone's frame is the modal's window.
 */
@Composable
internal fun ModalSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    var position by rememberSaveable { mutableStateOf(ModalPosition.Top) }
    var open by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf<String?>(null) }
    // The modal's own scrim is a rectangle, and the phone's screen is round at its corners: the sketch dims the
    // screen itself, inside its corners, and the modal's scrim only takes the press.
    val dim by animateFloatAsState(if (open && fix) 1f else 0f)
    val close = { open = false }
    val choose = { result: String ->
        chosen = result
        open = false
    }
    SketchFigure(caption = ModalSay.Caption.of(language)) {
        PhoneFrame {
            BoxWithPantrixWindow { window ->
                PantrixWindowFrame(window) {
                    SketchPhoneScreen(
                        app = ModalSay.App.of(language),
                        title = ModalSay.Title.of(language),
                        overlay = {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = dim }
                                    .background(PantrixTheme.colors.scrim, RoundedCornerShape(SCREEN_CORNER))
                            )
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PantrixButton(
                                text = ModalSay.Open.of(language),
                                onClick = { open = true },
                                variant = ButtonVariant.Outline,
                                fullWidth = true,
                                tagKey = "sketch_open"
                            )
                            PantrixText(
                                text = chosen ?: ModalSay.Nothing.of(language),
                                style = PantrixTheme.typography.footnote,
                                color = PantrixTheme.colors.textSecondary
                            )
                        }
                    }
                    if (fix) {
                        PantrixModal(
                            visible = open,
                            onDismissRequest = close,
                            position = position,
                            margin = PaddingValues(12.dp),
                            paneTitle = ModalSay.Search.of(language),
                            scrimColor = Color.Transparent,
                            tagKey = "sketch"
                        ) { SearchCard(onChoose = choose) }
                    } else if (open) {
                        Popup(
                            alignment = if (position == ModalPosition.Top) Alignment.TopCenter else Alignment.Center,
                            onDismissRequest = close,
                            properties = PopupProperties(focusable = true)
                        ) {
                            // A popup is measured against the whole window: it is given the phone's width.
                            SearchCard(onChoose = choose, modifier = Modifier.width(window.size.width))
                        }
                    }
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = ModalPosition.entries.map { it to it.name },
                selected = position,
                onSelect = {
                    position = it
                    open = false
                },
                tag = "sketch_position"
            )
            SketchSwitch(
                code = "PantrixModal",
                checked = fix,
                onToggle = {
                    fix = !fix
                    open = false
                },
                tag = "sketch_modal"
            )
            SketchNote(
                when {
                    !open -> ModalSay.Start
                    fix -> ModalSay.Done
                    else -> ModalSay.Bare
                }.of(language)
            )
        }
    }
}

/** The search the sketch opens: a field and three results, each closing it. [modifier] sets its width. */
@Composable
private fun SearchCard(
    onChoose: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    var query by remember { mutableStateOf("") }
    PantrixSurface(
        modifier.padding(horizontal = 12.dp).fillMaxWidth(),
        shape = PantrixTheme.shapes.lg,
        elevation = 8.dp
    ) {
        Column(Modifier.padding(10.dp)) {
            PantrixTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = ModalSay.Placeholder.of(language),
                contentDescription = ModalSay.Search.of(language),
                tagKey = "sketch_query"
            )
            ModalSay.Results.map { it.of(language) }
                .filter { it.contains(query, ignoreCase = true) }
                .forEachIndexed { index, result ->
                    PantrixListItem(
                        headline = result,
                        onClick = { onChoose(result) },
                        style = ListItemStyle(oneLineHeight = 36.dp, horizontalPadding = 8.dp),
                        tagKey = "sketch_result_$index"
                    )
                }
        }
    }
}

/** How round the phone's screen is inside its frame. */
private val SCREEN_CORNER = 28.dp

/** The sketch's words, in each language. */
private object ModalSay {
    val App = Words("Shop", "Mağaza")
    val Title = Words("Today's picks", "Bugünün seçtikleri")
    val Open = Words("Search the shop", "Mağazada ara")
    val Search = Words("Search", "Ara")
    val Placeholder = Words("A product", "Bir ürün")
    val Nothing = Words("Nothing chosen yet", "Henüz seçilen yok")
    val Results = listOf(
        Words("Rain jacket", "Yağmurluk"),
        Words("Running shoes", "Koşu ayakkabısı"),
        Words("Wool socks", "Yün çorap")
    )
    val Start = Words("Open the search, then close it.", "Aramayı aç, sonra kapat.")
    val Done = Words(
        "A scrim over the screen: a press on it, Escape or a result closes it, and the focus comes back.",
        "Ekranın üstünde bir perde: ona basmak, Escape ya da bir sonuç kapatır, odak da geri döner."
    )
    val Bare = Words(
        "A Popup is no modal: no scrim, no “Close” for a screen reader, and the focus is neither kept nor given back.",
        "Bir Popup modal değildir: perde yok, ekran okuyucuya “Kapat” yok, odak ne içinde tutulur ne geri verilir."
    )
    val Caption = Words(
        "Pantrix's modal layer, with the phone's frame for its window, running on this page.",
        "Pantrix'in modal katmanı, telefonun çerçevesini pencere sayarak, bu sayfada çalışıyor."
    )
}
