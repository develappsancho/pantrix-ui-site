package com.pantrixui.site.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.CodeView
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LinkCard
import com.pantrixui.site.core.designsystem.content.OnPlatform
import com.pantrixui.site.core.designsystem.content.live
import com.pantrixui.site.core.designsystem.content.siteName
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Route

/** The library in five numbers, on a band across the window. */
@Composable
internal fun Numbers() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(horizontal = 24.dp, vertical = 28.dp)
        SiteLayout.Tablet -> PaddingValues(24.dp)
        SiteLayout.Phone -> PaddingValues(horizontal = 16.dp, vertical = 20.dp)
    }
    val numbers = HomeSay.Stats.mapIndexed { index, (value, words) ->
        value to (if (layout == SiteLayout.Desktop) words else HomeSay.StatsShort[index]).of(language)
    }
    LandingBand(padding, ground = SiteTheme.colors.surfaceAlt) {
        when (layout) {
            SiteLayout.Desktop -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                numbers.forEach { (value, label) -> Number(value, label, size = 30.sp) }
            }

            SiteLayout.Tablet -> Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                numbers.forEach { (value, label) -> Number(value, label, size = 28.sp, Modifier.weight(1f)) }
            }

            SiteLayout.Phone -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                numbers.dropLast(1).chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        pair.forEach { (value, label) -> Number(value, label, size = 26.sp, Modifier.weight(1f)) }
                    }
                }
                val (value, label) = numbers.last()
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                    Number(value, null, size = 26.sp)
                    NumberLabel(label, Modifier.padding(bottom = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun Number(
    value: String,
    label: String?,
    size: TextUnit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        PantrixText(
            text = value,
            style = display(size, line = 1.3f, spacing = -0.03f),
            color = PantrixTheme.colors.textPrimary
        )
        if (label != null) NumberLabel(label)
    }
}

@Composable
private fun NumberLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    PantrixText(
        text = text,
        style = SiteTheme.type.factLabel.copy(
            fontSize = if (SiteTheme.layout == SiteLayout.Desktop) 13.sp else 12.5.sp
        ),
        color = SiteTheme.colors.muted,
        modifier = modifier
    )
}

/**
 * "One theme, four sets of manners": the same field and button drawn in each platform's conventions, live, with
 * what those conventions are.
 */
@Composable
internal fun Platforms() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val header: @Composable () -> Unit = {
        Column(
            if (layout == SiteLayout.Desktop) Modifier.widthIn(max = 680.dp) else Modifier,
            verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Desktop) 12.dp else 10.dp)
        ) {
            val kicker = if (layout == SiteLayout.Phone) HomeSay.PlatformsKickerShort else HomeSay.PlatformsKicker
            Kicker(kicker.of(language))
            SectionHeading(HomeSay.PlatformsTitle.of(language))
            when (layout) {
                SiteLayout.Desktop -> SectionLead(
                    start = HomeSay.PlatformsLead.of(language),
                    code = PLATFORM_CALL,
                    end = HomeSay.PlatformsLeadEnd.of(language)
                )

                SiteLayout.Tablet -> SectionLead(
                    start = HomeSay.PlatformsLeadShortStart.of(language),
                    code = PLATFORM_CALL,
                    end = HomeSay.PlatformsLeadEnd.of(language)
                )

                SiteLayout.Phone -> SectionLead(start = HomeSay.PlatformsLeadPhone.of(language))
            }
        }
    }
    when (layout) {
        SiteLayout.Desktop -> LandingBand(
            PaddingValues(start = 24.dp, end = 24.dp, top = 96.dp, bottom = 40.dp),
            gap = 40.dp
        ) {
            header()
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= MANNERS_ONE_ROW) {
                    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PlatformType.entries.forEach { MannersCard(it, Modifier.weight(1f).fillMaxHeight()) }
                    }
                } else {
                    MannersGrid()
                }
            }
        }

        SiteLayout.Tablet -> LandingBand(
            PaddingValues(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 32.dp),
            gap = 28.dp
        ) {
            header()
            MannersGrid()
        }

        SiteLayout.Phone -> LandingBand(PaddingValues(top = 48.dp, bottom = 16.dp), gap = 20.dp) {
            Box(Modifier.padding(horizontal = 16.dp)) { header() }
            // The four cards in a row that scrolls sideways, from the page's edge.
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .height(IntrinsicSize.Min)
                    .padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlatformType.entries.forEach { MannersCard(it, Modifier.width(272.dp).fillMaxHeight()) }
            }
        }
    }
}

/** The four platforms' cards, two to a row. */
@Composable
private fun MannersGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PlatformType.entries.chunked(2).forEach { pair ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                pair.forEach { MannersCard(it, Modifier.weight(1f).fillMaxHeight()) }
            }
        }
    }
}

/** A platform's card: a field and a button drawn live in its conventions over what those conventions are. */
@Composable
private fun MannersCard(
    platform: PlatformType,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val desktop = layout == SiteLayout.Desktop
    val shape = RoundedCornerShape(16.dp)
    val email = remember { TextFieldState() }
    Column(
        modifier
            .live()
            .clip(shape)
            .background(PantrixTheme.colors.surface)
            .border(1.dp, PantrixTheme.colors.border, shape)
    ) {
        val hairline = SiteTheme.colors.hairline
        Column(
            Modifier
                .fillMaxWidth()
                .background(SiteTheme.colors.canvas)
                .drawBehind { underline(hairline) }
                .padding(
                    when (layout) {
                        SiteLayout.Desktop -> 22.dp
                        SiteLayout.Tablet -> 20.dp
                        SiteLayout.Phone -> 18.dp
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Phone) 10.dp else 12.dp)
        ) {
            OnPlatform(platform) {
                PantrixTextField(
                    state = email,
                    placeholder = HomeSay.Email.of(language),
                    modifier = Modifier.fillMaxWidth(),
                    tagKey = "homeEmail_${platform.name.lowercase()}"
                )
                PantrixButton(
                    text = HomeSay.Continue.of(language),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    tagKey = "homeContinue_${platform.name.lowercase()}"
                )
            }
        }
        Column(
            Modifier.padding(
                when (layout) {
                    SiteLayout.Desktop -> PaddingValues(horizontal = 22.dp, vertical = 20.dp)
                    SiteLayout.Tablet -> PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 18.dp)
                    SiteLayout.Phone -> PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp)
                }
            ),
            verticalArrangement = Arrangement.spacedBy(
                when (layout) {
                    SiteLayout.Desktop -> 10.dp
                    SiteLayout.Tablet -> 8.dp
                    SiteLayout.Phone -> 6.dp
                }
            )
        ) {
            PantrixText(
                text = platform.siteName,
                style = display(if (layout == SiteLayout.Phone) 16.sp else 17.sp, line = 1.4f, spacing = 0f),
                color = PantrixTheme.colors.textPrimary
            )
            if (desktop) {
                Facts(platform)
            } else {
                PantrixText(
                    text = HomeSay.factsLine(platform).of(language),
                    style = SiteTheme.type.bodySmall.copy(fontSize = 13.sp),
                    color = PantrixTheme.colors.textSecondary
                )
            }
        }
    }
}

/** A platform's four facts, a label beside each value. */
@Composable
private fun Facts(platform: PlatformType) {
    val language = LocalLanguage.current
    val style = SiteTheme.type.bodySmall.copy(fontSize = 13.sp)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HomeSay.facts(platform).forEach { (label, value) ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PantrixText(
                    text = label.of(language),
                    style = style,
                    color = SiteTheme.colors.muted,
                    modifier = Modifier.weight(1f)
                )
                PantrixText(
                    text = value.of(language),
                    style = style.copy(fontWeight = FontWeight.Bold),
                    color = PantrixTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** "Write it once": what every component takes, beside the code of a form and of the site's own preset. */
@Composable
internal fun WriteOnce(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val code = state.code.valueOrNull
    when (layout) {
        SiteLayout.Desktop -> {
            LandingBand(PaddingValues(horizontal = 24.dp, vertical = 64.dp)) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val room = maxWidth - ONCE_BASIS - ONCE_CODE_BASIS - 48.dp
                    val words: @Composable (Modifier) -> Unit = { modifier ->
                        Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Kicker(HomeSay.OnceKicker.of(language))
                            SectionHeading(HomeSay.OnceTitle.of(language))
                            SectionLead(
                                start = HomeSay.OnceLeadStart.of(language),
                                code = "tagKey",
                                end = HomeSay.OnceLeadEnd.of(language)
                            )
                            Checks()
                        }
                    }
                    val panel: @Composable (Modifier) -> Unit = { modifier ->
                        if (code != null) {
                            val tabbed = if (state.codeTab == 0) code.form else code.preset
                            CodeView(
                                code = tabbed.copy(file = SOURCE_SET),
                                modifier = modifier.shadow(12.dp, RoundedCornerShape(14.dp), clip = false),
                                tabs = listOf(FORM_FILE, PRESET_FILE),
                                selectedTab = state.codeTab,
                                onSelectTab = { onAction(HomeAction.PickCodeTab(it)) }
                            )
                        }
                    }
                    if (room >= 0.dp) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            words(Modifier.width(ONCE_BASIS + room / 2))
                            panel(Modifier.width(ONCE_CODE_BASIS + room / 2))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(48.dp)) {
                            words(Modifier.fillMaxWidth())
                            panel(Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }

        SiteLayout.Tablet, SiteLayout.Phone -> {
            val phone = layout == SiteLayout.Phone
            LandingBand(
                PaddingValues(horizontal = layout.gutter, vertical = if (phone) 40.dp else 48.dp),
                gap = if (phone) 16.dp else 20.dp
            ) {
                Kicker(HomeSay.OnceKicker.of(language))
                SectionHeading(HomeSay.OnceTitle.of(language))
                SectionLead(
                    start = (if (phone) HomeSay.OnceLeadShortStart else HomeSay.OnceLeadStart).of(language),
                    code = "tagKey",
                    end = (if (phone) HomeSay.OnceLeadShortEnd else HomeSay.OnceLeadEnd).of(language)
                )
                if (code != null) {
                    if (phone) {
                        CodeView(code = code.shortForm)
                    } else {
                        CodeView(code = code.wideForm, tabs = listOf(FORM_FILE))
                    }
                }
            }
        }
    }
}

/** What the form's code shows, each with a tick. */
@Composable
private fun Checks() {
    val language = LocalLanguage.current
    val style = SiteTheme.type.body
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeSay.OnceChecks.forEachIndexed { index, words ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PantrixIcon(
                    imageVector = PantrixIcons.Check,
                    contentDescription = null,
                    tint = PantrixTheme.colors.primary.color,
                    size = 20.dp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                val text = words.of(language)
                // The last says a tag, which is written as code.
                if (index == HomeSay.OnceChecks.lastIndex) {
                    val (start, end) = text.split(TAG_EXAMPLE, limit = 2).let { it.first() to it.getOrElse(1) { "" } }
                    PantrixText(
                        text = withCode(start, TAG_EXAMPLE, end, 13.sp),
                        style = style,
                        color = PantrixTheme.colors.textSecondary
                    )
                } else {
                    PantrixText(text = text, style = style, color = PantrixTheme.colors.textSecondary)
                }
            }
        }
    }
}

/** "What comes with it": the problems every app meets, each a card that leads to its guide. */
@Composable
internal fun WhatComesWithIt(open: (Route) -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(horizontal = 24.dp, vertical = 96.dp)
        SiteLayout.Tablet -> PaddingValues(horizontal = 24.dp, vertical = 72.dp)
        SiteLayout.Phone -> PaddingValues(horizontal = 16.dp, vertical = 48.dp)
    }
    val gap = when (layout) {
        SiteLayout.Desktop -> 40.dp
        SiteLayout.Tablet -> 28.dp
        SiteLayout.Phone -> 20.dp
    }
    LandingBand(padding, ground = SiteTheme.colors.canvas, gap = gap) {
        when (layout) {
            SiteLayout.Desktop -> Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    Modifier.weight(1f, fill = false).widthIn(max = 640.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Kicker(HomeSay.ProblemsKicker.of(language))
                    SectionHeading(HomeSay.ProblemsTitle.of(language))
                }
                GuidesLink { open(Route.Guides) }
            }

            SiteLayout.Tablet -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Kicker(HomeSay.ProblemsKicker.of(language))
                SectionHeading(HomeSay.ProblemsTitle.of(language))
            }

            SiteLayout.Phone -> Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Kicker(HomeSay.ProblemsKicker.of(language))
                SectionHeading(HomeSay.ProblemsTitleShort.of(language))
            }
        }
        if (layout == SiteLayout.Phone) {
            FeatureList(open)
        } else {
            val desktop = layout == SiteLayout.Desktop
            Column(verticalArrangement = Arrangement.spacedBy(if (desktop) 16.dp else 14.dp)) {
                HomeSay.Features.chunked(FEATURES_A_ROW).forEach { row ->
                    Row(
                        Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(if (desktop) 16.dp else 14.dp)
                    ) {
                        row.forEach { FeatureCard(it, open, Modifier.weight(1f).fillMaxHeight()) }
                    }
                }
            }
        }
    }
}

/** "Read the guides →". */
@Composable
private fun GuidesLink(onClick: () -> Unit) {
    val language = LocalLanguage.current
    val color = PantrixTheme.colors.primary.onContainer
    Row(
        Modifier.siteClickable(
            tag = "homeGuides",
            role = Role.Button,
            shape = RoundedCornerShape(6.dp),
            onClick = onClick
        ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = HomeSay.ReadTheGuides.of(language),
            style = SiteTheme.type.body.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        PantrixIcon(imageVector = PantrixIcons.ArrowForward, contentDescription = null, tint = color, size = 16.dp)
    }
}

/** A card of what comes with the library: its mark, its title, what it does and its API. */
@Composable
private fun FeatureCard(
    feature: Feature,
    open: (Route) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    LinkCard(
        onClick = { open(feature.route) },
        tagKey = "homeFeature_${feature.api.substringBefore('(').substringAfterLast('.').substringAfterLast(':')}",
        modifier = modifier,
        contentPadding = PaddingValues(if (desktop) 24.dp else 20.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(if (desktop) 10.dp else 8.dp)) {
            Mark(feature.mark, if (desktop) 40.dp else 38.dp)
            PantrixText(
                text = feature.title.of(language),
                style = display(if (desktop) 17.sp else 16.sp, line = 1.3f, spacing = -0.01f),
                color = PantrixTheme.colors.textPrimary
            )
            PantrixText(
                text = feature.body.of(language),
                style = SiteTheme.type.body.copy(
                    fontSize = if (desktop) 14.5.sp else 13.5.sp,
                    lineHeight = if (desktop) 23.2.sp else 20.9.sp
                ),
                color = PantrixTheme.colors.textSecondary
            )
            if (desktop) {
                PantrixText(
                    text = feature.api,
                    style = SiteTheme.type.inlineCode.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                    color = SiteTheme.colors.muted
                )
            }
        }
    }
}

/** A phone's cards of what comes with the library: rows of one bordered list. */
@Composable
private fun FeatureList(open: (Route) -> Unit) {
    val language = LocalLanguage.current
    val shape = RoundedCornerShape(16.dp)
    val hairline = SiteTheme.colors.hairline
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PantrixTheme.colors.surface)
            .border(1.dp, PantrixTheme.colors.border, shape)
    ) {
        HomeSay.Features.forEachIndexed { index, feature ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .siteClickable(
                        tag = "homeFeature_$index",
                        role = Role.Button,
                        shape = RoundedCornerShape(0.dp),
                        onClick = { open(feature.route) }
                    )
                    .then(
                        if (index < HomeSay.Features.lastIndex) {
                            Modifier.drawBehind { underline(hairline) }
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Mark(feature.mark, 36.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    PantrixText(
                        text = feature.title.of(language),
                        style = display(15.sp, line = 1.4f, spacing = 0f),
                        color = PantrixTheme.colors.textPrimary
                    )
                    PantrixText(
                        text = feature.body.of(language),
                        style = SiteTheme.type.body.copy(fontSize = 13.5.sp, lineHeight = 20.25.sp),
                        color = PantrixTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

/** A hairline along the foot of what is drawn. */
private fun DrawScope.underline(color: Color) {
    val width = 1.dp.toPx()
    drawLine(color, Offset(0f, size.height - width / 2), Offset(size.width, size.height - width / 2), width)
}

/** A card's mark: a sign of what it is about, in the brand's soft square. */
@Composable
internal fun Mark(
    text: String,
    size: Dp
) {
    Box(
        Modifier
            .size(size)
            .background(
                PantrixTheme.colors.primary.container,
                RoundedCornerShape(if (size < 38.dp) 10.dp else 11.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        PantrixText(
            text = text,
            style = SiteTheme.type.inlineCode.copy(
                fontSize = if (size < 38.dp) 12.5.sp else 13.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = PantrixTheme.colors.primary.onContainer
        )
    }
}

/** How many of what comes with the library a desktop's and a tablet's row has. */
private const val FEATURES_A_ROW = 3

/** The call that asks for another platform's conventions, as the platforms' sentence names it. */
private const val PLATFORM_CALL = "PantrixTheme(platform = …)"

/** The tag the last of the code's ticks names. */
private const val TAG_EXAMPLE = "TextArea_comment"

/** The files of "Write it once", and the source set they are in. */
private const val FORM_FILE = "ReviewForm.kt"
private const val PRESET_FILE = "PantrixPreset.kt"
private const val SOURCE_SET = "commonMain"

/** How wide a desktop's page is where the four platforms' cards share a row: four of 250 and their gaps. */
private val MANNERS_ONE_ROW = 1048.dp

/** The widths "Write it once"'s words and code grow from. */
private val ONCE_BASIS = 340.dp
private val ONCE_CODE_BASIS = 620.dp
