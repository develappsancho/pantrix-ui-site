package com.pantrixui.site.feature.references

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.image.PantrixImage
import com.pantrixui.components.elements.link.LinkUnderline
import com.pantrixui.components.elements.link.PantrixLink
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.FilterChip
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.LandingButtonKind
import com.pantrixui.site.core.designsystem.content.landingCard
import com.pantrixui.site.core.designsystem.content.landingHeading
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.PlatformKind
import com.pantrixui.site.core.model.Reference
import org.jetbrains.compose.resources.painterResource

/**
 * The app the page features, the first: what it is, where it runs, three facts and the ways to reach it, beside a
 * picture of it on a desktop, over it on a tablet and a phone. Where the page lists others under it, a label says
 * this one is featured.
 */
@Composable
internal fun Featured(
    project: Reference,
    several: Boolean,
    onAction: (ReferencesAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val phone = layout == SiteLayout.Phone
    LandingBand(sidePadding(bottom = if (phone) 40.dp else 56.dp)) {
        if (several) {
            PantrixText(
                text = ReferencesSay.Featured.of(language).uppercase(),
                style = strong(13.sp).copy(letterSpacing = LABEL_SPACING),
                color = SiteTheme.colors.muted,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        val corner = if (phone) 20.dp else 24.dp
        val card = Modifier.fillMaxWidth().landingCard(colors.surface, colors.border, corner = corner)
        val sketch = @Composable { modifier: Modifier ->
            SketchGround(modifier) {
                SiteSketch(
                    address = project.address,
                    scale = when (layout) {
                        SiteLayout.Desktop -> DESKTOP_SKETCH
                        SiteLayout.Tablet -> 1f
                        SiteLayout.Phone -> PHONE_SKETCH
                    }
                )
            }
        }
        if (layout == SiteLayout.Desktop) {
            Row(card.height(IntrinsicSize.Min)) {
                Column(Modifier.weight(1f).padding(40.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    FeaturedWords(project, onAction)
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(SiteTheme.colors.hairline))
                sketch(Modifier.weight(SKETCH_SHARE).fillMaxHeight())
            }
        } else {
            Column(card) {
                sketch(Modifier.fillMaxWidth().height(if (phone) 260.dp else 420.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(SiteTheme.colors.hairline))
                Column(
                    Modifier.padding(if (phone) 20.dp else 28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) { FeaturedWords(project, onAction) }
            }
        }
    }
}

/** The featured card's words: who it is, what it is, where it runs, its facts and its buttons. */
@Composable
private fun ColumnScope.FeaturedWords(
    project: Reference,
    onAction: (ReferencesAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    Identity(project)
    PantrixText(
        text = project.about,
        style = small(if (phone) 15.sp else 16.sp),
        color = PantrixTheme.colors.textSecondary
    )
    Platforms(project, columns = if (phone) 1 else 2)
    Facts(project)
    val height = if (layout == SiteLayout.Desktop) 46.dp else 48.dp
    val visit = @Composable { modifier: Modifier ->
        LandingButton(
            text = ReferencesSay.visit(project.address).of(language),
            onClick = { onAction(ReferencesAction.Open(project.url)) },
            tag = "visit_${project.key}",
            modifier = modifier,
            height = height,
            padding = 20.dp,
            fill = phone,
            trailing = SiteIcons.ArrowUpRight
        )
    }
    val source = @Composable { modifier: Modifier ->
        project.source?.let { address ->
            LandingButton(
                text = ReferencesSay.Source.of(language),
                onClick = { onAction(ReferencesAction.Open(address)) },
                tag = "source_${project.key}",
                modifier = modifier,
                kind = LandingButtonKind.Outline,
                height = height,
                padding = 20.dp,
                fill = phone,
                trailing = SiteIcons.ArrowUpRight
            )
        }
    }
    val download = @Composable {
        project.download?.let { address ->
            PantrixLink(
                text = ReferencesSay.DownloadForMac.of(language) + " →",
                onClick = { onAction(ReferencesAction.Open(address)) },
                style = strong(15.sp, FontWeight.Bold),
                tagKey = "download_${project.key}",
                underline = LinkUnderline.OnHover
            )
        }
    }
    if (phone) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            visit(Modifier)
            source(Modifier)
            Box(Modifier.align(Alignment.CenterHorizontally)) { download() }
        }
    } else {
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            visit(Modifier)
            source(Modifier)
            download()
        }
    }
}

/** The app's mark, its name and address, and that its source is open where it is. */
@Composable
private fun Identity(project: Reference) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        AppMark(project.name, size = 48.dp)
        Column(Modifier.weight(1f)) {
            PantrixText(
                text = project.name,
                style = landingHeading(
                    if (SiteTheme.layout == SiteLayout.Desktop) 28.sp else 24.sp,
                    line = NAME_LINE,
                    spacing = NAME_SPACING
                ),
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
            PantrixText(text = project.address, style = small(14.sp), color = SiteTheme.colors.muted)
        }
        if (project.source != null) {
            Box(
                Modifier
                    .heightIn(min = 28.dp)
                    .background(colors.success.container, CircleShape)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                PantrixText(
                    text = ReferencesSay.OpenSource.of(language),
                    style = strong(12.5.sp),
                    color = colors.success.onContainer
                )
            }
        }
    }
}

/** An app's mark: the first letter of its name on the brand's colour, as the site's own is a "P". */
@Composable
internal fun AppMark(
    name: String,
    size: Dp
) {
    Box(
        Modifier.size(size).background(PantrixTheme.colors.primary.color, RoundedCornerShape(size * MARK_CORNER)),
        contentAlignment = Alignment.Center
    ) {
        PantrixText(
            text = name.first().uppercase(),
            style = strong((size.value / 2).sp),
            color = PantrixTheme.colors.primary.onColor
        )
    }
}

/** Where the app runs, each platform a cell, in [columns]. */
@Composable
private fun Platforms(
    project: Reference,
    columns: Int
) {
    val colors = PantrixTheme.colors
    val site = SiteTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        project.platforms.chunked(columns).forEach { row ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { platform ->
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .landingCard(site.surfaceAlt, site.hairline, corner = 12.dp)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        PantrixText(text = platform.name, style = strong(14.5.sp), color = colors.textPrimary)
                        PantrixText(text = platform.detail, style = small(13.sp), color = site.muted)
                    }
                }
                repeat(columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

/** Three facts, a number over what it counts: its components, its languages and its one codebase. */
@Composable
private fun Facts(project: Reference) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val site = SiteTheme.colors
    val facts = listOf(
        project.components.size.toString() to ReferencesSay.ComponentsFact,
        project.languages.size.toString() to ReferencesSay.LanguagesFact,
        "1" to ReferencesSay.CodebaseFact
    )
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(site.hairline))
        Row(Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            facts.forEach { (number, words) ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    PantrixText(
                        text = number,
                        style = landingHeading(
                            if (SiteTheme.layout == SiteLayout.Phone) 28.sp else 32.sp,
                            line = FACT_LINE,
                            spacing = NAME_SPACING
                        ),
                        color = colors.textPrimary
                    )
                    PantrixText(text = words.of(language), style = small(13.sp), color = site.muted)
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(site.hairline))
    }
}

/**
 * What the featured app is made of: the components it draws with, and how it is made; side by side on a desktop, one
 * under the other elsewhere.
 */
@Composable
internal fun MadeOf(project: Reference) {
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    LandingBand(sidePadding(bottom = if (phone) 40.dp else 56.dp)) {
        if (layout == SiteLayout.Desktop) {
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ComponentsCard(project, Modifier.weight(COMPONENTS_SHARE).fillMaxHeight())
                MadeCard(project, Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ComponentsCard(project, Modifier.fillMaxWidth())
                MadeCard(project, Modifier.fillMaxWidth())
            }
        }
    }
}

/** The components the app draws with, each by its composable's name. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComponentsCard(
    project: Reference,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val site = SiteTheme.colors
    Column(madeCard(modifier), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CardHeading(ReferencesSay.ComponentsTitle.of(language))
        PantrixText(
            text = ReferencesSay.componentsLead(project.components.size).of(language),
            style = small(14.5.sp),
            color = site.muted
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            project.components.forEach { name ->
                Box(
                    Modifier
                        .heightIn(min = 30.dp)
                        .landingCard(site.surfaceAlt, colors.border, corner = 8.dp)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PantrixText(text = name, style = SiteTheme.type.inlineCode.copy(fontSize = 12.5.sp))
                }
            }
        }
    }
}

/** How the app is made, a point a row, each under a hairline. */
@Composable
private fun MadeCard(
    project: Reference,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(madeCard(modifier), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CardHeading(ReferencesSay.MadeTitle.of(language))
        Column {
            project.made.forEach { point ->
                Box(Modifier.fillMaxWidth().height(1.dp).background(SiteTheme.colors.hairline))
                Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    PantrixText(text = point.title, style = strong(15.sp, FontWeight.Bold), color = colors.textPrimary)
                    PantrixText(text = point.detail, style = small(14.sp), color = colors.textSecondary)
                }
            }
        }
    }
}

/** Where another app is added: an issue on the site's repository, in a dashed box. */
@Composable
internal fun Yours(onAction: (ReferencesAction) -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    val colors = PantrixTheme.colors
    LandingBand(sidePadding(bottom = if (layout == SiteLayout.Desktop) 88.dp else 56.dp)) {
        val box = Modifier
            .fillMaxWidth()
            .dashed(colors.border, if (phone) 20.dp else 24.dp)
            .padding(if (layout == SiteLayout.Desktop) 40.dp else 24.dp)
        val words = @Composable { modifier: Modifier ->
            Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PantrixText(
                    text = ReferencesSay.YoursTitle.of(language),
                    style = landingHeading(
                        if (layout == SiteLayout.Desktop) 28.sp else 24.sp,
                        line = NAME_LINE,
                        spacing = NAME_SPACING
                    ),
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )
                PantrixText(
                    text = ReferencesSay.Yours.of(language),
                    style = small(if (phone) 15.sp else 16.sp),
                    color = colors.textSecondary
                )
            }
        }
        val add = @Composable {
            LandingButton(
                text = ReferencesSay.AddYours.of(language),
                onClick = { onAction(ReferencesAction.Open(ReferencesSay.ADD_PROJECT)) },
                tag = "addProject",
                fill = phone,
                trailing = SiteIcons.ArrowUpRight
            )
        }
        if (layout == SiteLayout.Desktop) {
            Row(
                box,
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                words(Modifier.weight(1f))
                add()
            }
        } else {
            Column(box, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                words(Modifier)
                add()
            }
        }
    }
}

/**
 * Every app but the featured one, as cards: three across on a desktop, two on a tablet, one on a phone, narrowed by
 * the platform the chips pick, and last a card that says where another is added.
 */
@Composable
internal fun AllProjects(
    state: ReferencesUiState,
    onAction: (ReferencesAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val projects = (state.projects as? LoadState.Loaded)?.value.orEmpty().drop(1)
    LandingBand(sidePadding(bottom = if (layout == SiteLayout.Desktop) 88.dp else 56.dp), gap = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PantrixText(
                text = ReferencesSay.AllTitle.of(language),
                style = landingHeading(if (layout == SiteLayout.Phone) 24.sp else 28.sp, NAME_LINE, NAME_SPACING),
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
            PantrixText(
                text = ReferencesSay.count(state.listed.size).of(language),
                style = small(14.sp),
                color = SiteTheme.colors.muted
            )
        }
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                label = ReferencesSay.All.of(language),
                count = projects.size,
                selected = state.platform == null,
                tag = "platform_all",
                onClick = { onAction(ReferencesAction.PickPlatform(null)) }
            )
            PlatformKind.entries.forEach { kind ->
                FilterChip(
                    label = ReferencesSay.platform(kind).of(language),
                    count = projects.count { it.runsOn(kind) },
                    selected = state.platform == kind,
                    tag = "platform_${kind.name.lowercase()}",
                    onClick = { onAction(ReferencesAction.PickPlatform(kind)) }
                )
            }
        }
        val columns = when (layout) {
            SiteLayout.Desktop -> DESKTOP_COLUMNS
            SiteLayout.Tablet -> 2
            SiteLayout.Phone -> 1
        }
        // Every app's card, and last the one that says where another is added: null stands for that one.
        val cards: List<Reference?> = state.listed + null
        Column(verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Phone) 16.dp else 20.dp)) {
            cards.chunked(columns).forEach { row ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    row.forEach { project ->
                        val modifier = Modifier.weight(1f).fillMaxHeight()
                        if (project == null) YoursHere(onAction, modifier) else ProjectCard(project, onAction, modifier)
                    }
                    repeat(columns - row.size) { Box(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/**
 * An app's card in the list: a picture of it, 16:10 as the design's card has it, its own screen where it has one
 * ([ReferencePictures]) and its mark where not; its name, and where its source is open a tag that leads to it; its
 * line, where it runs, how many components, and links to it and to its desktop app where it has one.
 */
@Composable
private fun ProjectCard(
    project: Reference,
    onAction: (ReferencesAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val site = SiteTheme.colors
    Column(modifier.landingCard(colors.surface, colors.border, corner = 18.dp)) {
        val picture = Modifier.fillMaxWidth().aspectRatio(CARD_PICTURE)
        val screen = ReferencePictures.of(project.key, dark = site.dark)
        if (screen != null) {
            PantrixImage(
                painter = painterResource(screen),
                modifier = picture,
                contentDescription = ReferencesSay.screen(project.address).of(language),
                contentScale = ContentScale.Crop,
                tagKey = "picture_${project.key}"
            )
        } else {
            SketchGround(picture) { AppMark(project.name, size = 56.dp) }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(site.hairline))
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                PantrixText(
                    text = project.name,
                    style = strong(17.sp),
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f).semantics { heading() }
                )
                project.source?.let { address -> SourceTag(project.key) { onAction(ReferencesAction.Open(address)) } }
            }
            PantrixText(text = project.line, style = small(14.sp), color = colors.textSecondary)
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                project.platforms.forEach { platform ->
                    Box(
                        Modifier.heightIn(min = 26.dp).background(site.hover, CircleShape)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PantrixText(
                            text = platform.name,
                            style = strong(12.5.sp, FontWeight.Bold),
                            color = colors.textSecondary
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(site.hairline))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PantrixText(
                    text = ReferencesSay.components(project.components.size).of(language),
                    style = small(13.sp),
                    color = site.muted,
                    modifier = Modifier.weight(1f)
                )
                project.download?.let { address ->
                    PantrixLink(
                        text = ReferencesSay.Download.of(language) + " →",
                        onClick = { onAction(ReferencesAction.Open(address)) },
                        style = strong(14.sp, FontWeight.Bold),
                        tagKey = "download_${project.key}",
                        underline = LinkUnderline.OnHover
                    )
                }
                PantrixLink(
                    text = ReferencesSay.Visit.of(language) + " →",
                    onClick = { onAction(ReferencesAction.Open(project.url)) },
                    style = strong(14.sp, FontWeight.Bold),
                    tagKey = "visit_${project.key}",
                    underline = LinkUnderline.OnHover
                )
            }
        }
    }
}

/**
 * The tag of an app whose source is open, on its card in the list: the featured card's "Open source", as a button that
 * leads to the source, since the card has no room for a button of its own.
 */
@Composable
private fun SourceTag(
    key: String,
    onClick: () -> Unit
) {
    val language = LocalLanguage.current
    val success = PantrixTheme.colors.success
    PantrixButton(
        onClick = onClick,
        contentDescription = "${ReferencesSay.OpenSource.of(language)}: ${ReferencesSay.Source.of(language)}",
        variant = ButtonVariant.Solid,
        size = ButtonSize.Small,
        style = ButtonStyle(small = ButtonSizeStyle(height = 28.dp, horizontalPadding = 12.dp)),
        shape = CircleShape,
        colors = ButtonColors(container = success.container, content = success.onContainer),
        tagKey = "source_$key"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixText(text = ReferencesSay.OpenSource.of(language), style = strong(12.5.sp), maxLines = 1)
            PantrixIcon(imageVector = SiteIcons.ArrowUpRight, contentDescription = null, size = 12.dp)
        }
    }
}

/** The last card of the list: where another app is added. */
@Composable
private fun YoursHere(
    onAction: (ReferencesAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(
        modifier.heightIn(min = 280.dp).dashed(colors.border, 18.dp).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        PantrixText(
            text = ReferencesSay.YoursHereTitle.of(language),
            style = strong(19.sp),
            color = colors.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
        PantrixText(text = ReferencesSay.YoursHere.of(language), style = small(14.sp), color = colors.textSecondary)
        LandingButton(
            text = ReferencesSay.AddYours.of(language),
            onClick = { onAction(ReferencesAction.Open(ReferencesSay.ADD_PROJECT)) },
            tag = "addProjectCard",
            fill = SiteTheme.layout == SiteLayout.Phone,
            trailing = SiteIcons.ArrowUpRight
        )
    }
}

/** A card's heading in the part that says what the app is made of. */
@Composable
private fun CardHeading(text: String) {
    PantrixText(
        text = text,
        style = strong(20.sp),
        color = PantrixTheme.colors.textPrimary,
        modifier = Modifier.semantics { heading() }
    )
}

/** A card of what the app is made of: the page's surface, its border and room inside. */
@Composable
private fun madeCard(modifier: Modifier): Modifier {
    val colors = PantrixTheme.colors
    val phone = SiteTheme.layout == SiteLayout.Phone
    return modifier.landingCard(colors.surface, colors.border, corner = 20.dp).padding(if (phone) 20.dp else 28.dp)
}

/** A band's sides, [bottom] under it and nothing over it: the parts under the page's title follow on from it. */
@Composable
private fun sidePadding(bottom: Dp): PaddingValues {
    val side = if (SiteTheme.layout == SiteLayout.Phone) 16.dp else 24.dp
    return PaddingValues(start = side, end = side, bottom = bottom)
}

/** A dashed edge of [color] around what it stands on, its corners [corner] round, as the design's empty places have. */
private fun Modifier.dashed(
    color: Color,
    corner: Dp
): Modifier = drawBehind {
    val width = DASH_WIDTH.toPx()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(corner.toPx()),
        style = Stroke(width = width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH, DASH)))
    )
}

/** The site's body type at [size] in [weight], its lines as far apart as the page's. */
@Composable
internal fun strong(
    size: TextUnit,
    weight: FontWeight = FontWeight.ExtraBold
): TextStyle = SiteTheme.type.body.copy(fontSize = size, lineHeight = (size.value * LINE).sp, fontWeight = weight)

/** The site's small type at [size], its lines as far apart as the page's. */
@Composable
internal fun small(size: TextUnit): TextStyle =
    SiteTheme.type.bodySmall.copy(fontSize = size, lineHeight = (size.value * LINE).sp)

/** How much wider the picture is than the words beside it, on a desktop. */
private const val SKETCH_SHARE = 1.05f

/** How much wider the components' card is than the one beside it, on a desktop. */
private const val COMPONENTS_SHARE = 1.3f

/** How large the sketch is drawn on a desktop, and on a phone, than on a tablet. */
private const val DESKTOP_SKETCH = 0.92f
private const val PHONE_SKETCH = 0.6f

/** How round a mark's corners are, a share of its size. */
private const val MARK_CORNER = 0.29f

/** How many apps' cards stand side by side on a desktop. */
private const val DESKTOP_COLUMNS = 3

/** How wide the picture of an app's card is to how tall: 16:10, as the design's card and a desktop's screen are. */
private const val CARD_PICTURE = 16f / 10f

private val DASH_WIDTH = 1.5.dp
private const val DASH = 10f

/** How far apart a label's capitals are: about a twelfth of their size. */
private val LABEL_SPACING = 1.sp
private const val NAME_LINE = 1.25f
private const val NAME_SPACING = -0.02f
private const val FACT_LINE = 1.1f
private const val LINE = 1.5f
