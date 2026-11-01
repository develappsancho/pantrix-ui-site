package com.pantrixui.site.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.components.inputs.textfield.TextFieldColors
import com.pantrixui.components.inputs.textfield.TextFieldStyle
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.typography.TypeRole
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.Hairline
import com.pantrixui.site.core.designsystem.inCapitals
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.SearchEntry
import com.pantrixui.site.core.model.SearchKind

/** Where the search is typed: a desktop's with the escape key's mark, a tablet's and a phone's with their cancel. */
@Composable
internal fun QueryRow(
    query: TextFieldState,
    focus: FocusRequester,
    onDismiss: () -> Unit
) {
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    if (layout == SiteLayout.Phone) {
        Row(
            Modifier.fillMaxWidth().padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(colors.surface, RoundedCornerShape(12.dp))
                    .border(2.dp, colors.primary.color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PantrixIcon(
                    imageVector = PantrixIcons.Search,
                    contentDescription = null,
                    tint = SiteTheme.colors.muted,
                    size = 18.dp
                )
                QueryField(query, focus, Modifier.weight(1f))
            }
            CancelButton(onDismiss)
        }
        Hairline()
        return
    }
    val desktop = layout == SiteLayout.Desktop
    Row(
        Modifier.fillMaxWidth().height(
            if (desktop) 60.dp else 64.dp
        ).padding(start = 18.dp, end = if (desktop) 16.dp else 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixIcon(
            imageVector = PantrixIcons.Search,
            contentDescription = null,
            tint = SiteTheme.colors.muted,
            size = 20.dp
        )
        QueryField(query, focus, Modifier.weight(1f))
        if (desktop) Key(SearchSay.ESC, 11.5.sp) else CancelButton(onDismiss)
    }
    Hairline()
}

/** The field the search is typed in: no box of its own, in a larger type, the cursor in it as the search opens. */
@Composable
private fun QueryField(
    query: TextFieldState,
    focus: FocusRequester,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val type = PantrixTheme.typography
    val clear = Color.Transparent
    // The field types in its style's role: here the callout, set as large as the design's search.
    PantrixTheme(
        typography = type.copy(
            callout = type.callout.copy(
                fontSize = if (layout ==
                    SiteLayout.Phone
                ) {
                    16.sp
                } else {
                    17.sp
                },
                fontWeight = FontWeight.SemiBold
            )
        )
    ) {
        PantrixTextField(
            state = query,
            modifier = modifier.focusRequester(focus),
            placeholder = SearchSay.Placeholder.of(layout).of(language),
            style = TextFieldStyle(
                height = if (layout == SiteLayout.Tablet) 44.dp else 40.dp,
                horizontalPadding = 0.dp,
                borderWidth = 0.dp,
                focusedBorderWidth = 0.dp,
                textRole = TypeRole.Callout
            ),
            colors = TextFieldColors(
                container = clear,
                border = clear,
                hoveredBorder = clear,
                focusedBorder = clear,
                placeholder = SiteTheme.colors.muted
            ),
            contentDescription = SearchSay.Placeholder.desktop.of(language),
            tagKey = "search"
        )
    }
}

/** A tablet's and a phone's way out of the search, in the brand's colour. */
@Composable
private fun CancelButton(onDismiss: () -> Unit) {
    val language = LocalLanguage.current
    val shape = RoundedCornerShape(10.dp)
    PantrixText(
        text = SearchSay.Cancel.of(language),
        style = SiteTheme.type.button.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
        color = PantrixTheme.colors.primary.onContainer,
        modifier = Modifier
            .siteClickable(tag = "searchCancel", role = Role.Button, shape = shape, onClick = onDismiss)
            .semantics { contentDescription = SearchSay.Close.of(language) }
            .height(if (SiteTheme.layout == SiteLayout.Phone) 48.dp else 44.dp)
            .wrapContentHeight()
            .padding(horizontal = if (SiteTheme.layout == SiteLayout.Phone) 10.dp else 12.dp)
    )
}

/** A key's mark, as a footer and the escape key show one. */
@Composable
private fun Key(
    text: String,
    size: TextUnit = 12.sp
) {
    val colors = PantrixTheme.colors
    PantrixText(
        text = text,
        style = SiteTheme.type.bodySmall.copy(fontSize = size, fontWeight = FontWeight.Bold),
        color = SiteTheme.colors.muted,
        modifier = Modifier
            .background(colors.surface, RoundedCornerShape(6.dp))
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .padding(horizontal = if (size > 12.sp) 5.dp else 7.dp, vertical = 1.dp)
    )
}

/** The filters, a kind each and all of them, with what each finds; a desktop's and a tablet's results' language. */
@Composable
internal fun Filters(
    state: SearchUiState,
    counts: Map<SearchKind?, Int>,
    onAction: (SearchAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val typed = state.query.isNotBlank()
    val buttons = @Composable {
        FilterButton(
            SearchSay.All.of(language),
            counts[null].takeIf {
                typed
            },
            state.kind == null,
            "searchFilter_all"
        ) {
            onAction(SearchAction.PickKind(null))
        }
        KINDS.forEach { kind ->
            FilterButton(
                SearchSay.kindName(kind).of(language),
                counts[kind].takeIf { typed },
                state.kind == kind,
                "searchFilter_${kind.name.lowercase()}"
            ) { onAction(SearchAction.PickKind(kind)) }
        }
    }
    if (layout == SiteLayout.Phone) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(
                rememberScrollState()
            ).padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) { buttons() }
    } else {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Desktop) 6.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            buttons()
            Spacer(Modifier.weight(1f))
            LanguageChoice(state.language) { onAction(SearchAction.PickLanguage(it)) }
        }
    }
    Hairline()
}

/** A filter: its name and, once something is typed, how many it finds; tinted when it is on. */
@Composable
private fun FilterButton(
    name: String,
    count: Int?,
    on: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val desktop = layout == SiteLayout.Desktop
    val shape = RoundedCornerShape(if (desktop) 8.dp else 10.dp)
    val ink = if (on) colors.primary.onContainer else colors.textSecondary
    val size = if (desktop) 12.5.sp else 13.5.sp
    Row(
        Modifier
            .height(
                when (layout) {
                    SiteLayout.Desktop -> 28.dp
                    SiteLayout.Tablet -> 36.dp
                    SiteLayout.Phone -> 38.dp
                }
            )
            .background(if (on) colors.primary.container else Color.Transparent, shape)
            .border(1.dp, if (on) SiteTheme.colors.accentLine else colors.border, shape)
            .semantics { selected = on }
            .siteClickable(tag = tag, role = Role.Button, shape = shape, onClick = onClick)
            .padding(horizontal = if (desktop) 10.dp else 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = name,
            style = SiteTheme.type.button.copy(fontSize = size, fontWeight = FontWeight.Bold),
            color = ink,
            maxLines = 1
        )
        if (count != null) {
            PantrixText(
                text = "$count",
                style = SiteTheme.type.button.copy(fontSize = size, fontWeight = FontWeight.SemiBold),
                color = ink,
                modifier = Modifier.alpha(COUNT_ALPHA)
            )
        }
    }
}

/** The language the pages are searched in: a button for each, the one on show raised. */
@Composable
private fun LanguageChoice(
    chosen: Language,
    onPick: (Language) -> Unit
) {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    val colors = PantrixTheme.colors
    Row(
        Modifier
            .background(SiteTheme.colors.hover, RoundedCornerShape(10.dp))
            .padding(3.dp)
            .semantics { contentDescription = SearchSay.ResultsLanguage.of(language) },
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Language.entries.forEach { each ->
            val on = each == chosen
            val (short, name) = SearchSay.languageName(each)
            val shape = RoundedCornerShape(8.dp)
            PantrixText(
                text = short,
                style = SiteTheme.type.button.copy(fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold),
                color = if (on) colors.textPrimary else SiteTheme.colors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(if (on) colors.surface else Color.Transparent, shape)
                    .semantics {
                        selected = on
                        contentDescription = name.of(language)
                    }
                    .siteClickable(tag = "searchLanguage_${each.code}", role = Role.Button, shape = shape) {
                        onPick(each)
                    }
                    .defaultMinSize(minWidth = if (phone) 48.dp else 40.dp)
                    .height(if (phone) 36.dp else 30.dp)
                    .wrapContentHeight()
                    .padding(horizontal = 10.dp)
            )
        }
    }
}

/**
 * The results, kind by kind under their headings, the one the arrows are on tinted and kept in view; what the search
 * says where it finds nothing.
 */
@Composable
internal fun Results(
    state: SearchUiState,
    groups: List<SearchGroup>,
    selected: Int,
    list: LazyListState,
    modifier: Modifier,
    onAction: (SearchAction) -> Unit,
    onOpen: (SearchEntry) -> Unit
) {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    // Where each result stands among the list's items: after its group's heading and those of the groups before it.
    val itemOf = groups.runningFold(0) { at, group -> at + 1 + group.rows.size }.let { starts ->
        groups.flatMapIndexed { index, group -> group.rows.indices.map { starts[index] + 1 + it } }
    }
    LaunchedEffect(selected, groups) {
        val item = itemOf.getOrNull(selected) ?: return@LaunchedEffect
        val visible = list.layoutInfo.visibleItemsInfo
        val fully = visible.filter {
            it.offset >= 0 && it.offset + it.size <= list.layoutInfo.viewportEndOffset
        }.map { it.index }
        if (item !in fully) list.animateScrollToItem((item - 1).coerceAtLeast(0))
    }
    LazyColumn(
        modifier,
        state = list,
        contentPadding = if (phone) {
            PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 4.dp,
                bottom = 16.dp
            )
        } else {
            PaddingValues(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 10.dp)
        }
    ) {
        var place = 0
        groups.forEach { group ->
            item(key = "group_${group.kind}") { GroupHeading(group.kind) }
            group.rows.forEach { row ->
                val at = place++
                item(key = "row_${group.kind}_${row.entry.path}_${row.entry.anchor}_${row.entry.title}") {
                    ResultRow(row, at, at == selected, { onAction(SearchAction.Point(at)) }) { onOpen(row.entry) }
                }
            }
        }
        if (groups.isEmpty() && state.query.isNotBlank() && state.index is LoadState.Loaded) {
            item(key = "nothing") { NothingFound(state.query) }
        }
    }
}

/** A group's heading, in small capitals. */
@Composable
private fun GroupHeading(kind: SearchKind?) {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    PantrixText(
        text = (kind?.let { SearchSay.kindName(it) } ?: SearchSay.Popular).of(language).inCapitals(),
        style = SiteTheme.type.bodySmall.copy(
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = HEADING_SPACING
        ),
        color = SiteTheme.colors.muted,
        modifier = Modifier.padding(
            start = if (phone) 8.dp else 10.dp,
            end = 10.dp,
            top = if (phone) 12.dp else 10.dp,
            bottom = 6.dp
        )
    )
}

/** A result: its kind's square, its title with the word typed marked, and what it is; it opens its page. */
@Composable
private fun ResultRow(
    row: SearchRow,
    place: Int,
    selected: Boolean,
    onPoint: () -> Unit,
    onOpen: () -> Unit
) {
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val entry = row.entry
    val shape = RoundedCornerShape(10.dp)
    val (markSize, titleSize, subSize) = when (layout) {
        SiteLayout.Desktop -> Triple(32.dp, 14.5.sp, 12.5.sp)
        SiteLayout.Tablet -> Triple(34.dp, 15.sp, 12.5.sp)
        SiteLayout.Phone -> Triple(36.dp, 15.5.sp, 13.sp)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeightOf(layout))
            .background(
                if (selected &&
                    layout != SiteLayout.Phone
                ) {
                    SiteTheme.colors.hover
                } else {
                    Color.Transparent
                },
                shape
            )
            .pointerInput(onPoint) {
                awaitPointerEventScope {
                    while (true) {
                        if (awaitPointerEvent().type == PointerEventType.Enter) onPoint()
                    }
                }
            }
            .semantics { this.selected = selected }
            .siteClickable(tag = "searchResult_$place", role = Role.Button, shape = shape, onClick = onOpen)
            .padding(
                horizontal = if (layout == SiteLayout.Phone) 8.dp else 10.dp,
                vertical = when (layout) {
                    SiteLayout.Desktop -> 9.dp
                    SiteLayout.Tablet -> 8.dp
                    SiteLayout.Phone -> 6.dp
                }
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Mark(entry, markSize)
        Column(Modifier.weight(1f)) {
            PantrixText(
                text = buildAnnotatedString {
                    val hit = row.hit
                    if (hit == null) {
                        append(entry.title)
                    } else {
                        append(entry.title.substring(0, hit.first))
                        withStyle(SpanStyle(color = colors.primary.onContainer, fontWeight = FontWeight.ExtraBold)) {
                            append(entry.title.substring(hit.first, hit.last + 1))
                        }
                        append(entry.title.substring(hit.last + 1))
                    }
                },
                style = SiteTheme.type.body.copy(fontSize = titleSize, fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            PantrixText(
                text = entry.sub,
                style = SiteTheme.type.bodySmall.copy(fontSize = subSize),
                color = SiteTheme.colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        when {
            layout == SiteLayout.Phone -> {
                PantrixIcon(
                    imageVector = PantrixIcons.ChevronForward,
                    contentDescription = null,
                    tint = SiteTheme.colors.muted,
                    size = 18.dp
                )
            }

            selected && layout == SiteLayout.Desktop -> {
                PantrixText(text = "↵", style = SiteTheme.type.body, color = SiteTheme.colors.muted)
            }
        }
    }
}

/** A result's square: a component's first letter on the brand's light ground, the mark of any other kind on grey. */
@Composable
private fun Mark(
    entry: SearchEntry,
    size: Dp
) {
    val colors = PantrixTheme.colors
    val component = entry.kind == SearchKind.Component
    Box(
        Modifier
            .size(size)
            .background(
                if (component) colors.primary.container else SiteTheme.colors.hover,
                RoundedCornerShape(if (SiteTheme.layout == SiteLayout.Phone) 10.dp else 9.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        PantrixText(
            text = entry.mark,
            style = SiteTheme.type.inlineCode.copy(
                fontSize = if (SiteTheme.layout == SiteLayout.Phone) 12.5.sp else 12.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = if (component) colors.primary.onContainer else colors.textSecondary
        )
    }
}

/** What the search says where [query] finds nothing, and what to try. */
@Composable
private fun NothingFound(query: String) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    Column(
        Modifier.fillMaxWidth().padding(
            horizontal = 16.dp,
            vertical = if (layout ==
                SiteLayout.Phone
            ) {
                40.dp
            } else {
                36.dp
            }
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PantrixText(
            text = SearchSay.nothingFor(query).of(language),
            style = SiteTheme.type.body.copy(
                fontSize = if (layout ==
                    SiteLayout.Phone
                ) {
                    16.sp
                } else {
                    15.sp
                },
                fontWeight = FontWeight.ExtraBold
            ),
            color = PantrixTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        PantrixText(
            text = SearchSay.TryAnother.of(layout).of(language),
            style = SiteTheme.type.body,
            color = PantrixTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/** The palette's foot: how to move with the keys and where the results come from; a phone's results' language. */
@Composable
internal fun Footer(
    state: SearchUiState,
    onAction: (SearchAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val muted = SiteTheme.colors.muted
    val type = SiteTheme.type.bodySmall.copy(fontSize = 12.sp)
    Hairline()
    Row(
        Modifier
            .fillMaxWidth()
            .background(SiteTheme.colors.surfaceAlt)
            .padding(
                horizontal = if (layout == SiteLayout.Phone) 12.dp else 16.dp,
                vertical = if (layout == SiteLayout.Phone) 9.dp else 10.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Phone) 12.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (layout) {
            SiteLayout.Desktop -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Key("↑")
                    Key("↓")
                    PantrixText(text = SearchSay.Move.of(language), style = type, color = muted)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Key("↵")
                    PantrixText(text = SearchSay.Open.of(language), style = type, color = muted)
                }
                Spacer(Modifier.weight(1f))
                PantrixText(text = SearchSay.Source.of(language), style = type, color = muted)
            }

            SiteLayout.Tablet -> {
                PantrixText(
                    text = SearchSay.Keys.of(language),
                    style = type,
                    color = muted,
                    modifier = Modifier.weight(1f)
                )
                PantrixText(text = SearchSay.Source.of(language), style = type, color = muted)
            }

            SiteLayout.Phone -> {
                PantrixText(
                    text = SearchSay.PagesIn.of(language),
                    style = type.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                    color = muted,
                    modifier = Modifier.weight(1f)
                )
                LanguageChoice(state.language) { onAction(SearchAction.PickLanguage(it)) }
            }
        }
    }
}

private fun minHeightOf(layout: SiteLayout): Dp = when (layout) {
    SiteLayout.Desktop -> 0.dp
    SiteLayout.Tablet -> 52.dp
    SiteLayout.Phone -> 56.dp
}

private const val COUNT_ALPHA = 0.7f
private val HEADING_SPACING = 0.06.em
