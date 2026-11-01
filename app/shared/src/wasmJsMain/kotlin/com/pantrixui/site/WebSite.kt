package com.pantrixui.site

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.pantrixui.foundation.token.typography.PantrixTypefaceGate
import com.pantrixui.foundation.token.typography.areTypefacesLoaded
import com.pantrixui.site.core.designsystem.SiteTypefaces
import com.pantrixui.site.core.navigation.browserNavigationHistory
import com.pantrixui.site.core.routing.SiteRoutes
import kotlinx.browser.window

/**
 * The site in a browser: the page and the language its address names, `/tr/docs/installation` the installation in
 * Turkish, the browser's history kept in step with the site's, and the first frame held back until both typefaces
 * have been read, so the site is never drawn in a fallback typeface that changes a moment later. The web's entry
 * point starts the site's dependencies first, with [startSiteKoin].
 *
 * @param onShown what is done once the first frame is drawn: the page's loading mark is taken away.
 */
@Composable
fun WebSite(onShown: () -> Unit) {
    val start = remember { SiteRoutes.locationOf(window.location.pathname) }
    val history = remember { browserNavigationHistory { address -> SiteRoutes.locationOf(address).route } }
    val loaded = areTypefacesLoaded(SiteTypefaces.Sans, SiteTypefaces.Mono)
    PantrixTypefaceGate(loaded = loaded, onShown = onShown) {
        SiteApp(history = history, startLanguage = start.language)
    }
}
