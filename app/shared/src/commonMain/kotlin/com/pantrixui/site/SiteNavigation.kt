package com.pantrixui.site

import com.pantrixui.site.core.designsystem.chrome.HeaderLink
import com.pantrixui.site.core.designsystem.chrome.NavItem
import com.pantrixui.site.core.designsystem.chrome.NavSection
import com.pantrixui.site.core.i18n.SiteStrings
import com.pantrixui.site.core.model.ComponentGroups
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.FoundationSection
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.ThemingLink
import com.pantrixui.site.core.routing.SiteRoutes

/**
 * The parts of the site its header links to, in the header's order, each with the page it opens.
 *
 * @property key its key, the same in every language: the header's automation tag and what it tells the site.
 * @property route the page it opens.
 */
internal enum class SitePart(
    val key: String,
    val route: Route
) {
    Docs("docs", Route.Doc("introduction")),
    Components("components", Route.Components),
    Foundations("foundations", Route.Foundations),
    Adaptive("adaptive", Route.Adaptive),
    Theme("theme", Route.Theme),
    Ai("ai", Route.Ai),
    Roadmap("roadmap", Route.Roadmap),
    Download("download", Route.Download);

    companion object {
        /** The part [key] names, or null for the brand's "home". */
        fun of(key: String): SitePart? = entries.firstOrNull { it.key == key }
    }
}

/** The part of the site [this] page is in, which the header marks; the landing page is in none. */
internal val Route.part: SitePart?
    get() = when (this) {
        Route.Home -> null
        is Route.Doc, Route.Guides, is Route.Guide, Route.Changelog, is Route.NotFound -> SitePart.Docs
        Route.Components, is Route.Component -> SitePart.Components
        Route.Foundations -> SitePart.Foundations
        Route.Adaptive -> SitePart.Adaptive
        Route.Theme -> SitePart.Theme
        Route.Ai -> SitePart.Ai
        Route.Roadmap -> SitePart.Roadmap
        Route.Download -> SitePart.Download
    }

/**
 * Whether [this] page is one the header opens, the landing page or a part's own: the pages a phone switches between in
 * place, as tabs, where a page opened from one of them comes in over it.
 */
internal val Route.isSitePart: Boolean
    get() = this == Route.Home || SitePart.entries.any { it.route == this }

/** Whether a desktop shows the components' sidebar beside [this] page: a component's page does. */
internal val Route.hasComponentsSidebar: Boolean
    get() = this is Route.Component

/**
 * The sidebar of a component's page: getting started, then the components' groups with how many each has, the group
 * of [current] opened to its components, each link the page's address in [language]. A group's own link leads to its
 * first component.
 */
internal fun componentsSections(
    strings: SiteStrings,
    language: Language,
    index: ComponentIndex?,
    current: Route.Component
): List<NavSection> {
    val started = docsSections(strings, language, current).first()
    val groups = index?.groups.orEmpty()
    val open = groups.firstOrNull { group -> group.components.any { it.key == current.key } }?.key
    return listOf(
        started,
        NavSection(
            strings.components,
            groups.flatMap { group ->
                val first = group.components.firstOrNull() ?: return@flatMap emptyList()
                listOf(
                    NavItem(
                        key = "group_${group.key}",
                        label = ComponentGroups.title(group.key).of(language),
                        href = SiteRoutes.addressOf(Route.Component(first.key), language),
                        count = group.components.size
                    )
                ) + if (group.key == open) {
                    group.components.map { card ->
                        NavItem(
                            key = "component_${card.key}",
                            label = card.name,
                            href = SiteRoutes.addressOf(Route.Component(card.key), language),
                            current = card.key == current.key,
                            nested = true
                        )
                    }
                } else {
                    emptyList()
                }
            }
        )
    )
}

/**
 * The sidebar of the foundations page: its sections, the one [section] names marked, the first where it names none,
 * then the pages about the theme, each link an address in [language].
 */
internal fun foundationsSections(
    strings: SiteStrings,
    language: Language,
    section: String?
): List<NavSection> {
    val shown = section?.takeIf { anchor -> FoundationSection.entries.any { it.anchor == anchor } }
        ?: FoundationSection.entries.first().anchor
    val page = SiteRoutes.addressOf(Route.Foundations, language)
    return listOf(
        NavSection(
            strings.foundations,
            FoundationSection.entries.map { part ->
                NavItem(
                    key = "foundation_${part.anchor}",
                    label = part.title.of(language),
                    href = "$page#${part.anchor}",
                    current = part.anchor == shown
                )
            }
        ),
        NavSection(
            strings.theming,
            ThemingLink.entries.map { link ->
                val address = SiteRoutes.addressOf(link.route, language)
                NavItem(
                    key = "theming_${link.key}",
                    label = link.title.of(language),
                    href = link.section?.let { "$address#$it" } ?: address
                )
            }
        )
    )
}

/** Whether a desktop shows the documentation's sidebar beside [this] page, as its design does. */
internal val Route.hasDocsSidebar: Boolean
    get() = this is Route.Doc || this == Route.Guides || this is Route.Guide || this == Route.Changelog ||
        this is Route.NotFound

/** The header's links, with the part [current] is in marked. */
internal fun headerLinks(
    strings: SiteStrings,
    current: Route
): List<HeaderLink> = SitePart.entries.map { part ->
    HeaderLink(key = part.key, label = part.label(strings), current = current.part == part)
}

/**
 * The documentation's sidebar: getting started, a choice of the guides and the project's pages, [current] marked,
 * each link the page's address in [language].
 */
internal fun docsSections(
    strings: SiteStrings,
    language: Language,
    current: Route
): List<NavSection> {
    fun item(
        key: String,
        label: String,
        route: Route
    ) = NavItem(key = key, label = label, href = SiteRoutes.addressOf(route, language), current = route == current)
    return listOf(
        NavSection(
            strings.gettingStarted,
            listOf(
                item("introduction", strings.introduction, Route.Doc("introduction")),
                item("installation", strings.installation, Route.Doc("installation")),
                item("theming", strings.theming, Route.Doc("theming")),
                item("platforms", strings.platforms, Route.Doc("platforms")),
                item("forms", strings.forms, Route.Doc("forms")),
                item("testing", strings.testing, Route.Doc("testing")),
                // The documentation's page on setting an assistant up; the header's "For AI" is the page about it.
                item("ai", strings.forAiAssistants, Route.Doc("ai-assistants"))
            )
        ),
        NavSection(
            strings.guides,
            listOf(item("guides", strings.allGuides(LibraryFacts.GUIDES), Route.Guides)) +
                FEATURED_GUIDES.map { (key, label) -> item("guide_$key", label(strings), Route.Guide(key)) }
        ),
        NavSection(
            strings.project,
            listOfNotNull(
                item("changelog", strings.changelog, Route.Changelog),
                item("roadmap", strings.roadmap, Route.Roadmap),
                NavItem(key = "issues", label = strings.reportAProblem, href = ISSUES).takeIf {
                    LibraryFacts.REPOSITORY_IS_OPEN
                }
            )
        )
    )
}

/**
 * The menu of a tablet and a phone: the documentation's sidebar, then the parts of the site the header of a desktop
 * links to and the sidebar does not.
 */
internal fun menuSections(
    strings: SiteStrings,
    language: Language,
    current: Route
): List<NavSection> {
    fun item(
        key: String,
        label: String,
        route: Route
    ) = NavItem(key = key, label = label, href = SiteRoutes.addressOf(route, language), current = route == current)
    return docsSections(strings, language, current) + NavSection(
        BRAND,
        listOf(
            item("components", strings.components, Route.Components),
            item("foundations", strings.foundations, Route.Foundations),
            item("adaptive", strings.adaptiveUi, Route.Adaptive),
            item("theme", strings.themeBuilder, Route.Theme),
            item("download", strings.download, Route.Download)
        )
    )
}

/** What the menu of a narrow window is called on [current]: the part of the site it is in. */
internal fun menuTitle(
    strings: SiteStrings,
    current: Route
): String = current.part?.label(strings) ?: BRAND

private fun SitePart.label(strings: SiteStrings): String = when (this) {
    SitePart.Docs -> strings.docs
    SitePart.Components -> strings.components
    SitePart.Foundations -> strings.foundations
    SitePart.Adaptive -> strings.adaptive
    SitePart.Theme -> strings.theme
    SitePart.Ai -> strings.forAi
    SitePart.Roadmap -> strings.roadmap
    SitePart.Download -> strings.download
}

/** The guides the sidebar lists by name, by key, with what it calls each. */
private val FEATURED_GUIDES: List<Pair<String, (SiteStrings) -> String>> = listOf(
    "adaptive" to { it.fittingTheWindow },
    "date-time" to { it.dateAndTime },
    "keyboard-dismissal" to { it.puttingTheKeyboardAway },
    "keyboard-shortcuts" to { it.keyboardShortcuts },
    "web-fonts" to { it.aTypefaceThatArrivesLate },
    "material3" to { it.theMaterial3Bridge },
    "diagnostics" to { it.runtimeDiagnostics },
    "testing" to { it.testingAScreen }
)

/** The library's name, the title of the menu's last group. */
private const val BRAND = "Pantrix UI"

/** Where a problem with the library is reported. */
private const val ISSUES = "${LibraryFacts.REPOSITORY}/issues"
