package com.pantrixui.site.feature.components

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.loadState
import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.model.Language

/**
 * The model of the components page: it reads the library's components in [language] and keeps what the reader
 * filters them by.
 *
 * @param language the language they are read in.
 * @param content where they are read from.
 */
class ComponentsViewModel(
    private val language: Language,
    private val content: ContentRepository
) : BaseViewModel<ComponentsUiState, ComponentsAction, ComponentsEffect>(ComponentsUiState()) {
    init {
        load()
    }

    override fun handle(action: ComponentsAction) {
        when (action) {
            is ComponentsAction.Filter -> updateState { copy(query = action.query) }

            is ComponentsAction.PickGroup -> updateState { copy(group = action.key) }

            is ComponentsAction.OpenLink -> sendEffect(
                if (action.href.startsWith("/")) {
                    ComponentsEffect.Navigate(action.href)
                } else {
                    ComponentsEffect.OpenUrl(action.href)
                }
            )

            ComponentsAction.Retry -> load()
        }
    }

    private fun load() {
        updateState { copy(index = LoadState.Loading) }
        launch {
            val index = loadState { content.componentIndex(language) }
            updateState { copy(index = index) }
        }
    }
}
