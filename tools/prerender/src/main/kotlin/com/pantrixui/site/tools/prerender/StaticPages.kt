package com.pantrixui.site.tools.prerender

import com.pantrixui.site.core.i18n.LandingHeads
import com.pantrixui.site.core.i18n.SiteStrings
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.ComponentGroups
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.ContentFiles
import com.pantrixui.site.core.model.GuideCatalogue
import com.pantrixui.site.core.model.GuideTopic
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.ShareImage
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.routing.SiteRoutes
import kotlinx.serialization.KSerializer
import java.io.File

/**
 * A page of the site as the build writes it in HTML: where it is, its head, and its words.
 *
 * @property route the page.
 * @property language the language it is written in.
 * @property title what the page is called, as its window names it before the site's name.
 * @property description what it is about, in a sentence or two.
 * @property kicker what part of the site it is in, which its share image writes over its title: `Components · Data`.
 * @property body its words, in HTML, from its first heading to its end.
 */
internal data class StaticPage(
    val route: Route,
    val language: Language,
    val title: String,
    val description: String,
    val kicker: String,
    val body: String
) {
    /** Where the page is: `/docs/installation`, `/tr/docs/installation`. */
    val address: String get() = SiteRoutes.addressOf(route, language)

    /** The image a shared link of the page shows. */
    val shareImage: ShareImage
        get() = ShareImage(ShareImage.fileOf(SiteRoutes.pathOf(route), language), title, kicker, language)
}

/**
 * Every page of the site in [language], from the content the content tool wrote under [root]: the documentation and
 * the guides, the components and their index, the changelog, and the landing pages, whose words are the app's.
 *
 * @param landing the landing pages' words in HTML, by route, where they have been read from the app; a landing page
 *   without them has its title and its description alone.
 */
internal class StaticPages(
    private val root: File,
    private val language: Language,
    private val landing: Map<Route, String> = emptyMap()
) {
    private val html = Html(language)
    private val strings = SiteStrings.of(language)
    private val components = ComponentHtml(language)

    /** The pages, the landing page first. */
    fun all(): List<StaticPage> = landingPages() + docs() + components() + listOf(changelog())

    private fun landingPages(): List<StaticPage> =
        listOf(Route.Home, Route.Foundations, Route.Theme, Route.Adaptive, Route.Ai, Route.Roadmap).map { route ->
            val head = checkNotNull(LandingHeads.of(route))
            val words = landing[route] ?: (
                html.heading(Html.TITLE, Html.text(head.title.of(language))) +
                    "\n<p>${Html.text(head.description.of(language))}</p>"
            )
            StaticPage(route, language, head.title.of(language), head.description.of(language), kickerOf(route), words)
        }

    private fun docs(): List<StaticPage> {
        val folder = File(root, ContentFiles.pageFile(language, "docs")).parentFile.resolve("docs")
        return folder.walkTopDown().filter { it.isFile && it.extension == "json" }.sortedBy { it.path }.map { file ->
            val page = read(file, Page.serializer())
            val route = SiteRoutes.routeOf(page.path)
            val body = if (route == Route.Guides) guidesBody(page) else articleBody(page)
            val kicker = when (route) {
                is Route.Guide -> strings.guides
                else -> page.section ?: strings.docs
            }
            StaticPage(route, language, page.title, page.description, kicker, body)
        }.toList()
    }

    private fun articleBody(page: Page): String =
        html.heading(Html.TITLE, Html.text(page.title)) + "\n<p>${Html.text(page.description)}</p>\n" +
            html.blocks(page.blocks)

    /** The guides by topic, as the guides page lists them: a guide's page is the place each one is read. */
    private fun guidesBody(page: Page): String {
        val topics = GuideTopic.entries.joinToString("\n") { topic ->
            val guides = GuideCatalogue.entries.filter { it.topic == topic }.map { guide ->
                html.link("/docs/guides/${guide.key}", Html.text(guide.title.of(language))) + ": " +
                    Html.text(guide.summary.of(language))
            }
            html.heading(Html.SECTION, Html.text(topic.title.of(language))) +
                "\n<p>${Html.text(topic.note.of(language))}</p>\n" +
                html.list(ordered = false, guides)
        }
        return articleBody(page) + "\n" + topics
    }

    private fun components(): List<StaticPage> {
        val index = read(File(root, ContentFiles.componentIndexFile(language)), ComponentIndex.serializer())
        val head = LandingHeads.Components
        val groups = index.groups.joinToString("\n") { group ->
            html.heading(Html.SECTION, Html.text(ComponentGroups.title(group.key).of(language))) + "\n" +
                html.list(
                    ordered = false,
                    group.components.map { card ->
                        html.link("/components/${card.key}", Html.text(card.name)) + ": " + html.spans(card.summary)
                    }
                )
        }
        val grid = StaticPage(
            route = Route.Components,
            language = language,
            title = head.title.of(language),
            description = head.description.of(language),
            kicker = strings.componentCount(LibraryFacts.COMPONENTS),
            body = html.heading(Html.TITLE, Html.text(head.title.of(language))) +
                "\n<p>${Html.text(head.description.of(language))}</p>\n" + groups
        )
        return listOf(grid) + index.components.map { card ->
            val page = read(File(root, ContentFiles.componentFile(language, card.key)), ComponentPage.serializer())
            val kicker = "${strings.components} · ${ComponentGroups.title(card.group).of(language)}"
            StaticPage(
                Route.Component(card.key),
                language,
                card.name,
                plain(card.summary),
                kicker,
                components.body(page)
            )
        }
    }

    private fun changelog(): StaticPage {
        val changelog = read(File(root, ContentFiles.changelogFile(language)), Changelog.serializer())
        val words = StaticWords.of(language)
        val body = buildString {
            append(html.heading(Html.TITLE, Html.text(changelog.title)))
            append("\n<p>${html.spans(changelog.description)}</p>")
            changelog.releases.forEach { release ->
                val name = release.version?.let { version -> version + release.date?.let { " · $it" }.orEmpty() }
                    ?: words.unreleased
                append("\n" + html.heading(Html.SECTION, Html.text(name)))
                append("\n" + html.blocks(release.lead + release.intro))
                if (release.targets.isNotEmpty()) {
                    append("\n" + html.list(ordered = false, release.targets.map { html.spans(it) }))
                }
                release.sections.forEach { section ->
                    append("\n" + html.heading(Html.PART, Html.text(section.title)))
                    section.groups.forEach { group ->
                        append("\n" + html.heading(Html.DETAIL, Html.text(group.module)))
                        append(
                            "\n" + html.list(
                                ordered = false,
                                group.entries.map { entry ->
                                    entry.label?.let { "<strong>${Html.text(it)}:</strong> " }.orEmpty() +
                                        html.blocks(entry.blocks)
                                }
                            )
                        )
                    }
                }
            }
        }
        val description = plain(changelog.description)
        return StaticPage(Route.Changelog, language, changelog.title, description, strings.project, body)
    }

    /** What part of the site a landing page is in: the site itself, for the landing page. */
    private fun kickerOf(route: Route): String = when (route) {
        Route.Home -> "${strings.componentCount(LibraryFacts.COMPONENTS)} · Compose Multiplatform"
        Route.Foundations -> strings.foundations
        Route.Theme -> strings.theme
        Route.Adaptive -> strings.adaptive
        Route.Ai -> strings.forAi
        else -> strings.roadmap
    }

    private fun <T> read(
        file: File,
        serializer: KSerializer<T>
    ): T = ContentFiles.json.decodeFromString(serializer, file.readText())

    companion object {
        /** [spans] as the words they say, without their marks: what a page's description is written from. */
        fun plain(spans: List<Span>): String = spans.joinToString("") { span ->
            when (span) {
                is Span.Text -> span.text
                is Span.Code -> span.text
                is Span.Link -> plain(span.text)
            }
        }
    }
}
