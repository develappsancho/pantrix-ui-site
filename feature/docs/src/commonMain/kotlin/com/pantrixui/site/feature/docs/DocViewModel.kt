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
 * The model of a page of the documentation: it reads the page of [route] in [language] and answers what the user does
 * on it.
 *
 * @param route the page.
 * @param language the language it is read in.
 * @param content where its content is read from.
 */
class DocViewModel(
    private val route: Route,
    private val language: Language,
    private val content: ContentRepository
) : BaseViewModel<DocUiState, DocAction, DocEffect>(DocUiState()) {
    private var copiedReset: Job? = null

    init {
        load()
    }

    override fun handle(action: DocAction) {
        when (action) {
            DocAction.CopyPage -> currentState.page.valueOrNull?.let { page ->
                sendEffect(DocEffect.Copy(page.markdown ?: page.title))
            }

            is DocAction.Copied -> if (action.done) showCopied()

            is DocAction.OpenLink -> sendEffect(
                if (action.href.startsWith("/")) DocEffect.Navigate(action.href) else DocEffect.OpenUrl(action.href)
            )

            DocAction.Retry -> load()
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
            val page = loadState { content.page(language, route) }
            updateState { copy(page = page) }
        }
    }

    private companion object {
        /** How long the button says the page was copied. */
        val COPIED_FOR = 2.seconds
    }
}
