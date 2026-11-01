package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ComponentGroups
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.DemoPages
import com.pantrixui.site.core.model.FoundationSection
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.SearchEntry
import com.pantrixui.site.core.model.SearchIndex
import com.pantrixui.site.core.model.SearchKind
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Words
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * What the site's search finds, in each language: the components from their list, the library's API from
 * `pantrix-ui.json`'s declarations, each led to the page that shows it, the parts of the site's pages and of the
 * components' pages, and the guides.
 *
 * @param json the text of `pantrix-ui.json`.
 * @param pages the site's own pages, in every language.
 * @param indexes the components' lists, by language.
 * @param components the components' pages, by language.
 */
internal class SearchIndexer(
    json: String,
    private val pages: List<Page>,
    private val indexes: Map<Language, ComponentIndex>,
    private val components: Map<Language, List<ComponentPage>>
) {
    /** A declaration of the library, as the search lists it. */
    private class Declaration(
        val title: String,
        val kind: Words,
        val packageName: String,
        val path: String,
        val anchor: String?
    )

    private val declarations: List<Declaration>

    init {
        val library = Json.parseToJsonElement(json).jsonObject
        val listed = library.getValue("components").jsonArray.map { it.jsonObject }
        // A component's own function leads to its page; another declaration to the first page of a component the
        // demo documents it on.
        val byFunction = listed.associate { it.string("function") to ComponentReader.keyOf(it.string("name")) }
        val byDemoPage = listed.groupBy {
            it.string("page")
        }.mapValues { (_, all) -> ComponentReader.keyOf(all.first().string("name")) }
        declarations = library.getValue("declarations").jsonArray.map { it.jsonObject }.mapNotNull { declaration ->
            val name = declaration.string("name")
            val demoPages = declaration["pages"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty()
            val component = byFunction[name] ?: demoPages.firstNotNullOfOrNull { byDemoPage[it] }
            val (path, anchor) = when {
                component != null -> "/components/$component" to API
                else -> demoPages.firstNotNullOfOrNull { DemoPages.placeOf(it) } ?: return@mapNotNull null
            }
            Declaration(
                title = titleOf(name, declaration["signature"]?.jsonPrimitive?.content.orEmpty()),
                kind = kindOf(
                    declaration.string("kind"),
                    declaration["composable"]?.jsonPrimitive?.booleanOrNull == true
                ),
                packageName = declaration.string("package"),
                path = path,
                anchor = anchor
            )
        }.distinctBy { it.title to it.path }
    }

    /** What the search finds in [language]. */
    fun index(language: Language): SearchIndex = SearchIndex(
        language,
        componentsIn(language) + apiIn(language) + sectionsIn(language) + guidesIn(language)
    )

    private fun componentsIn(language: Language): List<SearchEntry> = indexes.getValue(
        language
    ).components.map { card ->
        SearchEntry(
            kind = SearchKind.Component,
            title = card.name,
            sub = "${COMPONENT.of(language)} · ${ComponentGroups.title(card.group).of(language)}",
            path = "/components/${card.key}",
            mark = card.name.take(1)
        )
    }

    private fun apiIn(language: Language): List<SearchEntry> = declarations.map {
        SearchEntry(
            kind = SearchKind.Api,
            title = it.title,
            sub = "${it.kind.of(language)} · ${it.packageName}",
            path = it.path,
            anchor = it.anchor,
            mark = "{}"
        )
    }

    private fun guidesIn(language: Language): List<SearchEntry> = pages
        .filter { it.language == language && it.path.startsWith(GUIDES_PATH) && it.path != GUIDES_PATH.trimEnd('/') }
        .sortedBy { it.title }
        .map { SearchEntry(SearchKind.Guide, it.title, GUIDE.of(language), it.path, mark = "§") }

    /**
     * The site's pages themselves, and the parts of the documentation's pages, of the foundations and of the
     * components' pages.
     */
    private fun sectionsIn(language: Language): List<SearchEntry> {
        val sitePages = SITE_PAGES.map { (path, words) ->
            SearchEntry(SearchKind.Section, words.first.of(language), words.second.of(language), path, mark = "#")
        }
        val documentPages = pages
            .filter { it.language == language && it.path.startsWith("/docs/") && !it.path.startsWith(GUIDES_PATH) }
            .map { SearchEntry(SearchKind.Section, it.title, firstSentenceOf(it.description), it.path, mark = "#") }
        val documents = pages.filter { it.language == language && it.path.startsWith("/docs/") }.flatMap { page ->
            page.contents.map { entry ->
                SearchEntry(
                    kind = SearchKind.Section,
                    title = "${page.title} › ${entry.title}",
                    sub = firstSentenceUnder(page, entry.anchor),
                    path = page.path,
                    anchor = entry.anchor,
                    mark = "#"
                )
            }
        }
        val foundations = FoundationSection.entries.map { section ->
            SearchEntry(
                kind = SearchKind.Section,
                title = "${FOUNDATIONS.of(language)} › ${section.title.of(language)}",
                sub = FOUNDATION_SUBS.getValue(section).of(language),
                path = "/foundations",
                anchor = section.anchor,
                mark = "#"
            )
        }
        return sitePages + documentPages + documents + foundations +
            components.getValue(language).flatMap { componentSections(it, language) }
    }

    private fun componentSections(
        page: ComponentPage,
        language: Language
    ): List<SearchEntry> {
        val function = page.card.function
        val type = (page.style ?: page.colors)?.name
        val tag = page.tags.firstOrNull()?.prefix
        val parts = buildList {
            if (page.forms.isNotEmpty()) {
                add(
                    Part(
                        API,
                        API_TITLE,
                        Words("$function: its forms and parameters", "$function: biçimleri ve parametreleri")
                    )
                )
            }
            if (type != null) {
                add(
                    Part(
                        STYLES,
                        STYLES_TITLE,
                        Words("$type: each platform's preset", "$type: her platformun hazır ayarı")
                    )
                )
            }
            if (page.accessibility.isNotEmpty()) add(Part(ACCESSIBILITY, ACCESSIBILITY_TITLE, ACCESSIBILITY_SUB))
            if (tag != null) {
                add(
                    Part(
                        TESTING,
                        TESTING_TITLE,
                        Words(
                            "${tag}_<tagKey>, the tags a test finds it by",
                            "${tag}_<tagKey>, testin onu bulduğu etiketler"
                        )
                    )
                )
            }
        }
        return parts.map { part ->
            SearchEntry(
                kind = SearchKind.Section,
                title = "${page.card.name} › ${part.title.of(language)}",
                sub = part.sub.of(language),
                path = "/components/${page.card.key}",
                anchor = part.anchor,
                mark = "#"
            )
        }
    }

    /** A part of a component's page: its anchor, its title and what a result says of it. */
    private class Part(
        val anchor: String,
        val title: Words,
        val sub: Words
    )

    companion object {
        /**
         * The first sentence of what [page] says right under its part [anchor], a heading's or a step's, cut short
         * where it is long; the page's own first sentence where the part opens with no words of its own.
         */
        fun firstSentenceUnder(
            page: Page,
            anchor: String
        ): String {
            val step = page.blocks.filterIsInstance<Block.Steps>().flatMap { it.steps }.firstOrNull {
                it.anchor ==
                    anchor
            }
            val heading = page.blocks.indexOfFirst { it is Block.Heading && it.anchor == anchor }
            val after = page.blocks.drop(heading + 1).takeWhile { it !is Block.Heading }
            val under = step?.blocks ?: after.takeIf { heading >= 0 }
            return firstSentenceOf(under.orEmpty().firstNotNullOfOrNull { it.words() } ?: page.description)
        }

        /** The first sentence of [text], cut short where it is long. */
        fun firstSentenceOf(text: String): String {
            val whole = text.trim()
            val first = whole.substringBefore(". ")
            val sentence = if (first.length < whole.length && !first.endsWith(".")) "$first." else first
            if (sentence.length <= SUB_LENGTH) return sentence
            return sentence.take(SUB_LENGTH).substringBeforeLast(' ') + "…"
        }

        /** The words a block opens with, where it opens with words: a paragraph, a note, a row, a step's first. */
        private fun Block.words(): String? = when (this) {
            is Block.Paragraph -> text.plain()
            is Block.Note -> text.plain()
            is Block.Callout -> blocks.firstNotNullOfOrNull { it.words() }
            is Block.Rows -> rows.firstOrNull()?.text?.plain()
            is Block.Steps -> steps.firstOrNull()?.blocks?.firstNotNullOfOrNull { it.words() }
            else -> null
        }?.takeIf { it.isNotBlank() }

        /** A declaration's name as code calls it: `Modifier.clearFocusOnTap` for an extension, else its name. */
        fun titleOf(
            name: String,
            signature: String
        ): String {
            val receiver = Regex(
                """(?:fun|val|var)\s+(?:<[^>]*>\s+)?([A-Z][A-Za-z0-9_]*)(?:<[^>]*>)?\.${Regex.escape(name)}\b"""
            )
                .find(signature)?.groupValues?.get(1)
            return if (receiver != null) "$receiver.$name" else name
        }

        private fun kindOf(
            kind: String,
            composable: Boolean
        ): Words = when {
            composable -> Words("Composable", "Composable")
            kind == "function" -> Words("Function", "Fonksiyon")
            kind == "class" -> Words("Class", "Sınıf")
            kind == "enum" -> Words("Enum", "Enum")
            kind == "interface" -> Words("Interface", "Arayüz")
            kind == "object" -> Words("Object", "Nesne")
            kind == "property" -> Words("Property", "Özellik")
            else -> Words("Annotation", "İşaret")
        }

        private fun List<Span>.plain(): String = joinToString("") { span ->
            when (span) {
                is Span.Text -> span.text
                is Span.Code -> span.text
                is Span.Link -> span.text.plain()
            }
        }

        private fun JsonObject.string(name: String): String = getValue(name).jsonPrimitive.content

        /** The site's own pages, which no Markdown writes: what each is called, and what it is. */
        private val SITE_PAGES: List<Pair<String, Pair<Words, Words>>> = listOf(
            "/components" to (
                Words("Components", "Bileşenler") to
                    Words("Every component of the library, by group", "Kütüphanenin bütün bileşenleri, gruplarıyla")
            ),
            "/foundations" to (
                Words("Foundations", "Temeller") to
                    Words(
                        "Colour, type, spacing, shape, elevation, motion and density",
                        "Renk, yazı, boşluk, şekil, yükselti, hareket ve yoğunluk"
                    )
            ),
            "/theme" to (
                Words("Theme builder", "Tema oluşturucu") to
                    Words("Your brand's preset, written as Kotlin", "Markanın hazır ayarı, Kotlin olarak yazılır")
            ),
            "/adaptive" to (
                Words("Adaptive UI", "Uyarlanabilir arayüz") to
                    Words(
                        "Windows, panes and folds, in a simulator",
                        "Pencereler, bölmeler ve katlanmalar, bir simülatörde"
                    )
            ),
            "/ai" to (
                Words("For coding assistants", "Kodlama asistanları için") to
                    Words("The MCP server, the skill, llms.txt and the JSON", "MCP sunucusu, beceri, llms.txt ve JSON")
            ),
            "/roadmap" to (
                Words("Roadmap", "Yol haritası") to
                    Words("What is built, and what comes next", "Neyin yapıldığı, sırada ne olduğu")
            ),
            "/download" to (
                Words("Download", "İndir") to
                    Words("The site as a desktop app for macOS", "Site, macOS için bir masaüstü uygulaması olarak")
            ),
            "/changelog" to (
                Words("Changelog", "Değişiklik günlüğü") to
                    Words("What changed, version by version", "Sürüm sürüm ne değiştiği")
            ),
            "/docs/guides" to
                (Words("Guides", "Rehberler") to Words("Every guide, by topic", "Bütün rehberler, konularıyla"))
        )

        private val FOUNDATION_SUBS = mapOf(
            FoundationSection.Colour to Words("Roles, contrast and the palette", "Roller, kontrast ve palet"),
            FoundationSection.Type to Words("The type scale, role by role", "Rol rol yazı ölçeği"),
            FoundationSection.Spacing to Words("The gaps, from a hair to a section", "Boşluklar, kıl payından bölüme"),
            FoundationSection.Shape to Words("Corner radii, from small to full", "Köşe yarıçapları, küçükten tama"),
            FoundationSection.Elevation to
                Words("Shadows and the surfaces they lift", "Gölgeler ve kaldırdıkları yüzeyler"),
            FoundationSection.Motion to Words("Durations and easings", "Süreler ve yumuşatmalar"),
            FoundationSection.Density to Words("Compact, comfortable and spacious", "Sıkı, rahat ve geniş")
        )

        private val COMPONENT = Words("Component", "Bileşen")
        private val GUIDE = Words("Guide", "Rehber")
        private val FOUNDATIONS = Words("Foundations", "Temeller")
        private val API_TITLE = Words("API", "API")
        private val STYLES_TITLE = Words("Styles and colours", "Stiller ve renkler")
        private val ACCESSIBILITY_TITLE = Words("Accessibility", "Erişilebilirlik")
        private val TESTING_TITLE = Words("Testing", "Test")
        private val ACCESSIBILITY_SUB = Words("Keyboard, focus and screen readers", "Klavye, odak ve ekran okuyucular")

        /** The anchors of a component page's parts, as its screen marks them. */
        private const val API = "api"
        private const val STYLES = "styles"
        private const val ACCESSIBILITY = "accessibility"
        private const val TESTING = "testing"

        private const val GUIDES_PATH = "/docs/guides/"

        /** The longest a result's second line is before it is cut. */
        private const val SUB_LENGTH = 96
    }
}
