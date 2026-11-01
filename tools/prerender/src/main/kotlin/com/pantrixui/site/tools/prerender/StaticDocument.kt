package com.pantrixui.site.tools.prerender

import com.pantrixui.site.core.i18n.SiteStrings
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.ShareImage
import com.pantrixui.site.core.routing.SiteRoutes

/**
 * A page's HTML file: the web app's own `index.html`, with the page's head (its title, its description, its address
 * and its other language's, and what a shared link shows) and its words in a `#static` box before the app's. The
 * app's `index.html` hides the box where the browser runs the app, so a reader sees the app; a browser without Wasm
 * and a search engine read the box.
 *
 * @param template the app's `index.html`, as the web build writes it.
 * @param site the site's address, with no slash at its end: `https://pantrixui.com`.
 */
internal class StaticDocument(
    private val template: String,
    private val site: String
) {
    init {
        listOf(LANG, TITLE, DESCRIPTION, HEAD_END, APP).forEach { marker ->
            require(marker.containsMatchIn(template)) { "The app's index.html has no ${marker.pattern}" }
        }
    }

    /** The file of [page], whose [other] is the same page in the other language. */
    fun of(
        page: StaticPage,
        other: StaticPage
    ): String = written(
        language = page.language,
        title = "${page.title} · $SITE_NAME",
        description = page.description,
        head = alternates(page, other) + card(page) +
            "\n    <link rel=\"canonical\" href=\"${Html.text(site + page.address)}\">",
        body = page.body,
        other = other
    )

    /** The file a server gives for an address with no page: `404.html`, which search engines leave out. */
    fun notFound(language: Language): String {
        val strings = SiteStrings.of(language)
        val html = Html(language)
        return written(
            language = language,
            title = "${strings.pageNotHere} · $SITE_NAME",
            description = strings.pageNotHereText,
            head = "\n    <meta name=\"robots\" content=\"noindex\">",
            body = html.heading(1, Html.text(strings.pageNotHere)) + "\n<p>${Html.text(strings.pageNotHereText)}</p>",
            other = null
        )
    }

    private fun written(
        language: Language,
        title: String,
        description: String,
        head: String,
        body: String,
        other: StaticPage?
    ): String = template
        .replaceFirst(LANG, "<html lang=\"${language.code}\">")
        .replaceFirst(TITLE, Regex.escapeReplacement("<title>${Html.text(title)}</title>"))
        .replaceFirst(
            DESCRIPTION,
            Regex.escapeReplacement("<meta name=\"description\" content=\"${Html.text(description)}\">")
        )
        .replaceFirst(HEAD_END, Regex.escapeReplacement("$head\n</head>"))
        .replace(APP) { found -> frame(language, body, other) + found.value }

    /** The page's address in each language, and the one a reader whose language the site has not is given. */
    private fun alternates(
        page: StaticPage,
        other: StaticPage
    ): String {
        val english = if (page.language == Language.English) page else other
        return listOf(page, other).sortedBy { it.language }.joinToString("") {
            "\n    <link rel=\"alternate\" hreflang=\"${it.language.code}\" href=\"${Html.text(site + it.address)}\">"
        } + "\n    <link rel=\"alternate\" hreflang=\"x-default\" href=\"${Html.text(site + english.address)}\">"
    }

    /** What a shared link of the page shows: its title, its description, its address and its image. */
    private fun card(page: StaticPage): String {
        val tags = listOf(
            "og:type" to "website",
            "og:site_name" to SITE_NAME,
            "og:title" to page.title,
            "og:description" to page.description,
            "og:url" to site + page.address,
            "og:locale" to if (page.language == Language.English) "en_US" else "tr_TR"
        )
        val image = site + "/" + page.shareImage.file
        val picture = listOf(
            "og:image" to image,
            "og:image:width" to "${ShareImage.WIDTH}",
            "og:image:height" to "${ShareImage.HEIGHT}",
            "og:image:alt" to page.title
        )
        return (tags + picture).joinToString("") { (name, value) ->
            "\n    <meta property=\"$name\" content=\"${Html.text(value)}\">"
        } + "\n    <meta name=\"twitter:card\" content=\"summary_large_image\">" +
            "\n    <meta name=\"twitter:image\" content=\"${Html.text(image)}\">"
    }

    /** The page's words with the site's links around them: its sections, the other language, and its foot. */
    private fun frame(
        language: Language,
        body: String,
        other: StaticPage?
    ): String {
        val strings = SiteStrings.of(language)
        val html = Html(language)
        val sections = listOf(
            strings.docs to Route.Doc("introduction"),
            strings.components to Route.Components,
            strings.foundations to Route.Foundations,
            strings.adaptive to Route.Adaptive,
            strings.theme to Route.Theme,
            strings.forAi to Route.Ai,
            strings.roadmap to Route.Roadmap,
            strings.changelog to Route.Changelog
        ).joinToString(" ") { (words, route) -> html.link(SiteRoutes.pathOf(route), Html.text(words)) }
        val switch = other?.let {
            " <a href=\"${Html.text(it.address)}\" hreflang=\"${it.language.code}\">" +
                "${Html.text(StaticWords.of(language).otherLanguage)}</a>"
        }.orEmpty()
        val repository = if (LibraryFacts.REPOSITORY_IS_OPEN) {
            " · <a href=\"${LibraryFacts.REPOSITORY}\">GitHub</a>"
        } else {
            ""
        }
        return """
    <div id="static">
        <header><a href="${Html.text(SiteRoutes.addressOf(Route.Home, language))}"><strong>$SITE_NAME</strong></a>
            <nav>$sections$switch</nav></header>
        <main>
$body
        </main>
        <footer><p>$SITE_NAME$repository</p></footer>
    </div>"""
    }

    companion object {
        /** The site's name, after each page's in its window's title. */
        const val SITE_NAME = "Pantrix UI"

        private val LANG = Regex("""<html lang="[a-z]+">""")
        private val TITLE = Regex("""<title>[^<]*</title>""")
        private val DESCRIPTION = Regex("""<meta name="description" content="[^"]*">""")
        private val HEAD_END = Regex("""\s*</head>""")

        /** The app's box, with the comment over it where it has one: the page's words go before both. */
        private val APP = Regex("""\n\s*(<!--[^>]*-->\s*)?<div id="app"></div>""")
    }
}
