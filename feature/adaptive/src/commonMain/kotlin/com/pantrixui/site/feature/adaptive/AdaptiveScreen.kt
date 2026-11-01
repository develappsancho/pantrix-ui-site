package com.pantrixui.site.feature.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.chrome.SiteFoot
import com.pantrixui.site.core.designsystem.content.CodeView
import com.pantrixui.site.core.designsystem.content.Hairline
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.landingBandPadding
import com.pantrixui.site.core.designsystem.content.landingGutter
import com.pantrixui.site.core.designsystem.device.SimDevice
import com.pantrixui.site.core.designsystem.inCapitals
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LandingHeads
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.routing.SiteRoutes
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The adaptive page, with its model: a window simulator drawn by Pantrix's own scaffolds, the window's classes, the
 * API that reads them, the patterns that use them and what not to do.
 *
 * @param onNavigate what opening a page of the site does, given its path.
 */
@Composable
fun AdaptiveRoute(onNavigate: (String) -> Unit) {
    val language = LocalLanguage.current
    val viewModel = koinViewModel<AdaptiveViewModel>(key = "adaptive_${language.code}") { parametersOf(language) }
    BaseScreen(viewModel) { state, onAction ->
        AdaptiveScreen(state, onAction, onGuides = {
            onNavigate(SiteRoutes.addressOf(Route.Guide("adaptive"), language))
        })
    }
}

/**
 * The adaptive page, drawn from its [state].
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param onGuides what the link to the adaptive guides does.
 * @param modifier the screen's modifier.
 */
@Composable
fun AdaptiveScreen(
    state: AdaptiveUiState,
    onAction: (AdaptiveAction) -> Unit,
    onGuides: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val code = state.code.valueOrNull.orEmpty()
    PantrixWindowTitle(LandingHeads.Adaptive.title.of(language))
    Column(modifier.fillMaxSize().pantrixVerticalScroll(rememberScrollState())) {
        Hero()
        LandingBand(
            PaddingValues(
                start = if (layout == SiteLayout.Phone) 0.dp else 24.dp,
                end = if (layout == SiteLayout.Phone) 0.dp else 24.dp,
                top = if (layout == SiteLayout.Phone) 4.dp else 8.dp,
                bottom = sectionBottom()
            )
        ) {
            Simulator(state, onAction)
        }
        Classes(state.device)
        WindowInCode(code)
        Patterns(code)
        Pitfalls(code, onGuides)
        // The design ends a tablet's and a phone's page with its pitfalls; the header keeps the languages there.
        if (SiteTheme.layout == SiteLayout.Desktop) SiteFoot()
    }
}

@Composable
private fun sectionBottom(): Dp = when (SiteTheme.layout) {
    SiteLayout.Desktop -> 56.dp
    SiteLayout.Tablet -> 44.dp
    SiteLayout.Phone -> 36.dp
}

/** The page's kicker, title and what it is. */
@Composable
private fun Hero() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    LandingBand(
        PaddingValues(
            start = landingGutter,
            end = landingGutter,
            top = when (layout) {
                SiteLayout.Desktop -> 56.dp
                SiteLayout.Tablet -> 44.dp
                SiteLayout.Phone -> 28.dp
            },
            bottom = if (phone) 16.dp else 24.dp
        ),
        gap = if (phone) 12.dp else 16.dp
    ) {
        PantrixText(
            text = AdaptiveSay.Kicker.of(language).inCapitals(),
            style = heading(if (phone) 12.sp else 13.sp, line = 1.4f, spacing = 0.08f),
            color = PantrixTheme.colors.primary.onContainer
        )
        PantrixText(
            text = AdaptiveSay.Title.of(language),
            style = when (layout) {
                SiteLayout.Desktop -> heading(56.sp, line = 1.04f, spacing = -0.035f)
                SiteLayout.Tablet -> heading(46.sp, line = 1.06f, spacing = -0.035f)
                SiteLayout.Phone -> heading(36.sp, line = 1.08f, spacing = -0.035f)
            },
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.widthIn(max = 900.dp).semantics { heading() }
        )
        PantrixText(
            text = (if (phone) AdaptiveSay.LeadShort else AdaptiveSay.Lead).of(language),
            style = SiteTheme.type.lead,
            color = PantrixTheme.colors.textSecondary,
            modifier = Modifier.widthIn(max = 760.dp)
        )
    }
}

/** The five widths, the window chosen marked on them, and the three heights. */
@Composable
private fun Classes(device: SimDevice) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    LandingBand(landingBandPadding(), ground = SiteTheme.colors.canvas, gap = if (phone) 12.dp else 24.dp) {
        Column(Modifier.widthIn(max = 760.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            H2(AdaptiveSay.ClassesTitle.of(language))
            if (!phone) Lead(AdaptiveSay.ClassesLead.of(language))
        }
        val classes = listOf(
            Triple("Compact", "${AdaptiveSay.Under.of(language)} ${MEDIUM_FROM}dp", MEDIUM_FROM),
            Triple("Medium", "$MEDIUM_FROM–${EXPANDED_FROM - 1}", EXPANDED_FROM - MEDIUM_FROM),
            Triple("Expanded", "$EXPANDED_FROM–${LARGE_FROM - 1}", LARGE_FROM - EXPANDED_FROM),
            Triple("Large", "$LARGE_FROM–${EXTRA_LARGE_FROM - 1}", EXTRA_LARGE_FROM - LARGE_FROM),
            Triple("ExtraLarge", "$EXTRA_LARGE_FROM ${AdaptiveSay.AndUp.of(language)}", EXTRA_LARGE_SHARE)
        )
        val current = widthClassName(device.width)
        if (phone) {
            Column(Modifier.fillMaxWidth().framed()) {
                classes.forEachIndexed { index, (name, range, _) ->
                    if (index > 0) Hairline()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                if (name ==
                                    current
                                ) {
                                    PantrixTheme.colors.primary.container
                                } else {
                                    PantrixTheme.colors.surface
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PantrixText(
                            text = name,
                            style = strong(15.sp),
                            color = PantrixTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        PantrixText(text = range, style = small(), color = SiteTheme.colors.muted)
                    }
                }
            }
        } else {
            Marker(device, classes)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AdaptiveSay.Heights.forEach { (title, range, body) ->
                    Column(
                        Modifier.weight(
                            1f
                        ).background(
                            PantrixTheme.colors.surface,
                            RoundedCornerShape(14.dp)
                        ).border(1.dp, PantrixTheme.colors.border, RoundedCornerShape(14.dp)).padding(16.dp)
                    ) {
                        PantrixText(
                            text = title.of(language),
                            style = strong(15.sp),
                            color = PantrixTheme.colors.textPrimary
                        )
                        PantrixText(text = range.of(language), style = small(), color = SiteTheme.colors.muted)
                        PantrixText(
                            text = body.of(language),
                            style = small(14.sp),
                            color = PantrixTheme.colors.textSecondary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/** The widths' bar, each class as wide as its share of the first 1800dp, and the window chosen marked over it. */
@Composable
private fun Marker(
    device: SimDevice,
    classes: List<Triple<String, String, Int>>
) {
    val language = LocalLanguage.current
    val shape = RoundedCornerShape(14.dp)
    BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 30.dp)) {
        val at = maxWidth * (minOf(device.width, MARKER_LAST).toFloat() / MARKER_END)
        Row(Modifier.fillMaxWidth().height(64.dp).clip(shape).border(1.dp, PantrixTheme.colors.border, shape)) {
            classes.forEachIndexed { index, (name, range, share) ->
                Column(
                    Modifier
                        .weight(share.toFloat())
                        .fillMaxSize()
                        .background(if (index % 2 == 0) PantrixTheme.colors.surface else SiteTheme.colors.surfaceAlt)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    PantrixText(
                        text = name,
                        style = strong(14.sp),
                        color = PantrixTheme.colors.textPrimary,
                        maxLines = 1
                    )
                    PantrixText(text = range, style = small(), color = SiteTheme.colors.muted, maxLines = 1)
                }
            }
        }
        PantrixText(
            text = "${AdaptiveSay.device(device).of(language)} · ${device.width}dp",
            style = strong(12.sp),
            color = PantrixTheme.colors.primary.onColor,
            maxLines = 1,
            modifier = Modifier
                .offset(x = at - MARKER_HALF, y = (-30).dp)
                .background(PantrixTheme.colors.primary.color, RoundedCornerShape(percent = 50))
                .padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}

/** "The window comes with the theme": the API that reads the window, and which helper is for what. */
@Composable
private fun WindowInCode(code: Map<String, Block.Code>) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val words: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            H2(AdaptiveSay.CodeTitle.of(language))
            Lead(AdaptiveSay.CodeLeadStart.of(language), "PantrixTheme", AdaptiveSay.CodeLeadEnd.of(language))
            code["window"]?.let { CodeView(it) }
        }
    }
    val helpers: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (layout != SiteLayout.Phone) {
                PantrixText(
                    text = AdaptiveSay.WhichHelper.of(language),
                    style = strong(20.sp),
                    color = PantrixTheme.colors.textPrimary
                )
            }
            Helpers()
        }
    }
    LandingBand(landingBandPadding(), gap = 40.dp) {
        if (layout == SiteLayout.Desktop) {
            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                words(Modifier.weight(WORDS_SHARE))
                helpers(Modifier.weight(HELPERS_SHARE))
            }
        } else {
            words(Modifier.fillMaxWidth())
            helpers(Modifier.fillMaxWidth())
        }
    }
}

/** Which helper reads the window for what. */
@Composable
private fun Helpers() {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    val code = SiteTheme.type.inlineCode.copy(fontSize = 12.5.sp)
    Column(Modifier.fillMaxWidth().framed()) {
        if (!phone) {
            Row(
                Modifier.fillMaxWidth().background(
                    SiteTheme.colors.surfaceAlt
                ).padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                PantrixText(
                    AdaptiveSay.YouNeed.of(language),
                    style = strong(12.sp),
                    color = SiteTheme.colors.muted,
                    modifier = Modifier.weight(1f)
                )
                PantrixText(
                    AdaptiveSay.Write.of(language),
                    style = strong(12.sp),
                    color = SiteTheme.colors.muted,
                    modifier = Modifier.weight(HELPER_CODE_SHARE)
                )
            }
        }
        AdaptiveSay.Helpers.forEachIndexed { index, (need, write) ->
            if (index > 0 || !phone) Hairline()
            if (phone) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                    PantrixText(need.of(language), style = small(13.5.sp), color = PantrixTheme.colors.textSecondary)
                    PantrixText(write, style = code, color = PantrixTheme.colors.primary.onContainer)
                }
            } else {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PantrixText(
                        need.of(language),
                        style = small(13.5.sp),
                        color = PantrixTheme.colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    PantrixText(
                        write,
                        style = code,
                        color = PantrixTheme.colors.primary.onContainer,
                        modifier = Modifier.weight(HELPER_CODE_SHARE)
                    )
                }
            }
        }
    }
}

/** "Six patterns, ready made": each its sketches beside what it does and its code. */
@Composable
private fun Patterns(code: Map<String, Block.Code>) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val desktop = layout == SiteLayout.Desktop
    LandingBand(landingBandPadding(), ground = SiteTheme.colors.canvas, gap = if (desktop) 28.dp else 18.dp) {
        Column(Modifier.widthIn(max = 760.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            H2(AdaptiveSay.PatternsTitle.of(language))
            if (layout != SiteLayout.Phone) Lead(AdaptiveSay.PatternsLead.of(language))
        }
        AdaptiveSay.Patterns.forEach { pattern ->
            val shape = RoundedCornerShape(if (desktop) 20.dp else 16.dp)
            val card = Modifier
                .fillMaxWidth()
                .background(PantrixTheme.colors.surface, shape)
                .border(1.dp, PantrixTheme.colors.border, shape)
                .padding(if (layout == SiteLayout.Phone) 16.dp else 24.dp)
            val text: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PantrixText(pattern.api, style = strong(12.5.sp), color = PantrixTheme.colors.primary.onContainer)
                    PantrixText(
                        pattern.title.of(language),
                        style = strong(if (desktop) 22.sp else 20.sp),
                        color = PantrixTheme.colors.textPrimary,
                        modifier = Modifier.semantics { heading() }
                    )
                    if (!desktop) PatternSketches(pattern, Modifier.fillMaxWidth())
                    PantrixText(
                        pattern.body.of(language),
                        style = SiteTheme.type.body,
                        color = PantrixTheme.colors.textSecondary
                    )
                    code[pattern.code]?.let { CodeView(it) }
                }
            }
            if (desktop) {
                Row(
                    card,
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PatternSketches(pattern, Modifier.weight(1f))
                    text(Modifier.weight(1f))
                }
            } else {
                Box(card) { text(Modifier.fillMaxWidth()) }
            }
        }
    }
}

/** A fold in a preview and a test, on a desktop, and the pitfalls, with the way to the guides. */
@Composable
private fun Pitfalls(
    code: Map<String, Block.Code>,
    onGuides: () -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val frame: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            H2(AdaptiveSay.FrameTitle.of(language), small = true)
            Lead("", "PantrixWindowFrame", AdaptiveSay.FrameLeadEnd.of(language))
            code["frame"]?.let { CodeView(it) }
        }
    }
    val pitfalls: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            H2(AdaptiveSay.PitfallsTitle.of(language), small = true)
            PitfallList()
            GuidesLink(language, onGuides)
        }
    }
    LandingBand(landingBandPadding(), gap = 40.dp) {
        if (layout == SiteLayout.Desktop) {
            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                frame(Modifier.weight(FRAME_SHARE))
                pitfalls(Modifier.weight(PITFALLS_SHARE))
            }
        } else {
            pitfalls(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun PitfallList() {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    val label: @Composable (Boolean) -> Unit = { good ->
        PantrixText(
            text = (if (good) AdaptiveSay.ButThis else AdaptiveSay.NotThis).of(language),
            style = strong(12.sp),
            color = if (good) PantrixTheme.colors.success.onContainer else PantrixTheme.colors.danger.onContainer
        )
    }
    if (phone) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AdaptiveSay.Pitfalls.forEach { (bad, good) ->
                Column(
                    Modifier.fillMaxWidth().framed(12.dp).padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    label(false)
                    PantrixText(bad.of(language), style = small(14.sp), color = PantrixTheme.colors.textPrimary)
                    label(true)
                    PantrixText(good.of(language), style = small(14.sp), color = PantrixTheme.colors.textSecondary)
                }
            }
        }
    } else {
        Column(Modifier.fillMaxWidth().framed()) {
            AdaptiveSay.Pitfalls.forEachIndexed { index, (bad, good) ->
                if (index > 0) Hairline()
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        label(false)
                        PantrixText(bad.of(language), style = small(14.sp), color = PantrixTheme.colors.textPrimary)
                    }
                    Column(Modifier.weight(1f)) {
                        label(true)
                        PantrixText(good.of(language), style = small(14.sp), color = PantrixTheme.colors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidesLink(
    language: Language,
    onClick: () -> Unit
) {
    val color = PantrixTheme.colors.primary.onContainer
    Row(
        Modifier.siteClickable(
            tag = "adaptiveGuides",
            role = Role.Button,
            shape = RoundedCornerShape(6.dp),
            onClick = onClick
        ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(AdaptiveSay.ReadTheGuides.of(language), style = strong(15.sp, FontWeight.Bold), color = color)
        PantrixIcon(imageVector = PantrixIcons.ArrowForward, contentDescription = null, tint = color, size = 16.dp)
    }
}

/** A section's heading: 36 on a desktop, 32 on a tablet and 26 on a phone; a size smaller where [small]. */
@Composable
private fun H2(
    text: String,
    small: Boolean = false
) {
    val size = when (SiteTheme.layout) {
        SiteLayout.Desktop -> if (small) 32.sp else 36.sp
        SiteLayout.Tablet -> 32.sp
        SiteLayout.Phone -> 26.sp
    }
    PantrixText(
        text = text,
        style = heading(size, line = 1.12f, spacing = -0.03f),
        color = PantrixTheme.colors.textPrimary,
        modifier = Modifier.semantics { heading() }
    )
}

/** A section's sentence, a name of the code in it where [code] is given. */
@Composable
private fun Lead(
    start: String,
    code: String? = null,
    end: String = ""
) {
    val mono = SiteTheme.type.inlineCode
    PantrixText(
        text = buildAnnotatedString {
            append(start)
            if (code != null) {
                withStyle(
                    SpanStyle(fontFamily = mono.fontFamily, fontSize = 14.sp, color = PantrixTheme.colors.textPrimary)
                ) {
                    append(code)
                }
            }
            append(end)
        },
        style = SiteTheme.type.body.copy(fontSize = if (SiteTheme.layout == SiteLayout.Phone) 15.sp else 16.sp),
        color = PantrixTheme.colors.textSecondary
    )
}

/** The site's heading type at [size], its line and its letters' spacing in its size. */
@Composable
private fun heading(
    size: TextUnit,
    line: Float,
    spacing: Float
): TextStyle = SiteTheme.type.pageTitle.copy(
    fontSize = size,
    lineHeight = (size.value * line).sp,
    letterSpacing = (size.value * spacing).sp,
    fontWeight = FontWeight.ExtraBold
)

@Composable
private fun strong(
    size: TextUnit,
    weight: FontWeight = FontWeight.ExtraBold
): TextStyle = SiteTheme.type.body.copy(fontSize = size, lineHeight = (size.value * LINE).sp, fontWeight = weight)

@Composable
private fun small(size: TextUnit = 12.5.sp): TextStyle =
    SiteTheme.type.bodySmall.copy(fontSize = size, lineHeight = (size.value * LINE).sp)

@Composable
private fun Modifier.framed(corner: Dp = 14.dp): Modifier {
    val shape = RoundedCornerShape(corner)
    return clip(shape).background(PantrixTheme.colors.surface).border(1.dp, PantrixTheme.colors.border, shape)
}

private val MARKER_HALF = 60.dp
private const val MARKER_END = 1800

/** The furthest the marker stands, a little short of the bar's end. */
private const val MARKER_LAST = 1790
private const val EXTRA_LARGE_SHARE = 200
private const val WORDS_SHARE = 460f
private const val HELPERS_SHARE = 560f
private const val FRAME_SHARE = 440f
private const val PITFALLS_SHARE = 560f
private const val HELPER_CODE_SHARE = 1.4f
private const val LINE = 1.45f
