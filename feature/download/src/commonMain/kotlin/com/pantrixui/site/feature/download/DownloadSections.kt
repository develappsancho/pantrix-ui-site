package com.pantrixui.site.feature.download

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.LandingButtonKind
import com.pantrixui.site.core.designsystem.content.landingCard
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.DesktopRelease
import com.pantrixui.site.core.model.SiteDownloads

/** The platforms the app comes to, a card each: three side by side on a desktop, one under another elsewhere. */
@Composable
internal fun Platforms(
    release: DesktopRelease?,
    onAction: (DownloadAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val (padding, gap) = when (layout) {
        SiteLayout.Desktop -> PaddingValues(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 72.dp) to 24.dp
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 56.dp) to 20.dp
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 36.dp, bottom = 40.dp) to 16.dp
    }
    LandingBand(padding, ground = SiteTheme.colors.canvas, gap = gap) {
        Column(verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Phone) 6.dp else 8.dp)) {
            SectionHeading(DownloadSay.PlatformsTitle.of(language))
            PantrixText(
                text = DownloadSay.PlatformsLead.of(layout).of(language),
                style = small(
                    when (layout) {
                        SiteLayout.Desktop -> 16.sp
                        SiteLayout.Tablet -> 15.5.sp
                        SiteLayout.Phone -> 14.5.sp
                    }
                ),
                color = PantrixTheme.colors.textSecondary
            )
        }
        when (layout) {
            SiteLayout.Desktop -> {
                // The cards are as tall as the tallest, their buttons on one line.
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DownloadSay.Platforms.forEach { platform ->
                        PlatformCard(
                            platform,
                            release,
                            onAction,
                            Modifier.weight(1f).fillMaxHeight(),
                            across = false,
                            evenHeight = true
                        )
                    }
                }
            }

            SiteLayout.Tablet -> {
                DownloadSay.Platforms.forEach { platform -> PlatformCard(platform, release, onAction, across = true) }
            }

            SiteLayout.Phone -> {
                DownloadSay.Platforms.forEach { platform -> PlatformCard(platform, release, onAction, across = false) }
            }
        }
    }
}

/**
 * A platform's card: its mark, its name and whether it is here yet, what it asks for, its file, and the button that
 * downloads it or leads to the releases; the button beside the rest where [across], under it where not. Where
 * [evenHeight], the card is as tall as its neighbours and its button stands at its foot.
 */
@Composable
private fun PlatformCard(
    platform: Platform,
    release: DesktopRelease?,
    onAction: (DownloadAction) -> Unit,
    modifier: Modifier = Modifier,
    across: Boolean,
    evenHeight: Boolean = false
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val site = SiteTheme.colors
    val card = modifier
        .fillMaxWidth()
        .landingCard(colors.surface, if (platform.available) site.accentLine else colors.border, corner = 16.dp)
    val file = platform.file ?: listOfNotNull(
        SiteDownloads.MAC_FILE,
        release?.macSize?.let(PantrixTheme.strings::fileSize)
    ).joinToString(" · ")
    val head = @Composable {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(
                        if (platform.available) colors.primary.container else site.hover,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                PantrixIcon(
                    imageVector = platform.icon,
                    contentDescription = null,
                    tint = if (platform.available) colors.primary.onContainer else colors.textSecondary,
                    size = 22.dp
                )
            }
            PantrixText(
                text = platform.name,
                style = strong(18.sp, FontWeight.Bold),
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (platform.available) {
                Chip(DownloadSay.Available.of(language), colors.success.container, colors.success.onContainer)
            } else {
                Chip(DownloadSay.ComingLater.of(language), colors.warning.container, colors.warning.onContainer)
            }
        }
    }
    val body = @Composable { bodyModifier: Modifier ->
        PantrixText(
            text = platform.body.of(language),
            style = small(14.sp),
            color = colors.textSecondary,
            modifier = bodyModifier
        )
    }
    val fileLine = @Composable { PantrixText(text = file, style = mono(12.5.sp), color = colors.textSecondary) }
    val action = @Composable { fill: Boolean ->
        val key = platform.name.lowercase()
        if (platform.available) {
            LandingButton(
                text = DownloadSay.DownloadDmg.of(language),
                onClick = { onAction(DownloadAction.OpenUrl(SiteDownloads.MAC_DOWNLOAD)) },
                tag = "download_$key",
                height = 48.dp,
                fill = fill,
                textSize = 15.5.sp,
                leading = PantrixIcons.Download,
                iconSize = 18.dp
            )
        } else {
            LandingButton(
                text = DownloadSay.WatchReleases.of(language),
                onClick = { onAction(DownloadAction.OpenUrl(SiteDownloads.RELEASES)) },
                tag = "releases_$key",
                kind = LandingButtonKind.Outline,
                height = 48.dp,
                padding = 18.dp,
                fill = fill,
                trailing = SiteIcons.ArrowUpRight
            )
        }
    }
    if (across) {
        Row(
            card.padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                head()
                body(Modifier)
                fileLine()
            }
            action(false)
        }
    } else {
        Column(card.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            head()
            // Where the cards are as tall as the tallest, the words take the room left, so the buttons stand in a
            // line; in a column as tall as its content a weight would leave them none.
            body(if (evenHeight) Modifier.weight(1f) else Modifier)
            fileLine()
            action(true)
        }
    }
}

/** A small rounded label: whether a platform is here yet, in [color] on [ground]. */
@Composable
private fun Chip(
    text: String,
    ground: Color,
    color: Color
) {
    PantrixText(
        text = text,
        style = strong(12.sp),
        color = color,
        maxLines = 1,
        modifier = Modifier.background(ground, CircleShape).padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

/** How the app is installed on macOS, and how the download is checked: side by side on a desktop. */
@Composable
internal fun Install(
    state: DownloadUiState,
    onAction: (DownloadAction) -> Unit
) {
    when (SiteTheme.layout) {
        SiteLayout.Desktop -> LandingBand(PaddingValues(horizontal = 24.dp, vertical = 72.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(56.dp)) {
                Steps(Modifier.weight(1f))
                CheckCard(state, onAction, Modifier.weight(1f))
            }
        }

        SiteLayout.Tablet -> LandingBand(PaddingValues(horizontal = 24.dp, vertical = 56.dp), gap = 40.dp) {
            Steps()
            CheckCard(state, onAction)
        }

        SiteLayout.Phone -> LandingBand(PaddingValues(horizontal = 16.dp, vertical = 40.dp), gap = 32.dp) {
            Steps()
            CheckCard(state, onAction)
        }
    }
}

/** The three steps of installing the app, each with its number. */
@Composable
private fun Steps(modifier: Modifier = Modifier) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SectionHeading(DownloadSay.InstallTitle.of(language), tablet = 32.sp)
        DownloadSay.Steps.forEachIndexed { index, step ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier.size(32.dp).background(colors.primary.container, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    PantrixText(text = "${index + 1}", style = strong(14.sp), color = colors.primary.onContainer)
                }
                Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    PantrixText(
                        text = step.title.of(language),
                        style = strong(15.5.sp, FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    val mono = SiteTheme.type.inlineCode.fontFamily
                    PantrixText(
                        text = buildAnnotatedString {
                            append(step.start.of(language))
                            if (step.file) {
                                withStyle(SpanStyle(fontFamily = mono, fontSize = 13.sp)) {
                                    append(SiteDownloads.MAC_FILE)
                                }
                            }
                            append(step.end.of(language))
                        },
                        style = small(14.5.sp),
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * How to check the download: the command that gives a file's SHA-256, and the release's own to compare it with, which
 * is copied with a button; while the release is read, and where it cannot be, the row says so instead.
 */
@Composable
private fun CheckCard(
    state: DownloadUiState,
    onAction: (DownloadAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val site = SiteTheme.colors
    Column(
        modifier.fillMaxWidth().landingCard(colors.surface, colors.border, corner = 16.dp).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixIcon(
                imageVector = SiteIcons.Shield,
                contentDescription = null,
                tint = colors.textPrimary,
                size = 20.dp
            )
            PantrixText(
                text = DownloadSay.CheckTitle.of(language),
                style = strong(17.sp, FontWeight.Bold),
                color = colors.textPrimary
            )
        }
        PantrixText(text = DownloadSay.CheckBody.of(language), style = small(14.5.sp), color = colors.textSecondary)
        PantrixText(
            text = HASH_COMMAND,
            style = mono(13.sp),
            color = site.code.text,
            modifier = Modifier
                .fillMaxWidth()
                .background(site.code.background, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )
        val hash = state.release.valueOrNull?.macSha256
        Row(
            Modifier
                .fillMaxWidth()
                .landingCard(site.surfaceAlt, colors.border, corner = 10.dp)
                .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hash != null) {
                PantrixText(
                    text = hash,
                    style = mono(12.5.sp),
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                CopyButton(state.copied) { onAction(DownloadAction.CopyHash) }
            } else {
                PantrixText(
                    text = if (state.release is LoadState.Loading) {
                        DownloadSay.ReadingRelease.of(language)
                    } else {
                        DownloadSay.HashOnReleasePage.of(language)
                    },
                    style = small(13.sp),
                    color = site.muted,
                    // As tall as the row is with its button, so the card does not move when the release arrives.
                    modifier = Modifier.weight(1f).padding(vertical = HASH_ROW_ROOM)
                )
            }
        }
    }
}

/** The button that copies the hash, which says for a while that it did. */
@Composable
private fun CopyButton(
    copied: Boolean,
    onClick: () -> Unit
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    PantrixButton(
        onClick = onClick,
        contentDescription = DownloadSay.CopyHash.of(language),
        variant = ButtonVariant.Outline,
        size = ButtonSize.Medium,
        style = ButtonStyle(medium = ButtonSizeStyle(height = 36.dp, horizontalPadding = 12.dp)),
        shape = RoundedCornerShape(9.dp),
        colors = ButtonColors(container = colors.surface, content = colors.textPrimary, border = colors.borderHover),
        tagKey = "copyHash"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixIcon(
                imageVector = if (copied) PantrixIcons.Check else PantrixIcons.Copy,
                contentDescription = null,
                size = 15.dp
            )
            PantrixText(
                text = (if (copied) DownloadSay.Copied else DownloadSay.Copy).of(language),
                style = strong(13.sp, FontWeight.Bold)
            )
        }
    }
}

/** What is in the app, a tile each: four across on a desktop, two on a tablet, one on a phone. */
@Composable
internal fun Features(lineUnder: Boolean) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 72.dp)
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 64.dp)
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 36.dp, bottom = 48.dp)
    }
    val (gap, columns) = when (layout) {
        SiteLayout.Desktop -> 28.dp to DESKTOP_FEATURES
        SiteLayout.Tablet -> 24.dp to 2
        SiteLayout.Phone -> 20.dp to 1
    }
    LandingBand(padding, ground = SiteTheme.colors.canvas, gap = gap, lineUnder = lineUnder) {
        SectionHeading(DownloadSay.FeaturesTitle.of(language))
        Column(verticalArrangement = Arrangement.spacedBy(FEATURE_GAP)) {
            DownloadSay.Features.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(FEATURE_GAP)) {
                    row.forEach { feature -> FeatureTile(feature, Modifier.weight(1f)) }
                    repeat(columns - row.size) { Box(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** Something the app has: its mark, what it is and what it is for. */
@Composable
private fun FeatureTile(
    feature: Feature,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier.size(40.dp).background(colors.primary.container, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            PantrixIcon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = colors.primary.onContainer,
                size = 20.dp
            )
        }
        PantrixText(
            text = feature.title.of(language),
            style = strong(16.sp, FontWeight.Bold),
            color = colors.textPrimary
        )
        PantrixText(text = feature.body.of(language), style = small(14.sp), color = colors.textSecondary)
    }
}

/** The command that gives a file's SHA-256 on macOS. */
private const val HASH_COMMAND = "shasum -a 256 ${SiteDownloads.MAC_FILE}"

/** The room above and below the hash row's words when it has no button: the button's height less a line's, halved. */
private val HASH_ROW_ROOM: Dp = 8.dp

/** How many tiles of what is in the app a desktop's row holds: all of them. */
private const val DESKTOP_FEATURES = 4

/** The room between the tiles of what is in the app, across and down. */
private val FEATURE_GAP = 24.dp
