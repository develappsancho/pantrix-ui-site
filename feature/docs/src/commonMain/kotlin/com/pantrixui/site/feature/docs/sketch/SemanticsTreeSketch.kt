package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Words

/**
 * The semantics tree guide's sketch: a list of two hundred orders, each a number and its amount, in a column or in a
 * lazy list, each line merged into one node or not. Under it, what iOS goes through at every change while something
 * reads the screen's accessibility tree: the lines composed, the nodes walked and the elements made of them.
 */
@Composable
internal fun SemanticsTreeSketch() {
    val language = LocalLanguage.current
    var lazy by rememberSaveable { mutableStateOf(false) }
    var merged by rememberSaveable { mutableStateOf(false) }
    val list = rememberLazyListState()
    val composed = if (lazy) list.layoutInfo.visibleItemsInfo.size else ORDERS
    // A line is a row and its two texts, three nodes; merged, the row is the one element of them.
    val nodes = 1 + composed * NODES_A_LINE
    val elements = if (merged) composed else composed * TEXTS_A_LINE
    SketchFigure(caption = TreeSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(app = TreeSay.App.of(language), title = TreeSay.Title.of(language)) {
                if (lazy) {
                    LazyColumn(state = list, modifier = Modifier.fillMaxSize()) {
                        items(ORDERS) { OrderLine(it, merged, language) }
                    }
                } else {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        repeat(ORDERS) { OrderLine(it, merged, language) }
                    }
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = listOf(false to "Column", true to "LazyColumn"),
                selected = lazy,
                onSelect = { lazy = it },
                tag = "sketch_list"
            )
            SketchSwitch(
                code = "mergeDescendants",
                checked = merged,
                onToggle = { merged = !merged },
                tag = "sketch_merge"
            )
            SketchNote(TreeSay.counts(composed, nodes, elements).of(language))
        }
    }
}

/** One order of the list: its number and its amount, one node where [merged]. */
@Composable
private fun OrderLine(
    index: Int,
    merged: Boolean,
    language: Language
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .then(if (merged) Modifier.semantics(mergeDescendants = true) {} else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PantrixText(text = "${TreeSay.Order.of(language)} #${FIRST_ORDER + index}")
        PantrixText(
            text = "${(index * AMOUNT_STEP) % AMOUNT_RANGE + AMOUNT_FLOOR}.00",
            color = PantrixTheme.colors.textSecondary
        )
    }
}

private object TreeSay {
    val App = Words("Shop", "Mağaza")
    val Title = Words("Orders", "Siparişler")
    val Order = Words("Order", "Sipariş")
    val Caption = Words(
        "What iOS goes through at every change, a scroll or an animation, while the tree is read.",
        "Ağaç okunurken her değişiklikte, bir kaydırmada ya da bir animasyonda, iOS'un geçtiği yol."
    )

    fun counts(
        composed: Int,
        nodes: Int,
        elements: Int
    ) = Words(
        "$composed lines composed · $nodes nodes walked · $elements elements made",
        "$composed satır kurulu · $nodes düğüm dolaşılıyor · $elements öğe kuruluyor"
    )
}

private const val ORDERS = 200
private const val NODES_A_LINE = 3
private const val TEXTS_A_LINE = 2
private const val FIRST_ORDER = 1000
private const val AMOUNT_STEP = 37
private const val AMOUNT_RANGE = 900
private const val AMOUNT_FLOOR = 100
