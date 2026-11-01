package com.pantrixui.site.core.testing

import com.pantrixui.site.core.content.AskFailure
import com.pantrixui.site.core.content.AskRepository
import com.pantrixui.site.core.model.AskComponent
import com.pantrixui.site.core.model.ComponentHits
import com.pantrixui.site.core.model.DeclarationDetail
import com.pantrixui.site.core.model.DeclarationHits
import com.pantrixui.site.core.model.DocumentHits
import com.pantrixui.site.core.model.ExampleList
import com.pantrixui.site.core.model.IconHits
import com.pantrixui.site.core.model.Language

/**
 * The library's MCP server for a test: it answers every question with what it is given, and throws [AskFailure] for
 * every call while [failing] is set, as a server that does not answer. It keeps each call it was asked.
 *
 * @param components what a search of the components finds.
 * @param cards each component's card, by its composable.
 * @param examples each component's page's examples, by its composable.
 * @param declarations what a search of the API finds.
 * @param details each declaration in full, by its title.
 * @param icons what a search of the icons finds.
 * @param documents what a search of the documents finds.
 */
class FakeAskRepository(
    private val components: ComponentHits = ComponentHits(0, emptyList()),
    private val cards: Map<String, AskComponent> = emptyMap(),
    private val examples: Map<String, ExampleList> = emptyMap(),
    private val declarations: DeclarationHits = DeclarationHits(0, emptyList()),
    private val details: Map<String, List<DeclarationDetail>> = emptyMap(),
    private val icons: IconHits = IconHits(0, emptyList()),
    private val documents: DocumentHits = DocumentHits(0, emptyList())
) : AskRepository {
    /** Whether every call fails, as with no network. */
    var failing: Boolean = false

    /** Each call asked, as `tool(argument, language)`, in its order. */
    val calls: MutableList<String> = mutableListOf()

    override suspend fun searchComponents(
        query: String,
        language: Language,
        limit: Int
    ): ComponentHits = answer("search_components", query, language) {
        components.copy(components = components.components.take(limit))
    }

    override suspend fun component(
        function: String,
        language: Language
    ): AskComponent = answer("get_component_metadata", function, language) {
        cards[function] ?: throw AskFailure("No component is called $function")
    }

    override suspend fun examples(
        function: String,
        language: Language
    ): ExampleList = answer("list_examples", function, language) { examples[function] ?: ExampleList() }

    override suspend fun searchApi(
        query: String,
        language: Language,
        limit: Int
    ): DeclarationHits = answer("search_api", query, language) {
        declarations.copy(declarations = declarations.declarations.take(limit))
    }

    override suspend fun declaration(
        title: String,
        language: Language
    ): List<DeclarationDetail> = answer("get_api", title, language) { details[title].orEmpty() }

    override suspend fun searchIcons(
        query: String,
        language: Language,
        limit: Int
    ): IconHits = answer("search_icons", query, language) { icons.copy(icons = icons.icons.take(limit)) }

    override suspend fun searchDocumentation(
        query: String,
        language: Language,
        limit: Int
    ): DocumentHits = answer("search_documentation", query, language) {
        documents.copy(documents = documents.documents.take(limit))
    }

    private fun <T> answer(
        tool: String,
        argument: String,
        language: Language,
        given: () -> T
    ): T {
        calls += "$tool($argument, ${language.code})"
        if (failing) throw AskFailure("$tool: the server did not answer")
        return given()
    }
}
