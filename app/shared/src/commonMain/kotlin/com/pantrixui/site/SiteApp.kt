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
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.pantrixui.foundation.platform.LocalPantrixWindowTitleFormat
import com.pantrixui.foundation.platform.PantrixWindowTitleFormat
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.chrome.SidebarCard
import com.pantrixui.site.core.designsystem.chrome.SiteHeader
import com.pantrixui.site.core.designsystem.chrome.SiteMenu
import com.pantrixui.site.core.designsystem.chrome.SiteSidebar
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.navigation.NavigationHistory
import com.pantrixui.site.core.navigation.Navigator
import com.pantrixui.site.core.navigation.NoNavigationHistory
import com.pantrixui.site.core.navigation.rememberNavigator
import com.pantrixui.site.core.routing.SiteRoutes
import com.pantrixui.site.feature.docs.ChangelogRoute
import com.pantrixui.site.feature.docs.DocRoute
import com.pantrixui.site.feature.docs.GuidesRoute
import com.pantrixui.site.feature.docs.NotFoundScreen
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
                    val navigator = rememberNavigator(history.startStack(listOf(START_PAGE)), Route.serializer())
                    val pathOf =
                        remember(state.language) { { route: Route -> SiteRoutes.addressOf(route, state.language) } }
                    history.Bind(navigator, pathOf)
                    SiteShell(state, systemDark, onAction, navigator)
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
    val open: (String) -> Unit = { href ->
        if (href.startsWith("/")) navigator.navigate(SiteRoutes.locationOf(href).route) else uriHandler.openUri(href)
    }
    // The search is built in a later stage: its box is drawn and opens nothing yet.
    val onSearch: () -> Unit = {}
    // On Android and iOS the site is drawn under the system's bars: its frame keeps clear of the top and the sides,
    // and a page scrolls under the bottom one, its own end clear of it.
    Column(
        Modifier
            .fillMaxSize()
            .background(PantrixTheme.colors.background)
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
    SiteMenu(
        visible = state.menuOpen && layout != SiteLayout.Desktop,
        onDismissRequest = { onAction(SiteAction.CloseMenu) },
        title = menuTitle(strings, route),
        sections = menuSections(strings, state.language, route),
        onSelect = open
    )
}

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

        is Route.NotFound -> {
            NotFoundScreen(route.path, onNavigate, onSearch)
        }

        Route.Home, Route.Components, is Route.Component, Route.Foundations,
        Route.Adaptive, Route.Theme, Route.Ai, Route.Roadmap -> {
            NotFoundScreen(SiteRoutes.pathOf(route), onNavigate, onSearch)
        }
    }
}

/** The language of the system the app runs on, where it is one the site is written in; English otherwise. */
internal fun systemLanguage(): Language =
    Language.entries.firstOrNull { it.code == Locale.current.language } ?: Language.English

/** A page's title as its window's: `Installation · Pantrix UI`. */
private val SiteTitleFormat = PantrixWindowTitleFormat { "$it · Pantrix UI" }

/** The page the site opens at where no address names one. */
private val START_PAGE: Route = Route.Doc("installation")

/** No animation from one page to the next. */
private val NO_TRANSITION = ContentTransform(EnterTransition.None, ExitTransition.None)
