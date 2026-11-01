package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.content.AskRepository
import com.pantrixui.site.core.model.AskComponent
import com.pantrixui.site.core.model.AskScope
import com.pantrixui.site.core.model.ComponentHit
import com.pantrixui.site.core.model.DeclarationDetail
import com.pantrixui.site.core.model.DeclarationHits
import com.pantrixui.site.core.model.DocumentHits
import com.pantrixui.site.core.model.ExampleList
import com.pantrixui.site.core.model.IconHits
import com.pantrixui.site.core.model.Language
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** The ways the page asks: the server itself, the reader's own assistant, and a model with the reader's own key. */
enum class AskTab(val key: String) {
    Server("server"),
    Assistant("assistant"),
    Key("key")
}

/** Where a question stands: none asked, on its way, answered, found nothing, or not answered at all. */
enum class AskPhase {
    Empty,
    Searching,
    Answered,
    Nothing,
    Failed
}

/**
 * A tool the page called for a question, and what it got.
 *
 * @property tool the tool's name: `search_components`.
 * @property argument what it was given: the question, or the name it looked up.
 * @property result what it got, or null while the call is on its way.
 */
data class AskStep(
    val tool: String,
    val argument: String,
    val result: AskResult? = null
)

/** What a call got, which the page says in its own words. */
sealed interface AskResult {
    /** [total] components, of which [first] came first. */
    data class Components(val total: Int, val first: String?) : AskResult

    /** [total] documents, [titles] the first of them. */
    data class Documents(val total: Int, val titles: List<String>) : AskResult

    /** A component's card: its import, description, keywords and short examples. */
    data object Card : AskResult

    /** A page's [count] examples. */
    data class Examples(val count: Int) : AskResult

    /** [total] declarations, of which [shown] are given. */
    data class Declarations(val total: Int, val shown: Int) : AskResult

    /** A declaration in full. */
    data object Declaration : AskResult

    /** [total] icons, [names] the first of them. */
    data class Icons(val total: Int, val names: List<String>) : AskResult

    /** Nothing matched. */
    data object Nothing : AskResult

    /** The server did not answer. */
    data object NoAnswer : AskResult
}

/** What the server answered a question with, in the shape of the question's scope. */
sealed interface AskAnswer {
    /**
     * A component: [hits] what the search found, [card] and [examples] those of the one shown, [shown] its
     * composable, and [documents] the documents that speak of it, where the scope reads them.
     */
    data class Component(
        val hits: List<ComponentHit>,
        val shown: String,
        val card: AskComponent? = null,
        val examples: ExampleList? = null,
        val documents: DocumentHits? = null
    ) : AskAnswer

    /** Declarations: [hits] what the search found, [open] the one opened, and [details] each one opened so far. */
    data class Api(
        val hits: DeclarationHits,
        val open: String?,
        val details: Map<String, List<DeclarationDetail>> = emptyMap()
    ) : AskAnswer

    /** Icons. */
    data class Icons(val hits: IconHits) : AskAnswer

    /** Documents. */
    data class Documents(val hits: DocumentHits) : AskAnswer
}

/**
 * The page that asks the library's MCP server, as its screen draws it.
 *
 * @property tab the way of asking on show.
 * @property scope where a question is looked for.
 * @property asked the question asked last, or null for none.
 * @property phase where that question stands.
 * @property steps the tools called for it, in their order.
 * @property answer what the server answered.
 * @property toolsOpen whether a phone shows the tools called, which it folds away.
 */
data class AskUiState(
    val tab: AskTab = AskTab.Server,
    val scope: AskScope = AskScope.All,
    val asked: String? = null,
    val phase: AskPhase = AskPhase.Empty,
    val steps: List<AskStep> = emptyList(),
    val answer: AskAnswer? = null,
    val toolsOpen: Boolean = false
) : UiState

/** What the reader does on the page that asks the server. */
sealed interface AskAction : UiAction {
    /** Asks [question], in the scope on show; a blank one clears what was asked. */
    data class Ask(val question: String) : AskAction

    /** Looks in [scope], asking the question again there, where one was asked. */
    data class PickScope(val scope: AskScope) : AskAction

    /** Asks [question] in [scope]: an example of the page. */
    data class Try(
        val question: String,
        val scope: AskScope
    ) : AskAction

    /** Asks the last question again, after the server did not answer it. */
    data object Retry : AskAction

    /** Shows the way of asking [tab]. */
    data class PickTab(val tab: AskTab) : AskAction

    /** Shows the card of the component whose composable is [function], one the search found too. */
    data class ShowComponent(val function: String) : AskAction

    /** Opens the declaration [title], or closes it where it is open. */
    data class ToggleDeclaration(val title: String) : AskAction

    /** Shows every declaration the search found, past the first it gave. */
    data object MoreDeclarations : AskAction

    /** Shows a phone the tools called, or folds them away. */
    data object ToggleTools : AskAction

    /** Opens [address], a page of the site. */
    data class Open(val address: String) : AskAction

    /** Opens [url] outside the site. */
    data class OpenUrl(val url: String) : AskAction

    /** Copies [text]. */
    data class Copy(val text: String) : AskAction
}

/** What the page asks of the platform. */
sealed interface AskEffect : UiEffect {
    /** Opens [address], a page of the site. */
    data class Navigate(val address: String) : AskEffect

    /** Opens [url] outside the site: in a new tab on the web, in the browser elsewhere. */
    data class OpenUrl(val url: String) : AskEffect

    /** Puts [text] on the clipboard. */
    data class Copy(val text: String) : AskEffect
}

/**
 * The model of the page that asks the server: it calls the tools a question needs in [language], in the order the
 * page shows them, and keeps what each got. A new question stops the one before it.
 *
 * @param language the language the answers are asked in.
 * @param repository where the server's tools are called.
 */
class AskViewModel(
    private val language: Language,
    private val repository: AskRepository
) : BaseViewModel<AskUiState, AskAction, AskEffect>(AskUiState()) {
    private var asking: Job? = null

    override fun handle(action: AskAction) {
        when (action) {
            is AskAction.Ask -> ask(action.question, currentState.scope)
            is AskAction.PickScope -> ask(currentState.asked.orEmpty(), action.scope)
            is AskAction.Try -> ask(action.question, action.scope)
            AskAction.Retry -> ask(currentState.asked.orEmpty(), currentState.scope)
            is AskAction.PickTab -> updateState { copy(tab = action.tab) }
            is AskAction.ShowComponent -> showComponent(action.function)
            is AskAction.ToggleDeclaration -> toggleDeclaration(action.title)
            AskAction.MoreDeclarations -> moreDeclarations()
            AskAction.ToggleTools -> updateState { copy(toolsOpen = !toolsOpen) }
            is AskAction.Open -> sendEffect(AskEffect.Navigate(action.address))
            is AskAction.OpenUrl -> sendEffect(AskEffect.OpenUrl(action.url))
            is AskAction.Copy -> sendEffect(AskEffect.Copy(action.text))
        }
    }

    /** Asks [question] in [scope]; with no question, only takes the scope for the next one. */
    private fun ask(
        question: String,
        scope: AskScope
    ) {
        asking?.cancel()
        val text = question.trim()
        if (text.isEmpty()) {
            updateState {
                copy(scope = scope, asked = null, phase = AskPhase.Empty, steps = emptyList(), answer = null)
            }
            return
        }
        updateState {
            copy(
                tab = AskTab.Server,
                scope = scope,
                asked = text,
                phase = AskPhase.Searching,
                steps = emptyList(),
                answer = null
            )
        }
        asking = launch {
            try {
                when (scope) {
                    AskScope.All -> askComponents(text, documents = true)
                    AskScope.Components -> askComponents(text, documents = false)
                    AskScope.Api -> askApi(text)
                    AskScope.Icons -> askIcons(text)
                    AskScope.Documents -> askDocuments(text)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") failure: Exception) {
                // A call can fail in any way the network and the server can: the page says the server did not answer.
                updateState { failed() }
            }
        }
    }

    /** The components first, and the documents beside them where the scope reads them; then the best one's card. */
    private suspend fun askComponents(
        text: String,
        documents: Boolean
    ) = coroutineScope {
        updateState { asking(SEARCH_COMPONENTS, text) }
        if (documents) updateState { asking(SEARCH_DOCUMENTATION, text) }
        val found = async { repository.searchComponents(text, language, COMPONENTS) }
        val pages = if (documents) async { repository.searchDocumentation(text, language, CARD_DOCUMENTS) } else null
        val hits = found.await()
        updateState { done(SEARCH_COMPONENTS, AskResult.Components(hits.total, hits.components.firstOrNull()?.name)) }
        val read = pages?.await()
        if (read != null) {
            updateState { done(SEARCH_DOCUMENTATION, AskResult.Documents(read.total, read.documents.map { it.title })) }
        }
        val best = hits.components.firstOrNull()
        if (best == null) {
            updateState {
                if (read != null && read.documents.isNotEmpty()) answered(AskAnswer.Documents(read)) else nothing()
            }
            return@coroutineScope
        }
        updateState { answered(AskAnswer.Component(hits = hits.components, shown = best.function, documents = read)) }
        card(best.function)
    }

    /** The card of [function]: its metadata and its page's examples, side by side. */
    private suspend fun card(function: String) = coroutineScope {
        updateState { asking(GET_COMPONENT_METADATA, function).asking(LIST_EXAMPLES, function) }
        val card = async { repository.component(function, language) }
        val examples = async { repository.examples(function, language) }
        val read = card.await()
        updateState { done(GET_COMPONENT_METADATA, AskResult.Card) }
        val listed = examples.await()
        updateState {
            done(LIST_EXAMPLES, AskResult.Examples(listed.examples.size))
                .component(function) { copy(card = read, examples = listed) }
                .copy(phase = AskPhase.Answered)
        }
    }

    private suspend fun askApi(text: String) {
        updateState { asking(SEARCH_API, text) }
        val hits = repository.searchApi(text, language, DECLARATIONS)
        updateState { done(SEARCH_API, AskResult.Declarations(hits.total, hits.declarations.size)) }
        val first = hits.declarations.firstOrNull() ?: return updateState { nothing() }
        updateState { asking(GET_API, first.title) }
        val details = repository.declaration(first.title, language)
        updateState {
            done(GET_API, AskResult.Declaration)
                .answered(AskAnswer.Api(hits, open = first.title, details = mapOf(first.title to details)))
        }
    }

    private suspend fun askIcons(text: String) {
        updateState { asking(SEARCH_ICONS, text) }
        val hits = repository.searchIcons(text, language, ICONS)
        updateState {
            val shown = done(SEARCH_ICONS, AskResult.Icons(hits.total, hits.icons.map { it.name }))
            if (hits.icons.isEmpty()) shown.nothing() else shown.answered(AskAnswer.Icons(hits))
        }
    }

    private suspend fun askDocuments(text: String) {
        updateState { asking(SEARCH_DOCUMENTATION, text) }
        val hits = repository.searchDocumentation(text, language, DOCUMENTS)
        updateState {
            val shown = done(SEARCH_DOCUMENTATION, AskResult.Documents(hits.total, hits.documents.map { it.title }))
            if (hits.documents.isEmpty()) shown.nothing() else shown.answered(AskAnswer.Documents(hits))
        }
    }

    private fun showComponent(function: String) {
        val answer = currentState.answer as? AskAnswer.Component ?: return
        if (answer.shown == function) return
        asking?.cancel()
        updateState {
            // The calls of a card still on its way are stopped, and leave the list of steps.
            copy(
                phase = AskPhase.Searching,
                steps = steps.filter { it.result != null },
                answer = answer.copy(shown = function, card = null, examples = null)
            )
        }
        asking = launch {
            try {
                card(function)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") failure: Exception) {
                // As in a question's calls: the page says the server did not answer.
                updateState { failed() }
            }
        }
    }

    /** Opens the declaration [title], reading it the first time, or closes it where it is open. */
    private fun toggleDeclaration(title: String) {
        val answer = currentState.answer as? AskAnswer.Api ?: return
        val opening = answer.open != title
        updateState { copy(answer = answer.copy(open = title.takeIf { opening })) }
        if (opening && title !in answer.details) {
            launch {
                val details = runCatching { repository.declaration(title, language) }.getOrNull() ?: return@launch
                updateState {
                    val now = this.answer as? AskAnswer.Api ?: return@updateState this
                    copy(answer = now.copy(details = now.details + (title to details)))
                }
            }
        }
    }

    /** The search for declarations again, giving every one it found, up to [ALL_DECLARATIONS]. */
    private fun moreDeclarations() {
        if (currentState.answer !is AskAnswer.Api) return
        val asked = currentState.asked ?: return
        launch {
            updateState { asking(SEARCH_API, asked) }
            val all = runCatching { repository.searchApi(asked, language, ALL_DECLARATIONS) }.getOrNull()
            if (all == null) {
                updateState { done(SEARCH_API, AskResult.NoAnswer) }
                return@launch
            }
            updateState {
                val shown = done(SEARCH_API, AskResult.Declarations(all.total, all.declarations.size))
                val now = shown.answer as? AskAnswer.Api ?: return@updateState shown
                shown.copy(answer = now.copy(hits = all))
            }
        }
    }

    private companion object {
        const val SEARCH_COMPONENTS = "search_components"
        const val SEARCH_DOCUMENTATION = "search_documentation"
        const val GET_COMPONENT_METADATA = "get_component_metadata"
        const val LIST_EXAMPLES = "list_examples"
        const val SEARCH_API = "search_api"
        const val GET_API = "get_api"
        const val SEARCH_ICONS = "search_icons"

        /** How many of each a search gives: the best component and four more, the card's documents, and the rest. */
        const val COMPONENTS = 5
        const val CARD_DOCUMENTS = 3
        const val DECLARATIONS = 5
        const val ALL_DECLARATIONS = 50
        const val ICONS = 12
        const val DOCUMENTS = 5
    }
}

/** A step on its way: [tool] called with [argument], added to the ones shown. */
private fun AskUiState.asking(
    tool: String,
    argument: String
): AskUiState = copy(steps = steps + AskStep(tool, argument))

/** The last call of [tool] still on its way has got [result]: a tool is called once at a time. */
private fun AskUiState.done(
    tool: String,
    result: AskResult
): AskUiState {
    val index = steps.indexOfLast { it.tool == tool && it.result == null }
    return copy(steps = steps.mapIndexed { at, step -> if (at == index) step.copy(result = result) else step })
}

/** [answer] shown; a component's card is still on its way, and the page waits for it. */
private fun AskUiState.answered(answer: AskAnswer): AskUiState =
    copy(answer = answer, phase = if (answer is AskAnswer.Component) phase else AskPhase.Answered)

/** The component's answer changed by [change], where it still shows [function]. */
private fun AskUiState.component(
    function: String,
    change: AskAnswer.Component.() -> AskAnswer.Component
): AskUiState {
    val now = answer as? AskAnswer.Component
    return if (now == null || now.shown != function) this else copy(answer = now.change())
}

/** The search found nothing. */
private fun AskUiState.nothing(): AskUiState = copy(phase = AskPhase.Nothing, answer = null)

/** The server did not answer: the step on its way says so, and the rest that never came are left out. */
private fun AskUiState.failed(): AskUiState {
    val waiting = steps.indexOfFirst { it.result == null }
    val kept = if (waiting < 0) steps else steps.take(waiting + 1)
    return copy(
        phase = AskPhase.Failed,
        answer = null,
        steps = kept.map { if (it.result == null) it.copy(result = AskResult.NoAnswer) else it }
    )
}
