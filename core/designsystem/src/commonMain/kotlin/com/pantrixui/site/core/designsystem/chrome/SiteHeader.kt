package com.pantrixui.site.core.designsystem.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
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
 * The site's header, as wide as the window: the brand, the parts of the site, the search, the language and the
 * theme. On a desktop the parts are links in it; on a tablet and a phone they are in the menu its first button opens.
 *
 * @param links the parts of the site, in order.
 * @param onNavigate what choosing a part does, given its key; the brand is the key "home".
 * @param onSearch what the search box does: open the search.
 * @param onLanguage what choosing a language does.
 * @param addressIn the page's address in a language: the language menu writes it under the language's name.
 * @param dark whether the theme is dark: the theme button offers the other.
 * @param onToggleTheme what the theme button does.
 * @param onOpenMenu what the menu button of a tablet or a phone does.
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
    modifier: Modifier = Modifier
) {
    val layout = SiteTheme.layout
    val strings = LocalSiteStrings.current
    Column(modifier.fillMaxWidth().background(PantrixTheme.colors.background)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val showLinks = layout == SiteLayout.Desktop && maxWidth >= 960.dp
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(if (layout == SiteLayout.Phone) 56.dp else 64.dp)
                    .padding(layout.headerRoom),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    when (layout) {
                        SiteLayout.Desktop -> 28.dp
                        SiteLayout.Tablet -> 12.dp
                        SiteLayout.Phone -> 4.dp
                    }
                )
            ) {
                if (layout != SiteLayout.Desktop) {
                    HeaderIconButton(
                        icon = PantrixIcons.Menu,
                        description = strings.openTheMenu,
                        onClick = onOpenMenu,
                        tint = PantrixTheme.colors.textPrimary,
                        iconSize = 22.dp,
                        tag = "openMenu"
                    )
                }
                Brand(onClick = { onNavigate("home") })
                if (showLinks) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        links.forEachIndexed { index, link ->
                            HeaderLinkButton(link, narrow = index >= WIDE_LINKS, onNavigate)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
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
                HeaderIconButton(
                    icon = if (dark) SiteIcons.Sun else SiteIcons.Moon,
                    description = if (dark) strings.switchToLight else strings.switchToDark,
                    onClick = onToggleTheme,
                    tint = if (layout ==
                        SiteLayout.Phone
                    ) {
                        PantrixTheme.colors.textPrimary
                    } else {
                        PantrixTheme.colors.textSecondary
                    },
                    iconSize = if (layout == SiteLayout.Desktop) 18.dp else 20.dp,
                    tag = "theme"
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(SiteTheme.colors.hairline))
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
        PantrixText(
            text = link.label,
            style = SiteTheme.type.nav.copy(fontWeight = if (link.current) FontWeight.Bold else FontWeight.SemiBold)
        )
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
                supportingText = addressIn(choice)
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

/** How many of the header's first links have the wider room on each side; the later ones are set closer. */
private const val WIDE_LINKS = 3

/** The brand's own blue: the mark is the same in both themes. */
private val BrandBlue = Color(0xFF2264FF)
