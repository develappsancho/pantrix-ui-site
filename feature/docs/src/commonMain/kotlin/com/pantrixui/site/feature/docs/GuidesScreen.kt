package com.pantrixui.site.feature.docs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.platform.rememberPantrixClipboard
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.ArticlePage
import com.pantrixui.site.core.designsystem.content.Crumb
import com.pantrixui.site.core.designsystem.content.FilterChip
import com.pantrixui.site.core.designsystem.content.FilterField
import com.pantrixui.site.core.designsystem.content.LinkCard
import com.pantrixui.site.core.designsystem.content.PageAnchors
import com.pantrixui.site.core.designsystem.content.PageFailed
import com.pantrixui.site.core.designsystem.content.PageHeader
import com.pantrixui.site.core.designsystem.content.RoundTag
import com.pantrixui.site.core.designsystem.content.edgeToEdge
import com.pantrixui.site.core.designsystem.content.pageAnchor
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.model.ContentsEntry
import com.pantrixui.site.core.model.GuideEntry
import com.pantrixui.site.core.model.GuideTopic
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.routing.SiteRoutes
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The guides page, with its model: what the site's navigation shows for [Route.Guides].
 *
 * @param onNavigate what opening a page of the site does, given its path.
 * @param onSearch what opening the search does, from the page shown where the site has no such page.
 */
@Composable
fun GuidesRoute(
    onNavigate: (String) -> Unit,
    onSearch: () -> Unit
) {
    val language = LocalLanguage.current
    val viewModel = koinViewModel<GuidesViewModel>(key = "guides_${language.code}") { parametersOf(language) }
    val clipboard = rememberPantrixClipboard()
    val uriHandler = LocalUriHandler.current
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is DocEffect.Copy -> viewModel.onAction(GuidesAction.Copied(clipboard.copyText(effect.text)))
                is DocEffect.Navigate -> onNavigate(effect.path)
                is DocEffect.OpenUrl -> uriHandler.openUri(effect.url)
            }
        }
    ) { state, onAction ->
        GuidesScreen(state, onAction, onNavigate, onSearch)
    }
}

/**
 * The guides page, drawn from its [state]: its head, a filter and the topics' chips, the guides by topic, and the card
 * that leads to the page for coding assistants.
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param onNavigate what opening a page of the site does, from the page for an address with no page.
 * @param onSearch what opening the search does, from the same page.
 * @param modifier the screen's modifier.
 */
@Composable
fun GuidesScreen(
    state: GuidesUiState,
    onAction: (GuidesAction) -> Unit,
    onNavigate: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (val page = state.page) {
        LoadState.Loading -> {
            Box(modifier.fillMaxSize())
        }

        is LoadState.Failed -> {
            PageFailed(onRetry = { onAction(GuidesAction.Retry) }, modifier = modifier)
        }

        is LoadState.Loaded -> {
            val value = page.value
            if (value == null) {
                NotFoundScreen(SiteRoutes.pathOf(Route.Guides), onNavigate, onSearch, modifier = modifier)
            } else {
                GuidesArticle(value, state, onAction, modifier)
            }
        }
    }
}

@Composable
private fun GuidesArticle(
    page: Page,
    state: GuidesUiState,
    onAction: (GuidesAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val strings = LocalSiteStrings.current
    val anchors = remember(page) { PageAnchors() }
    val onLink: (String) -> Unit = { onAction(GuidesAction.OpenLink(it)) }
    val contents = remember(language) { GuideTopic.entries.map { ContentsEntry(it.anchor, it.title.of(language)) } }
    val gap = when (SiteTheme.layout) {
        SiteLayout.Desktop -> 32.dp
        SiteLayout.Tablet -> 28.dp
        SiteLayout.Phone -> 24.dp
    }
    val groups = state.groups(language)
    PantrixWindowTitle(page.title)
    ArticlePage(
        contents = contents,
        modifier = modifier,
        scrollState = rememberScrollState(),
        anchors = anchors,
        articleWidth = GUIDES_WIDTH
    ) {
        PageHeader(
            trail = listOf(Crumb(strings.docs, SiteRoutes.addressOf(Route.Doc(FIRST_PAGE), language))),
            title = page.title,
            lead = page.description.ifEmpty { null },
            copied = state.copied,
            onCopy = { onAction(GuidesAction.CopyPage) },
            onLink = onLink,
            fullTrail = true
        )
        Spacer(Modifier.height(gap))
        GuideFilter(state, onAction)
        groups.forEach { group ->
            Spacer(Modifier.height(gap))
            GuideSection(group.topic, group.guides, state.minutes, anchors, onLink)
        }
        if (groups.isEmpty()) {
            Spacer(Modifier.height(gap))
            NothingMatches(state.query.trim())
        }
        Spacer(Modifier.height(gap))
        AssistantsCard(onLink = { onLink(SiteRoutes.addressOf(Route.Ai, language)) })
    }
}

/** The filter and the topics' chips, each chip counting the guides of its topic that the filter leaves. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GuideFilter(
    state: GuidesUiState,
    onAction: (GuidesAction) -> Unit
) {
    val language = LocalLanguage.current
    val strings = LocalSiteStrings.current
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    val field = rememberTextFieldState(state.query)
    val currentAction by rememberUpdatedState(onAction)
    LaunchedEffect(field) {
        snapshotFlow { field.text.toString() }.collect { currentAction(GuidesAction.Filter(it)) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterField(field, strings.filterTheGuides, height = if (desktop) 40.dp else 48.dp, tag = "guideFilter")
        val chips: @Composable () -> Unit = {
            FilterChip(strings.allTopics, state.count(language, null), state.topic == null, "topic_all") {
                onAction(GuidesAction.PickTopic(null))
            }
            GuideTopic.entries.forEach { topic ->
                FilterChip(
                    topic.title.of(language),
                    state.count(language, topic),
                    state.topic == topic,
                    "topic_${topic.anchor}"
                ) {
                    onAction(GuidesAction.PickTopic(topic))
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

/** A topic: its heading and the line under it, then its guides' cards, two side by side and one on a phone. */
@Composable
private fun GuideSection(
    topic: GuideTopic,
    guides: List<GuideEntry>,
    minutes: Map<String, Int>,
    anchors: PageAnchors,
    onLink: (String) -> Unit
) {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    Column(Modifier.pageAnchor(anchors, topic.anchor), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            PantrixText(
                text = topic.title.of(language),
                style = SiteTheme.type.sectionTitle,
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
            PantrixText(
                text = topic.note.of(language),
                style = SiteTheme.type.bodySmall.sized(14.sp),
                color = SiteTheme.colors.muted
            )
        }
        val columns = if (phone) 1 else 2
        guides.chunked(columns).forEach { row ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { guide ->
                    GuideCard(guide, minutes[guide.key], onLink, Modifier.weight(1f).fillMaxHeight())
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun GuideCard(
    guide: GuideEntry,
    minutes: Int?,
    onLink: (String) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val strings = LocalSiteStrings.current
    LinkCard(
        onClick = { onLink(SiteRoutes.addressOf(Route.Guide(guide.key), language)) },
        tagKey = "guide_${guide.key}",
        modifier = modifier
    ) {
        // One child: the card's own room between its children is not put between its lines. It fills the card, which
        // is as tall as the tallest of its row, and its last line stands at the foot.
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PantrixText(
                text = guide.title.of(language),
                style = SiteTheme.type.cardTitle.sized(15.5.sp).copy(lineHeight = (15.5 * TITLE_LINE).sp),
                color = PantrixTheme.colors.textPrimary
            )
            PantrixText(
                text = guide.summary.of(language),
                style = SiteTheme.type.bodySmall.copy(lineHeight = (13.5 * SUMMARY_LINE).sp),
                color = PantrixTheme.colors.textSecondary
            )
            if (minutes != null || guide.withoutPantrix) {
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val small = SiteTheme.type.tag.sized(12.sp)
                    if (minutes != null) {
                        PantrixText(
                            text = strings.minutesToRead(minutes),
                            style = small,
                            color = SiteTheme.colors.muted
                        )
                    }
                    if (guide.withoutPantrix) {
                        RoundTag(
                            text = strings.alsoWithoutPantrix,
                            container = SiteTheme.colors.hover,
                            content = PantrixTheme.colors.textSecondary,
                            textStyle = small,
                            horizontalPadding = 8.dp,
                            verticalPadding = 2.dp
                        )
                    }
                }
            }
        }
    }
}

/** What the page says where no guide holds the filter's words: in a box drawn with a dashed line. */
@Composable
private fun NothingMatches(query: String) {
    val strings = LocalSiteStrings.current
    val line = PantrixTheme.colors.borderHover
    Column(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                val width = 1.dp.toPx()
                drawRoundRect(
                    color = line,
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(
                        width,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                    )
                )
            }
            .padding(horizontal = 16.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PantrixText(
            text = strings.noGuideSays(query),
            style = SiteTheme.type.body.copy(fontWeight = FontWeight.ExtraBold),
            color = PantrixTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        PantrixText(
            text = strings.tryAnotherWord,
            style = SiteTheme.type.bodySmall.sized(14.sp),
            color = SiteTheme.colors.muted,
            textAlign = TextAlign.Center
        )
    }
}

/** The card that leads to the page for coding assistants: the guides reach them through the MCP server too. */
@Composable
private fun AssistantsCard(onLink: () -> Unit) {
    val strings = LocalSiteStrings.current
    LinkCard(
        onClick = onLink,
        tagKey = "guidesForAssistants",
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        container = SiteTheme.colors.surfaceAlt
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(SiteTheme.colors.violet, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                PantrixText(
                    text = "AI",
                    style = SiteTheme.type.body.copy(fontWeight = FontWeight.ExtraBold),
                    color = SiteTheme.colors.onViolet
                )
            }
            Column(Modifier.weight(1f)) {
                PantrixText(
                    text = strings.guidesForAssistants,
                    style = SiteTheme.type.cardTitle,
                    color = PantrixTheme.colors.textPrimary
                )
                PantrixText(
                    text = strings.guidesForAssistantsText,
                    style = SiteTheme.type.bodySmall,
                    color = PantrixTheme.colors.textSecondary
                )
            }
        }
    }
}

/** A topic's anchor on the guides page: `layout`, as the design's addresses name it. */
private val GuideTopic.anchor: String
    get() = name.lowercase()

/** The widest the guides page's article is laid out on a desktop. */
private val GUIDES_WIDTH = 840.dp

/** A card's title's line, and its summary's, as many times their size. */
private const val TITLE_LINE = 1.35
private const val SUMMARY_LINE = 1.55
