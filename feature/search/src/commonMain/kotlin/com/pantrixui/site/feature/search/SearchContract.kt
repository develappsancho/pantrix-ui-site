package com.pantrixui.site.feature.search

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.base.loadState
import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.SearchIndex
import com.pantrixui.site.core.model.SearchKind
import kotlinx.coroutines.Job

/**
 * The search, as its palette draws it.
 *
 * @property query what is typed.
 * @property kind the kind of results shown alone, or null for every kind.
 * @property language the language the results are searched in: the site's, unless the reader picks the other.
 * @property selected the place of the result the arrows are on, among those shown.
 * @property index what the search finds in [language].
 */
data class SearchUiState(
    val query: String = "",
    val kind: SearchKind? = null,
    val language: Language = Language.English,
    val selected: Int = 0,
    val index: LoadState<SearchIndex> = LoadState.Loading
) : UiState

/** What the reader does in the search. */
sealed interface SearchAction : UiAction {
    /** Opens the search afresh, nothing typed and every kind shown, its results in [language]: the site's. */
    data class Open(val language: Language) : SearchAction

    /** Types [query]. */
    data class Type(val query: String) : SearchAction

    /** Shows the results of [kind] alone, or of every kind for null. */
    data class PickKind(val kind: SearchKind?) : SearchAction

    /** Searches the pages in [language]. */
    data class PickLanguage(val language: Language) : SearchAction

    /** Moves the arrows by [by] among the [shown] results, stopping at the first and the last. */
    data class Move(
        val by: Int,
        val shown: Int
    ) : SearchAction

    /** Puts the arrows on the result at [place], as a pointer over it does. */
    data class Point(val place: Int) : SearchAction
}

/** The search asks nothing of the platform: its palette opens a result and closes itself. */
sealed interface SearchEffect : UiEffect

/** The model of the search, opened in [language]: what is typed, what is shown, and what it finds. */
class SearchViewModel(
    language: Language,
    private val content: ContentRepository
) : BaseViewModel<SearchUiState, SearchAction, SearchEffect>(SearchUiState(language = language)) {
    private var loading: Job? = null

    init {
        load(language)
    }

    override fun handle(action: SearchAction) {
        when (action) {
            is SearchAction.Open -> {
                updateState { copy(query = "", kind = null, selected = 0) }
                handle(SearchAction.PickLanguage(action.language))
            }

            is SearchAction.Type -> {
                updateState { copy(query = action.query, selected = 0) }
            }

            is SearchAction.PickKind -> {
                updateState { copy(kind = action.kind, selected = 0) }
            }

            is SearchAction.PickLanguage -> {
                if (action.language != state.value.language) {
                    updateState { copy(language = action.language, selected = 0, index = LoadState.Loading) }
                    load(action.language)
                }
            }

            is SearchAction.Move -> {
                updateState {
                    copy(selected = (selected + action.by).coerceIn(0, (action.shown - 1).coerceAtLeast(0)))
                }
            }

            is SearchAction.Point -> {
                updateState { copy(selected = action.place) }
            }
        }
    }

    private fun load(language: Language) {
        loading?.cancel()
        loading = launch {
            val index = loadState { content.searchIndex(language) }
            updateState { if (this.language == language) copy(index = index) else this }
        }
    }
}
