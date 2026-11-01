package com.pantrixui.site.core.routing

import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route

/**
 * The one table of the site's addresses: what a route's path is, and what route a path is. The app's navigation,
 * the browser's history, the static pages and the sitemap all read it, so that an address cannot mean one page in
 * one place and another elsewhere.
 */
object SiteRoutes {
    /** The path of [route] in no language: `/docs/installation`. */
    fun pathOf(route: Route): String = when (route) {
        Route.Home -> "/"
        is Route.Doc -> "/docs/${route.slug}"
        Route.Guides -> "/docs/guides"
        is Route.Guide -> "/docs/guides/${route.key}"
        Route.Components -> "/components"
        is Route.Component -> "/components/${route.key}"
        Route.Foundations -> "/foundations"
        Route.Adaptive -> "/adaptive"
        Route.Theme -> "/theme"
        Route.Ai -> "/ai"
        Route.Ask -> "/ai/ask"
        Route.Roadmap -> "/roadmap"
        Route.Download -> "/download"
        Route.Changelog -> "/changelog"
        Route.References -> "/references"
        is Route.NotFound -> route.path
    }

    /** The route [path] names, in no language; [Route.NotFound] when the site has no page there. */
    fun routeOf(path: String): Route {
        val parts = path.substringBefore('?').substringBefore('#').split('/').filter { it.isNotEmpty() }
        val guide = parts.keyUnder("docs", "guides")
        val doc = parts.keyUnder("docs")
        val component = parts.keyUnder("components")
        return when {
            parts.isEmpty() -> Route.Home
            parts == listOf("docs", "guides") -> Route.Guides
            guide != null -> Route.Guide(guide)
            doc != null -> Route.Doc(doc)
            parts == listOf("components") -> Route.Components
            parts == listOf("ai", "ask") -> Route.Ask
            component != null -> Route.Component(component)
            parts.size == 1 -> SINGLE[parts[0]] ?: Route.NotFound(path)
            else -> Route.NotFound(path)
        }
    }

    /** The address of [route] in [language]: English has no prefix, `/tr/docs/installation` is Turkish. */
    fun addressOf(
        route: Route,
        language: Language
    ): String {
        val path = pathOf(route)
        return when {
            language == Language.English -> path
            path == "/" -> "/${language.code}"
            else -> "/${language.code}$path"
        }
    }

    /** What [address] asks for: its language, from its first segment, and its route. */
    fun locationOf(address: String): SiteLocation {
        val path = address.substringBefore('?').substringBefore('#')
        val first = path.split('/').firstOrNull { it.isNotEmpty() }
        val language = Language.entries.firstOrNull { it != Language.English && it.code == first }
        return if (language == null) {
            SiteLocation(Language.English, routeOf(path))
        } else {
            SiteLocation(language, routeOf(path.removePrefix("/${language.code}").ifEmpty { "/" }))
        }
    }

    private val SINGLE: Map<String, Route> = mapOf(
        "foundations" to Route.Foundations,
        "adaptive" to Route.Adaptive,
        "theme" to Route.Theme,
        "ai" to Route.Ai,
        "roadmap" to Route.Roadmap,
        "download" to Route.Download,
        "changelog" to Route.Changelog,
        "references" to Route.References
    )

    /** The key of a path that is [prefix] and one key more, `docs/installation` under `docs`; null for any other. */
    private fun List<String>.keyUnder(vararg prefix: String): String? =
        takeIf { size == prefix.size + 1 && subList(0, prefix.size) == prefix.asList() }?.last()?.takeIf { it.isKey() }

    /** A key the site writes: small letters, digits and hyphens, as `text-area` and `keyboard-dismissal` are. */
    private fun String.isKey(): Boolean = isNotEmpty() && all { it in 'a'..'z' || it in '0'..'9' || it == '-' }
}

/**
 * What an address asks for.
 *
 * @property language the language its pages are in.
 * @property route the page.
 */
data class SiteLocation(
    val language: Language,
    val route: Route
)
