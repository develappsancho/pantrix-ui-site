package com.pantrixui.site.feature.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.Hairline
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.landingBandPadding
import com.pantrixui.site.core.designsystem.content.landingCard
import com.pantrixui.site.core.designsystem.content.landingGutter
import com.pantrixui.site.core.i18n.LocalLanguage

/**
 * The three libraries: what each is for, with its sample, and what is the same in all three and what differs; a
 * desktop adds where their tokens may come from.
 */
@Composable
internal fun Libraries(modifier: Modifier) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val gap = when (layout) {
        SiteLayout.Desktop -> 28.dp
        SiteLayout.Tablet -> 18.dp
        SiteLayout.Phone -> 14.dp
    }
    LandingBand(landingBandPadding(), modifier, gap = gap) {
        Column(
            Modifier.widthIn(max = 820.dp),
            verticalArrangement = Arrangement.spacedBy(
                if (layout ==
                    SiteLayout.Desktop
                ) {
                    10.dp
                } else {
                    gap
                }
            )
        ) {
            Pill(NativeSay.Pill.of(language), SiteTheme.colors.violet, SiteTheme.colors.onViolet)
            PantrixText(
                text = NativeSay.Title.of(language),
                style = when (layout) {
                    SiteLayout.Desktop -> sectionHeading(38.sp, line = BIG_HEADING_LINE)
                    SiteLayout.Tablet -> sectionHeading(32.sp)
                    SiteLayout.Phone -> sectionHeading(26.sp)
                },
                color = colors.textPrimary
            )
            PantrixText(
                text = NativeSay.Lead.of(layout).of(language),
                style = small(
                    when (layout) {
                        SiteLayout.Desktop -> 16.5.sp
                        SiteLayout.Tablet -> 16.sp
                        SiteLayout.Phone -> 14.5.sp
                    }
                ),
                color = colors.textSecondary
            )
        }
        when (layout) {
            SiteLayout.Desktop -> {
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NativeSay.Libraries.forEach { LibraryCard(it, Modifier.weight(1f).fillMaxHeight()) }
                }
                SameAndDiffers()
                Tokens()
            }

            SiteLayout.Tablet -> {
                NativeSay.Libraries.forEach { LibraryRow(it) }
                SameAndDiffers()
            }

            SiteLayout.Phone -> {
                NativeSay.Libraries.forEach { LibraryCard(it, Modifier.fillMaxWidth()) }
                PhoneSameAndDiffers()
            }
        }
    }
}

/** A library as a card: what it is over its sample, a desktop's three side by side, a phone's one under another. */
@Composable
private fun LibraryCard(
    library: Library,
    modifier: Modifier
) {
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    val shape = RoundedCornerShape(if (desktop) 20.dp else 16.dp)
    Column(
        modifier
            .clip(shape)
            .background(PantrixTheme.colors.surface)
            .border(1.dp, libraryEdge(library), shape)
    ) {
        LibraryWords(
            library,
            Modifier.padding(
                start = if (desktop) 20.dp else 16.dp,
                end = if (desktop) 20.dp else 16.dp,
                top = if (desktop) 20.dp else 14.dp,
                bottom = if (desktop) 16.dp else 14.dp
            )
        )
        Spacer(Modifier.weight(1f, fill = false))
        LibraryCode(library, Modifier.fillMaxWidth())
    }
}

/** A tablet's library: what it is beside its sample. */
@Composable
private fun LibraryRow(library: Library) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(PantrixTheme.colors.surface)
            .border(1.dp, libraryEdge(library), shape)
    ) {
        LibraryWords(
            library,
            Modifier.width(TABLET_WORDS).fillMaxHeight().padding(horizontal = 20.dp, vertical = 18.dp)
        )
        LibraryCode(library, Modifier.weight(1f).fillMaxHeight())
    }
}

/** What a library is for: its project, name, status, what it is and where it runs. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LibraryWords(
    library: Library,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val (statusGround, statusInk) = if ((library.availability == Availability.Today)) {
        colors.success.container to colors.success.onContainer
    } else {
        SiteTheme.colors.violet to SiteTheme.colors.onViolet
    }
    val status = @Composable {
        Pill(
            library.availability.status.of(language),
            statusGround,
            statusInk,
            size = if (layout ==
                SiteLayout.Phone
            ) {
                11.sp
            } else {
                11.5.sp
            }
        )
    }
    val project = @Composable {
        PantrixText(
            text = library.project.of(language),
            style = strong(12.sp),
            color = SiteTheme.colors.muted,
            modifier = Modifier
        )
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Phone) 6.dp else 8.dp)) {
        if (layout == SiteLayout.Tablet) {
            project()
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { project() }
                status()
            }
        }
        PantrixText(
            text = library.name.of(language),
            style = strong(
                when (layout) {
                    SiteLayout.Desktop -> 20.sp
                    SiteLayout.Tablet -> 19.sp
                    SiteLayout.Phone -> 17.sp
                }
            ).copy(lineHeight = ((if (layout == SiteLayout.Phone) 17f else 20f) * NAME_LINE).sp),
            color = colors.textPrimary
        )
        if (layout == SiteLayout.Tablet) status()
        PantrixText(text = library.body.of(language), style = small(14.sp), color = colors.textSecondary)
        if (layout == SiteLayout.Tablet) Spacer(Modifier.weight(1f))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            library.targets.forEach { target ->
                PantrixText(
                    text = target.of(language),
                    style = strong(if (layout == SiteLayout.Phone) 11.5.sp else 12.sp, FontWeight.Bold),
                    color = colors.textSecondary,
                    modifier = Modifier
                        .background(SiteTheme.colors.hover, RoundedCornerShape(7.dp))
                        .padding(horizontal = 8.dp, vertical = if (layout == SiteLayout.Phone) 2.dp else 3.dp)
                )
            }
        }
    }
}

/** A library's sample on its dark ground, under the file it is from and what it is: today's code or a sketch. */
@Composable
private fun LibraryCode(
    library: Library,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val code = SiteTheme.colors.code
    val phone = layout == SiteLayout.Phone
    val side = if (phone) 12.dp else 14.dp
    Column(modifier.background(code.background)) {
        CodeHeader(
            when (layout) {
                SiteLayout.Desktop -> 36.dp
                SiteLayout.Tablet -> 34.dp
                SiteLayout.Phone -> 32.dp
            },
            side
        ) {
            val type = strong(if (phone) 11.5.sp else 12.sp, FontWeight.Bold)
            PantrixText(text = library.file, style = type, color = code.comment, modifier = Modifier.weight(1f))
            PantrixText(
                text = library.availability.note.of(language),
                style = type,
                color = if ((library.availability == Availability.Today)) TODAY_INK else SKETCH_INK
            )
        }
        CodeLines(
            library.code(NativeSay.NoteLabel.of(layout).of(language)),
            size = if (phone) 11.sp else 12.sp,
            line = if (layout == SiteLayout.Desktop) DESKTOP_CODE_LINE else CODE_LINE,
            side = side,
            modifier = Modifier.padding(top = if (phone) 8.dp else 10.dp, bottom = if (phone) 10.dp else 12.dp)
        )
    }
}

/** The table of what is the same in all three and what differs, a desktop's and a tablet's. */
@Composable
private fun SameAndDiffers() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val desktop = layout == SiteLayout.Desktop
    val size = if (desktop) 14.sp else 13.5.sp
    val cell = Modifier.padding(horizontal = if (desktop) 16.dp else 14.dp, vertical = if (desktop) 11.dp else 10.dp)
    val firstShare = if (desktop) DESKTOP_FIRST else TABLET_FIRST
    Column(Modifier.fillMaxWidth().landingCard(colors.surface, colors.border, corner = 16.dp)) {
        PantrixText(
            text = NativeSay.Table.of(language),
            style = strong(14.sp),
            color = colors.textPrimary,
            modifier = Modifier.fillMaxWidth().background(
                SiteTheme.colors.surfaceAlt
            ).padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Hairline(color = colors.border)
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(firstShare))
            NativeSay.ColumnNames.of(layout).forEach { name ->
                PantrixText(
                    text = name,
                    style = strong(12.sp),
                    color = SiteTheme.colors.muted,
                    modifier = Modifier.weight((1f - firstShare) / NativeSay.Libraries.size).then(cell)
                )
            }
        }
        Hairline(color = colors.border)
        NativeSay.SameRows.forEachIndexed { index, (name, value) ->
            if (index > 0) Hairline()
            Row(Modifier.fillMaxWidth()) {
                PantrixText(
                    text = name.of(language),
                    style = strong(size, FontWeight.Bold),
                    color = colors.textPrimary,
                    modifier = Modifier.weight(firstShare).then(cell)
                )
                PantrixText(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = colors.success.onContainer)) {
                            append(NativeSay.Same.of(language))
                        }
                        append(value.of(language))
                    },
                    style = small(size),
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f - firstShare).then(cell)
                )
            }
        }
        NativeSay.DiffRows.forEach { (name, values) ->
            Hairline()
            Row(Modifier.fillMaxWidth()) {
                PantrixText(
                    text = name.of(language),
                    style = strong(size, FontWeight.Bold),
                    color = colors.textPrimary,
                    modifier = Modifier.weight(firstShare).then(cell)
                )
                values.forEach { value ->
                    PantrixText(
                        text = value.of(language),
                        style = small(size),
                        color = colors.textSecondary,
                        modifier = Modifier.weight((1f - firstShare) / values.size).then(cell)
                    )
                }
            }
        }
    }
}

/** A phone's table, as two lists: what is the same in all three, and what differs. */
@Composable
private fun PhoneSameAndDiffers() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PantrixText(
            text = NativeSay.SameInAll.of(language),
            style = strong(15.sp, FontWeight.Bold),
            color = colors.textPrimary
        )
        NativeSay.SameRows.forEach { (name, value) ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                PantrixText(text = name.of(language), style = strong(13.5.sp), color = colors.success.onContainer)
                PantrixText(text = value.of(language), style = small(13.5.sp), color = colors.textSecondary)
            }
        }
        Hairline()
        PantrixText(
            text = NativeSay.WhatDiffers.of(language),
            style = strong(15.sp, FontWeight.Bold),
            color = colors.textPrimary
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            NativeSay.Differs.forEach {
                PantrixText(text = it.of(language), style = small(13.5.sp), color = colors.textSecondary)
            }
        }
    }
}

/** A desktop's line on where the tokens may come from: one file, and what would be made from it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Tokens() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val chip = @Composable { name: String ->
        PantrixText(
            text = name,
            style = mono(12.5.sp),
            color = colors.textPrimary,
            modifier = Modifier
                .background(colors.surface, RoundedCornerShape(7.dp))
                .border(1.dp, colors.border, RoundedCornerShape(7.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
    FlowRow(
        Modifier
            .fillMaxWidth()
            .landingCard(SiteTheme.colors.canvas, SiteTheme.colors.hairline)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = NativeSay.Tokens.of(language),
            style = strong(14.sp, FontWeight.Bold),
            color = colors.textPrimary
        )
        chip(NativeSay.TOKENS_FILE)
        PantrixText(text = "→", style = small(14.sp), color = SiteTheme.colors.muted)
        NativeSay.TokenOutputs.forEach { chip(it) }
    }
}

/**
 * The versions: on a desktop side by side on a line, on a tablet side by side each with a stripe, and on a phone one
 * under another on a line down, with the way to suggest an item after them.
 *
 * @param onSuggest what suggesting an item does, or null where there is no way to suggest one.
 * @param lineUnder whether the band has its hairline under it: not over a desktop's foot, which has its own.
 */
@Composable
internal fun Versions(
    onSuggest: (() -> Unit)?,
    lineUnder: Boolean
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(start = landingGutter, end = landingGutter, top = 64.dp, bottom = 72.dp)
        SiteLayout.Tablet -> PaddingValues(start = landingGutter, end = landingGutter, top = 48.dp, bottom = 64.dp)
        SiteLayout.Phone -> PaddingValues(start = landingGutter, end = landingGutter, top = 40.dp, bottom = 48.dp)
    }
    val gap = when (layout) {
        SiteLayout.Desktop -> 24.dp
        SiteLayout.Tablet -> 18.dp
        SiteLayout.Phone -> 14.dp
    }
    LandingBand(
        padding,
        ground = SiteTheme.colors.canvas,
        gap = gap,
        lineUnder =
            lineUnder && layout != SiteLayout.Phone
    ) {
        PantrixText(
            text = RoadmapSay.VersionsTitle.of(language),
            style = when (layout) {
                SiteLayout.Desktop -> sectionHeading(32.sp)
                SiteLayout.Tablet -> sectionHeading(28.sp)
                SiteLayout.Phone -> sectionHeading(24.sp)
            },
            color = PantrixTheme.colors.textPrimary
        )
        if (layout == SiteLayout.Phone) {
            Column {
                RoadmapSay.Versions.forEach { PhoneVersion(it) }
            }
            if (onSuggest != null) {
                LandingButton(
                    text = RoadmapSay.Suggest.of(language),
                    onClick = onSuggest,
                    tag = "roadmapSuggest",
                    height = 50.dp,
                    fill = true,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        } else {
            Row(
                Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(
                    if (layout ==
                        SiteLayout.Tablet
                    ) {
                        14.dp
                    } else {
                        0.dp
                    }
                )
            ) {
                RoadmapSay.Versions.forEach { version ->
                    if (layout == SiteLayout.Desktop) {
                        DesktopVersion(version, Modifier.weight(1f))
                    } else {
                        TabletVersion(version, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
}

/** A desktop's version: its dot on the line the versions stand on, then what it is. */
@Composable
private fun DesktopVersion(
    version: Version,
    modifier: Modifier
) {
    val canvas = SiteTheme.colors.canvas
    val dot = stageColor(version.stage)
    Column(modifier.padding(end = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // A ring in the dot's colour around the ground, around the dot.
            Box(Modifier.background(dot, CircleShape).padding(2.dp).background(canvas, CircleShape).padding(3.dp)) {
                Dot(dot, size = 8.dp)
            }
            Box(Modifier.weight(1f).height(2.dp).background(PantrixTheme.colors.border))
        }
        VersionWords(version)
    }
}

/** A tablet's version: a stripe in its colour beside what it is. */
@Composable
private fun TabletVersion(
    version: Version,
    modifier: Modifier
) {
    Row(modifier) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(stageColor(version.stage)))
        Column(Modifier.padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            VersionWords(version)
        }
    }
}

/** A phone's version: its dot over the line down to the next, beside what it is. */
@Composable
private fun PhoneVersion(version: Version) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.padding(top = 5.dp)) { Dot(stageColor(version.stage), size = 12.dp) }
            Box(Modifier.padding(top = 4.dp).width(2.dp).weight(1f).background(PantrixTheme.colors.border))
        }
        Column(Modifier.padding(bottom = 8.dp)) { VersionWords(version) }
    }
}

/** What a version is: its name, what it brings and what is in it. */
@Composable
private fun VersionWords(version: Version) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val phone = SiteTheme.layout == SiteLayout.Phone
    PantrixText(
        text = version.name.of(language),
        style = strong(if (phone) 12.sp else 12.5.sp),
        color = SiteTheme.colors.muted
    )
    PantrixText(text = version.title.of(language), style = strong(15.sp, FontWeight.Bold), color = colors.textPrimary)
    PantrixText(text = version.body.of(language), style = small(13.5.sp), color = colors.textSecondary)
}

/** The edge of a library's card: the brand's for today's, the quiet one for those to come. */
@Composable
private fun libraryEdge(library: Library): Color =
    if ((library.availability == Availability.Today)) SiteTheme.colors.accentLine else PantrixTheme.colors.border

private const val BIG_HEADING_LINE = 1.1f
private const val DESKTOP_CODE_LINE = 1.75f
private const val CODE_LINE = 1.7f
private const val NAME_LINE = 1.25f
private const val DESKTOP_FIRST = 0.24f
private const val TABLET_FIRST = 0.22f
private val TABLET_WORDS = 280.dp

/** "Today" on a dark block: a light green. */
private val TODAY_INK = Color(0xFFA6E5C6)
