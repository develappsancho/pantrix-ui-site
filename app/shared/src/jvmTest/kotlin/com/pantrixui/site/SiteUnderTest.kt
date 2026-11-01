package com.pantrixui.site

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.SystemTheme
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.pantrixui.foundation.date.LocalPantrixClock
import com.pantrixui.foundation.date.PantrixClock
import com.pantrixui.site.core.content.ReleaseRepository
import com.pantrixui.site.core.model.DesktopRelease
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.navigation.NavigationHistory
import com.pantrixui.site.core.navigation.Navigator
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Starts the site's dependencies for the tests that draw it, once for the JVM: Koin keeps one graph for the whole
 * process. The desktop app's release is a fixed one, so the download page is drawn the same whatever the network
 * answers.
 */
internal fun startSiteForTests() {
    if (GlobalContext.getOrNull() == null) {
        startSiteKoin(module { single<ReleaseRepository> { ReleaseRepository { SCREEN_RELEASE } } })
    }
}

/**
 * The site opened at [route] in [language], with the system's theme set to [dark] or light, and the screen's clock on a
 * fixed day: on the desktop the system's theme is the machine's and the day is today, and a picture must change with
 * neither.
 */
@Suppress("DEPRECATION") // Compose's own local for the system's theme, the one way to set it on the desktop.
@OptIn(InternalComposeUiApi::class)
@Composable
internal fun PageAt(
    route: Route,
    language: Language,
    dark: Boolean
) {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides owner,
        LocalSystemTheme provides if (dark) SystemTheme.Dark else SystemTheme.Light,
        LocalPantrixClock provides SCREEN_CLOCK
    ) {
        SiteApp(history = remember { StartAt(route) }, startLanguage = language)
    }
}

/** The desktop app's release the download page shows: a day's version, its disk image's size and hash. */
private val SCREEN_RELEASE = DesktopRelease(
    version = "26.10.31",
    year = 2026,
    month = 10,
    day = 31,
    notes = "https://github.com/develappsancho/pantrix-ui-site/releases/tag/v26.10.31",
    macSize = 92_143_171,
    macSha256 = "0e6bb0079ca77d507192d5b8530f5897002a62ae4ab8d02dde71863f766707a5"
)

/** The day the pages are drawn on, noon in UTC: a calendar marks it as today. */
private val SCREEN_CLOCK = PantrixClock(
    clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-10-30T12:00:00Z")
    },
    utcOffset = { Duration.ZERO }
)

/** A history that opens the site at [route] and keeps nothing more. */
private class StartAt(
    private val route: Route
) : NavigationHistory<Route> {
    override fun startStack(default: List<Route>): List<Route> = listOf(route)

    @Composable
    override fun Bind(
        navigator: Navigator<Route>,
        pathOf: (Route) -> String
    ) = Unit
}
