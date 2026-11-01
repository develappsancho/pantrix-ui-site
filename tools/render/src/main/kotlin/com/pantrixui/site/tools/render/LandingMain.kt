package com.pantrixui.site.tools.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.pantrixui.site.SiteApp
import com.pantrixui.site.core.content.ReleaseRepository
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.navigation.NavigationHistory
import com.pantrixui.site.core.navigation.Navigator
import com.pantrixui.site.core.routing.SiteRoutes
import com.pantrixui.site.startSiteKoin
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import java.io.File
import kotlin.system.exitProcess

/**
 * Writes the landing pages' words, as the app shows them on a desktop, to `--out`: JSON of each language's code to
 * each page's path to its words in HTML ([LandingText]). The static pages take them for their bodies.
 */
fun main(args: Array<String>) {
    val options = args.toList().chunked(2).associate { it.first() to it.last() }
    val out = File(options.getValue("--out"))
    // The static pages are written from what the site has, not from what the network answers at that moment.
    startSiteKoin(
        module {
            single<ReleaseRepository> { ReleaseRepository { error("No release while the pages are written") } }
        }
    )
    val words = Language.entries.associate { language ->
        language.code to LANDING.associate { route -> SiteRoutes.pathOf(route) to wordsOf(route, language) }
    }
    out.apply { parentFile.mkdirs() }.writeText(Json.encodeToString(words))
    println("Wrote the words of ${LANDING.size} landing pages, in both languages, to $out")
    // The desktop's toolkit keeps threads of its own: the program ends here, its work done.
    exitProcess(0)
}

/** The pages whose words are the app's code, not one of the site's content files. */
internal val LANDING =
    listOf(Route.Home, Route.Foundations, Route.Theme, Route.Adaptive, Route.Ai, Route.Roadmap, Route.Download)

/** The words of [route] in [language]: the app drawn in a window of a desktop's size, one window for each page. */
@OptIn(ExperimentalTestApi::class)
private fun wordsOf(
    route: Route,
    language: Language
): String {
    var words = ""
    runDesktopComposeUiTest(WIDTH, HEIGHT) {
        // The test's clock is driven by hand: a page's motion never ends, and a clock that waited for it would not.
        mainClock.autoAdvance = false
        setContent { AppAt(route, language) }
        repeat(FRAMES) {
            Thread.sleep(READ_TIME)
            mainClock.advanceTimeBy(FRAME_TIME)
        }
        words = LandingText.of(onRoot(useUnmergedTree = true).fetchSemanticsNode())
    }
    return words
}

@Composable
private fun AppAt(
    route: Route,
    language: Language
) {
    val owner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        SiteApp(history = StartAt(route), startLanguage = language)
    }
}

/** A history that opens the app at [route] and keeps nothing more. */
private class StartAt(private val route: Route) : NavigationHistory<Route> {
    override fun startStack(default: List<Route>): List<Route> = listOf(route)

    @Composable
    override fun Bind(
        navigator: Navigator<Route>,
        pathOf: (Route) -> String
    ) = Unit
}

private const val WIDTH = 1440
private const val HEIGHT = 900

/** How many times the page is given real time to read its content, and the test's clock a step for its frames. */
private const val FRAMES = 12
private const val READ_TIME = 250L
private const val FRAME_TIME = 400L
