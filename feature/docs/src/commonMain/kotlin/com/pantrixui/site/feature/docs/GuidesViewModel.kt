package com.pantrixui.site.feature.docs

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.loadState
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * The model of the guides page: it reads the page's head and how long each guide takes to read in [language], and
 * keeps the filter and the chosen topic.
 *
 * @param language the language it is read in.
 * @param content where its content is read from.
 */
class GuidesViewModel(
    private val language: Language,
    private val content: ContentRepository
) : BaseViewModel<GuidesUiState, GuidesAction, DocEffect>(GuidesUiState()) {
    private var copiedReset: Job? = null

    init {
        load()
    }

    override fun handle(action: GuidesAction) {
        when (action) {
            is GuidesAction.Filter -> updateState { copy(query = action.query) }

            is GuidesAction.PickTopic -> updateState { copy(topic = action.topic) }

            GuidesAction.CopyPage -> currentState.page.valueOrNull?.let { page ->
                sendEffect(DocEffect.Copy(page.markdown ?: page.title))
            }

            is GuidesAction.Copied -> if (action.done) showCopied()

            is GuidesAction.OpenLink -> sendEffect(
                if (action.href.startsWith("/")) DocEffect.Navigate(action.href) else DocEffect.OpenUrl(action.href)
            )

            GuidesAction.Retry -> load()
        }
    }

    private fun showCopied() {
        copiedReset?.cancel()
        updateState { copy(copied = true) }
        copiedReset = launch {
            delay(COPIED_FOR)
            updateState { copy(copied = false) }
        }
    }

    private fun load() {
        updateState { copy(page = LoadState.Loading) }
        launch {
            val page = loadState { content.page(language, Route.Guides) }
            // A guide not written yet has no time to read: its card says nothing of it.
            val minutes = loadState { content.guideMinutes() }.valueOrNull.orEmpty()
            updateState { copy(page = page, minutes = minutes) }
        }
    }

    private companion object {
        /** How long the button says the page was copied. */
        val COPIED_FOR = 2.seconds
    }
}
