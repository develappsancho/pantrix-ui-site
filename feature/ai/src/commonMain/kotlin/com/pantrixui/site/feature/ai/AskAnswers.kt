package com.pantrixui.site.feature.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.container.accordion.AccordionSection
import com.pantrixui.components.container.accordion.PantrixAccordion
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.data.tag.TagVariant
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.chip.ChipStyle
import com.pantrixui.components.elements.chip.PantrixChip
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.iconbutton.PantrixIconButton
import com.pantrixui.components.elements.link.PantrixLink
import com.pantrixui.components.elements.skeleton.PantrixSkeleton
import com.pantrixui.components.elements.spinner.PantrixSpinner
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.SmallCode
import com.pantrixui.site.core.designsystem.content.landingCard
import com.pantrixui.site.core.designsystem.content.withCode
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.AskComponent
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ComponentGroups
import com.pantrixui.site.core.model.ComponentHit
import com.pantrixui.site.core.model.DeclarationDetail
import com.pantrixui.site.core.model.DemoPages
import com.pantrixui.site.core.model.DocumentHit
import com.pantrixui.site.core.model.DocumentHits
import com.pantrixui.site.core.model.Highlighter
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.routing.SiteRoutes

/** The tools called, under their heading: one under another on a desktop, two to a row on a tablet. */
@Composable
internal fun ToolsCalled(
    steps: List<AskStep>,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    Column(
        modifier.semantics {
            liveRegion = LiveRegionMode.Polite
        },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PantrixText(
            text = AnswerSay.ToolsCalled.of(language).uppercase(),
            style = label(),
            color = SiteTheme.colors.muted
        )
        if (SiteTheme.layout == SiteLayout.Desktop) {
            steps.forEach { ToolStep(it, Modifier.fillMaxWidth()) }
        } else {
            steps.chunked(2).forEach { pair ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { ToolStep(it, Modifier.weight(1f).fillMaxHeight()) }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/** A phone's tools called, folded under a line that counts them, which opens them. */
@Composable
internal fun ToolsFolded(
    steps: List<AskStep>,
    open: Boolean,
    searching: Boolean,
    onToggle: () -> Unit
) {
    val language = LocalLanguage.current
    val title = AnswerSay.toolsCalled(steps.size).of(language)
    PantrixAccordion(
        sections = listOf(
            AccordionSection(
                key = TOOLS,
                title = title,
                icon = null,
                tagKey = "askTools",
                trailing = if (searching) ({ PantrixSpinner(size = 14.dp) }) else null
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.forEach { ToolStep(it, Modifier.fillMaxWidth()) }
                }
            }
        ),
        expanded = if (open) setOf(TOOLS) else emptySet(),
        onExpandedChange = { onToggle() },
        modifier = Modifier.fillMaxWidth(),
        tagKey = "askToolsFolded"
    )
}

/** A tool called: the call as code, and what it got, or that it is on its way. */
@Composable
private fun ToolStep(
    step: AskStep,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(
        modifier.landingCard(
            colors.surface,
            colors.border,
            corner = 12.dp
        ).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PantrixText(
            text = "${step.tool}(\"${step.argument}\")",
            style = mono(12.sp),
            color = colors.textPrimary,
            maxLines = if (SiteTheme.layout == SiteLayout.Phone) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis
        )
        val result = step.result
        if (result == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PantrixSpinner(size = 13.dp, contentDescription = null)
                PantrixText(
                    text = AnswerSay.Asking.of(language),
                    style = small(12.5.sp),
                    color = SiteTheme.colors.muted
                )
            }
        } else {
            PantrixText(
                text = AnswerSay.result(result).of(language),
                style = small(12.5.sp),
                color = if (result == AskResult.NoAnswer) colors.danger.onContainer else colors.textSecondary
            )
        }
    }
}

/** A component's first short example, in a block that copies it; its place held while the card is on its way. */
@Composable
private fun Snippet(card: AskComponent?) {
    val phone = SiteTheme.layout == SiteLayout.Phone
    val snippet = card?.snippets?.firstOrNull()
    when {
        card == null -> PantrixSkeleton(
            Modifier.fillMaxWidth().height(SNIPPET_PLACE),
            shape = RoundedCornerShape(12.dp)
        )

        snippet != null -> SmallCode(
            code = Block.Code("kotlin", lines = Highlighter.lines(snippet.code, "kotlin")),
            textSize = if (phone) 12.sp else 12.5.sp,
            line = CODE_LINE,
            horizontalPadding = if (phone) 14.dp else 16.dp,
            verticalPadding = 12.dp,
            corner = 12.dp,
            title = "${AnswerSay.Example.of(LocalLanguage.current)} · ${snippet.title}",
            onCopy = {}
        )
    }
}

/**
 * A component's card, from what the search found of it and, as they come, its short examples and its page's
 * examples; the documents that speak of it where the scope reads them; and the other components the search found.
 */
@Composable
internal fun ComponentAnswer(
    answer: AskAnswer.Component,
    onAction: (AskAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val hit = answer.hits.firstOrNull { it.function == answer.shown } ?: return
    val card = answer.card
    val page = card?.page ?: hit.page
    val open = { section: String? -> onAction(AskAction.Open(componentAddress(page, language, section))) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        AnswerCard {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                PantrixText(
                    text = hit.name,
                    style = strong(if (layout == SiteLayout.Phone) 21.sp else 22.sp),
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )
                PantrixTag(text = ComponentGroups.title(hit.category).of(language), tone = TagTone.Primary)
                Spacer(Modifier.weight(1f))
                if (layout != SiteLayout.Phone) {
                    PantrixLink(
                        text = AnswerSay.OpenPage.of(language),
                        onClick = { open(null) },
                        style = small(14.sp).copy(fontWeight = FontWeight.Bold),
                        tagKey = "askOpenPage"
                    )
                }
            }
            PantrixText(
                text = "${hit.function} · ${hit.artifact}",
                style = mono(if (layout == SiteLayout.Phone) 12.sp else 12.5.sp),
                color = SiteTheme.colors.muted
            )
            PantrixText(
                text = (card?.description ?: hit.description),
                style = small(15.sp),
                color = colors.textSecondary
            )
            ImportLine(hit.import, onAction)
            Snippet(card)
            Keywords(card?.keywords ?: hit.keywords)
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                answer.examples?.let { examples ->
                    PantrixLink(
                        text = AnswerSay.examples(examples.examples.size).of(language),
                        onClick = { open(EXAMPLES) },
                        style = small(14.sp).copy(fontWeight = FontWeight.Bold),
                        tagKey = "askExamples",
                        modifier = Modifier.heightIn(min = touch())
                    )
                }
                PantrixLink(
                    text = AnswerSay.ApiReference.of(language),
                    onClick = { open(API) },
                    style = small(14.sp).copy(fontWeight = FontWeight.Bold),
                    tagKey = "askApi",
                    modifier = Modifier.heightIn(min = touch())
                )
            }
            if (layout == SiteLayout.Phone) {
                LandingButton(
                    text = AnswerSay.OpenPage.of(language),
                    onClick = { open(null) },
                    tag = "askOpenPage",
                    kind = com.pantrixui.site.core.designsystem.content.LandingButtonKind.Outline,
                    fill = true
                )
            }
            val documents = answer.documents?.documents.orEmpty()
            if (documents.isNotEmpty()) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(SiteTheme.colors.hairline))
                PantrixText(
                    text = AnswerSay.InDocuments.of(language).uppercase(),
                    style = label(),
                    color = SiteTheme.colors.muted
                )
                documents.forEach { DocumentRow(it, onAction, compact = true) }
            }
        }
        AlsoMatches(answer.hits.filter { it.function != answer.shown }.take(ALSO), onAction)
    }
}

/** The import of what was found, as one line of code, with a button that copies it. */
@Composable
private fun ImportLine(
    import: String,
    onAction: (AskAction) -> Unit
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .landingCard(SiteTheme.colors.surfaceAlt, colors.border, corner = 10.dp)
            .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = "import $import",
            style = mono(12.5.sp),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        PantrixIconButton(
            imageVector = PantrixIcons.Copy,
            contentDescription = AnswerSay.CopyImport.of(language),
            onClick = { onAction(AskAction.Copy("import $import")) },
            variant = ButtonVariant.Outline,
            size = if (SiteTheme.layout == SiteLayout.Desktop) ButtonSize.Small else ButtonSize.Medium,
            tagKey = "askCopyImport"
        )
    }
}

/** The words a search also finds it by. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Keywords(keywords: List<String>) {
    if (keywords.isEmpty()) return
    val language = LocalLanguage.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PantrixText(
            text = AnswerSay.AlsoFound.of(language),
            style = small(13.sp),
            color = SiteTheme.colors.muted,
            modifier = Modifier.padding(end = 4.dp)
        )
        keywords.forEach { PantrixTag(text = it, variant = TagVariant.Outline) }
    }
}

/** The other components the search found, each a button that shows its card. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlsoMatches(
    others: List<ComponentHit>,
    onAction: (AskAction) -> Unit
) {
    if (others.isEmpty()) return
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PantrixText(
            text = AnswerSay.AlsoMatches.of(language).uppercase(),
            style = label(),
            color = SiteTheme.colors.muted
        )
        val buttons: @Composable () -> Unit = {
            others.forEach { other ->
                PantrixButton(
                    text = "${other.name} · ${ComponentGroups.title(other.category).of(language)}",
                    onClick = { onAction(AskAction.ShowComponent(other.function)) },
                    variant = ButtonVariant.Outline,
                    size = if (layout == SiteLayout.Desktop) ButtonSize.Small else ButtonSize.Medium,
                    tagKey = "askAlso_${other.page}"
                )
            }
        }
        if (layout == SiteLayout.Phone) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                buttons()
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                buttons()
            }
        }
    }
}

/** The declarations found, each a row that opens to its signature and what it holds. */
@Composable
internal fun ApiAnswer(
    answer: AskAnswer.Api,
    onAction: (AskAction) -> Unit
) {
    val language = LocalLanguage.current
    val hits = answer.hits
    AnswerCard {
        Heading(
            AnswerSay.Declarations.of(language),
            AnswerSay.declarations(hits.total, hits.declarations.size).of(language)
        )
        PantrixAccordion(
            sections = hits.declarations.map { hit ->
                AccordionSection(
                    key = hit.title,
                    title = hit.title,
                    subtitle = hit.description,
                    tagKey = "askDeclaration_${hit.title}",
                    trailing = { PantrixTag(text = hit.kind, tone = TagTone.Primary) }
                ) {
                    DeclarationOpened(hit.import, hit.artifact, answer.details[hit.title], onAction)
                }
            },
            expanded = setOfNotNull(answer.open),
            onExpandedChange = { now ->
                val toggled = (now - setOfNotNull(answer.open)).firstOrNull() ?: answer.open
                toggled?.let { onAction(AskAction.ToggleDeclaration(it)) }
            },
            singleExpanded = true,
            tagKey = "askDeclarations"
        )
        if (hits.total > hits.declarations.size) {
            PantrixButton(
                text = AnswerSay.showOthers(hits.total - hits.declarations.size).of(language),
                onClick = { onAction(AskAction.MoreDeclarations) },
                variant = ButtonVariant.Outline,
                size = if (SiteTheme.layout == SiteLayout.Desktop) ButtonSize.Small else ButtonSize.Medium,
                tagKey = "askMoreDeclarations"
            )
        }
    }
}

/** An opened declaration: its import, and once read, its signature, its entries or parameters and its page. */
@Composable
private fun DeclarationOpened(
    import: String,
    artifact: String,
    details: List<DeclarationDetail>?,
    onAction: (AskAction) -> Unit
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PantrixText(text = "import $import · $artifact", style = mono(12.5.sp), color = SiteTheme.colors.muted)
        if (details == null) {
            PantrixSkeleton(Modifier.fillMaxWidth().height(SIGNATURE_PLACE), shape = RoundedCornerShape(10.dp))
            return@Column
        }
        details.forEach { detail ->
            SmallCode(
                code = Block.Code("kotlin", lines = Highlighter.lines(detail.signature, "kotlin")),
                textSize = 12.5.sp,
                line = CODE_LINE,
                horizontalPadding = 14.dp,
                verticalPadding = 10.dp,
                corner = 10.dp
            )
            val parts = detail.entries.ifEmpty { detail.parameters }
            if (parts.isNotEmpty()) {
                PantrixText(
                    text = (if (detail.entries.isNotEmpty()) AnswerSay.Entries else AnswerSay.Parameters).of(
                        language
                    ).uppercase(),
                    style = label(),
                    color = SiteTheme.colors.muted
                )
                parts.forEach { part ->
                    PartLine(part.name, part.description)
                }
            }
            detail.pages.firstOrNull()?.let { page ->
                PantrixLink(
                    text = AnswerSay.OnItsPage.of(language),
                    onClick = { onAction(AskAction.Open(pageAddress(page, language))) },
                    style = small(14.sp).copy(fontWeight = FontWeight.Bold),
                    tagKey = "askDeclarationPage",
                    modifier = Modifier.heightIn(min = touch())
                )
            }
        }
        if (details.isEmpty()) {
            PantrixText(
                text = AnswerSay.result(AskResult.Nothing).of(language),
                style = small(14.sp),
                color = colors.textSecondary
            )
        }
    }
}

/** An entry or a parameter: its name as code and what it is for, beside it, or under it on a phone. */
@Composable
private fun PartLine(
    name: String,
    description: String
) {
    val colors = PantrixTheme.colors
    if (SiteTheme.layout == SiteLayout.Phone) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            PantrixText(text = name, style = mono(13.sp, FontWeight.SemiBold), color = colors.textPrimary)
            PantrixText(text = description, style = small(14.sp), color = colors.textSecondary)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PantrixText(
                text = name,
                style = mono(13.sp, FontWeight.SemiBold),
                color = colors.textPrimary,
                modifier = Modifier.width(PART_NAME)
            )
            PantrixText(
                text = description,
                style = small(14.sp),
                color = colors.textSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** The icons found, each drawn by the library itself, with its name, what it shows and a button that copies it. */
@Composable
internal fun IconsAnswer(
    answer: AskAnswer.Icons,
    onAction: (AskAction) -> Unit
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val icons = answer.hits.icons
    BoxWithConstraints {
        // Two to a row where each has room for its name; one where the answer's column is narrow.
        val perRow = if (maxWidth < TWO_ICONS) 1 else 2
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Heading(AnswerSay.Icons.of(language), AnswerSay.icons(answer.hits.total).of(language))
            icons.chunked(perRow).forEach { row ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { icon ->
                        Row(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .landingCard(colors.surface, colors.border)
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                Modifier.size(
                                    ICON_TILE
                                ).landingCard(SiteTheme.colors.surfaceAlt, colors.border, corner = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AskIcons.of(icon.name)?.let {
                                    PantrixIcon(
                                        imageVector = it,
                                        contentDescription = null,
                                        tint = colors.textPrimary,
                                        size = 24.dp
                                    )
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PantrixText(
                                    text = icon.reference,
                                    style = mono(13.sp, FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                PantrixText(
                                    text = icon.description,
                                    style = small(13.5.sp),
                                    color = colors.textSecondary
                                )
                                PantrixTag(
                                    text = (if (icon.mirrored) AnswerSay.Mirrors else AnswerSay.NeverMirrors).of(
                                        language
                                    ),
                                    variant = TagVariant.Outline
                                )
                            }
                            PantrixIconButton(
                                imageVector = PantrixIcons.Copy,
                                contentDescription = AnswerSay.copyIcon(icon.reference).of(language),
                                onClick = { onAction(AskAction.Copy(icon.reference)) },
                                variant = ButtonVariant.Outline,
                                size = if (SiteTheme.layout ==
                                    SiteLayout.Desktop
                                ) {
                                    ButtonSize.Small
                                } else {
                                    ButtonSize.Medium
                                },
                                tagKey = "askCopyIcon_${icon.name}"
                            )
                        }
                    }
                    if (row.size < perRow) Spacer(Modifier.weight(1f))
                }
            }
            val (start, middle, end) = AnswerSay.IconsDrawn
            PantrixText(
                text = twoCodes(
                    start.of(language),
                    "PantrixIcons",
                    middle.of(language),
                    "com.pantrixui:foundation",
                    end.of(language)
                ),
                style = small(13.sp),
                color = SiteTheme.colors.muted
            )
        }
    }
}

/** The documents found, each a card that leads to it. */
@Composable
internal fun DocumentsAnswer(
    hits: DocumentHits,
    onAction: (AskAction) -> Unit
) {
    val language = LocalLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading(AnswerSay.InDocuments.of(language), AnswerSay.pages(hits.total).of(language))
        hits.documents.forEach { DocumentRow(it, onAction, compact = false) }
    }
}

/**
 * A document found, which leads to it on the site, or to the skill's reference the site serves: what it is and its
 * key, its title, its first sentence and, where it is not [compact], the line of it that holds the question.
 */
@Composable
private fun DocumentRow(
    hit: DocumentHit,
    onAction: (AskAction) -> Unit,
    compact: Boolean
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(if (compact) 10.dp else 14.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .landingCard(colors.surface, colors.border, corner = if (compact) 10.dp else 14.dp)
            .siteClickable(tag = "askDocument_${hit.key}", role = Role.Button, shape = shape) {
                onAction(documentAction(hit, language))
            }
            .padding(horizontal = if (compact) 12.dp else 18.dp, vertical = if (compact) 10.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 6.dp)
    ) {
        if (!compact) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PantrixTag(text = AnswerSay.kind(hit.kind).of(language), tone = TagTone.Primary)
                PantrixText(text = hit.key, style = mono(12.sp), color = SiteTheme.colors.muted)
            }
        }
        PantrixText(
            text = hit.title,
            style = strong(if (compact) 14.sp else 16.sp, FontWeight.Bold),
            color = colors.textPrimary
        )
        PantrixText(text = hit.summary, style = small(if (compact) 13.5.sp else 14.sp), color = colors.textSecondary)
        val line = hit.line
        if (!compact && line != null) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(Modifier.width(3.dp).fillMaxHeight().background(SiteTheme.colors.accentLine))
                PantrixText(
                    text = line,
                    style = small(13.sp),
                    color = colors.textSecondary,
                    modifier = Modifier
                        .weight(1f)
                        .background(SiteTheme.colors.surfaceAlt, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/** A question that found nothing: what it was, and words to ask in its place. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NothingCard(
    question: String,
    onAction: (AskAction) -> Unit,
    field: TextFieldState
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(
        Modifier.fillMaxWidth().landingCard(
            SiteTheme.colors.surfaceAlt,
            colors.border,
            corner = 16.dp
        ).padding(cardPadding())
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PantrixText(
            text = AnswerSay.nothingFor(question).of(language),
            style = strong(18.sp),
            color = colors.textPrimary
        )
        PantrixText(text = AnswerSay.NothingLine.of(language), style = small(15.sp), color = colors.textSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AskSay.suggestions(language).forEachIndexed { index, word ->
                PantrixChip(
                    label = word,
                    onClick = {
                        field.setTextAndPlaceCursorAtEnd(word)
                        onAction(AskAction.Ask(word))
                    },
                    style = ChipStyle(height = touch()),
                    tagKey = "askSuggestion_$index"
                )
            }
        }
    }
}

/** A question the server did not answer: why that may be, and the ways on. */
@Composable
internal fun NoAnswerCard(onAction: (AskAction) -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    Column(
        Modifier.fillMaxWidth().landingCard(
            SiteTheme.colors.surfaceAlt,
            colors.border,
            corner = 16.dp
        ).padding(cardPadding())
            .semantics { liveRegion = LiveRegionMode.Assertive },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixIcon(
                imageVector = PantrixIcons.Error,
                contentDescription = null,
                tint = colors.danger.onContainer,
                size = 20.dp
            )
            PantrixText(
                text = AnswerSay.NoAnswer.of(language),
                style = strong(if (layout == SiteLayout.Phone) 17.sp else 18.sp),
                color = colors.textPrimary
            )
        }
        val (start, end) = AnswerSay.NoAnswerLine
        PantrixText(
            text = withCode(start.of(language), mcpHost(), end.of(language), 13.sp),
            style = small(15.sp),
            color = colors.textSecondary
        )
        val again: @Composable () -> Unit = {
            LandingButton(
                text = AnswerSay.AskAgain.of(language),
                onClick = { onAction(AskAction.Retry) },
                tag = "askAgain",
                height = if (layout == SiteLayout.Desktop) 40.dp else 48.dp,
                padding = 18.dp,
                fill = layout == SiteLayout.Phone
            )
        }
        val elsewhere: @Composable () -> Unit = {
            PantrixLink(
                text = AnswerSay.InYourAssistant.of(language),
                onClick = { onAction(AskAction.PickTab(AskTab.Assistant)) },
                style = small(14.sp).copy(fontWeight = FontWeight.Bold),
                tagKey = "askElsewhere",
                modifier = Modifier.heightIn(min = touch())
            )
        }
        if (layout == SiteLayout.Phone) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                again()
                elsewhere()
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                again()
                elsewhere()
            }
        }
    }
}

/** An answer on its way: a card's shape, hidden from a screen reader, which hears the tools' line instead. */
@Composable
internal fun AnswerSkeleton() {
    val colors = PantrixTheme.colors
    Column(
        Modifier.fillMaxWidth().landingCard(colors.surface, colors.border, corner = 16.dp).padding(cardPadding()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PantrixSkeleton(Modifier.fillMaxWidth(TITLE_SHARE).height(22.dp), contentDescription = null)
        PantrixSkeleton(Modifier.fillMaxWidth(LINE_SHARE).height(14.dp), contentDescription = null)
        PantrixSkeleton(
            Modifier.fillMaxWidth().height(SNIPPET_PLACE),
            shape = RoundedCornerShape(12.dp),
            contentDescription = null
        )
    }
}

/** A card of the answer, at each size's room. */
@Composable
private fun AnswerCard(content: @Composable () -> Unit) {
    val colors = PantrixTheme.colors
    Column(
        Modifier.fillMaxWidth().landingCard(colors.surface, colors.border, corner = 16.dp).padding(cardPadding()),
        verticalArrangement = Arrangement.spacedBy(if (SiteTheme.layout == SiteLayout.Phone) 12.dp else 14.dp)
    ) { content() }
}

/** A list's heading, and how many it holds beside it. */
@Composable
private fun Heading(
    title: String,
    count: String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        PantrixText(
            text = title,
            style = strong(18.sp),
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
        PantrixText(text = count, style = small(13.sp), color = SiteTheme.colors.muted)
    }
}

/** The words a list's label is written in: small capitals, wide apart. */
@Composable
private fun label() = small(12.sp).copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.7.sp)

/** The room inside an answer's card, in each size. */
@Composable
private fun cardPadding() = when (SiteTheme.layout) {
    SiteLayout.Desktop -> 24.dp
    SiteLayout.Tablet -> 22.dp
    SiteLayout.Phone -> 18.dp
}

/** A control's least height: a finger's on a tablet and a phone. */
@Composable
private fun touch() = if (SiteTheme.layout == SiteLayout.Desktop) 0.dp else TOUCH

/** [start], then [first] as code, [middle], [second] as code, and [end]. */
@Composable
private fun twoCodes(
    start: String,
    first: String,
    middle: String,
    second: String,
    end: String
): AnnotatedString {
    val code =
        SpanStyle(
            fontFamily = SiteTheme.type.inlineCode.fontFamily,
            fontSize = 12.sp,
            color = PantrixTheme.colors.textPrimary
        )
    return buildAnnotatedString {
        append(start)
        withStyle(code) { append(first) }
        append(middle)
        withStyle(code) { append(second) }
        append(end)
    }
}

/** Where the page of the component [page] is in [language], at its [section] where one is given. */
internal fun componentAddress(
    page: String,
    language: Language,
    section: String? = null
): String = SiteRoutes.addressOf(Route.Component(page), language) + (section?.let { "#$it" } ?: "")

/** Where the demo's page [key] stands on the site in [language]: a component's page, or the place the site has it. */
internal fun pageAddress(
    key: String,
    language: Language
): String {
    val (path, section) = DemoPages.placeOf(key) ?: return componentAddress(key, language)
    return SiteRoutes.addressOf(SiteRoutes.routeOf(path), language) + (section?.let { "#$it" } ?: "")
}

/** What opens the document [hit]: a page or a guide of the site, or the skill's reference the site serves. */
internal fun documentAction(
    hit: DocumentHit,
    language: Language
): AskAction = when (hit.kind) {
    GUIDE -> AskAction.Open(SiteRoutes.addressOf(Route.Guide(hit.key.removePrefix("guide/")), language))

    REFERENCE -> AskAction.OpenUrl(
        "${SiteAddress.origin}/llms/skills/pantrix-ui/references/${hit.key.removePrefix("skill/")}.md"
    )

    else -> AskAction.Open(pageAddress(hit.key, language))
}

private const val TOOLS = "tools"
private const val GUIDE = "guide"
private const val REFERENCE = "reference"

/** The sections of a component's page the card's links lead to. */
private const val EXAMPLES = "examples"
private const val API = "api"

/** How many other components the card offers. */
private const val ALSO = 4

private const val CODE_LINE = 1.7f
private const val TITLE_SHARE = 0.4f
private const val LINE_SHARE = 0.82f
private val SNIPPET_PLACE = 200.dp
private val SIGNATURE_PLACE = 40.dp
private val ICON_TILE = 52.dp

/** How wide the icons' list is at least for two to a row. */
private val TWO_ICONS = 600.dp
private val PART_NAME = 130.dp
