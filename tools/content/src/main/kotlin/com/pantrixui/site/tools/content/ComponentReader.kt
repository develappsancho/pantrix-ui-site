package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.ComponentCard
import com.pantrixui.site.core.model.ComponentExample
import com.pantrixui.site.core.model.ComponentForm
import com.pantrixui.site.core.model.ComponentGroup
import com.pantrixui.site.core.model.ComponentGroups
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.ComponentLink
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.DemoPages
import com.pantrixui.site.core.model.Language
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * The library's components as the site shows them, in both languages, from its documents: the list of
 * `pantrix-ui.json`, and each component's page from the page of the demo that documents it, `pages/<key>.md` and
 * `tr/pages/<key>.md`, with the changelog's lines about it.
 *
 * A component's page is its own even where it shares the demo's page with another: its examples are those of the
 * page whose code calls it, and its API is its own function's forms. A component the site writes examples of its own
 * for shows those in place of the demo's.
 *
 * @param json the text of `pantrix-ui.json`.
 * @param pages the demo's pages in each language, by their keys.
 * @param changelogs the changelog in each language.
 * @param siteExamples the site's own examples in each language, by the key of the component's page ([SiteExamples]).
 */
internal class ComponentReader(
    json: String,
    private val pages: Map<Language, Map<String, LibraryPage>>,
    changelogs: Map<Language, Changelog> = emptyMap(),
    private val siteExamples: Map<Language, Map<String, LibraryPage>> = emptyMap()
) {
    /** A component as `pantrix-ui.json` lists it. */
    private class Listed(
        val name: String,
        val function: String,
        val group: String,
        val description: String,
        val packageName: String,
        val page: String,
        val tags: List<String>
    ) {
        val key: String = keyOf(name)
    }

    private val listed: List<Listed>
    private val pageTitles: Map<String, String>
    private val types = LibraryTypes(pages)
    private val changes = ComponentChanges(changelogs)

    init {
        val library = Json.parseToJsonElement(json).jsonObject
        pageTitles = library.getValue("pages").jsonArray.associate { page ->
            page.jsonObject.string("key") to page.jsonObject.string("title")
        }
        val all = library.getValue("components").jsonArray.map { it.jsonObject }.map { component ->
            Listed(
                name = component.string("name"),
                function = component.string("function"),
                group = component.string("category"),
                description = component.string("description"),
                packageName = component.string("package"),
                page = component.string("page"),
                tags = component["tags"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty()
            )
        }
        // The demo's order within each group, and the groups in the order the site shows them.
        listed = ComponentGroups.keys.flatMap { group -> all.filter { it.group == group } }
        val unknown = all.map { it.group }.distinct() - ComponentGroups.keys.toSet()
        check(unknown.isEmpty()) { "The library has components in groups the site does not show: $unknown" }
    }

    /** The list of the components in [language]. */
    fun index(language: Language): ComponentIndex = ComponentIndex(
        language = language,
        groups = ComponentGroups.keys.map { group ->
            ComponentGroup(group, listed.filter { it.group == group }.map { cardOf(it, language) })
        }
    )

    /** Every component's page in [language]. */
    fun pages(language: Language): List<ComponentPage> = listed.mapIndexed { index, component ->
        pageOf(component, language, listed.getOrNull(index - 1), listed.getOrNull(index + 1))
    }

    /**
     * Each component's examples in [language] that the site runs as they are written: those whose code calls nothing
     * of the demo app itself and none of its resources, by the component and the example's name.
     */
    fun runnable(language: Language): List<RunnableExample> = listed.flatMap { component ->
        examplesOf(component, language).mapNotNull { (name, example) ->
            val runs = !example.callsTheApp && APP_RESOURCES.none { it in example.code } &&
                example.imports.none { it.startsWith(APP_PACKAGE) }
            if (!runs) return@mapNotNull null
            RunnableExample(component.key, name, example.title, example.imports, example.code)
        }
    }

    private fun cardOf(
        component: Listed,
        language: Language
    ): ComponentCard = ComponentCard(
        key = component.key,
        name = nameOf(component),
        function = component.function,
        // The annotation says what it is in English alone; in Turkish its documentation's first sentence does.
        summary = if (language == Language.English) {
            MarkdownBlocks.spans(component.description)
        } else {
            firstParagraphOf(functionOf(component, language)?.description.orEmpty())
        },
        group = component.group
    )

    private fun pageOf(
        component: Listed,
        language: Language,
        previous: Listed?,
        next: Listed?
    ): ComponentPage {
        val page = pageIn(component.page, language)
        val forms = formsOf(component, language)
        val features = page.features.map(::featureOf)
        val typed = forms.flatMap { form -> form.parameters }
        val englishColors = formsOf(component, Language.English).flatMap { it.parameters }.firstOrNull {
            it.name ==
                COLORS
        }
        return ComponentPage(
            language = language,
            card = cardOf(component, language),
            packageName = component.packageName,
            lead = firstParagraphOf(functionOf(component, language)?.description.orEmpty()),
            features = features.filterNot(::isAboutAccessibility),
            accessibility = features.filter(::isAboutAccessibility),
            bindsToForm = typed.any { it.type.startsWith(FORM_FIELD) },
            examples = examplesOf(component, language).map { (name, example) ->
                ComponentExample(
                    name = name,
                    title = example.title,
                    description = MarkdownBlocks.blocks(example.description)
                        .filterIsInstance<Block.Paragraph>()
                        .map { it.text },
                    code = Block.Code(language = KOTLIN, lines = Highlighter.lines(example.code, KOTLIN)),
                    imports = example.imports,
                    wide = siteExamples[Language.English]?.containsKey(component.key) == true
                )
            },
            forms = forms,
            types = types.takenBy(typed, own = component.function),
            style = types.typeOf(typed.firstOrNull { it.name == STYLE }?.type, language),
            colors = types.typeOf(
                typed.firstOrNull { it.name == COLORS }?.type,
                language,
                roles = types.rolesOf(englishColors?.type)
            ),
            tags = tagsOf(
                component.tags,
                functionOf(component, language)?.parameters?.firstOrNull { it.name == TAG_KEY }?.description
            ),
            tagKey = page.examples.firstNotNullOfOrNull { TAG_KEY_GIVEN.find(it.code)?.groupValues?.get(1) },
            changes = changes.of(component.function, nameOf(component), language),
            previous = previous?.let { ComponentLink(it.key, nameOf(it)) },
            next = next?.let { ComponentLink(it.key, nameOf(it)) }
        )
    }

    private fun formsOf(
        component: Listed,
        language: Language
    ): List<ComponentForm> = pageIn(component.page, language).declarations
        .filter { it.name == component.function }
        .map { declaration ->
            ComponentForm(
                label = declaration.parameters.firstOrNull()?.name.orEmpty(),
                description = firstParagraphOf(declaration.description),
                parameters = declaration.parameters.map(::parameterOf)
            )
        }

    /**
     * The site's own examples of [component] in [language] where it has written some, each by its name, and otherwise
     * the examples of its page in the demo whose code calls it, or all of them where the page is its alone. A name is
     * the example's English title's key, the same in both languages.
     */
    private fun examplesOf(
        component: Listed,
        language: Language
    ): List<Pair<String, LibraryExample>> {
        siteExamples[Language.English]?.get(component.key)?.examples?.let { english ->
            val written = siteExamples[language]?.get(component.key)?.examples ?: english
            return written.mapIndexed { index, example -> keyOf(english[index].title) to example }
        }
        val english = pageIn(component.page, Language.English).examples
        val calls = Regex("""\b${component.function}\s*[({]""")
        val own = listed.count { it.page == component.page } == 1
        return pageIn(component.page, language).examples.mapIndexedNotNull { index, example ->
            if (!own && !calls.containsMatchIn(example.code)) return@mapIndexedNotNull null
            keyOf(english.getOrNull(index)?.title ?: example.title) to example
        }
    }

    private fun pageIn(
        key: String,
        language: Language
    ): LibraryPage = pages[language]?.get(key) ?: pages.getValue(Language.English).getValue(key)

    private fun functionOf(
        component: Listed,
        language: Language
    ): LibraryDeclaration? = pageIn(component.page, language).declarations.firstOrNull { it.name == component.function }

    /** What the site calls [component]: its own page's title where it has one, and its name in words elsewhere. */
    private fun nameOf(component: Listed): String {
        val title = pageTitles[component.key]?.takeIf { component.key == component.page }
        val name = title ?: component.name.replace(Regex("""(?<=[a-z])(?=[A-Z])"""), " ")
        return SPELLINGS.entries.fold(name) { written, (from, to) -> written.replace(from, to) }
    }

    companion object {
        /** The key of the page of the component [name]: `text-area` for `TextArea`. */
        fun keyOf(name: String): String = DemoPages.componentKey(name)

        private fun JsonObject.string(name: String): String = getValue(name).jsonPrimitive.content

        /** How the site writes a few names, as its design does. */
        private val SPELLINGS = mapOf(
            "Auto Resize" to "Auto-resize",
            "Tri State" to "Tri-state",
            "Json View" to "JSON View"
        )

        private const val KOTLIN = "kotlin"

        /** What an example calls of the demo's own resources, which the site does not have. */
        private val APP_RESOURCES = listOf("Res.", "stringResource(", "painterResource(")

        /** The demo app's own packages. */
        private const val APP_PACKAGE = "com.pantrixui.cmp"
        private const val STYLE = "style"
        private const val COLORS = "colors"
        private const val FORM_FIELD = "FormField"
        private const val TAG_KEY = "tagKey"

        /** A key an example's code gives a component, `tagKey = "comment"`. */
        private val TAG_KEY_GIVEN = Regex("""tagKey\s*=\s*"(\w+)"""")
    }
}

/**
 * An example the site runs as the demo's code writes it.
 *
 * @property component the key of the component's page.
 * @property name the example's name, from its English title.
 * @property title what it shows, in the example's language.
 * @property imports what its code imports.
 * @property code its code: its body, then what of its file it calls.
 */
internal class RunnableExample(
    val component: String,
    val name: String,
    val title: String,
    val imports: List<String>,
    val code: String
)
