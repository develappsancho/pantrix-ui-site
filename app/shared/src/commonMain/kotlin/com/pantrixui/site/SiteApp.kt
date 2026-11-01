package com.pantrixui.site

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.pantrixui.foundation.keyboard.PantrixShortcut
import com.pantrixui.foundation.keyboard.onShortcut
import com.pantrixui.foundation.keyboard.shortcutRoot
import com.pantrixui.foundation.platform.LocalPantrixWindowTitleFormat
import com.pantrixui.foundation.platform.PantrixWindowTitleFormat
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.chrome.LocalChooseLanguage
import com.pantrixui.site.core.designsystem.chrome.SidebarCard
import com.pantrixui.site.core.designsystem.chrome.SiteHeader
import com.pantrixui.site.core.designsystem.chrome.SiteMenu
import com.pantrixui.site.core.designsystem.chrome.SiteSidebar
import com.pantrixui.site.core.designsystem.content.LocalPageSection
import com.pantrixui.site.core.designsystem.content.PageSection
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.navigation.NavigationHistory
import com.pantrixui.site.core.navigation.Navigator
import com.pantrixui.site.core.navigation.NoNavigationHistory
import com.pantrixui.site.core.navigation.rememberNavigator
import com.pantrixui.site.core.routing.SiteRoutes
import com.pantrixui.site.feature.adaptive.AdaptiveRoute
import com.pantrixui.site.feature.ai.AiRoute
import com.pantrixui.site.feature.components.ComponentRoute
import com.pantrixui.site.feature.components.ComponentsRoute
import com.pantrixui.site.feature.docs.ChangelogRoute
import com.pantrixui.site.feature.docs.DocRoute
import com.pantrixui.site.feature.docs.GuidesRoute
import com.pantrixui.site.feature.docs.NotFoundScreen
import com.pantrixui.site.feature.foundations.FoundationsRoute
import com.pantrixui.site.feature.home.HomeRoute
import com.pantrixui.site.feature.roadmap.RoadmapRoute
import com.pantrixui.site.feature.search.SearchRoute
import com.pantrixui.site.feature.theme.ThemeRoute
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The whole site, as each platform's entry point draws it: its frame, its pages and its navigation. The entry point
 * starts its dependencies first, with [startSiteKoin].
 *
 * @param history the platform's own history of pages: the browser's on the web, none elsewhere.
 * @param startLanguage the language the site opens in: the address's on the web, the system's elsewhere.
 */
@Composable
fun SiteApp(
    history: NavigationHistory<Route> = remember { NoNavigationHistory() },
    startLanguage: Language = systemLanguage()
) {
    val viewModel = koinViewModel<SiteViewModel> { parametersOf(startLanguage) }
    BaseScreen(viewModel) { state, onAction ->
        val systemDark = isSystemInDarkTheme()
        val dark = state.dark ?: systemDark
        SiteTheme(dark = dark, language = state.language) {
            // The desktop and the web compose once before their window has a size: a phone's layout is not drawn
            // for that moment, and the start page is not decided in it.
            if (PantrixTheme.window.isSizeKnown) {
                CompositionLocalProvider(LocalPantrixWindowTitleFormat provides SiteTitleFormat) {
                    val navigator = rememberNavigator(
                        start = history.startStack(listOf(START_PAGE)),
                        serializer = Route.serializer(),
                        section = history.startSection()
                    )
                    val pathOf =
                        remember(state.language) { { route: Route -> SiteRoutes.addressOf(route, state.language) } }
                    history.Bind(navigator, pathOf)
                    val section = remember(navigator) { NavigatorSection(navigator) }
                    val chooseLanguage: (Language) -> Unit = { onAction(SiteAction.ChooseLanguage(it)) }
                    CompositionLocalProvider(
                        LocalPageSection provides section,
                        LocalChooseLanguage provides chooseLanguage
                    ) {
                        SiteShell(state, systemDark, onAction, navigator)
                    }
                }
            }
        }
    }
}

/** The header, the sidebar beside a desktop's page, the page, and the menu of a narrow window. */
@Composable
private fun SiteShell(
    state: SiteUiState,
    systemDark: Boolean,
    onAction: (SiteAction) -> Unit,
    navigator: Navigator<Route>
) {
    val strings = LocalSiteStrings.current
    val layout = SiteTheme.layout
    val uriHandler = LocalUriHandler.current
    val route = navigator.current
    // A link of the site's own opens its page, at the part its `#` names; a `#` alone moves the page on show.
    val open: (String) -> Unit = { href ->
        val section = href.substringAfter('#', "").ifEmpty { null }
        when {
            href.startsWith("#") -> section?.let(navigator::showSection)
            href.startsWith("/") -> navigator.navigate(SiteRoutes.locationOf(href.substringBefore('#')).route, section)
            else -> uriHandler.openUri(href)
        }
    }
    // The search opens from the header's box, and from ⌘K or Ctrl+K anywhere on the site.
    var searching by rememberSaveable { mutableStateOf(false) }
    val onSearch: () -> Unit = { searching = true }
    // On Android and iOS the site is drawn under the system's bars: its frame keeps clear of the top and the sides,
    // and a page scrolls under the bottom one, its own end clear of it.
    Column(
        Modifier
            .fillMaxSize()
            .background(PantrixTheme.colors.background)
            .searchShortcut(onSearch)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
    ) {
        SiteHeader(
            links = headerLinks(strings, route),
            onNavigate = { key -> navigator.navigate(SitePart.of(key)?.route ?: Route.Home) },
            onSearch = onSearch,
            onLanguage = { onAction(SiteAction.ChooseLanguage(it)) },
            addressIn = { SiteRoutes.addressOf(route, it) },
            dark = state.dark ?: systemDark,
            onToggleTheme = { onAction(SiteAction.ToggleTheme(systemDark)) },
            onOpenMenu = { onAction(SiteAction.OpenMenu) }
        )
        Row(Modifier.weight(1f)) {
            if (layout == SiteLayout.Desktop && route is Route.Component) {
                val content = koinInject<ContentRepository>()
                val index by produceState<ComponentIndex?>(null, state.language) {
                    value = runCatching { content.componentIndex(state.language) }.getOrNull()
                }
                SiteSidebar(sections = componentsSections(strings, state.language, index, route), onSelect = open)
            }
            if (layout == SiteLayout.Desktop && route == Route.Foundations) {
                SiteSidebar(sections = foundationsSections(strings, state.language, navigator.section), onSelect = open)
            }
            if (layout == SiteLayout.Desktop && route.hasDocsSidebar) {
                SiteSidebar(
                    sections = docsSections(strings, state.language, route),
                    onSelect = open,
                    footer = {
                        SidebarCard(
                            title = strings.componentCount(LibraryFacts.COMPONENTS),
                            text = strings.browseByGroup,
                            onClick = { navigator.navigate(Route.Components) },
                            tag = "componentsCard"
                        )
                    }
                )
            }
            NavDisplay(
                backStack = navigator.backStack,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onBack = { navigator.back() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                // A page of a site replaces the one before it at once, as a browser's does.
                transitionSpec = { NO_TRANSITION },
                popTransitionSpec = { NO_TRANSITION },
                predictivePopTransitionSpec = { NO_TRANSITION },
                entryProvider = { key -> NavEntry(key) { SitePage(it, open, onSearch) } }
            )
        }
    }
    // Always composed, so that the search leaves with its motion: closed, it composes nothing.
    SiteSearch(searching, state.language, onAction, open) { searching = false }
    SiteMenu(
        visible = state.menuOpen && layout != SiteLayout.Desktop,
        onDismissRequest = { onAction(SiteAction.CloseMenu) },
        title = menuTitle(strings, route),
        sections = menuSections(strings, state.language, route),
        onSelect = open
    )
}

/**
 * The site's search over the page, and what opening a result does: the site moves to the result's language, where
 * it was found in the other, and opens its page at its part.
 */
@Composable
private fun SiteSearch(
    visible: Boolean,
    siteLanguage: Language,
    onAction: (SiteAction) -> Unit,
    open: (String) -> Unit,
    onClose: () -> Unit
) {
    SearchRoute(
        visible = visible,
        onDismiss = onClose,
        onOpen = { entry, language ->
            onClose()
            if (language != siteLanguage) onAction(SiteAction.ChooseLanguage(language))
            open(entry.path + entry.anchor?.let { "#$it" }.orEmpty())
        }
    )
}

/**
 * Opens the search on ⌘K, or Ctrl+K: the site's root holds the focus while nothing else does, so that the shortcut is
 * heard from the moment the site opens.
 */
private fun Modifier.searchShortcut(onSearch: () -> Unit): Modifier =
    shortcutRoot().onShortcut(PantrixShortcut(Key.K, command = true), onTrigger = onSearch)

/** The page [route] names. Each kind of page is a feature's; the pages of later stages are not built yet. */
@Composable
private fun SitePage(
    route: Route,
    onNavigate: (String) -> Unit,
    onSearch: () -> Unit
) {
    when (route) {
        is Route.Doc, is Route.Guide -> {
            DocRoute(route, onNavigate, onSearch)
        }

        Route.Guides -> {
            GuidesRoute(onNavigate, onSearch)
        }

        Route.Changelog -> {
            ChangelogRoute(onNavigate)
        }

        Route.Components -> {
            ComponentsRoute(onNavigate)
        }

        is Route.Component -> {
            ComponentRoute(route.key, onNavigate)
        }

        is Route.NotFound -> {
            NotFoundScreen(route.path, onNavigate, onSearch)
        }

        Route.Home -> {
            HomeRoute(onNavigate)
        }

        Route.Foundations -> {
            FoundationsRoute()
        }

        Route.Theme -> {
            ThemeRoute()
        }

        Route.Adaptive -> {
            AdaptiveRoute(onNavigate)
        }

        Route.Ai -> {
            AiRoute()
        }

        Route.Roadmap -> {
            RoadmapRoute(onNavigate)
        }
    }
}

/** The language of the system the app runs on, where it is one the site is written in; English otherwise. */
internal fun systemLanguage(): Language =
    Language.entries.firstOrNull { it.code == Locale.current.language } ?: Language.English

/** A page's title as its window's: `Installation · Pantrix UI`. */
private val SiteTitleFormat = PantrixWindowTitleFormat { "$it · Pantrix UI" }

/** The page the site opens at where no address names one. */
private val START_PAGE: Route = Route.Home

/** No animation from one page to the next. */
private val NO_TRANSITION = ContentTransform(EnterTransition.None, ExitTransition.None)

/** The part of the page on show, as the site's [Navigator] keeps it and the browser's address shows it. */
private class NavigatorSection(private val navigator: Navigator<Route>) : PageSection {
    override val current: String?
        get() = navigator.section

    override fun show(anchor: String) {
        navigator.showSection(anchor)
    }
}
