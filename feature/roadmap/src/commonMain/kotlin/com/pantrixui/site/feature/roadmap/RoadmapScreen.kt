package com.pantrixui.site.feature.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.chrome.SiteFoot
import com.pantrixui.site.core.designsystem.content.FollowPageSection
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.LandingKicker
import com.pantrixui.site.core.designsystem.content.PageAnchors
import com.pantrixui.site.core.designsystem.content.landingHeading
import com.pantrixui.site.core.designsystem.content.pageAnchor
import com.pantrixui.site.core.designsystem.content.pageAnchorRoot
import com.pantrixui.site.core.designsystem.content.rememberScrollToAnchor
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LandingHeads
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.routing.SiteRoutes
import org.koin.compose.viewmodel.koinViewModel

/**
 * The roadmap, with its model: what is built, what comes next, server-driven UI, the three libraries and the
 * versions.
 *
 * @param onNavigate what opening a page of the site does, given its path.
 */
@Composable
fun RoadmapRoute(onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<RoadmapViewModel>()
    val uriHandler = LocalUriHandler.current
    BaseScreen(
        viewModel,
        onEffect = { effect ->
            when (effect) {
                is RoadmapEffect.Navigate -> onNavigate(effect.path)
                is RoadmapEffect.OpenUrl -> uriHandler.openUri(effect.url)
            }
        }
    ) { state, onAction -> RoadmapScreen(state, onAction) }
}

/**
 * The roadmap, drawn from its [state], as its design lays it out in each size: a tablet and a phone say it shorter,
 * and a phone lists the board's columns one under another.
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun RoadmapScreen(
    state: RoadmapUiState,
    onAction: (RoadmapAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    val scroll = rememberScrollState()
    val anchors = remember { PageAnchors() }
    val scrollTo = rememberScrollToAnchor(anchors, scroll)
    FollowPageSection(anchors, scroll)
    PantrixWindowTitle(LandingHeads.Roadmap.title.of(language))
    val suggest = { onAction(RoadmapAction.OpenLink(RoadmapSay.ISSUES)) }
    val open: (Link) -> Unit = { link ->
        when (link) {
            is Link.Page -> onAction(RoadmapAction.OpenLink(SiteRoutes.addressOf(link.route, language)))
            is Link.Section -> scrollTo(link.anchor)
        }
    }
    Column(modifier.fillMaxSize().pantrixVerticalScroll(scroll)) {
        Column(Modifier.fillMaxWidth().pageAnchorRoot(anchors)) {
            Hero(suggest)
            Board(state, onAction, open)
            Sdui(state, onAction, Modifier.pageAnchor(anchors, SDUI))
            Libraries(Modifier.pageAnchor(anchors, NATIVE))
            Versions(suggest, lineUnder = !desktop)
            // The design ends a tablet's and a phone's page with its versions; the header keeps the languages there.
            if (desktop) SiteFoot()
        }
    }
}

/** The page's words, and on a desktop and a tablet the way to suggest an item. */
@Composable
private fun Hero(onSuggest: () -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 28.dp)
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 44.dp, bottom = 24.dp)
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 20.dp)
    }
    val (title, line) = when (layout) {
        SiteLayout.Desktop -> 54.sp to DESKTOP_TITLE_LINE
        SiteLayout.Tablet -> 46.sp to TABLET_TITLE_LINE
        SiteLayout.Phone -> 36.sp to PHONE_TITLE_LINE
    }
    val words: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier, verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Phone) 12.dp else 14.dp)) {
            LandingKicker(RoadmapSay.Kicker.of(language))
            PantrixText(
                text = RoadmapSay.Title.of(language),
                style = landingHeading(title, line = line, spacing = TITLE_SPACING),
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
            PantrixText(
                text = RoadmapSay.Lead.of(layout).of(language),
                style = SiteTheme.type.lead.sized(
                    when (layout) {
                        SiteLayout.Desktop -> 18.sp
                        SiteLayout.Tablet -> 17.sp
                        SiteLayout.Phone -> 15.5.sp
                    }
                ),
                color = colors.textSecondary,
                modifier = Modifier.widthIn(max = LEAD_WIDTH)
            )
        }
    }
    val button = @Composable {
        LandingButton(
            text = RoadmapSay.Suggest.of(language),
            onClick = onSuggest,
            tag = "roadmapSuggest",
            padding = 20.dp,
            leading = PantrixIcons.Plus
        )
    }
    LandingBand(padding, gap = 14.dp) {
        when (layout) {
            SiteLayout.Desktop -> {
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.Bottom) {
                    words(Modifier.weight(1f))
                    button()
                }
            }

            SiteLayout.Tablet -> {
                words(Modifier)
                button()
            }

            SiteLayout.Phone -> {
                words(Modifier)
            }
        }
    }
}

/** A small rounded label over a part of the page: what it is, in [color] on [ground]. */
@Composable
internal fun Pill(
    text: String,
    ground: Color,
    color: Color,
    size: TextUnit = 12.sp
) {
    PantrixText(
        text = text,
        style = strong(size),
        color = color,
        modifier = Modifier
            .background(ground, CircleShape)
            .padding(horizontal = if (size > 12.sp) 10.dp else 8.dp, vertical = if (size > 11.5.sp) 3.dp else 2.dp)
    )
}

/** A small dot of [color], [size] across. */
@Composable
internal fun Dot(
    color: Color,
    size: Dp = 8.dp
) {
    Box(Modifier.size(size).background(color, CircleShape))
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

/** The site's code type at [size] in [weight]. */
@Composable
internal fun mono(
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal
): TextStyle = SiteTheme.type.inlineCode.copy(fontSize = size, fontWeight = weight)

/** A section heading at [size], its lines [line] times it apart. */
@Composable
internal fun sectionHeading(
    size: TextUnit,
    line: Float = SECTION_LINE
): TextStyle = landingHeading(size, line = line, spacing = SECTION_SPACING)

private const val DESKTOP_TITLE_LINE = 1.05f
private const val TABLET_TITLE_LINE = 1.06f
private const val PHONE_TITLE_LINE = 1.08f
private const val TITLE_SPACING = -0.035f
private const val SECTION_SPACING = -0.03f
private const val SECTION_LINE = 1.15f
private const val LINE = 1.5f
private val LEAD_WIDTH = 680.dp
