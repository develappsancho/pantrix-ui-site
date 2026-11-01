package com.pantrixui.site.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.color.PantrixColors
import com.pantrixui.site.core.designsystem.PantrixPreset
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.chrome.FootBand
import com.pantrixui.site.core.designsystem.chrome.FootLine
import com.pantrixui.site.core.designsystem.content.CodeView
import com.pantrixui.site.core.designsystem.content.LANDING_WIDTH
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.LandingButtonKind
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ComponentGroups
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Words

/** "For coding assistants": where an assistant reads the library, beside a terminal that asks one for a form. */
@Composable
internal fun ForAssistants() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val terminal = Block.Code(
        language = "text",
        file = HomeSay.Terminal.of(language),
        lines = terminalLines(language, short = layout == SiteLayout.Phone)
    )
    when (layout) {
        SiteLayout.Desktop -> LandingBand(PaddingValues(horizontal = 24.dp, vertical = 96.dp)) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val room = maxWidth - AI_BASIS - TERMINAL_BASIS - 48.dp
                val words: @Composable (Modifier) -> Unit = { modifier ->
                    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Kicker(HomeSay.AssistantsKicker.of(language))
                        SectionHeading(HomeSay.AssistantsTitle.of(language))
                        SectionLead(
                            start = HomeSay.AssistantsLeadStart.of(language),
                            code = LLMS,
                            end = HomeSay.AssistantsLeadEnd.of(language)
                        )
                        Sources(HomeSay.AssistantsChips)
                    }
                }
                val panel: @Composable (Modifier) -> Unit = { modifier ->
                    CodeView(
                        code = terminal,
                        modifier = modifier.shadow(12.dp, RoundedCornerShape(14.dp), clip = false)
                    )
                }
                if (room >= 0.dp) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        words(Modifier.width(AI_BASIS + room / 2))
                        panel(Modifier.width(TERMINAL_BASIS + room / 2))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(48.dp)) {
                        words(Modifier.fillMaxWidth())
                        panel(Modifier.fillMaxWidth())
                    }
                }
            }
        }

        SiteLayout.Tablet -> LandingBand(PaddingValues(horizontal = 24.dp, vertical = 72.dp), gap = 20.dp) {
            Kicker(HomeSay.AssistantsKicker.of(language))
            SectionHeading(HomeSay.AssistantsTitle.of(language))
            SectionLead(
                start = HomeSay.AssistantsLeadStart.of(language),
                code = LLMS,
                end = HomeSay.AssistantsLeadEnd.of(language)
            )
            CodeView(code = terminal)
        }

        SiteLayout.Phone -> LandingBand(PaddingValues(horizontal = 16.dp, vertical = 48.dp), gap = 16.dp) {
            Kicker(HomeSay.AssistantsKicker.of(language))
            SectionHeading(HomeSay.AssistantsTitle.of(language))
            SectionLead(
                start = HomeSay.AssistantsLeadPhoneStart.of(language),
                code = LLMS,
                end = HomeSay.AssistantsLeadPhoneEnd.of(language)
            )
            CodeView(code = terminal)
            Sources(HomeSay.AssistantsChips.dropLast(1))
        }
    }
}

/** The places an assistant reads the library from, as labels. */
@Composable
private fun Sources(names: List<String>) {
    val phone = SiteTheme.layout == SiteLayout.Phone
    val shape = RoundedCornerShape(10.dp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        names.forEach { name ->
            Box(
                Modifier
                    .height(32.dp)
                    .then(if (phone) Modifier else Modifier.background(PantrixTheme.colors.surface, shape))
                    .border(1.dp, PantrixTheme.colors.border, shape)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                PantrixText(
                    text = name,
                    style = SiteTheme.type.tag.copy(fontSize = 13.sp),
                    color = PantrixTheme.colors.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * The terminal's lines: the server added to an assistant by the site's address, what the reader asks of it in
 * [language], and what it looks up and writes. A phone's are fewer and its command is broken over two lines.
 */
internal fun terminalLines(
    language: Language,
    short: Boolean
): List<List<Token>> {
    val address = LibraryFacts.MCP_ADDRESS
    val asked = if (short) HomeSay.AskedShort else HomeSay.Asked
    val prompt = listOf(Token(">", TokenKind.Keyword), Token(" " + asked.of(language)))
    val component = listOf(Token("  get_component(\"PantrixTextArea\")", TokenKind.Comment))
    return if (short) {
        listOf(
            listOf(Token("$", TokenKind.Keyword), Token(" claude mcp add -t http \\")),
            listOf(Token("    pantrix-ui $address")),
            emptyList(),
            prompt,
            component
        )
    } else {
        listOf(
            listOf(Token("$", TokenKind.Keyword), Token(" claude mcp add -t http pantrix-ui $address")),
            emptyList(),
            prompt,
            listOf(Token("  search_components(\"comment, several lines\")", TokenKind.Comment)),
            component,
            listOf(
                Token("  PantrixTextArea", TokenKind.StringLiteral),
                Token("(state = comment, label = …, maxLength = "),
                Token("280", TokenKind.Number),
                Token(", tagKey = "),
                Token("\"comment\"", TokenKind.StringLiteral),
                Token(")")
            )
        )
    }
}

/** "Your brand": the site's own preset, its colours and its blues, and the way to make one. */
@Composable
internal fun YourBrand(open: (Route) -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val shape = RoundedCornerShape(if (layout == SiteLayout.Phone) 20.dp else 24.dp)
    val bottom = when (layout) {
        SiteLayout.Desktop -> 96.dp
        SiteLayout.Tablet -> 64.dp
        SiteLayout.Phone -> 48.dp
    }
    LandingBand(PaddingValues(start = layout.gutter, end = layout.gutter, bottom = bottom)) {
        val card = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PantrixTheme.colors.surface)
            .border(1.dp, PantrixTheme.colors.border, shape)
        when (layout) {
            SiteLayout.Desktop -> BoxWithConstraints(card.padding(40.dp)) {
                val room = maxWidth - BRAND_BASIS - SWATCHES_BASIS - 40.dp
                val words: @Composable (Modifier) -> Unit = { modifier ->
                    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Kicker(HomeSay.BrandKicker.of(language))
                        BrandTitle(36.sp, 30.sp)
                        SectionLead(start = HomeSay.BrandLead.of(language), size = 16.sp)
                        BrandButtons(open, phone = false)
                    }
                }
                val swatches: @Composable (Modifier) -> Unit = { modifier ->
                    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Swatches(columns = 6, height = 72.dp, named = SwatchNames.Full)
                        Blues()
                    }
                }
                if (room >= 0.dp) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        words(Modifier.width(BRAND_BASIS + room / 2))
                        swatches(Modifier.width(SWATCHES_BASIS + room / 2))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
                        words(Modifier.fillMaxWidth())
                        swatches(Modifier.fillMaxWidth())
                    }
                }
            }

            SiteLayout.Tablet -> Column(card.padding(32.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Kicker(HomeSay.BrandKicker.of(language))
                    BrandTitle(30.sp, 26.sp)
                    SectionLead(start = HomeSay.BrandLeadShort.of(language), size = 15.5.sp)
                }
                Swatches(columns = 6, height = 64.dp, named = SwatchNames.Short)
                BrandButtons(open, phone = false)
            }

            SiteLayout.Phone -> Column(
                card.padding(horizontal = 18.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Kicker(HomeSay.BrandKicker.of(language))
                PantrixText(
                    text = HomeSay.BrandTitleStart.of(language) + DESIGN_PRESET + HomeSay.BrandTitleEnd.of(language),
                    style = display(24.sp, line = 1.2f, spacing = -0.02f),
                    color = PantrixTheme.colors.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )
                Swatches(columns = 3, height = 48.dp, named = SwatchNames.Short)
                BrandButtons(open, phone = true)
            }
        }
    }
}

/** "A brand's look in one DesignPreset.", the type's name in the code's type. */
@Composable
private fun BrandTitle(
    size: TextUnit,
    codeSize: TextUnit
) {
    val language = LocalLanguage.current
    PantrixText(
        text = buildAnnotatedString {
            append(HomeSay.BrandTitleStart.of(language))
            withStyle(
                SpanStyle(
                    fontFamily = SiteTheme.type.inlineCode.fontFamily,
                    fontSize = codeSize,
                    letterSpacing = (codeSize.value * CODE_SPACING).sp
                )
            ) { append(DESIGN_PRESET) }
            append(HomeSay.BrandTitleEnd.of(language))
        },
        style = display(size, line = 1.1f, spacing = -0.03f),
        color = PantrixTheme.colors.textPrimary,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun BrandButtons(
    open: (Route) -> Unit,
    phone: Boolean
) {
    val language = LocalLanguage.current
    if (phone) {
        LandingButton(
            text = HomeSay.OpenThemeBuilder.of(language),
            onClick = { open(Route.Theme) },
            tag = "homeThemeBuilder",
            kind = LandingButtonKind.Main,
            fill = true
        )
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LandingButton(
                text = HomeSay.OpenThemeBuilder.of(language),
                onClick = { open(Route.Theme) },
                tag = "homeThemeBuilder",
                kind = LandingButtonKind.Main,
                height = 44.dp,
                padding = 18.dp,
                textSize = 14.sp
            )
            LandingButton(
                text = HomeSay.SeeTheTokens.of(language),
                onClick = { open(Route.Foundations) },
                tag = "homeTokens",
                kind = LandingButtonKind.Outline,
                height = 44.dp,
                padding = 18.dp,
                textSize = 14.sp
            )
        }
    }
}

/** How a swatch is named: its role in full with its colour's code, or its last word alone. */
private enum class SwatchNames { Full, Short }

/** Six of the preset's colours, [columns] to a row, each over its name. */
@Composable
private fun Swatches(
    columns: Int,
    height: Dp,
    named: SwatchNames
) {
    // The preset's own light colours, whichever theme the page is in: they are the brand's values.
    val colors = PantrixPreset.colors(dark = false)
    val phone = SiteTheme.layout == SiteLayout.Phone
    Column(verticalArrangement = Arrangement.spacedBy(if (phone) 8.dp else 10.dp)) {
        SWATCHES.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(if (phone) 8.dp else 10.dp)) {
                row.forEach { (role, color) ->
                    val fill = color(colors)
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(if (phone) 4.dp else 6.dp)
                    ) {
                        val shape = RoundedCornerShape(if (phone) 10.dp else 12.dp)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(height)
                                .background(fill, shape)
                                .then(
                                    if (fill.luminance() > LIGHT_SWATCH) {
                                        Modifier.border(1.dp, SiteTheme.colors.hairline, shape)
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                        PantrixText(
                            // A long role breaks after its dot where its swatch is too narrow for it.
                            text = if (named == SwatchNames.Full) {
                                role.replace(".", ".$BREAK")
                            } else {
                                role.substringAfterLast('.')
                            },
                            style = SiteTheme.type.tag.copy(fontSize = if (phone) 11.5.sp else 12.sp),
                            color = PantrixTheme.colors.textPrimary
                        )
                        if (named == SwatchNames.Full) {
                            PantrixText(
                                text = fill.hex(),
                                style = SiteTheme.type.inlineCode.copy(fontSize = 11.sp),
                                color = SiteTheme.colors.muted
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The brand's blues from 50 to 700 in one band, and what it is. */
@Composable
private fun Blues() {
    val language = LocalLanguage.current
    Box(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(Brush.horizontalGradient(colorStops = BLUES), RoundedCornerShape(percent = 50))
    )
    PantrixText(
        text = HomeSay.Palette.of(language),
        style = SiteTheme.type.bodySmall.copy(fontSize = 12.sp),
        color = SiteTheme.colors.muted
    )
}

/** The call at the page's end: one line in Gradle, on the brand's deepest blues. */
@Composable
internal fun StartHere(open: (Route) -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val bottom = when (layout) {
        SiteLayout.Desktop -> 96.dp
        SiteLayout.Tablet -> 64.dp
        SiteLayout.Phone -> 48.dp
    }
    val shape = RoundedCornerShape(if (layout == SiteLayout.Phone) 24.dp else 28.dp)
    val ground = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(Brush.linearGradient(colorStops = CALL_BLUES, start = Offset.Zero, end = Offset.Infinite))
    val title: @Composable () -> Unit = {
        SectionHeading(HomeSay.CallTitle.of(language), color = Color.White)
    }
    val lead: @Composable (Modifier) -> Unit = { modifier ->
        PantrixText(
            text = (if (layout == SiteLayout.Phone) HomeSay.CallLeadShort else HomeSay.CallLead).of(language),
            style = SiteTheme.type.body.copy(fontSize = leadSize(), lineHeight = (leadSize().value * CALL_LINE).sp),
            color = CALL_TEXT,
            modifier = modifier
        )
    }
    val getStarted: @Composable (Boolean) -> Unit = { fill ->
        LandingButton(
            text = HomeSay.GetStarted.of(language),
            onClick = { open(Route.Doc("installation")) },
            tag = "homeCallGetStarted",
            colors = ButtonColors(container = Color.White, content = CALL_INK),
            weight = FontWeight.ExtraBold,
            height = 50.dp,
            padding = 24.dp,
            fill = fill
        )
    }
    val component: @Composable () -> Unit = {
        LandingButton(
            text = HomeSay.SeeComponentPage.of(language),
            onClick = { open(Route.Component("text-area")) },
            tag = "homeComponentPage",
            kind = LandingButtonKind.Outline,
            colors = ButtonColors(
                container = Color.Transparent,
                content = Color.White,
                border = Color.White.copy(alpha = ON_DARK_LINE)
            ),
            height = 50.dp,
            padding = 24.dp
        )
    }
    LandingBand(PaddingValues(start = layout.gutter, end = layout.gutter, bottom = bottom)) {
        when (layout) {
            SiteLayout.Desktop -> Row(
                ground.padding(horizontal = 48.dp, vertical = 64.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    title()
                    lead(Modifier.widthIn(max = 520.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    getStarted(false)
                    component()
                }
            }

            SiteLayout.Tablet -> Column(
                ground.padding(horizontal = 36.dp, vertical = 48.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                title()
                lead(Modifier)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    getStarted(false)
                    component()
                }
            }

            SiteLayout.Phone -> Column(
                ground.padding(horizontal = 20.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                title()
                lead(Modifier)
                getStarted(true)
            }
        }
    }
}

/**
 * The page's foot: Pantrix UI and what it is, the site's pages by kind, and the licence and languages under them.
 *
 * @param open what a link to a page of the site does.
 * @param onLink what a link outside the site does, given its address.
 */
@Composable
internal fun HomeFoot(
    open: (Route) -> Unit,
    onLink: (String) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val columns = footColumns(language, tablet = layout == SiteLayout.Tablet)
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 32.dp)
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 28.dp)
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 24.dp)
    }
    val go: (FootLink) -> Unit = { link -> link.route?.let(open) ?: onLink(LibraryFacts.REPOSITORY) }
    FootBand {
        Column(
            Modifier.padding(padding).widthIn(max = LANDING_WIDTH).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(
                when (layout) {
                    SiteLayout.Desktop -> 40.dp
                    SiteLayout.Tablet -> 28.dp
                    SiteLayout.Phone -> 24.dp
                }
            )
        ) {
            when (layout) {
                SiteLayout.Desktop -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                        FootBrand(Modifier.weight(2f))
                        columns.forEach { column -> FootColumn(column, go, Modifier.weight(1f)) }
                    }
                }

                SiteLayout.Tablet -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        FootBrand(Modifier.weight(1f))
                        columns.forEach { column -> FootColumn(column, go, Modifier.weight(1f)) }
                    }
                }

                SiteLayout.Phone -> {
                    FootBrand(Modifier, tagline = false)
                    Column {
                        phoneLinks(language).chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                pair.forEach { link ->
                                    FootLinkText(link, go, Modifier.weight(1f).heightIn(min = 40.dp))
                                }
                                // A last link alone keeps the column it would share with another.
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            val line = SiteTheme.colors.hairline
            FootLine(
                Modifier
                    .fillMaxWidth()
                    .drawBehind { drawLine(line, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
                    .padding(
                        top = when (layout) {
                            SiteLayout.Desktop -> 24.dp
                            SiteLayout.Tablet -> 20.dp
                            SiteLayout.Phone -> 16.dp
                        }
                    )
            )
        }
    }
}

/** The P, the name and, on a desktop and a tablet, what Pantrix UI is. */
@Composable
private fun FootBrand(
    modifier: Modifier,
    tagline: Boolean = true
) {
    val language = LocalLanguage.current
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (desktop) 12.dp else 10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (desktop) 10.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(if (desktop) 28.dp else 26.dp)
                    .background(LOGO_BLUE, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                PantrixText(
                    text = "P",
                    style = display(if (desktop) 15.sp else 14.sp, line = 1.2f, spacing = 0f),
                    color = Color.White
                )
            }
            PantrixText(
                text = "Pantrix UI",
                style = display(if (desktop) 16.sp else 15.sp, line = 1.3f, spacing = 0f),
                color = PantrixTheme.colors.textPrimary
            )
        }
        if (tagline) {
            PantrixText(
                text = HomeSay.Tagline.of(language),
                style = SiteTheme.type.body.copy(fontSize = if (desktop) 14.sp else 13.sp),
                color = SiteTheme.colors.muted,
                modifier = Modifier.widthIn(max = 300.dp)
            )
        }
    }
}

/** A column of the foot: its title and its links. */
@Composable
private fun FootColumn(
    column: FootColumnOf,
    go: (FootLink) -> Unit,
    modifier: Modifier
) {
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (desktop) 10.dp else 8.dp)) {
        PantrixText(
            text = column.title,
            style = display(13.sp, line = 1.5f, spacing = 0f),
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
        column.links.forEach { link -> FootLinkText(link, go, Modifier) }
    }
}

@Composable
private fun FootLinkText(
    link: FootLink,
    go: (FootLink) -> Unit,
    modifier: Modifier
) {
    Box(modifier, contentAlignment = Alignment.CenterStart) {
        PantrixText(
            text = link.label,
            style = SiteTheme.type.body.copy(fontSize = 14.sp),
            color = PantrixTheme.colors.textSecondary,
            modifier = Modifier.siteClickable(
                tag = "foot_${link.tag}",
                role = Role.Button,
                shape = RoundedCornerShape(4.dp),
                onClick = { go(link) }
            )
        )
    }
}

/**
 * A link of the foot.
 *
 * @property label what it says.
 * @property tag its automation key.
 * @property route the page it opens, or null for the library's repository.
 */
private data class FootLink(
    val label: String,
    val tag: String,
    val route: Route?
)

/** A column of the foot: what its links are about, and them. */
private data class FootColumnOf(
    val title: String,
    val links: List<FootLink>
)

/** The foot's three columns of links, in [language]: a desktop's four in each, a tablet's three. */
private fun footColumns(
    language: Language,
    tablet: Boolean
): List<FootColumnOf> {
    fun link(
        words: Words,
        tag: String,
        route: Route?
    ) = FootLink(words.of(language), tag, route)
    val docs = listOf(
        link(FootSay.GettingStarted, "introduction", Route.Doc("introduction")),
        link(FootSay.Installation, "installation", Route.Doc("installation")),
        link(FootSay.Theming, "theming", Route.Doc("theming")),
        link(if (tablet) FootSay.FittingTheWindow else FootSay.AdaptiveLayout, "adaptive", Route.Guide("adaptive"))
    )
    val groups = FOOT_GROUPS.map { key ->
        FootLink(ComponentGroups.title(key).of(language), "group_$key", Route.Components)
    }
    val resources = listOfNotNull(
        link(FootSay.Changelog, "changelog", Route.Changelog),
        link(FootSay.McpServer, "mcp", Route.Ai),
        link(FootSay.Roadmap, "roadmap", Route.Roadmap),
        link(FootSay.GitHub, "github", null).takeIf { LibraryFacts.REPOSITORY_IS_OPEN }
    )
    return listOf(
        FootColumnOf(FootSay.Docs.of(language), if (tablet) docs.drop(1) else docs),
        FootColumnOf(FootSay.Components.of(language), if (tablet) groups.take(TABLET_LINKS) else groups),
        FootColumnOf(
            FootSay.Resources.of(language),
            if (tablet) resources.filter { it.tag != "roadmap" } else resources
        )
    )
}

/** A phone's links, two to a row: six, the last GitHub, while the repository is open. */
private fun phoneLinks(language: Language): List<FootLink> = listOfNotNull(
    FootLink(FootSay.Installation.of(language), "installation", Route.Doc("installation")),
    FootLink(FootSay.Components.of(language), "components", Route.Components),
    FootLink(FootSay.Theming.of(language), "theming", Route.Doc("theming")),
    FootLink(FootSay.Changelog.of(language), "changelog", Route.Changelog),
    FootLink(FootSay.McpServer.of(language), "mcp", Route.Ai),
    FootLink(FootSay.GitHub.of(language), "github", null).takeIf { LibraryFacts.REPOSITORY_IS_OPEN }
)

/** The foot's words. */
private object FootSay {
    val Docs = Words("Docs", "Belgeler")
    val Components = Words("Components", "Bileşenler")
    val Resources = Words("Resources", "Kaynaklar")
    val GettingStarted = Words("Getting started", "Başlarken")
    val Installation = Words("Installation", "Kurulum")
    val Theming = Words("Theming", "Tema")
    val FittingTheWindow = Words("Fitting the window", "Pencereye uymak")
    val AdaptiveLayout = Words("Adaptive layout", "Uyarlanabilir düzen")
    val Changelog = Words("Changelog", "Değişiklikler")
    val McpServer = Words("MCP server", "MCP sunucusu")
    val Roadmap = Words("Roadmap", "Yol haritası")
    const val GITHUB = "GitHub"
    val GitHub = Words(GITHUB, GITHUB)
}

/** The colour's code as the design writes it: `#2264FF`. */
private fun Color.hex(): String = "#" + (toArgb() and RGB).toString(HEX).padStart(HEX_DIGITS, '0').uppercase()

/** Where a line may break inside a word, unseen. */
private const val BREAK = "\u200B"

/** The preset's six colours the brand shows, by their roles. */
private val SWATCHES: List<Pair<String, (PantrixColors) -> Color>> = listOf(
    "primary" to { it.primary.color },
    "primary.container" to { it.primary.container },
    "textPrimary" to { it.textPrimary },
    "success" to { it.success.color },
    "warning" to { it.warning.color },
    "danger" to { it.danger.color }
)

/** The preset's blues from 50 to 700, where each stands along the band. */
private val BLUES = arrayOf(
    0f to Color(0xFFF4F8FF),
    0.22f to Color(0xFFCFDFFF),
    0.40f to Color(0xFF8BADFF),
    0.56f to Color(0xFF5386FF),
    0.70f to Color(0xFF2264FF),
    0.84f to Color(0xFF1A52D6),
    1f to Color(0xFF1442B8)
)

/** The call's blues, from its top corner to its bottom one. */
private val CALL_BLUES = arrayOf(
    0f to Color(0xFF2659D2),
    0.38f to Color(0xFF264795),
    0.68f to CALL_INK,
    1f to Color(0xFF181D2B)
)

/** How close the letters of the brand's title's code are, in ems. */
private const val CODE_SPACING = -0.02f

/** The call's sentence's line, as many times its size. */
private const val CALL_LINE = 1.65f

/** The call's sentence, a pale blue on its blues. */
private val CALL_TEXT = Color(0xFFCFDFFF)

/** The P of the logo's square. */
private val LOGO_BLUE = Color(0xFF2264FF)

/** The groups the foot's column of components names, in its order. */
private val FOOT_GROUPS = listOf("inputs", "data", "chart", "overlay")

/** How many of a column's links a tablet's foot shows. */
private const val TABLET_LINKS = 3

/** Where a swatch is light enough to need a hairline around it on a white card. */
private const val LIGHT_SWATCH = 0.85f

private const val RGB = 0xFFFFFF
private const val HEX = 16
private const val HEX_DIGITS = 6

/** The text of the assistants' sentence, and the type the brand's title names. */
private const val LLMS = "llms.txt"
private const val DESIGN_PRESET = "DesignPreset"

/** The widths the assistants' words and terminal grow from, and the brand's words and swatches. */
private val AI_BASIS = 380.dp
private val TERMINAL_BASIS = 560.dp
private val BRAND_BASIS = 360.dp
private val SWATCHES_BASIS = 480.dp

/** How strongly the line of a button on the dark call shows. */
private const val ON_DARK_LINE = 0.35f
