package com.pantrixui.site.feature.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.platform.rememberPantrixClipboard
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.chrome.SiteFoot
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.LandingButtonKind
import com.pantrixui.site.core.designsystem.content.LandingKicker
import com.pantrixui.site.core.designsystem.content.landingHeading
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LandingHeads
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.DesktopRelease
import com.pantrixui.site.core.model.SiteDownloads
import org.koin.compose.viewmodel.koinViewModel

/** The download page, with its model, which reads the desktop app's latest release. */
@Composable
fun DownloadRoute() {
    val viewModel = koinViewModel<DownloadViewModel>()
    val uriHandler = LocalUriHandler.current
    val clipboard = rememberPantrixClipboard()
    BaseScreen(
        viewModel,
        onEffect = { effect ->
            when (effect) {
                is DownloadEffect.OpenUrl -> uriHandler.openUri(effect.url)
                is DownloadEffect.Copy -> viewModel.onAction(DownloadAction.Copied(clipboard.copyText(effect.text)))
            }
        }
    ) { state, onAction -> DownloadScreen(state, onAction) }
}

/**
 * The download page, drawn from its [state], as its design lays it out in each size: the app and what it is, the
 * platforms it comes to, how it is installed and checked, and what is in it.
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun DownloadScreen(
    state: DownloadUiState,
    onAction: (DownloadAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    PantrixWindowTitle(LandingHeads.Download.title.of(language))
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    Column(modifier.fillMaxSize().pantrixVerticalScroll(rememberScrollState())) {
        Hero(state.release.valueOrNull, onAction)
        Platforms(state.release.valueOrNull, onAction)
        Install(state, onAction)
        Features(lineUnder = !desktop)
        // The design ends a tablet's and a phone's page with what the app has; the header keeps the languages there.
        if (desktop) SiteFoot()
    }
}

/** The app and what it is, the way to download it, and a picture of its window. */
@Composable
private fun Hero(
    release: DesktopRelease?,
    onAction: (DownloadAction) -> Unit
) {
    val layout = SiteTheme.layout
    when (layout) {
        SiteLayout.Desktop -> {
            LandingBand(PaddingValues(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 72.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        HeroWords(release, onAction)
                    }
                    AppWindow(Modifier.weight(WINDOW_SHARE), height = 360.dp, sidebar = true)
                }
            }
        }

        SiteLayout.Tablet -> {
            LandingBand(PaddingValues(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 48.dp), gap = 16.dp) {
                HeroWords(release, onAction)
                AppWindow(Modifier.padding(top = 16.dp), height = 340.dp, sidebar = true)
            }
        }

        SiteLayout.Phone -> {
            LandingBand(PaddingValues(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 32.dp), gap = 12.dp) {
                HeroWords(release, onAction)
                AppWindow(Modifier.padding(top = 12.dp), height = 300.dp, sidebar = false)
            }
        }
    }
}

/** The hero's words, its two buttons and what the release is, one under another in the column they stand in. */
@Composable
private fun HeroWords(
    release: DesktopRelease?,
    onAction: (DownloadAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val phone = layout == SiteLayout.Phone
    LandingKicker(DownloadSay.Kicker.of(language))
    PantrixText(
        text = DownloadSay.Title.of(language),
        style = when (layout) {
            SiteLayout.Desktop -> landingHeading(54.sp, line = 1.05f, spacing = TITLE_SPACING)
            SiteLayout.Tablet -> landingHeading(46.sp, line = 1.06f, spacing = TITLE_SPACING)
            SiteLayout.Phone -> landingHeading(36.sp, line = 1.08f, spacing = TITLE_SPACING)
        },
        color = colors.textPrimary,
        modifier = Modifier.semantics { heading() }
    )
    PantrixText(
        text = DownloadSay.Lead.of(layout).of(language),
        style = SiteTheme.type.lead.sized(
            when (layout) {
                SiteLayout.Desktop -> 18.sp
                SiteLayout.Tablet -> 17.sp
                SiteLayout.Phone -> 15.5.sp
            }
        ),
        color = colors.textSecondary
    )
    val download = @Composable { modifier: Modifier ->
        LandingButton(
            text = DownloadSay.DownloadForMac.of(language),
            onClick = { onAction(DownloadAction.OpenUrl(SiteDownloads.MAC_DOWNLOAD)) },
            tag = "downloadMac",
            modifier = modifier,
            height = if (phone) 50.dp else 52.dp,
            fill = phone,
            textSize = 15.5.sp,
            leading = PantrixIcons.Download,
            iconSize = 18.dp
        )
    }
    val notes = @Composable { modifier: Modifier ->
        LandingButton(
            text = DownloadSay.ReleaseNotes.of(language),
            onClick = { onAction(DownloadAction.OpenUrl(release?.notes ?: SiteDownloads.LATEST)) },
            tag = "releaseNotes",
            modifier = modifier,
            kind = LandingButtonKind.Outline,
            height = if (phone) 50.dp else 52.dp,
            padding = 18.dp,
            fill = phone,
            trailing = SiteIcons.ArrowUpRight
        )
    }
    if (phone) {
        Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            download(Modifier)
            notes(Modifier)
        }
    } else {
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            download(Modifier)
            notes(Modifier)
        }
    }
    ReleaseLine(release)
}

/** What the release is, where it is known, and what the app runs on; then that Apple has checked it. */
@Composable
private fun ReleaseLine(release: DesktopRelease?) {
    val language = LocalLanguage.current
    val strings = PantrixTheme.strings
    val size = if (SiteTheme.layout == SiteLayout.Phone) 13.sp else 13.5.sp
    val known = release?.let {
        listOfNotNull(
            "${DownloadSay.Version.of(language)} ${it.version}",
            strings.date(it.day, it.month, it.year),
            it.macSize?.let(strings::fileSize)
        )
    }.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PantrixText(
            text = (known + DownloadSay.Runs.of(language)).joinToString(SEPARATOR),
            style = small(size),
            color = SiteTheme.colors.muted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixIcon(
                imageVector = SiteIcons.Shield,
                contentDescription = null,
                tint = PantrixTheme.colors.success.onContainer,
                size = 15.dp
            )
            PantrixText(
                text = DownloadSay.Signed.of(language),
                style = small(size).copy(fontWeight = FontWeight.SemiBold),
                color = PantrixTheme.colors.success.onContainer
            )
        }
    }
}

/** A section's heading: 32 on a desktop, [tablet] on a tablet and 24 on a phone. */
@Composable
internal fun SectionHeading(
    text: String,
    tablet: TextUnit = 28.sp
) {
    PantrixText(
        text = text,
        style = landingHeading(
            when (SiteTheme.layout) {
                SiteLayout.Desktop -> 32.sp
                SiteLayout.Tablet -> tablet
                SiteLayout.Phone -> 24.sp
            },
            line = SECTION_LINE,
            spacing = SECTION_SPACING
        ),
        color = PantrixTheme.colors.textPrimary,
        modifier = Modifier.fillMaxWidth().semantics { heading() }
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

/** The site's code type at [size]. */
@Composable
internal fun mono(size: TextUnit): TextStyle = SiteTheme.type.inlineCode.copy(fontSize = size)

/** How much wider than the words the window's picture is beside them, on a desktop. */
private const val WINDOW_SHARE = 1.05f

/** What stands between the facts of the release. */
private const val SEPARATOR = " · "

private const val TITLE_SPACING = -0.035f
private const val SECTION_SPACING = -0.03f
private const val SECTION_LINE = 1.15f
private const val LINE = 1.5f
