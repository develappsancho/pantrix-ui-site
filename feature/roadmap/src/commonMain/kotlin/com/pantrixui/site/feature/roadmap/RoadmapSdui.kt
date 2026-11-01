package com.pantrixui.site.feature.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.Hairline
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.landingBandPadding
import com.pantrixui.site.core.designsystem.content.landingCard
import com.pantrixui.site.core.designsystem.content.withCode
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.TokenKind

/**
 * Server-driven UI: what it is, a sample description beside the screen Pantrix draws from it, what keeps it safe,
 * and on a desktop its steps and what assistants may get.
 */
@Composable
internal fun Sdui(
    state: RoadmapUiState,
    onAction: (RoadmapAction) -> Unit,
    modifier: Modifier
) {
    val layout = SiteTheme.layout
    val pick: (Sample) -> Unit = { onAction(RoadmapAction.PickSample(it)) }
    val gap = when (layout) {
        SiteLayout.Desktop -> 28.dp
        SiteLayout.Tablet -> 18.dp
        SiteLayout.Phone -> 14.dp
    }
    LandingBand(landingBandPadding(), modifier, ground = SiteTheme.colors.canvas, gap = gap) {
        when (layout) {
            SiteLayout.Desktop -> {
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.Bottom) {
                    SduiWords(Modifier.weight(1f))
                    SampleTabs(state.sample, pick)
                }
                DescriptionBesidePreview(
                    description = { Description(state.sample, Modifier.fillMaxSize()) },
                    checked = { Checked(Modifier.fillMaxSize()) },
                    preview = { Preview(state.sample) }
                )
                Principles()
                StepsAndAssistants()
            }

            SiteLayout.Tablet -> {
                SduiWords(Modifier)
                SampleTabs(state.sample, pick)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Description(state.sample, Modifier.weight(1f))
                    Preview(state.sample)
                }
                Principles()
            }

            SiteLayout.Phone -> {
                SduiWords(Modifier)
                SampleTabs(state.sample, pick)
                Description(state.sample, Modifier.fillMaxWidth())
                Checked(Modifier.fillMaxWidth())
                Preview(state.sample)
                Principles()
            }
        }
    }
}

/**
 * A desktop's description, what it is checked for and the screen drawn from it, side by side: the screen as tall as
 * its content, and the other two as tall as the taller of it and the description. A row of intrinsic heights would
 * ask the components of the screen for theirs, and a button answers less than it draws.
 */
@Composable
private fun DescriptionBesidePreview(
    description: @Composable () -> Unit,
    checked: @Composable () -> Unit,
    preview: @Composable () -> Unit
) {
    Layout(
        contents = listOf(description, checked, { Box(contentAlignment = Alignment.TopCenter) { preview() } }),
        modifier = Modifier.fillMaxWidth()
    ) { (descriptions, checks, previews), constraints ->
        val gap = SIDE_GAP.roundToPx()
        val previewWidth = PREVIEW_ROOM.roundToPx()
        val checkedWidth = CHECKED_WIDTH.roundToPx()
        val shown = previews.first().measure(Constraints(previewWidth, previewWidth, 0, Constraints.Infinity))
        val descriptionWidth = (constraints.maxWidth - previewWidth - checkedWidth - gap * 2).coerceAtLeast(0)
        val written = descriptions.first().measure(
            Constraints(descriptionWidth, descriptionWidth, shown.height, Constraints.Infinity)
        )
        val height = maxOf(written.height, shown.height)
        val check = checks.first().measure(Constraints.fixed(checkedWidth, height))
        layout(constraints.maxWidth, height) {
            written.place(0, 0)
            check.place(descriptionWidth + gap, 0)
            shown.place(descriptionWidth + gap + checkedWidth + gap, 0)
        }
    }
}

/** The part's pill, heading and lead. */
@Composable
private fun SduiWords(modifier: Modifier) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Desktop) 12.dp else 18.dp)) {
        Pill(SduiSay.Pill.of(layout).of(language), colors.primary.container, colors.primary.onContainer)
        PantrixText(
            text = SduiSay.Title.of(layout).of(language),
            style = when (layout) {
                SiteLayout.Desktop -> sectionHeading(38.sp, line = BIG_HEADING_LINE)
                SiteLayout.Tablet -> sectionHeading(32.sp)
                SiteLayout.Phone -> sectionHeading(26.sp)
            },
            color = colors.textPrimary
        )
        PantrixText(
            text = SduiSay.Lead.of(layout).of(language),
            style = small(
                when (layout) {
                    SiteLayout.Desktop -> 16.5.sp
                    SiteLayout.Tablet -> 16.sp
                    SiteLayout.Phone -> 15.sp
                }
            ),
            color = colors.textSecondary
        )
    }
}

/** The two samples to choose between: as wide as the column on a phone. */
@Composable
private fun SampleTabs(
    sample: Sample,
    onPick: (Sample) -> Unit
) {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    PantrixSegmentButton(
        options = Sample.entries.map {
            SegmentOption(it, SduiSay.sampleName(it).of(language), tagKey = it.name.lowercase())
        },
        selected = sample,
        onSelect = onPick,
        modifier = if (phone) Modifier.fillMaxWidth() else Modifier,
        fullWidth = phone,
        tagKey = "sduiSample"
    )
}

/** The sample's description as the server sends it, in its colours, under a header that says it is a sketch. */
@Composable
private fun Description(
    sample: Sample,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val code = SiteTheme.colors.code
    val (corner, header, side) = when (layout) {
        SiteLayout.Desktop -> Triple(18.dp, 44.dp, 18.dp)
        SiteLayout.Tablet -> Triple(16.dp, 40.dp, 14.dp)
        SiteLayout.Phone -> Triple(14.dp, 38.dp, 12.dp)
    }
    Column(modifier.landingCard(code.background, code.background, corner = corner)) {
        CodeHeader(header, if (layout == SiteLayout.Desktop) 16.dp else side) {
            PantrixText(
                text = SduiSay.FromServer.of(language),
                style = strong(if (layout == SiteLayout.Desktop) 12.5.sp else 12.sp, FontWeight.Bold),
                color = code.onSelectedTab
            )
            Spacer(Modifier.weight(1f))
            val note = SduiSay.Sketch.of(layout).of(language)
            if (layout == SiteLayout.Desktop) {
                Pill(note, SKETCH_GROUND, SKETCH_INK, size = 11.5.sp)
            } else {
                PantrixText(text = note, style = strong(11.sp, FontWeight.Bold), color = SKETCH_INK)
            }
        }
        val (size, line) = when (layout) {
            SiteLayout.Desktop -> 12.5.sp to DESKTOP_CODE_LINE
            SiteLayout.Tablet -> 11.5.sp to CODE_LINE
            SiteLayout.Phone -> 11.sp to CODE_LINE
        }
        CodeLines(
            SduiSay.lines(sample, layout, language),
            size,
            line,
            side,
            Modifier.padding(vertical = if (layout == SiteLayout.Desktop) 10.dp else 8.dp)
        )
    }
}

/** A dark block's header: [height] tall, its content [side] from the edges, a line under it. */
@Composable
internal fun CodeHeader(
    height: Dp,
    side: Dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        Modifier.fillMaxWidth().height(height).padding(horizontal = side),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
    Hairline(color = SiteTheme.colors.code.line)
}

/** Lines of code in their colours, [size] and [line] times that apart, [side] from the edges; they scroll sideways. */
@Composable
internal fun CodeLines(
    lines: List<CodeLine>,
    size: TextUnit,
    line: Float,
    side: Dp,
    modifier: Modifier = Modifier
) {
    val code = SiteTheme.colors.code
    Column(modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        lines.forEach {
            PantrixText(
                text = it.text,
                style = SiteTheme.type.code.copy(fontSize = size, lineHeight = (size.value * line).sp),
                color = when (it.kind) {
                    TokenKind.Keyword -> code.keyword
                    TokenKind.StringLiteral -> code.literal
                    TokenKind.Number -> code.number
                    TokenKind.Comment -> code.comment
                    TokenKind.Function, TokenKind.Type -> code.function
                    TokenKind.Annotation -> code.annotation
                    TokenKind.Plain -> code.text
                },
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(horizontal = side)
            )
        }
    }
}

/** What a description is checked for before it is drawn: a desktop's column between the two, a phone's row. */
@Composable
private fun Checked(modifier: Modifier) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val chip: @Composable (Modifier, String) -> Unit = { chipModifier, text ->
        PantrixText(
            text = text,
            style = strong(11.5.sp, FontWeight.Bold),
            color = colors.success.onContainer,
            textAlign = TextAlign.Center,
            modifier = chipModifier
                .background(colors.success.container, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = if (SiteTheme.layout == SiteLayout.Phone) 4.dp else 5.dp)
        )
    }
    if (SiteTheme.layout == SiteLayout.Phone) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
            SduiSay.Checks.forEach { chip(Modifier, it.of(language)) }
        }
        return
    }
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PantrixText(
            text = SduiSay.CheckedThenDrawn.of(language),
            style = strong(11.5.sp),
            color = SiteTheme.colors.muted,
            textAlign = TextAlign.Center
        )
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SduiSay.Checks.forEach { chip(Modifier.fillMaxWidth(), it.of(language)) }
        }
        PantrixIcon(
            imageVector = PantrixIcons.ArrowForward,
            contentDescription = null,
            tint = colors.primary.color,
            size = 20.dp
        )
    }
}

/** What keeps a description safe: three cards on a desktop and a tablet, one list on a phone. */
@Composable
private fun Principles() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    if (layout == SiteLayout.Phone) {
        Column(Modifier.fillMaxWidth().landingCard(colors.surface, colors.border)) {
            SduiSay.Principles.forEachIndexed { index, (title, body) ->
                if (index > 0) Hairline()
                PantrixText(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.textPrimary)) {
                            append(title.phone.of(language) + ".")
                        }
                        append(" " + body.phone.of(language))
                    },
                    style = small(14.sp),
                    color = colors.textSecondary,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }
        return
    }
    val desktop = layout == SiteLayout.Desktop
    Row(
        Modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(if (desktop) 14.dp else 12.dp)
    ) {
        SduiSay.Principles.forEach { (title, body) ->
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .landingCard(colors.surface, colors.border, corner = if (desktop) 16.dp else 14.dp)
                    .padding(if (desktop) 18.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (desktop) 6.dp else 4.dp)
            ) {
                PantrixText(
                    text = title.of(layout).of(language),
                    style = strong(16.sp, FontWeight.Bold),
                    color = colors.textPrimary
                )
                PantrixText(
                    text = body.of(layout).of(language),
                    style = small(if (desktop) 14.sp else 13.5.sp),
                    color = colors.textSecondary
                )
            }
        }
    }
}

/** A desktop's steps of the plan beside what assistants may get from it. */
@Composable
private fun StepsAndAssistants() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val card = Modifier.landingCard(
        colors.surface,
        colors.border,
        corner = 16.dp
    ).padding(horizontal = 20.dp, vertical = 18.dp)
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(
            Modifier.weight(STEPS_SHARE).fillMaxHeight().then(card),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PantrixText(
                text = SduiSay.StepsTitle.of(language),
                style = strong(16.sp, FontWeight.Bold),
                color = colors.textPrimary
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SduiSay.Steps.forEachIndexed { index, step ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PantrixText(text = "${index + 1}.", style = small(14.sp), color = colors.textSecondary)
                        PantrixText(text = step.of(language), style = small(14.sp), color = colors.textSecondary)
                    }
                }
            }
        }
        Column(
            Modifier.weight(ASSISTANTS_SHARE).fillMaxHeight().then(card),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PantrixText(
                text = SduiSay.AssistantsTitle.of(language),
                style = strong(16.sp, FontWeight.Bold),
                color = colors.textPrimary
            )
            PantrixText(
                text = withCode(
                    SduiSay.AssistantsStart.of(language),
                    "pantrix-ui.json",
                    SduiSay.AssistantsEnd.of(language),
                    12.5.sp
                ),
                style = small(14.sp),
                color = colors.textSecondary
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SduiSay.Tools.forEach { (name, what) ->
                    PantrixText(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontFamily = mono(12.5.sp).fontFamily,
                                    fontSize = 12.5.sp,
                                    color = colors.primary.onContainer
                                )
                            ) {
                                append(name)
                            }
                            append(what.of(language))
                        },
                        style = small(13.5.sp),
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

private const val BIG_HEADING_LINE = 1.1f
private const val DESKTOP_CODE_LINE = 1.75f
private const val CODE_LINE = 1.7f
private const val STEPS_SHARE = 520f
private const val ASSISTANTS_SHARE = 420f
private val CHECKED_WIDTH = 120.dp
private val SIDE_GAP = 20.dp
private val PREVIEW_ROOM = 340.dp

/** The ground of a "sketch" note on a dark block: the number's orange, faint. */
private val SKETCH_GROUND = Color(0x24FA8A37)

/** A "sketch" note on a dark block. */
internal val SKETCH_INK = Color(0xFFFFC897)
