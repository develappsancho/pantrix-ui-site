package com.pantrixui.site.feature.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.container.card.CardColors
import com.pantrixui.components.container.card.CardVariant
import com.pantrixui.components.container.card.PantrixCard
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.chrome.SearchBox
import com.pantrixui.site.core.designsystem.content.RichText
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.routing.SiteRoutes

/**
 * The page for an address the site has no page at: the address asked for, marked as one with no page, the search,
 * the pages to start from and the link that reports a broken link.
 *
 * @param path the address asked for, without the site's own.
 * @param onNavigate what opening a page of the site does, given its path.
 * @param onSearch what opening the search does.
 * @param modifier the screen's modifier.
 */
@Composable
fun NotFoundScreen(
    path: String,
    onNavigate: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalSiteStrings.current
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val uriHandler = LocalUriHandler.current
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(start = 40.dp, end = 40.dp, top = 64.dp, bottom = 96.dp)
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 72.dp)
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 48.dp)
    }
    PantrixWindowTitle(strings.pageNotHere)
    Box(
        modifier
            .fillMaxSize()
            .pantrixVerticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(padding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier.widthIn(max = if (layout == SiteLayout.Desktop) 620.dp else 560.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            AddressField(path)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PantrixText(
                    text = strings.error404.uppercase(),
                    style = SiteTheme.type.overline.copy(fontSize = 13.sp),
                    color = SiteTheme.colors.muted
                )
                PantrixText(
                    text = strings.pageNotHere,
                    style = SiteTheme.type.pageTitle.copy(
                        fontSize = when (layout) {
                            SiteLayout.Desktop -> 44.sp
                            SiteLayout.Tablet -> 40.sp
                            SiteLayout.Phone -> 32.sp
                        }
                    ),
                    color = PantrixTheme.colors.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )
                PantrixText(
                    text = strings.pageNotHereText,
                    style = SiteTheme.type.lead,
                    color = PantrixTheme.colors.textSecondary
                )
            }
            SearchBox(onSearch, Modifier.fillMaxWidth(), tall = true, height = 48.dp, tag = "notFoundSearch")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PantrixText(
                    text = strings.orStartFrom.uppercase(),
                    style = SiteTheme.type.overline.copy(fontSize = 13.sp),
                    color = SiteTheme.colors.muted
                )
                StartCards(
                    listOf(
                        StartCard(
                            strings.installation,
                            strings.startInstallation,
                            Route.Doc("installation"),
                            "installation"
                        ),
                        StartCard(
                            strings.components,
                            strings.startComponents(LibraryFacts.COMPONENTS),
                            Route.Components,
                            "components"
                        ),
                        StartCard(strings.guides, strings.startGuides, Route.Guides, "guides"),
                        StartCard(strings.changelog, strings.startChangelog, Route.Changelog, "changelog")
                    ),
                    onOpen = { onNavigate(SiteRoutes.addressOf(it, language)) }
                )
            }
            // One paragraph, so that the sentence wraps as one around its link.
            RichText(
                spans = listOf(
                    Span.Text("${strings.linkBroughtYou} "),
                    Span.Link(listOf(Span.Text(strings.tellUs)), NEW_ISSUE),
                    Span.Text(strings.withThePage)
                ),
                onLink = { uriHandler.openUri(it) },
                style = SiteTheme.type.bodySmall.copy(fontSize = 14.sp),
                color = SiteTheme.colors.muted
            )
        }
    }
}

/**
 * The address asked for, drawn as a field that holds an address with no page: a picture of the mistake, not a field
 * a reader types in, so a screen reader reads the heading under it instead.
 */
@Composable
private fun AddressField(path: String) {
    val strings = LocalSiteStrings.current
    val danger = PantrixTheme.colors.danger
    val phone = SiteTheme.layout == SiteLayout.Phone
    val mono = SiteTheme.type.code.copy(fontSize = if (phone) 13.5.sp else 15.sp, fontWeight = FontWeight.Medium)
    val shape = RoundedCornerShape(12.dp)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            PantrixText(
                text = strings.address,
                style = SiteTheme.type.breadcrumb.copy(fontWeight = FontWeight.Bold),
                color = danger.onContainer,
                modifier = Modifier.weight(1f)
            )
            PantrixText(text = "404", style = mono.copy(fontSize = 13.sp), color = SiteTheme.colors.muted)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(if (phone) 48.dp else 52.dp)
                .background(PantrixTheme.colors.surface, shape)
                .border(2.dp, danger.color, shape)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PantrixText(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = SiteTheme.colors.muted)) { append(SITE_HOST) }
                    append(path)
                },
                style = mono,
                color = PantrixTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Box(Modifier.padding(start = 1.dp).width(2.dp).height(20.dp).background(danger.color))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixIcon(
                imageVector = PantrixIcons.Error,
                contentDescription = null,
                tint = danger.onContainer,
                size = 16.dp
            )
            PantrixText(
                text = strings.noPageAtAddress,
                style = SiteTheme.type.breadcrumb,
                color = danger.onContainer
            )
        }
    }
}

/**
 * A page to start from.
 *
 * @property title its name.
 * @property text what it holds.
 * @property route where it leads.
 * @property key its automation key.
 */
private data class StartCard(
    val title: String,
    val text: String,
    val route: Route,
    val key: String
)

@Composable
private fun StartCards(
    cards: List<StartCard>,
    onOpen: (Route) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cards.chunked(2).forEach { row ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { card ->
                    PantrixCard(
                        onClick = { onOpen(card.route) },
                        variant = CardVariant.Outlined,
                        shape = RoundedCornerShape(14.dp),
                        colors = CardColors(border = PantrixTheme.colors.border),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        tagKey = "start_${card.key}"
                    ) {
                        // One child: the card's own room between its children is not put between the two lines.
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            PantrixText(
                                text = card.title,
                                style = SiteTheme.type.cardTitle,
                                color = PantrixTheme.colors.textPrimary
                            )
                            PantrixText(
                                text = card.text,
                                style = SiteTheme.type.bodySmall,
                                color = PantrixTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The site's own host, before the path in the address field. */
private const val SITE_HOST = "pantrixui.com"

/** Where a broken link is reported: a new issue of the library's repository. */
private const val NEW_ISSUE = "https://github.com/developersancho/pantrix-ui-cmp/issues/new"
