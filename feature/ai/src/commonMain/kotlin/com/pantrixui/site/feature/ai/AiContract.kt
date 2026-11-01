package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.base.loadState
import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.codeByHeading
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * The page for coding assistants, as its screen draws it.
 *
 * @property step how many steps of the conversation are shown, the last of them included: all of them until the
 *   reader plays it again.
 * @property client the assistant whose setup is shown.
 * @property answers the code the assistant writes at the conversation's end, in each size, by its heading on the
 *   page's content: [ANSWER], [ANSWER_TABLET] and [ANSWER_PHONE].
 */
data class AiUiState(
    val step: Int = ALL_STEPS,
    val client: Int = 0,
    val answers: LoadState<Map<String, Block.Code>> = LoadState.Loading
) : UiState

/** What the user does on the page for coding assistants. */
sealed interface AiAction : UiAction {
    /** Plays the conversation again from its first step, a step at a time until the last of its [steps]. */
    data class Replay(val steps: Int) : AiAction

    /** Shows the setup of the assistant [index]. */
    data class PickClient(val index: Int) : AiAction

    /** Opens the index of the documents for coding assistants, `llms.txt`, where the site serves it. */
    data object ReadLlms : AiAction

    /** Opens [url]: a file for coding assistants, where the site serves it or the release has it, or the release. */
    data class Open(val url: String) : AiAction
}

/** What the page asks of the platform. */
sealed interface AiEffect : UiEffect {
    /** Opens [url] outside the app: in a new tab on the web, in the browser elsewhere. */
    data class OpenUrl(val url: String) : AiEffect
}

/**
 * The model of the page for coding assistants: the conversation's steps and the setup shown, and the code the
 * conversation ends with, read in [language].
 */
class AiViewModel(
    private val language: Language,
    private val content: ContentRepository
) : BaseViewModel<AiUiState, AiAction, AiEffect>(AiUiState()) {
    private var replay: Job? = null

    init {
        launch {
            val answers = loadState { answersOf(content.page(language, Route.Ai)?.codeByHeading().orEmpty()) }
            updateState { copy(answers = answers) }
        }
    }

    override fun handle(action: AiAction) {
        when (action) {
            is AiAction.Replay -> {
                replay?.cancel()
                updateState { copy(step = 0) }
                replay = launch {
                    repeat(action.steps - 1) {
                        delay(STEP_TIME)
                        updateState { copy(step = step + 1) }
                    }
                }
            }

            is AiAction.PickClient -> {
                updateState { copy(client = action.index) }
            }

            AiAction.ReadLlms -> {
                sendEffect(AiEffect.OpenUrl(LLMS_ADDRESS))
            }

            is AiAction.Open -> {
                sendEffect(AiEffect.OpenUrl(action.url))
            }
        }
    }

    private companion object {
        /** How long each step of the conversation waits before the next shows. */
        val STEP_TIME = 700.milliseconds

        /** The page's answers, which must all be there: a page without one fails rather than draw half a screen. */
        fun answersOf(code: Map<String, Block.Code>): Map<String, Block.Code> = code.also {
            check(it.keys.containsAll(listOf(ANSWER, ANSWER_TABLET, ANSWER_PHONE))) { "The page has no answer" }
        }
    }
}

/** A conversation that is not being played: every step of it shows. */
internal const val ALL_STEPS = Int.MAX_VALUE

/** The heading of the answer a desktop shows. */
internal const val ANSWER = "answer"

/** The heading of the answer a tablet shows, the call alone on two lines. */
internal const val ANSWER_TABLET = "answer-tablet"

/** The heading of the answer a phone shows, the call alone with an argument to a line. */
internal const val ANSWER_PHONE = "answer-phone"
