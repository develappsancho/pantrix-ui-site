package com.pantrixui.site.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Where a question to the library's MCP server is looked for: among everything a component's answer reads, or among
 * one kind of thing alone.
 *
 * @property key what the page's automation tags name it by.
 */
enum class AskScope(val key: String) {
    /** The components and the documents: a component's card, with the documents that speak of it. */
    All("all"),

    /** The components alone. */
    Components("components"),

    /** The library's declarations. */
    Api("api"),

    /** The icons of `PantrixIcons`. */
    Icons("icons"),

    /** The documents: the pages, the guides and the skill's references. */
    Documents("documents")
}

/**
 * The components `search_components` found, as its structured content gives them: the best first.
 *
 * @property total how many matched, of which [components] are the first.
 * @property components the ones given.
 */
@Serializable
data class ComponentHits(
    val total: Int,
    val components: List<ComponentHit>
)

/**
 * A component a search found.
 *
 * @property name its name: `Snackbar`.
 * @property function its composable: `PantrixSnackbarHost`.
 * @property category the group of packages it is in.
 * @property page the key of its page, which is the key of its page on the site too.
 * @property description what it is for, in the language asked for.
 * @property keywords the words a search finds it by, in that language.
 * @property import what a file imports to call it.
 * @property artifact the artifact it is published in.
 */
@Serializable
data class ComponentHit(
    val name: String,
    val function: String,
    val category: String,
    val page: String,
    val description: String,
    val keywords: List<String> = emptyList(),
    val import: String,
    val artifact: String
)

/**
 * A component as `get_component_metadata` gives it, as much of it as a card shows.
 *
 * @property name its name.
 * @property function its composable.
 * @property category the group of packages it is in.
 * @property description what it is for, in the language asked for.
 * @property keywords the words a search finds it by, in that language.
 * @property import what a file imports to call it.
 * @property artifact the artifact it is published in.
 * @property page the key of its page.
 * @property snippets its short examples, from its annotation: code a reader pastes into a composable.
 */
@Serializable
data class AskComponent(
    val name: String,
    val function: String,
    val category: String,
    val description: String,
    val keywords: List<String> = emptyList(),
    val import: String,
    val artifact: String,
    val page: String,
    val snippets: List<ComponentSnippet> = emptyList()
)

/**
 * A short example of a component, which its annotation carries.
 *
 * @property title what it shows.
 * @property code its code.
 */
@Serializable
data class ComponentSnippet(
    val title: String,
    val code: String
)

/**
 * The examples of a page, as `list_examples` gives them.
 *
 * @property page the page they are on.
 * @property examples each example, in the page's order.
 */
@Serializable
data class ExampleList(
    val page: PageNamed? = null,
    val examples: List<ExampleNamed> = emptyList()
)

/**
 * A page an answer names.
 *
 * @property key its key.
 * @property title its title, in the language asked for.
 */
@Serializable
data class PageNamed(
    val key: String,
    val title: String
)

/**
 * An example a list names.
 *
 * @property name what `get_example` takes.
 * @property title what it shows, in the language asked for.
 * @property description its first sentence, empty for none.
 */
@Serializable
data class ExampleNamed(
    val name: String,
    val title: String,
    val description: String = ""
)

/**
 * The declarations `search_api` found, the best first.
 *
 * @property total how many matched, of which [declarations] are the first.
 * @property declarations the ones given.
 */
@Serializable
data class DeclarationHits(
    val total: Int,
    val declarations: List<DeclarationHit>
)

/**
 * A declaration a search found.
 *
 * @property title what a reader calls it: `SnackbarDuration`, `PantrixSnackbarState.show`.
 * @property name its own name.
 * @property kind what it is: `function`, `class`, `enum` and the rest.
 * @property module the module it is in.
 * @property artifact the artifact it is published in.
 * @property packageName the package it is in.
 * @property import what a file imports to use it: a member's type.
 * @property description its first sentence, in the language asked for.
 */
@Serializable
data class DeclarationHit(
    val title: String,
    val name: String,
    val kind: String,
    val module: String,
    val artifact: String,
    @SerialName("package") val packageName: String,
    val import: String,
    val description: String
)

/**
 * A declaration in full, as `get_api` gives it, as much of it as a row that opens shows.
 *
 * @property name its name.
 * @property kind what it is.
 * @property signature how it is declared.
 * @property description what its documentation says, in the language asked for.
 * @property entries an enum's entries.
 * @property parameters a function's parameters.
 * @property pages the keys of the demo's pages that document it, the first of them its component's.
 */
@Serializable
data class DeclarationDetail(
    val name: String,
    val kind: String,
    val signature: String,
    val description: String = "",
    val entries: List<DeclarationPart> = emptyList(),
    val parameters: List<DeclarationPart> = emptyList(),
    val pages: List<String> = emptyList()
)

/**
 * An entry of an enum, or a parameter of a function, by its name and what it is for.
 *
 * @property name its name.
 * @property description what it is for, in the language asked for.
 */
@Serializable
data class DeclarationPart(
    val name: String,
    val description: String = ""
)

/**
 * The icons `search_icons` found.
 *
 * @property total how many matched, of which [icons] are the first.
 * @property icons the ones given.
 */
@Serializable
data class IconHits(
    val total: Int,
    val icons: List<IconHit>
)

/**
 * An icon of `PantrixIcons` a search found.
 *
 * @property name its name: `ArrowBack`.
 * @property reference how code names it: `PantrixIcons.ArrowBack`.
 * @property description what it shows, in the language asked for.
 * @property mirrored whether it is mirrored in a language read from right to left.
 */
@Serializable
data class IconHit(
    val name: String,
    val reference: String,
    val description: String,
    val mirrored: Boolean = false
)

/**
 * The documents `search_documentation` found, the best first.
 *
 * @property total how many matched, of which [documents] are the first.
 * @property documents the ones given.
 */
@Serializable
data class DocumentHits(
    val total: Int,
    val documents: List<DocumentHit>
)

/**
 * A document a search found: a page of the demo, a guide or a reference of the skill.
 *
 * @property key what names it: `snackbar`, `guide/ai`, `skill/components`.
 * @property kind what it is: `page`, `guide` or `reference`.
 * @property title its title, in the language asked for.
 * @property summary its first sentence, in that language.
 * @property line the line of it that holds the most of the question, as plain words, where one does.
 */
@Serializable
data class DocumentHit(
    val key: String,
    val kind: String,
    val title: String,
    val summary: String,
    val line: String? = null
)
