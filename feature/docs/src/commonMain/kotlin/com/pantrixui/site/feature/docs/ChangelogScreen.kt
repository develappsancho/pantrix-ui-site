package com.pantrixui.site.feature.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.link.LinkUnderline
import com.pantrixui.components.elements.link.PantrixLink
import com.pantrixui.components.elements.switchbutton.PantrixSwitchButton
import com.pantrixui.components.elements.switchbutton.SwitchStyle
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.platform.rememberPantrixClipboard
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.ArticlePage
import com.pantrixui.site.core.designsystem.content.ContentsHeading
import com.pantrixui.site.core.designsystem.content.ContentsLink
import com.pantrixui.site.core.designsystem.content.Crumb
import com.pantrixui.site.core.designsystem.content.FilterChip
import com.pantrixui.site.core.designsystem.content.PageAnchors
import com.pantrixui.site.core.designsystem.content.PageFailed
import com.pantrixui.site.core.designsystem.content.PageHeader
import com.pantrixui.site.core.designsystem.content.RichText
import com.pantrixui.site.core.designsystem.content.RoundTag
import com.pantrixui.site.core.designsystem.content.edgeToEdge
import com.pantrixui.site.core.designsystem.content.pageAnchor
import com.pantrixui.site.core.designsystem.inCapitals
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.i18n.SiteStrings
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ChangeEntry
import com.pantrixui.site.core.model.ChangeGroup
import com.pantrixui.site.core.model.ChangeKind
import com.pantrixui.site.core.model.ChangeSection
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.ContentsEntry
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Release
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.routing.SiteRoutes
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The changelog page, with its model: what the site's navigation shows for [Route.Changelog].
 *
 * @param onNavigate what opening a page of the site does, given its path.
 */
@Composable
fun ChangelogRoute(onNavigate: (String) -> Unit) {
    val language = LocalLanguage.current
    val viewModel = koinViewModel<ChangelogViewModel>(key = "changelog_${language.code}") { parametersOf(language) }
    val clipboard = rememberPantrixClipboard()
    val uriHandler = LocalUriHandler.current
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is DocEffect.Copy -> viewModel.onAction(ChangelogAction.Copied(clipboard.copyText(effect.text)))
                is DocEffect.Navigate -> onNavigate(effect.path)
                is DocEffect.OpenUrl -> uriHandler.openUri(effect.url)
            }
        }
    ) { state, onAction ->
        ChangelogScreen(state, onAction)
    }
}

/**
 * The changelog page, drawn from its [state]: its head, the modules' chips and the breaking switch, each version in
 * a card of its own, and the version the roadmap plans next.
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun ChangelogScreen(
    state: ChangelogUiState,
    onAction: (ChangelogAction) -> Unit,
    modifier: Modifier = Modifier
) {
    when (val changelog = state.changelog) {
        LoadState.Loading -> Box(modifier.fillMaxSize())
        is LoadState.Failed -> PageFailed(onRetry = { onAction(ChangelogAction.Retry) }, modifier = modifier)
        is LoadState.Loaded -> ChangelogArticle(changelog.value, state, onAction, modifier)
    }
}

@Composable
private fun ChangelogArticle(
    changelog: Changelog,
    state: ChangelogUiState,
    onAction: (ChangelogAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val strings = LocalSiteStrings.current
    val anchors = remember(changelog) { PageAnchors() }
    val onLink: (String) -> Unit = { onAction(ChangelogAction.OpenLink(it)) }
    val versions = remember(changelog, strings) {
        changelog.releases.map { release ->
            ContentsEntry(
                anchor = release.anchor(changelog),
                title = release.number(changelog),
                note = if (release.version == null) strings.unreleasedNote else release.date
            )
        }
    }
    val gap = when (SiteTheme.layout) {
        SiteLayout.Desktop -> 28.dp
        SiteLayout.Tablet -> 24.dp
        SiteLayout.Phone -> 20.dp
    }
    PantrixWindowTitle(changelog.title)
    ArticlePage(
        contents = versions,
        modifier = modifier,
        scrollState = rememberScrollState(),
        anchors = anchors,
        articleWidth = CHANGELOG_WIDTH,
        contentsTitle = strings.versions,
        contentsFooter = { VersionsFooter(onLink) }
    ) {
        PageHeader(
            trail = listOf(Crumb(strings.docs, SiteRoutes.addressOf(Route.Doc(FIRST_PAGE), language))),
            title = changelog.title,
            lead = null,
            copied = state.copied,
            onCopy = { onAction(ChangelogAction.CopyPage) },
            onLink = onLink,
            fullTrail = true,
            leadText = changelog.lead(strings)
        )
        Spacer(Modifier.height(gap))
        ChangelogFilter(changelog, state, onAction, onLink)
        changelog.releases.forEach { release ->
            Spacer(Modifier.height(gap))
            ReleaseCard(changelog, release, state, onLink, Modifier.pageAnchor(anchors, release.anchor(changelog)))
        }
        Spacer(Modifier.height(gap))
        PlannedCard(onClick = { onLink(SiteRoutes.addressOf(Route.Roadmap, language)) })
    }
}

/** The changelog's lead: its own first sentence, then how its versions are numbered and what marks a break. */
private fun Changelog.lead(strings: SiteStrings): List<Span> = description + listOf(
    Span.Text(" ${strings.semverBefore}"),
    Span.Text(strings.breaking, strong = true),
    Span.Text(strings.semverAfter)
)

/** The chips of the modules, the switch that keeps the breaking changes alone, and the links to the releases. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChangelogFilter(
    changelog: Changelog,
    state: ChangelogUiState,
    onAction: (ChangelogAction) -> Unit,
    onLink: (String) -> Unit
) {
    val strings = LocalSiteStrings.current
    val language = LocalLanguage.current
    val modules = remember(changelog) { changelog.modules() }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val chips: @Composable () -> Unit = {
            FilterChip(strings.allTopics, modules.sumOf { it.count }, state.module == null, "module_all") {
                onAction(ChangelogAction.PickModule(null))
            }
            modules.forEach { module ->
                FilterChip(module.name, module.count, state.module == module.key, "module_${module.key}") {
                    onAction(ChangelogAction.PickModule(module.key))
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
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            // Where the switch and the links take a finger's reach, the reach is the room between their lines.
            verticalArrangement = Arrangement.spacedBy(if (PantrixTheme.platform.isTouch) 0.dp else 10.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            PantrixSwitchButton(
                checked = state.breakingOnly,
                onCheckedChange = { onAction(ChangelogAction.ToggleBreaking) },
                label = strings.onlyBreaking,
                style = SwitchStyle(
                    trackWidth = 36.dp,
                    trackHeight = 22.dp,
                    thumbSizeOff = 16.dp,
                    thumbSizeOn = 16.dp,
                    thumbSizePressed = 16.dp
                ),
                tagKey = "onlyBreaking",
                labelStyle = SiteTheme.type.bodySmall.sized(14.sp).copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PantrixIcon(
                        imageVector = SiteIcons.Feed,
                        contentDescription = null,
                        tint = PantrixTheme.colors.primary.onContainer,
                        size = 15.dp
                    )
                    FootLink(strings.followTheReleases, "${LibraryFacts.REPOSITORY}/releases.atom", "releases", onLink)
                }
                val file = if (language == Language.English) "CHANGELOG.md" else "CHANGELOG.tr.md"
                FootLink(file, "${LibraryFacts.REPOSITORY}/blob/main/$file", "changelogFile", onLink)
            }
        }
    }
}

@Composable
private fun FootLink(
    text: String,
    href: String,
    tag: String,
    onLink: (String) -> Unit
) {
    PantrixLink(
        text = text,
        onClick = { onLink(href) },
        style = SiteTheme.type.bodySmall.copy(fontWeight = FontWeight.Bold),
        underline = LinkUnderline.OnHover,
        tagKey = tag
    )
}

/** A version in a card of its own: its head, with what it is and what it is built for, then its changes. */
@Composable
private fun ReleaseCard(
    changelog: Changelog,
    release: Release,
    state: ChangelogUiState,
    onLink: (String) -> Unit,
    modifier: Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    val padding = when (SiteTheme.layout) {
        SiteLayout.Desktop -> PaddingValues(horizontal = 24.dp, vertical = 22.dp)
        SiteLayout.Tablet -> PaddingValues(20.dp)
        SiteLayout.Phone -> PaddingValues(16.dp)
    }
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PantrixTheme.colors.surface)
            .border(1.dp, PantrixTheme.colors.border, shape)
    ) {
        ReleaseHead(changelog, release, onLink, Modifier.padding(padding))
        Box(Modifier.fillMaxWidth().height(1.dp).background(PantrixTheme.colors.border))
        val sections = state.shown(release)
        if (sections.isEmpty()) {
            NothingShown(release.number(changelog), Modifier.padding(horizontal = 20.dp, vertical = 36.dp))
        } else {
            Column(Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                release.lead.filterIsInstance<Block.Paragraph>().forEach { paragraph ->
                    RichText(paragraph.text, onLink, style = SiteTheme.type.bodySmall.sized(14.sp))
                }
                sections.forEach { section -> SectionView(section, onLink) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReleaseHead(
    changelog: Changelog,
    release: Release,
    onLink: (String) -> Unit,
    padding: Modifier
) {
    val strings = LocalSiteStrings.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    Column(
        Modifier.fillMaxWidth().background(SiteTheme.colors.surfaceAlt).then(padding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            PantrixText(
                text = release.number(changelog),
                style = SiteTheme.type.pageTitle.sized(if (phone) 28.sp else 32.sp)
                    .copy(lineHeight = if (phone) 28.sp else 32.sp),
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
            if (release.version == null) {
                val warning = PantrixTheme.colors.warning
                Pill(strings.notReleasedYet, warning.container, warning.onContainer)
            }
            PantrixText(
                text = release.date ?: strings.unreleased,
                style = SiteTheme.type.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = SiteTheme.colors.muted
            )
        }
        release.intro.filterIsInstance<Block.Paragraph>().forEach { paragraph ->
            RichText(paragraph.text, onLink, codeSize = 13.sp)
        }
        val artifacts = release.sections.flatMap { section -> section.groups.flatMap { it.artifacts } }.distinct()
        if (artifacts.isNotEmpty()) {
            Labelled(strings.publishedAs) {
                artifacts.forEach { artifact ->
                    PantrixText(
                        text = artifact,
                        style = SiteTheme.type.inlineCode.sized(12.sp),
                        color = PantrixTheme.colors.textPrimary,
                        modifier = Modifier
                            .background(SiteTheme.colors.hover, RoundedCornerShape(7.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
        if (release.targets.isNotEmpty()) {
            Labelled(strings.targets) {
                release.targets.forEach { target ->
                    PantrixText(
                        text = target.plain().asLabel(),
                        style = SiteTheme.type.tag.sized(12.sp),
                        color = PantrixTheme.colors.textSecondary,
                        modifier = Modifier
                            .border(1.dp, PantrixTheme.colors.border, CircleShape)
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/** A label in capitals over a row of marks that wraps. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Labelled(
    label: String,
    marks: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PantrixText(text = label.inCapitals(), style = SiteTheme.type.overline, color = SiteTheme.colors.muted)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) { marks() }
    }
}

/** A kind of change: its mark and how many lines it has, then its modules. */
@Composable
private fun SectionView(
    section: ChangeSection,
    onLink: (String) -> Unit
) {
    val strings = LocalSiteStrings.current
    val (container, content) = section.kind.colors()
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixText(
                text = section.title,
                style = SiteTheme.type.tag.sized(13.sp).copy(fontWeight = FontWeight.ExtraBold),
                color = content,
                modifier = Modifier
                    .background(container, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
            PantrixText(
                text = strings.entries(section.groups.sumOf { it.entries.size }),
                style = SiteTheme.type.tag.sized(13.sp),
                color = SiteTheme.colors.muted
            )
        }
        section.groups.forEach { group -> GroupView(group, onLink) }
    }
}

/** The fill and the colour of a kind of change's mark. */
@Composable
private fun ChangeKind.colors(): Pair<Color, Color> {
    val colors = PantrixTheme.colors
    val role = when (this) {
        ChangeKind.Added -> colors.success
        ChangeKind.Changed, ChangeKind.Fixed -> colors.info
        ChangeKind.Deprecated -> colors.warning
        ChangeKind.Removed, ChangeKind.Security -> colors.danger
    }
    return role.container to role.onContainer
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupView(
    group: ChangeGroup,
    onLink: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            PantrixText(
                text = group.module,
                style = SiteTheme.type.cardTitle.sized(17.sp),
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
            if (group.artifacts.isNotEmpty()) {
                PantrixText(
                    text = group.artifacts.joinToString(" · "),
                    style = SiteTheme.type.inlineCode.sized(12.sp),
                    color = SiteTheme.colors.muted
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            group.entries.forEach { entry -> EntryView(entry, onLink) }
        }
    }
}

/** A line of the changelog: its label in a column of its own, or over it on a phone, and what it says. */
@Composable
private fun EntryView(
    entry: ChangeEntry,
    onLink: (String) -> Unit
) {
    val phone = SiteTheme.layout == SiteLayout.Phone
    val hairline = SiteTheme.colors.hairline
    val lined = Modifier
        .fillMaxWidth()
        .drawBehind { drawLine(hairline, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
        .padding(top = 10.dp)
    val label: @Composable (Modifier) -> Unit = { labelModifier ->
        Column(labelModifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (entry.breaking) BreakingMark()
            entry.label?.let { text ->
                PantrixText(
                    text = text,
                    style = SiteTheme.type.body.sized(14.sp).copy(fontWeight = FontWeight.ExtraBold),
                    color = PantrixTheme.colors.textPrimary
                )
            }
        }
    }
    if (phone) {
        Column(lined, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            label(Modifier)
            EntryBlocks(entry.blocks, onLink)
        }
    } else {
        Row(lined, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            label(Modifier.width(ENTRY_LABEL))
            EntryBlocks(entry.blocks, onLink, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BreakingMark() {
    val danger = PantrixTheme.colors.danger
    Pill(LocalSiteStrings.current.breaking, danger.container, danger.onContainer)
}

@Composable
private fun Pill(
    text: String,
    container: Color,
    content: Color
) {
    RoundTag(text, container, content, SiteTheme.type.tag.copy(fontWeight = FontWeight.ExtraBold))
}

/** What a line says: its sentences, and the lists under them with a bullet each, one inside another. */
@Composable
private fun EntryBlocks(
    blocks: List<Block>,
    onLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val style = SiteTheme.type.body.sized(14.sp).copy(lineHeight = (14 * ENTRY_LINE).sp)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        blocks.forEach { block ->
            when (block) {
                is Block.Paragraph -> RichText(block.text, onLink, style = style, codeSize = 12.5.sp)

                is Block.ItemList -> block.items.forEach { item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PantrixText(text = "•", style = style, color = SiteTheme.colors.muted)
                        EntryBlocks(item, onLink, Modifier.weight(1f))
                    }
                }

                else -> Unit
            }
        }
    }
}

/** What a version's card says where the switch and the module leave none of its changes. */
@Composable
private fun NothingShown(
    version: String,
    modifier: Modifier
) {
    val strings = LocalSiteStrings.current
    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PantrixText(
            text = strings.nothingBreaking(version),
            style = SiteTheme.type.body.copy(fontWeight = FontWeight.ExtraBold),
            color = PantrixTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        PantrixText(
            text = strings.nothingBreakingText,
            style = SiteTheme.type.bodySmall.sized(14.sp),
            color = SiteTheme.colors.muted,
            textAlign = TextAlign.Center
        )
    }
}

/** The version the roadmap plans next, in a card drawn with a dashed line: it is not here yet. */
@Composable
private fun PlannedCard(onClick: () -> Unit) {
    val strings = LocalSiteStrings.current
    val shape = RoundedCornerShape(16.dp)
    val line = PantrixTheme.colors.borderHover
    Row(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = line,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            DASH.map {
                                it.toPx()
                            }.toFloatArray()
                        )
                    )
                )
            }
            .siteClickable("plannedVersion", Role.Button, shape, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Pill(
            "${LibraryFacts.PLANNED_VERSION} · ${strings.planned}",
            SiteTheme.colors.hover,
            PantrixTheme.colors.textSecondary
        )
        Column(Modifier.weight(1f)) {
            PantrixText(
                text = strings.plannedTitle,
                style = SiteTheme.type.body.copy(fontWeight = FontWeight.ExtraBold),
                color = PantrixTheme.colors.textPrimary
            )
            PantrixText(
                text = strings.plannedText,
                style = SiteTheme.type.bodySmall,
                color = PantrixTheme.colors.textSecondary
            )
        }
        PantrixIcon(imageVector = PantrixIcons.ChevronForward, contentDescription = null, tint = SiteTheme.colors.muted)
    }
}

/** The list's links of the changelog's own: the version planned next, and the tool that gives it to an assistant. */
@Composable
private fun VersionsFooter(onLink: (String) -> Unit) {
    val strings = LocalSiteStrings.current
    val language = LocalLanguage.current
    val muted = SiteTheme.colors.muted
    ContentsLink(
        label = buildAnnotatedString {
            append(LibraryFacts.PLANNED_VERSION)
            withStyle(SpanStyle(color = muted)) { append(" · ${strings.planned}") }
        },
        current = false,
        tag = "toc_planned"
    ) { onLink(SiteRoutes.addressOf(Route.Roadmap, language)) }
    ContentsHeading(strings.forAssistants, Modifier.padding(top = 18.dp))
    val mono = SiteTheme.type.inlineCode.sized(12.5.sp)
    ContentsLink(
        label = buildAnnotatedString {
            withStyle(SpanStyle(fontFamily = mono.fontFamily, fontSize = mono.fontSize)) { append(GET_CHANGELOG) }
        },
        current = false,
        tag = "toc_getChangelog"
    ) { onLink(SiteRoutes.addressOf(Route.Ai, language)) }
}

/** A version's number: its own, or the one still to come that "Unreleased" describes. */
private fun Release.number(changelog: Changelog): String = version ?: changelog.upcoming.orEmpty()

/** A version's anchor: `v010` for 0.1.0, as the design's addresses name it. */
private fun Release.anchor(changelog: Changelog): String = "v" + number(changelog).filter { it.isLetterOrDigit() }

/** A list's item as a label: its first letter a capital, but a name that starts small keeps it, as iOS does. */
private fun String.asLabel(): String = if (startsWith("iOS")) this else replaceFirstChar { it.uppercase() }

/** A run of text as it reads. */
private fun List<Span>.plain(): String = joinToString("") { span ->
    when (span) {
        is Span.Text -> span.text
        is Span.Code -> span.text
        is Span.Link -> span.text.plain()
    }
}

/** The widest the changelog's article is laid out on a desktop. */
private val CHANGELOG_WIDTH = 820.dp

/** How wide a line's label is beside it on a desktop and a tablet. */
private val ENTRY_LABEL = 170.dp

/** A line's text's line, as many times its size. */
private const val ENTRY_LINE = 1.6

/** The dashes and gaps of a card's line that is not here yet. */
private val DASH = listOf(6.dp, 4.dp)

/** The MCP server's tool that gives an assistant the changelog. */
private const val GET_CHANGELOG = "get_changelog"
