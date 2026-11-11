package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ChangeEntry
import com.pantrixui.site.core.model.ChangeKind
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.ComponentChange
import com.pantrixui.site.core.model.ComponentFeature
import com.pantrixui.site.core.model.ComponentParameter
import com.pantrixui.site.core.model.ComponentTag
import com.pantrixui.site.core.model.ComponentType
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Span

/** What [markdown]'s first paragraph says, or nothing where it has none. */
internal fun firstParagraphOf(markdown: String): List<Span> = MarkdownBlocks.blocks(markdown)
    .filterIsInstance<Block.Paragraph>()
    .firstOrNull()
    ?.text
    .orEmpty()

/** A feature of a demo's page, `**Lines.** minLines …`: its bold lead apart from what it says. */
internal fun featureOf(markdown: String): ComponentFeature {
    val lead = LEAD.find(markdown)
    val label = lead?.groupValues?.get(1)?.trim()?.removeSuffix(".")
    val rest = if (lead == null) markdown else markdown.substring(lead.range.last + 1).trim()
    return ComponentFeature(label = label, text = MarkdownBlocks.spans(rest))
}

/** Whether [feature]'s lead says it is about keyboards, focus or screen readers. */
internal fun isAboutAccessibility(feature: ComponentFeature): Boolean {
    val label = feature.label?.lowercase() ?: return false
    return ACCESSIBILITY.any { it in label }
}

/** [parameter] as a page shows it: its documentation's first paragraph. */
internal fun parameterOf(parameter: LibraryParameter): ComponentParameter = ComponentParameter(
    name = parameter.name,
    type = parameter.type,
    default = parameter.default,
    description = firstParagraphOf(parameter.description)
)

/**
 * The tags of [prefixes], each with what [said], what a component's `tagKey` says, makes it the tag of: the words
 * before it, "its clear button `TextAreaClear_<tagKey>`".
 */
internal fun tagsOf(
    prefixes: List<String>,
    said: String?
): List<ComponentTag> = prefixes.map { prefix ->
    val written = said?.let { Regex("""([^:;,`]{1,48}?)\s*`${Regex.escape(prefix)}_<tagKey>`""").find(it) }
    val label = written?.groupValues?.get(1)
        ?.substringAfterLast(" and ")
        ?.substringAfterLast(" ve ")
        ?.trim()
        ?.removeSuffix(" is")
        ?.removePrefix("the ")
        ?.removePrefix("its ")
        ?.trim()
        ?.takeIf { it.isNotEmpty() && it.length < MAX_LABEL }
        ?.replaceFirstChar { it.uppercase() }
    ComponentTag(prefix, label)
}

/**
 * The library's types as its documents declare them, on any of the demo's pages: what a component's parameters take,
 * its style and its colours.
 *
 * @param pages the demo's pages in each language.
 */
internal class LibraryTypes(private val pages: Map<Language, Map<String, LibraryPage>>) {
    /** The declaration of the type [name] in [language], or in English where that language has none. */
    fun declared(
        name: String,
        language: Language = Language.English
    ): LibraryDeclaration? = declaredIn(name, language) ?: declaredIn(name, Language.English)

    /** The library's types [parameters] take, by their names, in the order they are first named, but [own]. */
    fun takenBy(
        parameters: List<ComponentParameter>,
        own: String
    ): List<String> = parameters.flatMap { TYPE_NAME.findAll(it.type).map { found -> found.value } }
        .distinct()
        .filter { name -> name != own && name !in NOT_SHOWN && declared(name) != null }

    /** The type [written] names, in [language], each field with its colour role of [roles] where it has one. */
    fun typeOf(
        written: String?,
        language: Language,
        roles: Map<String, String> = emptyMap()
    ): ComponentType? {
        val declaration = written?.let { TYPE_NAME.find(it)?.value }?.let { declared(it, language) }
        return declaration?.let {
            ComponentType(
                name = it.name,
                description = firstParagraphOf(it.description),
                fields = it.parameters.map { field -> parameterOf(field).copy(role = roles[field.name]) }
            )
        }
    }

    /** The theme's role each field of the colours type [written] is by default, as its English documentation says. */
    fun rolesOf(written: String?): Map<String, String> {
        val declaration = written?.let { TYPE_NAME.find(it)?.value }?.let { declared(it) }
        return declaration?.parameters.orEmpty().mapNotNull { field ->
            ROLE.find(field.description)?.let { field.name to it.groupValues[1] }
        }.toMap()
    }

    private fun declaredIn(
        name: String,
        language: Language
    ): LibraryDeclaration? = pages[language]?.values?.firstNotNullOfOrNull { page ->
        page.declarations.firstOrNull { it.name == name }
    }
}

/**
 * The changelog's lines about a component: the English lines that name its function or its name, "text areas", and
 * in another language the lines that stand where those do, since the two changelogs have the same lines in the same
 * places.
 *
 * @param changelogs the changelog in each language.
 */
internal class ComponentChanges(private val changelogs: Map<Language, Changelog>) {
    /** The lines about the component [name], drawn by [function], in [language]. */
    fun of(
        function: String,
        name: String,
        language: Language
    ): List<ComponentChange> {
        val english = changelogs[Language.English] ?: return emptyList()
        val named = Regex("""\b${Regex.escape(name.lowercase())}(s|es)?\b""")
        val changelog = changelogs[language] ?: english
        return english.releases.flatMapIndexed { r, release ->
            release.sections.flatMapIndexed { s, section ->
                section.groups.flatMapIndexed { g, group ->
                    group.entries.mapIndexedNotNull { e, entry ->
                        if (!entry.isAbout(function, named)) return@mapIndexedNotNull null
                        val twin = changelog.releases.getOrNull(r)?.sections?.getOrNull(s)?.groups?.getOrNull(g)
                            ?.entries?.getOrNull(e) ?: entry
                        ComponentChange(version = release.version, kind = section.kind, entry = twin)
                    }
                }
            }
        }
    }

    /**
     * The version the component drawn by [function] came in: the release whose added lines name it as a new
     * component, as the changelog writes one (`` `PantrixCodeEditor`, the 101st component: ``), and the version still
     * to come where it is unreleased. Null for a component of the first release, which the changelog describes as a
     * whole. A preview's version is its release's: `0.1.0-alpha03` is `0.1.0`.
     */
    fun since(function: String): String? {
        val english = changelogs[Language.English] ?: return null
        // The releases are the newest first: the last that adds it is the one it came in.
        val release = english.releases.lastOrNull { release ->
            release.sections.any { section ->
                section.kind == ChangeKind.Added &&
                    section.groups.any { group -> group.entries.any { it.adds(function) } }
            }
        }
        return release?.let { (it.version ?: english.upcoming)?.substringBefore('-') }
    }

    /** Whether the line adds the component drawn by [function]: its first words are its name and its count. */
    private fun ChangeEntry.adds(function: String): Boolean {
        val spans = (blocks.firstOrNull() as? Block.Paragraph)?.text.orEmpty()
        val name = spans.firstOrNull() as? Span.Code
        return name?.text == function && NEW_COMPONENT.containsMatchIn(spans.drop(1).plainText())
    }

    private fun ChangeEntry.isAbout(
        function: String,
        named: Regex
    ): Boolean {
        val spans = blocks.filterIsInstance<Block.Paragraph>().flatMap { it.text }
        return spans.any { it is Span.Code && it.text == function } ||
            named.containsMatchIn(spans.plainText().lowercase())
    }
}

/** What follows a new component's name on its added line: `, the 101st component`. */
private val NEW_COMPONENT = Regex("""^, the \d+(st|nd|rd|th) component\b""")

private val LEAD = Regex("""^\*\*(.+?)\*\*""")
private val TYPE_NAME = Regex("""\b[A-Z][A-Za-z0-9]*""")
private val ROLE = Regex("""The theme's `([\w.]+)`""")

/** The words in a feature's lead that say it is about keyboards, focus or screen readers. */
private val ACCESSIBILITY = listOf(
    "screen reader",
    "keyboard",
    "focus",
    "touch target",
    "reduced motion",
    "accessib",
    "ekran okuyucu",
    "klavye",
    "odak",
    "dokunma hedefi",
    "erişilebilir"
)

/** The types the API's table names but a list of the types it takes leaves out: everyone knows them. */
private val NOT_SHOWN = setOf("Modifier", "String", "Boolean", "Int", "Float", "Unit", "Color", "Dp", "Shape")

/** The longest a tag's label may be: a longer text before a tag is a sentence, not what the tag is of. */
private const val MAX_LABEL = 32
