package com.pantrixui.site.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.WithSiteLayout
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.LandingButtonKind
import com.pantrixui.site.core.designsystem.content.OnPlatform
import com.pantrixui.site.core.designsystem.content.dotted
import com.pantrixui.site.core.designsystem.content.live
import com.pantrixui.site.core.designsystem.content.siteName
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route

/**
 * The top of the page: what Pantrix UI is and the way in, beside the live stage on a desktop and over it on a tablet
 * and a phone.
 */
@Composable
internal fun Hero(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    open: (Route) -> Unit
) {
    val layout = SiteTheme.layout
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 64.dp)
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 40.dp)
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 28.dp)
    }
    LandingBand(padding) {
        if (layout == SiteLayout.Desktop) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                // The design's two columns grow from 440 and 560 wide, each by half of what is left; a window too
                // narrow for both puts the stage under the words, which then take the tablet's single column: drawn
                // for a wide column, it fills the width, where the desktop's left its right side empty.
                val room = maxWidth - INTRO_BASIS - STAGE_BASIS - HERO_GAP
                if (room >= 0.dp) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(HERO_GAP),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Intro(state, onAction, open, Modifier.width(INTRO_BASIS + room / 2))
                        Stage(state, onAction, Modifier.width(STAGE_BASIS + room / 2))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(HERO_GAP)) {
                        WithSiteLayout(SiteLayout.Tablet) { Intro(state, onAction, open, Modifier.fillMaxWidth()) }
                        Stage(state, onAction, Modifier.fillMaxWidth())
                    }
                }
            }
        } else {
            Intro(state, onAction, open, Modifier.fillMaxWidth())
            Stage(
                state,
                onAction,
                Modifier.fillMaxWidth().padding(top = if (layout == SiteLayout.Phone) 24.dp else 32.dp)
            )
        }
    }
}

/** The news, the title, what Pantrix UI is, the way in and the line that installs it. */
@Composable
private fun Intro(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    open: (Route) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    val leadSize = if (phone) 16.5.sp else 18.sp
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(
            when (layout) {
                SiteLayout.Desktop -> 28.dp
                SiteLayout.Tablet -> 24.dp
                SiteLayout.Phone -> 20.dp
            }
        )
    ) {
        News(
            text = (if (phone) HomeSay.NewsLineShort else HomeSay.NewsLine).of(language),
            onClick = { open(Route.Doc("forms")) }
        )
        Title()
        PantrixText(
            text = (if (phone) HomeSay.LeadShort else HomeSay.Lead).of(language),
            style = SiteTheme.type.body.copy(fontSize = leadSize, lineHeight = (leadSize.value * LEAD_LINE).sp),
            color = PantrixTheme.colors.textSecondary,
            modifier = Modifier.widthIn(max = if (layout == SiteLayout.Tablet) 640.dp else 520.dp)
        )
        when (layout) {
            SiteLayout.Desktop -> {
                WayIn(open, fill = false)
                InstallLine(state.copied, onAction, Modifier.widthIn(max = 520.dp).fillMaxWidth())
            }

            SiteLayout.Tablet -> {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    if (maxWidth >= TABLET_ONE_ROW) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WayIn(open, fill = false)
                            InstallLine(state.copied, onAction, Modifier.weight(1f))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            WayIn(open, fill = false)
                            InstallLine(state.copied, onAction, Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            SiteLayout.Phone -> {
                WayIn(open, fill = true)
                InstallLine(state.copied, onAction, Modifier.fillMaxWidth())
            }
        }
    }
}

/** The pill over the title that leads to what is new. */
@Composable
private fun News(
    text: String,
    onClick: () -> Unit
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(percent = 50)
    Row(
        Modifier
            .siteClickable(tag = "homeNews", role = Role.Button, shape = shape, onClick = onClick)
            .background(colors.surface, shape)
            .border(1.dp, colors.border, shape)
            .padding(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = HomeSay.News.of(language),
            style = SiteTheme.type.tag.copy(fontSize = 12.sp),
            color = colors.primary.onContainer,
            modifier = Modifier
                .background(colors.primary.container, shape)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
        PantrixText(
            text = text,
            style = SiteTheme.type.breadcrumb,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f, fill = false)
        )
        PantrixIcon(imageVector = PantrixIcons.ArrowForward, contentDescription = null, size = 14.dp)
    }
}

/** "One codebase. Four platforms. Each one at home.", its last sentence in the brand's colour. */
@Composable
private fun Title() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val (first, second, third) = HomeSay.TitleLines.map { it.of(language) }
    val style = when (layout) {
        // The design's title is 5.2% of the window's width, from 40 to 64.
        SiteLayout.Desktop -> {
            val size = (PantrixTheme.window.size.width.value * TITLE_SHARE).coerceIn(PHONE_TITLE, DESKTOP_TITLE)
            display(size.sp, line = 1.04f, spacing = -0.035f)
        }

        SiteLayout.Tablet -> {
            display(TABLET_TITLE.sp, line = 1.04f, spacing = -0.035f)
        }

        SiteLayout.Phone -> {
            display(PHONE_TITLE.sp, line = 1.06f, spacing = -0.035f)
        }
    }
    val accent = PantrixTheme.colors.primary.onContainer
    val text = buildAnnotatedString {
        append(
            when (layout) {
                SiteLayout.Desktop -> "$first\n$second\n"
                SiteLayout.Tablet -> "$first $second\n"
                SiteLayout.Phone -> "$first $second "
            }
        )
        withStyle(SpanStyle(color = accent)) { append(third) }
    }
    PantrixText(
        text = text,
        style = style,
        color = PantrixTheme.colors.textPrimary,
        modifier = Modifier.semantics { heading() }
    )
}

/** "Get started" and "Browse 100 components": beside each other, or one over the other filling a phone. */
@Composable
private fun WayIn(
    open: (Route) -> Unit,
    fill: Boolean
) {
    val language = LocalLanguage.current
    val first: @Composable (Modifier) -> Unit = { modifier ->
        LandingButton(
            text = HomeSay.GetStarted.of(language),
            onClick = { open(Route.Doc("installation")) },
            tag = "homeGetStarted",
            kind = LandingButtonKind.Main,
            modifier = modifier,
            height = if (fill) 50.dp else 48.dp,
            arrow = true,
            fill = fill
        )
    }
    val second: @Composable (Modifier) -> Unit = { modifier ->
        LandingButton(
            text = HomeSay.Browse.of(language),
            onClick = { open(Route.Components) },
            tag = "homeBrowse",
            kind = LandingButtonKind.Outline,
            modifier = modifier,
            height = if (fill) 50.dp else 48.dp,
            fill = fill
        )
    }
    if (fill) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            first(Modifier)
            second(Modifier)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            first(Modifier)
            second(Modifier)
        }
    }
}

/** The line that installs the library, dark in both themes, and its copy button. */
@Composable
private fun InstallLine(
    copied: Boolean,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier
) {
    val layout = SiteTheme.layout
    val code = SiteTheme.colors.code
    val phone = layout == SiteLayout.Phone
    val desktop = layout == SiteLayout.Desktop
    val type = SiteTheme.type.code.copy(
        fontSize = when (layout) {
            SiteLayout.Desktop -> 13.5.sp
            SiteLayout.Tablet -> 13.sp
            SiteLayout.Phone -> 12.sp
        }
    )
    val line = buildAnnotatedString {
        withStyle(SpanStyle(color = code.annotation)) { append("implementation") }
        append("(")
        withStyle(SpanStyle(color = code.literal)) { append("\"com.pantrixui:components:${LibraryFacts.RELEASE}\"") }
        append(")")
    }
    Row(
        modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(code.background)
            .padding(start = if (desktop) 16.dp else 14.dp, end = if (phone) 4.dp else 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (desktop) {
            PantrixText(text = "›", style = type, color = code.keyword, modifier = Modifier.padding(end = 8.dp))
        }
        PantrixText(
            text = line,
            style = type,
            color = code.text,
            maxLines = 1,
            softWrap = false,
            overflow = if (phone) TextOverflow.Clip else TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .then(if (phone) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
        )
        CopyButton(copied, onClick = { onAction(HomeAction.CopyInstall) })
    }
}

/** The install line's copy button: its words on a desktop and a tablet, its icon alone on a phone. */
@Composable
private fun CopyButton(
    copied: Boolean,
    onClick: () -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val code = SiteTheme.colors.code
    val phone = layout == SiteLayout.Phone
    val label = (if (copied) HomeSay.Copied else HomeSay.Copy).of(language)
    PantrixButton(
        onClick = onClick,
        contentDescription = if (phone) HomeSay.CopyInstall.of(language) else label,
        size = ButtonSize.Medium,
        style = ButtonStyle(
            medium = ButtonSizeStyle(
                height = if (phone) 40.dp else 36.dp,
                horizontalPadding = if (phone) 0.dp else 12.dp
            )
        ),
        shape = RoundedCornerShape(if (phone) 9.dp else 8.dp),
        colors = ButtonColors(container = code.selectedTab, content = code.text),
        modifier = if (phone) Modifier.width(44.dp) else Modifier,
        tagKey = "copyInstall"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (layout != SiteLayout.Tablet) {
                PantrixIcon(
                    imageVector = if (copied) PantrixIcons.Check else PantrixIcons.Copy,
                    contentDescription = null,
                    size = if (phone) 16.dp else 14.dp
                )
            }
            if (!phone) {
                PantrixText(text = label, style = SiteTheme.type.button.copy(fontSize = 12.5.sp))
            }
        }
    }
}

/**
 * The live stage: the platforms to choose from, a review form, a month and a week's reviews drawn by Pantrix's own
 * components in the chosen platform's conventions, and what those conventions are.
 */
@Composable
private fun Stage(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(if (phone) 20.dp else 24.dp)
    Column(
        modifier
            .clip(shape)
            .background(SiteTheme.colors.canvas)
            .dotted(colors.borderHover, step = if (phone) 16.dp else 18.dp)
            .border(1.dp, SiteTheme.colors.hairline, shape)
            .padding(if (phone) 12.dp else 20.dp),
        verticalArrangement = Arrangement.spacedBy(if (phone) 12.dp else 16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PantrixSegmentButton(
                options = PlatformType.entries.map { SegmentOption(it, it.siteName, tagKey = it.name.lowercase()) },
                selected = state.platform,
                onSelect = { onAction(HomeAction.PickPlatform(it)) },
                modifier = if (phone) Modifier.weight(1f) else Modifier,
                fullWidth = phone,
                tagKey = "homePlatform"
            )
            if (!phone) {
                Spacer(Modifier.weight(1f))
                PlatformCode(state.platform)
            }
        }
        Box(Modifier.live()) {
            OnPlatform(state.platform) {
                Showcase(layout)
            }
        }
        PantrixText(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.Bold)) {
                    append("${state.platform.siteName}: ")
                }
                append(HomeSay.note(state.platform).of(language))
            },
            style = SiteTheme.type.bodySmall.copy(fontSize = if (phone) 12.5.sp else 13.sp),
            color = colors.textSecondary
        )
    }
}

/** `platform = PlatformType.Ios`: what the stage's choice is, as code. */
@Composable
private fun PlatformCode(platform: PlatformType) {
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(8.dp)
    PantrixText(
        text = buildAnnotatedString {
            append("platform = ")
            withStyle(SpanStyle(color = colors.primary.onContainer, fontWeight = FontWeight.SemiBold)) {
                append("PlatformType.${platform.name}")
            }
        },
        style = SiteTheme.type.inlineCode.copy(fontSize = 12.sp),
        color = colors.textSecondary,
        maxLines = 1,
        modifier = Modifier
            .background(colors.surface, shape)
            .border(1.dp, colors.border, shape)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

/** A card of the stage: a surface with a hairline and a shadow, its parts [gap] apart. */
@Composable
internal fun StageCard(
    modifier: Modifier = Modifier,
    padding: Dp = 20.dp,
    gap: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .shadow(STAGE_SHADOW, shape, clip = false)
            .background(PantrixTheme.colors.surface, shape)
            .border(1.dp, SiteTheme.colors.hairline, shape)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(gap),
        content = content
    )
}

/** How far a stage's card stands off the stage. */
private val STAGE_SHADOW = 12.dp

/** The widths the hero's two columns grow from, and the room between them. */
private val INTRO_BASIS = 440.dp
private val STAGE_BASIS = 560.dp
private val HERO_GAP = 56.dp

/** How wide a tablet's page is where its buttons and the install line share a row. */
private val TABLET_ONE_ROW = 680.dp

/** The lead's line, as many times its size. */
private const val LEAD_LINE = 1.6f

/** The title's share of the window's width on a desktop, and its sizes. */
private const val TITLE_SHARE = 0.052f
private const val DESKTOP_TITLE = 64f
private const val TABLET_TITLE = 54f
private const val PHONE_TITLE = 40f
