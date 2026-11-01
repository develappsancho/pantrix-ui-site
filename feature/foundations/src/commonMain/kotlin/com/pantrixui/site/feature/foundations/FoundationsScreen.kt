package com.pantrixui.site.feature.foundations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.ArticlePage
import com.pantrixui.site.core.designsystem.content.LocalPageSection
import com.pantrixui.site.core.designsystem.content.PageAnchors
import com.pantrixui.site.core.designsystem.content.RoundTag
import com.pantrixui.site.core.designsystem.content.edgeToEdge
import com.pantrixui.site.core.designsystem.content.pageAnchor
import com.pantrixui.site.core.designsystem.content.rememberScrollToAnchor
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LandingHeads
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.ContentsEntry
import com.pantrixui.site.core.model.FoundationSection
import org.koin.compose.viewmodel.koinViewModel

/**
 * The foundations page, with its model: the tokens every component is drawn with, read from the theme the site
 * wears, its colours' contrast worked out as the page draws them.
 */
@Composable
fun FoundationsRoute() {
    val viewModel = koinViewModel<FoundationsViewModel>()
    BaseScreen(viewModel) { state, onAction -> FoundationsScreen(state, onAction) }
}

/**
 * The foundations page, drawn from its [state]: its head, then a section for each kind of token, with the list of
 * them beside it on a wide desktop and over them elsewhere.
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun FoundationsScreen(
    state: FoundationsUiState,
    onAction: (FoundationsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    // A phone shows motion and density as one section, as its design does.
    val shown = if (phone) FoundationSection.entries - FoundationSection.Density else FoundationSection.entries
    val contents = shown.map { ContentsEntry(it.anchor, it.title.of(language)) }
    val anchors = remember { PageAnchors() }
    val scroll = rememberScrollState()
    val scrollTo = rememberScrollToAnchor(anchors, scroll)
    PantrixWindowTitle(LandingHeads.Foundations.title.of(language))
    ArticlePage(
        contents = contents,
        modifier = modifier,
        scrollState = scroll,
        anchors = anchors,
        articleWidth = ARTICLE_WIDTH
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                when (layout) {
                    SiteLayout.Desktop -> 64.dp
                    SiteLayout.Tablet -> 52.dp
                    SiteLayout.Phone -> 40.dp
                }
            )
        ) {
            Head(contents, scrollTo)
            ColourSection(state, onAction, Modifier.pageAnchor(anchors, FoundationSection.Colour.anchor))
            TypeSection(Modifier.pageAnchor(anchors, FoundationSection.Type.anchor))
            SpacingSection(Modifier.pageAnchor(anchors, FoundationSection.Spacing.anchor))
            ShapeSection(Modifier.pageAnchor(anchors, FoundationSection.Shape.anchor))
            ElevationSection(Modifier.pageAnchor(anchors, FoundationSection.Elevation.anchor))
            MotionSection(state, onAction, Modifier.pageAnchor(anchors, FoundationSection.Motion.anchor))
            DensitySection(Modifier.pageAnchor(anchors, FoundationSection.Density.anchor))
        }
    }
}

/** The page's title, what it shows, its tags on a desktop, and its sections as a row of links on a narrower window. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Head(
    contents: List<ContentsEntry>,
    onSection: (String) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    Column(verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Phone) 12.dp else 14.dp)) {
        PantrixText(
            text = FoundationsSay.Title.of(language),
            style = SiteTheme.type.pageTitle,
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
        val lead = when (layout) {
            SiteLayout.Desktop -> FoundationsSay.Lead
            SiteLayout.Tablet -> FoundationsSay.LeadTablet
            SiteLayout.Phone -> FoundationsSay.LeadPhone
        }
        PantrixText(text = lead.of(language), style = SiteTheme.type.lead, color = PantrixTheme.colors.textSecondary)
        when (layout) {
            SiteLayout.Desktop -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FoundationsSay.Tags.forEachIndexed { index, tag ->
                    RoundTag(
                        text = tag.of(language),
                        container = if (index == 0) {
                            PantrixTheme.colors.primary.container
                        } else {
                            SiteTheme.colors.hover
                        },
                        content = if (index == 0) {
                            PantrixTheme.colors.primary.onContainer
                        } else {
                            PantrixTheme.colors.textSecondary
                        }
                    )
                }
            }

            SiteLayout.Tablet -> SectionTabs(contents, onSection)

            SiteLayout.Phone -> SectionChips(contents, onSection)
        }
    }
}

/** The anchor of the section the site is at, or the first's. */
@Composable
private fun currentSection(contents: List<ContentsEntry>): String? =
    LocalPageSection.current?.current?.takeIf { anchor -> contents.any { it.anchor == anchor } }
        ?: contents.firstOrNull()?.anchor

/** A tablet's sections: a box of links that scrolls sideways, the section the page is at lifted out of it. */
@Composable
private fun SectionTabs(
    contents: List<ContentsEntry>,
    onSection: (String) -> Unit
) {
    val current = currentSection(contents)
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .background(SiteTheme.colors.surfaceAlt, shape)
            .border(1.dp, SiteTheme.colors.hairline, shape)
            .horizontalScroll(rememberScrollState())
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        contents.forEach { entry ->
            val on = entry.anchor == current
            val tab = RoundedCornerShape(10.dp)
            PantrixText(
                text = entry.title,
                style = SiteTheme.type.nav.copy(
                    fontSize = 13.5.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (on) PantrixTheme.colors.primary.onContainer else PantrixTheme.colors.textSecondary,
                maxLines = 1,
                modifier = Modifier
                    .then(if (on) Modifier.shadow(1.dp, tab).background(PantrixTheme.colors.surface, tab) else Modifier)
                    .siteClickable(tag = "section_${entry.anchor}", role = Role.Tab, shape = tab) {
                        onSection(entry.anchor)
                    }
                    .semantics { selected = on }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            )
        }
    }
}

/** A phone's sections: chips in a row that scrolls sideways from edge to edge, the section the page is at marked. */
@Composable
private fun SectionChips(
    contents: List<ContentsEntry>,
    onSection: (String) -> Unit
) {
    val current = currentSection(contents)
    Row(Modifier.edgeToEdge(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        contents.forEach { entry ->
            val on = entry.anchor == current
            val chip = RoundedCornerShape(10.dp)
            PantrixText(
                text = entry.title,
                style = SiteTheme.type.nav.copy(fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold),
                color = if (on) PantrixTheme.colors.primary.onContainer else PantrixTheme.colors.textSecondary,
                maxLines = 1,
                modifier = Modifier
                    .background(if (on) PantrixTheme.colors.primary.container else PantrixTheme.colors.surface, chip)
                    .border(1.dp, if (on) SiteTheme.colors.accentLine else PantrixTheme.colors.border, chip)
                    .siteClickable(tag = "section_${entry.anchor}", role = Role.Tab, shape = chip) {
                        onSection(entry.anchor)
                    }
                    .semantics { selected = on }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

/** A section of the page: its heading, what stands beside the heading, and what it shows, [gap] apart. */
@Composable
internal fun Section(
    title: String,
    modifier: Modifier = Modifier,
    aside: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val layout = SiteTheme.layout
    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            when (layout) {
                SiteLayout.Desktop -> 16.dp
                SiteLayout.Tablet -> 14.dp
                SiteLayout.Phone -> 12.dp
            }
        )
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PantrixText(
                text = title,
                style = sectionTitle(),
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            aside?.invoke()
        }
        content()
    }
}

/** The type of a section's heading: 28 on a desktop and a tablet, 24 on a phone. */
@Composable
private fun sectionTitle(): TextStyle {
    val size = if (SiteTheme.layout == SiteLayout.Phone) 24.sp else 28.sp
    return SiteTheme.type.pageTitle.copy(
        fontSize = size,
        lineHeight = (size.value * HEADING_LINE).sp,
        letterSpacing = (size.value * HEADING_SPACING).sp
    )
}

/** A section's sentence under its heading, a name of the code in it where [code] is given. */
@Composable
internal fun Lead(
    start: String,
    code: String? = null,
    end: String = "",
    size: TextUnit = SiteTheme.type.body.fontSize
) {
    PantrixText(
        text = withCode(start, code, end),
        style = SiteTheme.type.body.copy(fontSize = size),
        color = PantrixTheme.colors.textSecondary
    )
}

/** [start], [code] in the site's code type, and [end]. */
@Composable
internal fun withCode(
    start: String,
    code: String?,
    end: String
): AnnotatedString {
    val mono = SiteTheme.type.inlineCode
    val color = PantrixTheme.colors.textPrimary
    return buildAnnotatedString {
        append(start)
        if (code != null) {
            withStyle(SpanStyle(fontFamily = mono.fontFamily, fontSize = mono.fontSize, color = color)) { append(code) }
        }
        append(end)
    }
}

/** The widest the article is on a desktop, as the design lays it out. */
private val ARTICLE_WIDTH = 880.dp

/** A section heading's line and letter spacing, in its size. */
private const val HEADING_LINE = 1.25f
private const val HEADING_SPACING = -0.02f
