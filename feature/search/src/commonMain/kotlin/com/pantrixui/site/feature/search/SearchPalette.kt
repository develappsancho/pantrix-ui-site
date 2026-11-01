package com.pantrixui.site.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.pantrixui.foundation.overlay.PantrixPortal
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.SearchEntry
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The site's search, over the page it was opened on: a palette in the middle of a desktop's and a tablet's window
 * over a scrim, the whole screen on a phone. It opens afresh each time, in the site's language.
 *
 * @param onDismiss what closing it does: the escape key, its cancel, a press on the scrim.
 * @param onOpen what opening a result does, given it and the language it was found in.
 */
@Composable
fun SearchRoute(
    onDismiss: () -> Unit,
    onOpen: (SearchEntry, Language) -> Unit
) {
    val language = LocalLanguage.current
    val viewModel = koinViewModel<SearchViewModel>(key = "search") { parametersOf(language) }
    LaunchedEffect(viewModel) { viewModel.onAction(SearchAction.Open(language)) }
    // A layer of its own over the whole window, which takes the keys and, on Android, the back button.
    PantrixPortal {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = false)
        ) {
            BaseScreen(viewModel) { state, onAction -> SearchPalette(state, onAction, onDismiss, onOpen) }
        }
    }
}

/**
 * The search drawn from its [state]: what is typed, the filters and the results' language, the results, and how to
 * move among them.
 *
 * @param state what the search's model holds.
 * @param onAction where what the reader does goes.
 * @param onDismiss what closing it does.
 * @param onOpen what opening a result does.
 */
@Composable
fun SearchPalette(
    state: SearchUiState,
    onAction: (SearchAction) -> Unit,
    onDismiss: () -> Unit,
    onOpen: (SearchEntry, Language) -> Unit
) {
    val layout = SiteTheme.layout
    val entries = state.index.valueOrNull?.entries.orEmpty()
    val groups =
        remember(entries, state.query, state.kind, layout) { searchGroups(entries, state.query, state.kind, layout) }
    val counts = remember(entries, state.query) { searchCounts(entries, state.query) }
    val rows = groups.flatMap { it.rows }
    val selected = state.selected.coerceIn(0, (rows.size - 1).coerceAtLeast(0))
    // The search opens afresh: its field starts empty, and the model's query follows what is typed in it, never the
    // other way round, so that a query of the last time is not typed again before the model has forgotten it.
    val query = rememberTextFieldState()
    val focus = remember { FocusRequester() }
    val list = rememberLazyListState()
    LaunchedEffect(query) { snapshotFlow { query.text.toString() }.collect { onAction(SearchAction.Type(it)) } }
    LaunchedEffect(focus) { focus.requestFocus() }
    val open = { rows.getOrNull(selected)?.let { onOpen(it.entry, state.language) } }
    val keys = Modifier.onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when (event.key) {
            Key.Escape -> onDismiss()
            Key.DirectionDown -> onAction(SearchAction.Move(1, rows.size))
            Key.DirectionUp -> onAction(SearchAction.Move(-1, rows.size))
            Key.Enter, Key.NumPadEnter -> open()
            else -> return@onPreviewKeyEvent false
        }
        true
    }
    val content: @Composable ColumnScope.() -> Unit = {
        QueryRow(query, focus, onDismiss)
        Filters(state, counts, onAction)
        Results(
            state,
            groups,
            selected,
            list,
            when (layout) {
                SiteLayout.Desktop -> Modifier.heightIn(max = 560.dp)
                SiteLayout.Tablet -> Modifier.heightIn(max = 760.dp)
                SiteLayout.Phone -> Modifier.weight(1f)
            },
            onAction
        ) { onOpen(it, state.language) }
        Footer(state, onAction)
    }
    val title = SearchSay.Dialog.of(LocalLanguage.current)
    if (layout == SiteLayout.Phone) {
        Column(
            Modifier
                .fillMaxSize()
                .background(PantrixTheme.colors.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .semantics { paneTitle = title }
                .then(keys)
        ) { content() }
        return
    }
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier
            .fillMaxSize()
            .background(PantrixTheme.colors.scrim)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onDismiss
            )
            .padding(start = 16.dp, end = 16.dp, top = if (layout == SiteLayout.Desktop) 88.dp else 72.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier
                .widthIn(max = if (layout == SiteLayout.Desktop) 660.dp else 680.dp)
                .fillMaxWidth()
                .shadow(DIALOG_SHADOW, shape)
                .clip(shape)
                .background(PantrixTheme.colors.surface)
                .border(1.dp, SiteTheme.colors.hairline, shape)
                // A press inside the palette is its own, not the scrim's.
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .semantics { paneTitle = title }
                .then(keys)
        ) { content() }
    }
}

/** How far the palette stands off the page. */
private val DIALOG_SHADOW = 28.dp
