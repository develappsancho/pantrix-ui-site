package com.pantrixui.site.feature.components

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.loadState
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.model.Language
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * The model of a component's page: it reads the page of the component [key] in [language] and keeps which of its
 * forms the reader looks at.
 *
 * @param key the key of the component's page.
 * @param language the language it is read in.
 * @param content where it is read from.
 */
class ComponentViewModel(
    private val key: String,
    private val language: Language,
    private val content: ContentRepository
) : BaseViewModel<ComponentUiState, ComponentAction, ComponentsEffect>(ComponentUiState()) {
    private var copiedReset: Job? = null

    init {
        load()
    }

    override fun handle(action: ComponentAction) {
        when (action) {
            is ComponentAction.PickForm -> updateState { copy(form = action.index) }

            ComponentAction.ToggleParameters -> updateState { copy(allParameters = !allParameters) }

            ComponentAction.CopyPage -> currentState.page.valueOrNull?.let { page ->
                sendEffect(ComponentsEffect.Copy(ComponentMarkdown.of(page)))
            }

            is ComponentAction.Copied -> if (action.done) showCopied()

            is ComponentAction.OpenLink -> sendEffect(
                if (action.href.startsWith("/")) {
                    ComponentsEffect.Navigate(action.href)
                } else {
                    ComponentsEffect.OpenUrl(action.href)
                }
            )

            ComponentAction.Retry -> load()
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
            val page = loadState { content.component(language, key) }
            updateState { copy(page = page) }
        }
    }

    private companion object {
        /** How long the button says the page was copied. */
        val COPIED_FOR = 2.seconds
    }
}
