package com.pantrixui.site.core.model

import kotlinx.serialization.Serializable

/**
 * The library's components in one language, by group, as the components page lists them: what the content tool reads
 * from the `com.pantrixui:mcp` jar.
 *
 * @property language the language its words are in.
 * @property groups the groups, in the order the page shows them.
 */
@Serializable
data class ComponentIndex(
    val language: Language,
    val groups: List<ComponentGroup>
) {
    /** Every component, group by group. */
    val components: List<ComponentCard>
        get() = groups.flatMap { it.components }
}

/**
 * A group of components.
 *
 * @property key what the library calls it: `inputs`.
 * @property components its components, by name.
 */
@Serializable
data class ComponentGroup(
    val key: String,
    val components: List<ComponentCard>
)

/**
 * A component as a list shows it.
 *
 * @property key the key of its page: `text-area`.
 * @property name its name: `Text Area`.
 * @property function the composable that draws it: `PantrixTextArea`.
 * @property summary what it is for, in a sentence or two.
 * @property group the key of its group.
 */
@Serializable
data class ComponentCard(
    val key: String,
    val name: String,
    val function: String,
    val summary: List<Span>,
    val group: String
)

/**
 * A component's page: what it is, its examples, its API, its style and colours, how a test finds it, and what changed.
 *
 * @property language the language its words are in.
 * @property card the component as a list shows it.
 * @property packageName the package it is imported from.
 * @property lead what its documentation says first: what it is for.
 * @property features what its demo page says it does, a line each with a bold lead.
 * @property accessibility the lines of [features] that are about keyboards, focus and screen readers.
 * @property bindsToForm whether one of its forms takes a form's field.
 * @property examples its examples, in the demo's order: those whose code calls it.
 * @property forms its forms, each with its parameters.
 * @property types the library's types its forms take.
 * @property style the type that holds its geometry, where it takes one.
 * @property colors the type that holds its colours, where it takes one.
 * @property tags the automation tags it gives, each with what it is the tag of.
 * @property tagKey a key its examples give it, for the tags a test finds it by.
 * @property changes the changelog's lines about it, each with its version.
 * @property since the version it came in, where the changelog adds it as a new component; null for one of the first
 *   release, which the changelog describes as a whole.
 * @property previous the component before it in its group's order, and [next] the one after.
 */
@Serializable
data class ComponentPage(
    val language: Language,
    val card: ComponentCard,
    val packageName: String,
    val lead: List<Span>,
    val features: List<ComponentFeature> = emptyList(),
    val accessibility: List<ComponentFeature> = emptyList(),
    val bindsToForm: Boolean = false,
    val examples: List<ComponentExample> = emptyList(),
    val forms: List<ComponentForm> = emptyList(),
    val types: List<String> = emptyList(),
    val style: ComponentType? = null,
    val colors: ComponentType? = null,
    val tags: List<ComponentTag> = emptyList(),
    val tagKey: String? = null,
    val changes: List<ComponentChange> = emptyList(),
    val since: String? = null,
    val previous: ComponentLink? = null,
    val next: ComponentLink? = null
)

/**
 * A line of what a component does: its bold lead, and what it says after it.
 *
 * @property label the lead, `Lines`.
 * @property text what it says.
 */
@Serializable
data class ComponentFeature(
    val label: String? = null,
    val text: List<Span>
)

/**
 * An example of a component.
 *
 * @property name the demo's name of it, `TextAreaComment`, which a live example of the site's own is found by.
 * @property title what it shows.
 * @property description what it says of itself, paragraph by paragraph.
 * @property code its code, without its imports, its words told apart.
 * @property imports what its code imports.
 * @property wide whether it is drawn as wide as its frame: the site's own examples are laid out for the frame's width,
 *   and the demo's in a column of their size.
 */
@Serializable
data class ComponentExample(
    val name: String,
    val title: String,
    val description: List<List<Span>> = emptyList(),
    val code: Block.Code,
    val imports: List<String> = emptyList(),
    val wide: Boolean = false
)

/**
 * One form of a component's function, its overload.
 *
 * @property label what tells it from the others: the name of its first parameter, `state`.
 * @property description what its documentation says of it.
 * @property parameters its parameters, in order.
 */
@Serializable
data class ComponentForm(
    val label: String,
    val description: List<Span> = emptyList(),
    val parameters: List<ComponentParameter>
)

/**
 * A parameter of a component's form, or a field of a type of the library.
 *
 * @property name its name.
 * @property type its type, as written.
 * @property default its default, as written, or null where it has to be given.
 * @property description what it is for.
 * @property role the theme's colour role it is by default, `surface`, for a field of a colours type that names one.
 */
@Serializable
data class ComponentParameter(
    val name: String,
    val type: String,
    val default: String? = null,
    val description: List<Span> = emptyList(),
    val role: String? = null
)

/**
 * A type of the library a component takes: its style or its colours.
 *
 * @property name its name, `TextFieldStyle`.
 * @property description what its documentation says first.
 * @property fields its fields, in order.
 */
@Serializable
data class ComponentType(
    val name: String,
    val description: List<Span> = emptyList(),
    val fields: List<ComponentParameter>
)

/**
 * An automation tag a component gives.
 *
 * @property prefix what comes before the key: `TextAreaClear`.
 * @property label what it is the tag of, as the documentation says it: `its clear button`.
 */
@Serializable
data class ComponentTag(
    val prefix: String,
    val label: String? = null
)

/**
 * A line of the changelog about a component.
 *
 * @property version the version it came in, or null for the one still to come.
 * @property kind what kind of change it is.
 * @property entry the line.
 */
@Serializable
data class ComponentChange(
    val version: String? = null,
    val kind: ChangeKind,
    val entry: ChangeEntry
)

/**
 * A link to a component's page.
 *
 * @property key the key of its page.
 * @property name its name.
 */
@Serializable
data class ComponentLink(
    val key: String,
    val name: String
)

/** What the site calls each group of the library's components, by the library's key for it. */
object ComponentGroups {
    /** The groups, in the order the site shows them. */
    val keys: List<String> = listOf("elements", "inputs", "date", "container", "navigation", "overlay", "data", "chart")

    /** What the group [key] is called. */
    fun title(key: String): Words = when (key) {
        "elements" -> Words("Elements", "Öğeler")
        "inputs" -> Words("Inputs", "Girdiler")
        "date" -> Words("Date and time", "Tarih ve saat")
        "container" -> Words("Containers", "Kapsayıcılar")
        "navigation" -> Words("Navigation", "Gezinme")
        "overlay" -> Words("Overlays", "Katmanlar")
        "data" -> Words("Data", "Veri")
        "chart" -> Words("Charts", "Grafikler")
        else -> Words(key, key)
    }
}
