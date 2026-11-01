package com.pantrixui.site.core.designsystem.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
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
import com.pantrixui.components.elements.iconbutton.PantrixIconButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.overlay.menu.PantrixMenu
import com.pantrixui.components.overlay.menu.PantrixMenuItem
import com.pantrixui.components.overlay.popover.PopoverAlignment
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.i18n.ownName
import com.pantrixui.site.core.model.Language

/**
 * One of the header's links: a part of the site.
 *
 * @property key what [SiteHeader]'s `onNavigate` is told, the same in every language.
 * @property label what it says.
 * @property current whether the page on show is in it.
 */
data class HeaderLink(
    val key: String,
    val label: String,
    val current: Boolean = false
)

/**
 * The site's header, across the window: the brand, the parts of the site, the search, the language, the site's source
 * on GitHub and the theme. On a desktop wide enough for all of them the parts are links in it; on a narrower one, a
 * tablet and a phone they are in the menu its first button opens, which takes the links' place. It is laid out alike on
 * every page, from the window's one edge to the other, so that nothing in it moves as the reader goes from one part of
 * the site to another.
 *
 * @param links the parts of the site, in order.
 * @param onNavigate what choosing a part does, given its key; the brand is the key "home".
 * @param onSearch what the search box does: open the search.
 * @param onLanguage what choosing a language does.
 * @param addressIn the page's address in a language: the language menu writes it under the language's name.
 * @param dark whether the theme is dark: the theme button offers the other.
 * @param onToggleTheme what the theme button does.
 * @param onOpenMenu what the menu button does, where the links have no room.
 * @param onOpenRepository what the GitHub button does: open the site's repository.
 * @param modifier the header's modifier.
 */
@Composable
fun SiteHeader(
    links: List<HeaderLink>,
    onNavigate: (String) -> Unit,
    onSearch: () -> Unit,
    onLanguage: (Language) -> Unit,
    addressIn: (Language) -> String,
    dark: Boolean,
    onToggleTheme: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenRepository: () -> Unit,
    modifier: Modifier = Modifier
) {
    val layout = SiteTheme.layout
    val strings = LocalSiteStrings.current
    Column(modifier.fillMaxWidth().background(PantrixTheme.colors.background)) {
        val gap = when (layout) {
            SiteLayout.Desktop -> 20.dp
            SiteLayout.Tablet -> 12.dp
            SiteLayout.Phone -> 4.dp
        }
        // A desktop's tools stand close together, so that the room they leave goes to the links.
        val toolGap = if (layout == SiteLayout.Desktop) DESKTOP_TOOL_GAP else gap
        HeaderRow(
            gap = gap,
            toolGap = toolGap,
            modifier = Modifier
                .height(if (layout == SiteLayout.Phone) 56.dp else 64.dp)
                .padding(layout.headerRoom)
                .fillMaxWidth()
        ) {
            Box(Modifier.layoutId(HeaderPart.Menu)) {
                HeaderIconButton(
                    icon = PantrixIcons.Menu,
                    description = strings.openTheMenu,
                    onClick = onOpenMenu,
                    tint = PantrixTheme.colors.textPrimary,
                    iconSize = 22.dp,
                    tag = "openMenu"
                )
            }
            Box(Modifier.layoutId(HeaderPart.Brand)) { Brand(onClick = { onNavigate("home") }) }
            if (layout == SiteLayout.Desktop) {
                // The links stand side by side: the room inside each keeps their words apart.
                Row(Modifier.layoutId(HeaderPart.Links)) {
                    links.forEachIndexed { index, link ->
                        HeaderLinkButton(link, narrow = index >= WIDE_LINKS, onNavigate)
                    }
                }
            }
            Row(
                Modifier.layoutId(HeaderPart.Tools),
                horizontalArrangement = Arrangement.spacedBy(toolGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (layout) {
                    SiteLayout.Phone -> HeaderIconButton(
                        icon = PantrixIcons.Search,
                        description = strings.searchTheDocs,
                        onClick = onSearch,
                        tint = PantrixTheme.colors.textPrimary,
                        iconSize = 20.dp,
                        tag = "search"
                    )

                    else -> SearchBox(onSearch, Modifier.width(if (layout == SiteLayout.Tablet) 260.dp else 200.dp))
                }
                LanguageButton(onLanguage, addressIn)
            }
            // The two buttons with an icon alone stand together at the row's end, in one colour and size.
            val tint = if (layout == SiteLayout.Phone) {
                PantrixTheme.colors.textPrimary
            } else {
                PantrixTheme.colors.textSecondary
            }
            val iconSize = if (layout == SiteLayout.Desktop) 18.dp else 20.dp
            Box(Modifier.layoutId(HeaderPart.Repository)) {
                HeaderIconButton(
                    icon = SiteIcons.GitHub,
                    description = strings.theSiteOnGitHub,
                    onClick = onOpenRepository,
                    tint = tint,
                    iconSize = iconSize,
                    tag = "repository"
                )
            }
            Box(Modifier.layoutId(HeaderPart.Theme)) {
                HeaderIconButton(
                    icon = if (dark) SiteIcons.Sun else SiteIcons.Moon,
                    description = if (dark) strings.switchToLight else strings.switchToDark,
                    onClick = onToggleTheme,
                    tint = tint,
                    iconSize = iconSize,
                    tag = "theme"
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(SiteTheme.colors.hairline))
    }
}

/** The parts of the header's row, which [HeaderRow] lays out by what they are. */
private enum class HeaderPart { Menu, Brand, Links, Tools, Repository, Theme }

/**
 * The header's row: the brand, then the links where they are given and there is room for them beside the brand and
 * the tools; where there is not, the menu button before the brand in their place. The tools stand at the row's end,
 * the theme's button last and GitHub's before it where there is room for it too: a phone narrower than the design's
 * has none, and its menu leads to GitHub instead. Each part is measured as wide as it is, so a language whose names
 * are longer, or a larger type, takes the links and GitHub's button away sooner. What is not placed, the menu button,
 * the links or GitHub's button, is out of the reach of a screen reader and of the keyboard as well as out of sight:
 * Compose leaves a part that is not placed out of both.
 *
 * @param gap the room between the parts: the brand, the links and the tools.
 * @param toolGap the room between the tools, GitHub's button and the theme's.
 * @param modifier the row's modifier, which gives its height.
 * @param content the parts, each with its [HeaderPart] as its layout id.
 */
@Composable
private fun HeaderRow(
    gap: Dp,
    toolGap: Dp,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Layout(content, modifier) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)

        fun part(part: HeaderPart) = measurables.firstOrNull { it.layoutId == part }
        val room = gap.roundToPx()
        val brand = part(HeaderPart.Brand)?.measure(loose)
        val end = HeaderEnd(
            tools = part(HeaderPart.Tools)?.measure(loose),
            repository = part(HeaderPart.Repository)?.measure(loose),
            theme = part(HeaderPart.Theme)?.measure(loose),
            room = toolGap.roundToPx()
        )
        // The links as wide as they are, which no window's width limits: what decides whether they have room.
        val links = part(HeaderPart.Links)?.measure(loose.copy(maxWidth = Constraints.Infinity))
        val fits = links != null &&
            (brand?.width ?: 0) + room + links.width + room + end.widthWithRepository <= constraints.maxWidth
        val menu = if (fits) null else part(HeaderPart.Menu)?.measure(loose)
        val start = (menu?.let { it.width + minOf(room, MENU_GAP.roundToPx()) } ?: 0) + (brand?.width ?: 0)
        val repositoryFits = start + room + end.widthWithRepository <= constraints.maxWidth
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Centred as a row centres what it holds, its half pixel rounded the same way.
            fun Placeable.placeAt(x: Int) =
                placeRelative(x, Alignment.CenterVertically.align(height, constraints.maxHeight))
            var x = 0
            menu?.let {
                it.placeAt(x)
                x += it.width + minOf(room, MENU_GAP.roundToPx())
            }
            brand?.let {
                it.placeAt(x)
                x += it.width + room
            }
            if (fits) links.placeAt(x)
            end.at(constraints.maxWidth, repositoryFits).forEach { (part, at) -> part.placeAt(at) }
        }
    }
}

/**
 * What stands at the header's end, measured: the [tools], GitHub's button, the [repository], and the theme's button,
 * the [theme], last, [room] apart.
 */
private class HeaderEnd(
    val tools: Placeable?,
    val repository: Placeable?,
    val theme: Placeable?,
    val room: Int
) {
    /** How wide the end is with GitHub's button: what decides whether it has room. */
    val widthWithRepository: Int =
        listOfNotNull(tools, repository, theme).let { parts -> parts.sumOf { it.width } + room * (parts.size - 1) }

    /** Each part with where it starts, the row [right] wide, GitHub's button among them where it [fits]. */
    fun at(
        right: Int,
        fits: Boolean
    ): List<Pair<Placeable, Int>> {
        var edge = right
        return listOfNotNull(theme, repository.takeIf { fits }, tools).map { part ->
            edge -= part.width
            (part to edge).also { edge -= room }
        }
    }
}

/** The site's mark and name, which lead home. */
@Composable
private fun Brand(onClick: () -> Unit) {
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    Row(
        Modifier
            .siteClickable("home", Role.Button, RoundedCornerShape(8.dp), onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (phone) 8.dp else 10.dp)
    ) {
        Box(
            Modifier
                .size(if (phone) 26.dp else 30.dp)
                .background(BrandBlue, RoundedCornerShape(if (phone) 8.dp else 9.dp)),
            contentAlignment = Alignment.Center
        ) {
            PantrixText(
                text = "P",
                color = Color.White,
                style = SiteTheme.type.nav.copy(
                    fontSize = if (phone) 14.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
        PantrixText(
            text = "Pantrix UI",
            color = PantrixTheme.colors.textPrimary,
            style = SiteTheme.type.nav.copy(
                fontSize = if (phone) 16.sp else 17.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.34).sp
            )
        )
    }
}

@Composable
private fun HeaderLinkButton(
    link: HeaderLink,
    narrow: Boolean,
    onNavigate: (String) -> Unit
) {
    val primary = PantrixTheme.colors.primary
    PantrixButton(
        onClick = { onNavigate(link.key) },
        contentDescription = link.label,
        variant = ButtonVariant.Text,
        size = ButtonSize.Medium,
        style = ButtonStyle(medium = ButtonSizeStyle(height = 39.dp, horizontalPadding = if (narrow) 10.dp else 12.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = if (link.current) {
            ButtonColors(container = primary.container, content = primary.onContainer)
        } else {
            ButtonColors(container = Color.Transparent, content = PantrixTheme.colors.textSecondary)
        },
        tagKey = "nav_${link.key}"
    ) {
        // The label in the current link's weight, unseen, holds every link as wide as it is when it is the current one:
        // marking another part of the site moves none of the links after it.
        Box(contentAlignment = Alignment.Center) {
            PantrixText(
                text = link.label,
                modifier = Modifier.alpha(0f).clearAndSetSemantics {},
                style = SiteTheme.type.nav.copy(fontWeight = FontWeight.Bold)
            )
            PantrixText(
                text = link.label,
                style = SiteTheme.type.nav.copy(fontWeight = if (link.current) FontWeight.Bold else FontWeight.SemiBold)
            )
        }
    }
}

/**
 * One of the header's buttons that show an icon alone: 38 square with a desktop's pointer, 44 under a finger.
 *
 * @param icon what it shows.
 * @param description what a screen reader says of it.
 * @param onClick what pressing it does.
 * @param tint the icon's colour.
 * @param iconSize how large the icon is.
 * @param tag its automation key.
 */
@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color,
    iconSize: Dp,
    tag: String
) {
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    PantrixIconButton(
        imageVector = icon,
        contentDescription = description,
        onClick = onClick,
        size = ButtonSize.Medium,
        shape = RoundedCornerShape(if (desktop) 10.dp else 12.dp),
        colors = ButtonColors(container = Color.Transparent, content = tint),
        style = ButtonStyle(medium = ButtonSizeStyle(height = if (desktop) 38.dp else 44.dp, iconSize = iconSize)),
        tagKey = tag
    )
}

/**
 * The box that opens the search, with the shortcut that opens it from anywhere: in the header, and on the page for
 * an address with no page.
 *
 * @param onSearch what pressing it does: open the search.
 * @param modifier the box's modifier: its width.
 * @param tall whether it is as tall as a finger needs, 44 or more, rather than the desktop header's 38.
 * @param height how tall it is when [tall].
 * @param tag its automation tag.
 */
@Composable
fun SearchBox(
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    tall: Boolean = SiteTheme.layout != SiteLayout.Desktop,
    height: Dp = 44.dp,
    tag: String = "search"
) {
    val strings = LocalSiteStrings.current
    val shape = RoundedCornerShape(if (tall) 12.dp else 10.dp)
    val interactions = remember { MutableInteractionSource() }
    val hovered by interactions.collectIsHoveredAsState()
    Row(
        modifier
            .height(if (tall) height else 38.dp)
            .siteClickable(tag, Role.Button, shape, interactions, onClick = onSearch)
            .background(SiteTheme.colors.surfaceAlt, shape)
            .border(1.dp, if (hovered) PantrixTheme.colors.borderHover else PantrixTheme.colors.border, shape)
            .padding(start = if (tall) 14.dp else 12.dp, end = if (tall) 10.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PantrixIcon(
            imageVector = PantrixIcons.Search,
            contentDescription = null,
            tint = SiteTheme.colors.muted,
            size = if (tall) 17.dp else 16.dp
        )
        PantrixText(
            text = strings.searchTheDocs,
            color = SiteTheme.colors.muted,
            style = SiteTheme.type.nav.copy(fontWeight = FontWeight.Medium, fontSize = if (tall) 15.sp else 14.sp),
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        PantrixText(
            text = "\u2318K",
            color = PantrixTheme.colors.textSecondary,
            style = SiteTheme.type.button.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .background(PantrixTheme.colors.surface, RoundedCornerShape(6.dp))
                .border(1.dp, PantrixTheme.colors.border, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        )
    }
}

/**
 * The language button and the menu it opens: each language by its own name, with the page's address in it under the
 * name, the one on show ticked.
 */
@Composable
private fun LanguageButton(
    onLanguage: (Language) -> Unit,
    addressIn: (Language) -> String
) {
    val strings = LocalSiteStrings.current
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    var open by remember { mutableStateOf(false) }
    val code = language.code.uppercase()
    PantrixMenu(
        visible = open,
        onDismissRequest = { open = false },
        alignment = PopoverAlignment.End,
        tagKey = "language",
        anchor = {
            val desktop = layout == SiteLayout.Desktop
            PantrixButton(
                onClick = { open = !open },
                contentDescription = strings.languageButton(code, language.ownName),
                variant = ButtonVariant.Text,
                size = ButtonSize.Medium,
                style = ButtonStyle(
                    medium = ButtonSizeStyle(height = if (desktop) 38.dp else 44.dp, horizontalPadding = 0.dp)
                ),
                shape = RoundedCornerShape(if (desktop) 10.dp else 12.dp),
                colors = ButtonColors(
                    container = Color.Transparent,
                    content = if (layout ==
                        SiteLayout.Phone
                    ) {
                        PantrixTheme.colors.textPrimary
                    } else {
                        PantrixTheme.colors.textSecondary
                    }
                ),
                tagKey = "language"
            ) {
                Row(
                    Modifier.padding(start = 10.dp, end = if (layout == SiteLayout.Phone) 10.dp else 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PantrixIcon(
                        imageVector = SiteIcons.Globe,
                        contentDescription = null,
                        size = if (desktop) 17.dp else 19.dp
                    )
                    PantrixText(
                        text = code,
                        style = SiteTheme.type.nav.copy(
                            fontSize = if (desktop) 13.sp else 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (if (desktop) 0.26 else 0.28).sp
                        )
                    )
                    if (layout != SiteLayout.Phone) {
                        PantrixIcon(imageVector = PantrixIcons.ChevronDown, contentDescription = null, size = 14.dp)
                    }
                }
            }
        }
    ) {
        Language.entries.forEach { choice ->
            PantrixMenuItem(
                text = choice.ownName,
                onClick = { onLanguage(choice) },
                selected = choice == language,
                tagKey = "language_${choice.code}",
                supportingText = addressIn(choice),
                // The design writes the address as code, smaller than the menu's footnote.
                supportingTextStyle = SiteTheme.type.inlineCode.copy(fontSize = 11.5.sp)
            )
        }
        PantrixText(
            text = strings.eachPageInBothLanguages,
            color = SiteTheme.colors.muted,
            style = SiteTheme.type.factLabel.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).width(220.dp)
        )
    }
}

/** The room at the header's two ends in [this] layout. */
private val SiteLayout.headerRoom: PaddingValues
    get() = when (this) {
        SiteLayout.Desktop -> PaddingValues(horizontal = 24.dp)
        SiteLayout.Tablet -> PaddingValues(start = 12.dp, end = 16.dp)
        SiteLayout.Phone -> PaddingValues(horizontal = 6.dp)
    }

/** The room between a desktop's tools, GitHub's button and the theme's. */
private val DESKTOP_TOOL_GAP = 4.dp

/** The room after the menu button: a tablet's, on a desktop too, and a phone's own closer one on a phone. */
private val MENU_GAP = 12.dp

/** How many of the header's first links have the wider room on each side; the later ones are set closer. */
private const val WIDE_LINKS = 3

/** The brand's own blue: the mark is the same in both themes. */
private val BrandBlue = Color(0xFF2264FF)
