package com.pantrixui.site.feature.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.container.accordion.AccordionSection
import com.pantrixui.components.container.accordion.AccordionVariant
import com.pantrixui.components.container.accordion.PantrixAccordion
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.primitives.pantrixPressIndicationWithColor
import com.pantrixui.foundation.testing.pantrixTestTag
import com.pantrixui.foundation.testing.rememberPantrixTestTag
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.FilterChip
import com.pantrixui.site.core.designsystem.content.FilterField
import com.pantrixui.site.core.designsystem.content.LinkCard
import com.pantrixui.site.core.designsystem.content.PageFailed
import com.pantrixui.site.core.designsystem.content.RichText
import com.pantrixui.site.core.designsystem.content.edgeToEdge
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LandingHeads
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.ComponentCard
import com.pantrixui.site.core.model.ComponentGroup
import com.pantrixui.site.core.model.ComponentGroups
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Words
import com.pantrixui.site.core.routing.SiteRoutes
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The components page, with its model: every component of the library by group, filtered as the reader types, and
 * the ones most apps start with drawn live at its top.
 *
 * @param onNavigate what opening a page of the site does, given its path.
 */
@Composable
fun ComponentsRoute(onNavigate: (String) -> Unit) {
    val language = LocalLanguage.current
    val viewModel = koinViewModel<ComponentsViewModel>(key = "components_${language.code}") { parametersOf(language) }
    val uriHandler = LocalUriHandler.current
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is ComponentsEffect.Navigate -> onNavigate(effect.path)
                is ComponentsEffect.OpenUrl -> uriHandler.openUri(effect.url)
                is ComponentsEffect.Copy -> Unit
            }
        }
    ) { state, onAction -> ComponentsScreen(state, onAction) }
}

/**
 * The components page, drawn from its [state]: the components once they are read, nothing while they are, and what
 * to do when they could not be.
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun ComponentsScreen(
    state: ComponentsUiState,
    onAction: (ComponentsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state.index) {
        // The list is a few kilobytes read from the app's own files: there is nothing to show for the moment it takes.
        LoadState.Loading -> Box(modifier.fillMaxSize())

        is LoadState.Failed -> PageFailed(onRetry = { onAction(ComponentsAction.Retry) }, modifier = modifier)

        is LoadState.Loaded -> ComponentsPage(state, onAction, modifier)
    }
}

@Composable
private fun ComponentsPage(
    state: ComponentsUiState,
    onAction: (ComponentsAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val gap = when (layout) {
        SiteLayout.Desktop -> 40.dp
        SiteLayout.Tablet -> 32.dp
        SiteLayout.Phone -> 24.dp
    }
    val open: (String) -> Unit = { key -> onAction(ComponentsAction.OpenLink(componentAddress(key, language))) }
    PantrixWindowTitle(LandingHeads.Components.title.of(language))
    Box(modifier.fillMaxSize().pantrixVerticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = PAGE_WIDTH)
                .fillMaxWidth()
                .padding(layout.componentsPadding)
        ) {
            PantrixText(
                text = ComponentsSay.Title.of(language),
                style = SiteTheme.type.pageTitle,
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(12.dp))
            RichText(
                spans = ComponentsSay.lead(state.count(null), language, phone = layout == SiteLayout.Phone),
                onLink = {},
                style = SiteTheme.type.lead,
                modifier = Modifier.widthIn(max = LEAD_WIDTH)
            )
            Spacer(Modifier.height(24.dp))
            ComponentFilter(state, onAction)
            if (state.query.isBlank() && state.group == null) {
                Spacer(Modifier.height(gap))
                FeaturedSection(open)
            }
            val groups = state.shown
            if (layout == SiteLayout.Phone && groups.isNotEmpty()) {
                Spacer(Modifier.height(gap))
                PhoneGroups(groups, open)
            } else {
                groups.forEach { group ->
                    Spacer(Modifier.height(gap))
                    GroupSection(group, open)
                }
            }
            if (groups.isEmpty()) {
                Spacer(Modifier.height(gap))
                PantrixText(
                    text = ComponentsSay.nothingMatches(state.query.trim()).of(language),
                    style = SiteTheme.type.cardTitle,
                    color = PantrixTheme.colors.textPrimary
                )
            }
        }
    }
}

/** The filter, and the groups' chips, each counting the components of its group that the filter leaves. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComponentFilter(
    state: ComponentsUiState,
    onAction: (ComponentsAction) -> Unit
) {
    val language = LocalLanguage.current
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    val field = rememberTextFieldState(state.query)
    val currentAction by rememberUpdatedState(onAction)
    LaunchedEffect(field) {
        snapshotFlow { field.text.toString() }.collect { currentAction(ComponentsAction.Filter(it)) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FilterField(
            field = field,
            placeholder = ComponentsSay.Filter.of(language),
            height = if (desktop) 40.dp else 48.dp,
            tag = "componentFilter",
            modifier = Modifier.widthIn(max = FILTER_WIDTH).fillMaxWidth()
        )
        val chips: @Composable () -> Unit = {
            FilterChip(ComponentsSay.All.of(language), state.count(null), state.group == null, "group_all") {
                onAction(ComponentsAction.PickGroup(null))
            }
            ComponentGroups.keys.forEach { key ->
                FilterChip(
                    ComponentGroups.title(key).of(language),
                    state.count(key),
                    state.group == key,
                    "group_$key"
                ) {
                    onAction(ComponentsAction.PickGroup(key))
                }
            }
        }
        if (SiteTheme.layout == SiteLayout.Phone) {
            Row(Modifier.edgeToEdge(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) { chips() }
        }
    }
}

/** The components most apps start with, each drawn live over its name: a row that scrolls on a phone. */
@Composable
private fun FeaturedSection(open: (String) -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PantrixText(
            text = ComponentsSay.MostAppsStart.of(language),
            style = SiteTheme.type.sectionTitle.sized(20.sp),
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
        if (layout == SiteLayout.Phone) {
            Row(
                Modifier.edgeToEdge().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FEATURED.forEach { FeaturedCard(it, open, Modifier.width(FEATURED_PHONE_WIDTH).fillMaxHeight()) }
            }
        } else {
            val columns = if (layout == SiteLayout.Desktop) DESKTOP_COLUMNS else TABLET_COLUMNS
            CardRows(FEATURED, columns) { featured, cardModifier -> FeaturedCard(featured, open, cardModifier) }
        }
    }
}

@Composable
private fun FeaturedCard(
    featured: Featured,
    open: (String) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    LinkCard(
        onClick = { open(featured.key) },
        tagKey = "featured_${featured.key}",
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        Miniature(
            scale = featured.scale,
            modifier = Modifier
                .fillMaxWidth()
                .height(PREVIEW_HEIGHT)
                .background(SiteTheme.colors.canvas)
        ) { featured.preview() }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PantrixText(
                text = featured.name,
                style = SiteTheme.type.cardTitle.sized(15.sp),
                color = PantrixTheme.colors.textPrimary
            )
            if (SiteTheme.layout != SiteLayout.Phone) {
                PantrixText(
                    text = featured.line.of(language),
                    style = SiteTheme.type.bodySmall.sized(13.sp),
                    color = PantrixTheme.colors.textSecondary
                )
            }
        }
    }
}

/** A group: its heading with its count, then its components' cards, as many side by side as the layout takes. */
@Composable
private fun GroupSection(
    group: ComponentGroup,
    open: (String) -> Unit
) {
    val language = LocalLanguage.current
    val columns = if (SiteTheme.layout == SiteLayout.Desktop) DESKTOP_COLUMNS else TABLET_COLUMNS
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PantrixText(
                text = ComponentGroups.title(group.key).of(language),
                style = SiteTheme.type.sectionTitle.sized(20.sp),
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
            PantrixText(
                text = "${group.components.size}",
                style = SiteTheme.type.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = SiteTheme.colors.muted,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        CardRows(group.components, columns) { card, cardModifier -> ComponentCardView(card, open, cardModifier) }
    }
}

/** [items] in rows of [columns] cards, each row as tall as its tallest card. */
@Composable
private fun <T> CardRows(
    items: List<T>,
    columns: Int,
    card: @Composable (T, Modifier) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(columns).forEach { row ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { item -> card(item, Modifier.weight(1f).fillMaxHeight()) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** A component's card: its name and a mark that it leads to its page, what it is for, and its function. */
@Composable
private fun ComponentCardView(
    card: ComponentCard,
    open: (String) -> Unit,
    modifier: Modifier
) {
    LinkCard(
        onClick = { open(card.key) },
        tagKey = "component_${card.key}",
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PantrixText(
                    text = card.name,
                    style = SiteTheme.type.cardTitle.sized(15.sp),
                    color = PantrixTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                PantrixIcon(
                    imageVector = SiteIcons.ArrowUpRight,
                    contentDescription = null,
                    tint = SiteTheme.colors.muted,
                    size = 14.dp
                )
            }
            PantrixText(
                text = card.summary.joinToString("") { it.plain },
                style = SiteTheme.type.bodySmall.sized(13.sp),
                color = PantrixTheme.colors.textSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            PantrixText(
                text = card.function,
                style = SiteTheme.type.inlineCode.sized(11.5.sp),
                color = SiteTheme.colors.muted
            )
        }
    }
}

/** On a phone, the groups one under another, each opened to its components' rows. */
@Composable
private fun PhoneGroups(
    groups: List<ComponentGroup>,
    open: (String) -> Unit
) {
    val language = LocalLanguage.current
    var expanded by rememberSaveable { mutableStateOf(setOf(groups.firstOrNull()?.key.orEmpty())) }
    PantrixAccordion(
        sections = groups.map { group ->
            AccordionSection(
                key = group.key,
                title = ComponentGroups.title(group.key).of(language),
                tagKey = group.key,
                trailing = {
                    PantrixText(
                        text = "${group.components.size}",
                        style = SiteTheme.type.bodySmall,
                        color = SiteTheme.colors.muted
                    )
                }
            ) {
                group.components.forEach { card -> PhoneRow(card, open) }
            }
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
        variant = AccordionVariant.Bordered,
        tagKey = "componentGroups"
    )
}

@Composable
private fun PhoneRow(
    card: ComponentCard,
    open: (String) -> Unit
) {
    val tag = rememberPantrixTestTag("ComponentRow", tagKey = card.key)
    Box(
        Modifier
            .fillMaxWidth()
            .pantrixTestTag(tag)
            .clickable(
                interactionSource = null,
                indication = pantrixPressIndicationWithColor(PantrixTheme.colors.onSurface),
                role = Role.Button
            ) { open(card.key) }
            .padding(vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                PantrixText(
                    text = card.name,
                    style = SiteTheme.type.cardTitle.sized(15.sp),
                    color = PantrixTheme.colors.textPrimary
                )
                PantrixText(
                    text = card.summary.joinToString("") { it.plain },
                    style = SiteTheme.type.bodySmall.sized(13.sp),
                    color = PantrixTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            PantrixIcon(
                imageVector = PantrixIcons.ChevronForward,
                contentDescription = null,
                tint = SiteTheme.colors.muted,
                size = 16.dp
            )
        }
    }
}

/** The address of the page of the component [key] in [language]. */
internal fun componentAddress(
    key: String,
    language: Language
): String = SiteRoutes.addressOf(Route.Component(key), language)

/** The room around the components page, as each layout's design leaves it. */
private val SiteLayout.componentsPadding: PaddingValues
    get() = when (this) {
        SiteLayout.Desktop -> PaddingValues(start = 40.dp, end = 40.dp, top = 40.dp, bottom = 80.dp)
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 64.dp)
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 48.dp)
    }

/** The page's words, in each language. */
internal object ComponentsSay {
    val Title = Words("Components", "Bileşenler")
    val Filter = Words("Filter by name or what it does", "Adına ya da ne yaptığına göre süz")
    val All = Words("All", "Tümü")
    val MostAppsStart = Words("Where most apps start", "Çoğu uygulama buradan başlar")

    /** The page's lead: how many components there are, and what each is drawn in. */
    fun lead(
        count: Int,
        language: Language,
        phone: Boolean
    ): List<Span> = when {
        phone -> listOf(
            Span.Text(
                Words(
                    "$count components on Compose Foundation, each drawn in its platform's preset.",
                    "Compose Foundation üstünde $count bileşen, her biri platformunun hazır ayarında çizilir."
                ).of(language)
            )
        )

        else -> listOf(
            Span.Text(Words("$count components, each named ", "$count bileşen, her biri ").of(language)),
            Span.Code("Pantrix…"),
            Span.Text(
                Words(
                    ", on Compose Foundation. Every one is drawn in each platform's preset, in light and dark, " +
                        "and its page shows it live.",
                    " adıyla, Compose Foundation üstünde. Her biri her platformun hazır ayarında, açık ve koyu " +
                        "temada çizilir; sayfası onu canlı gösterir."
                ).of(language)
            )
        )
    }

    /** What the page says where no component matches the filter. */
    fun nothingMatches(query: String): Words =
        Words("No component matches “$query”", "“$query” ile eşleşen bileşen yok")
}

/** The widest the page is laid out, its lead, and its filter. */
private val PAGE_WIDTH = 1320.dp
private val LEAD_WIDTH = 760.dp
private val FILTER_WIDTH = 520.dp

/** How tall a featured component's preview is, and how wide its card is on a phone. */
private val PREVIEW_HEIGHT = 150.dp
private val FEATURED_PHONE_WIDTH = 240.dp

private const val DESKTOP_COLUMNS = 4
private const val TABLET_COLUMNS = 2
