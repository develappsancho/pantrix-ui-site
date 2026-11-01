package com.pantrixui.site.feature.docs

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
 * The model of the changelog page: it reads the library's changelog in [language] and keeps what the reader filters
 * it by.
 *
 * @param language the language it is read in.
 * @param content where it is read from.
 */
class ChangelogViewModel(
    private val language: Language,
    private val content: ContentRepository
) : BaseViewModel<ChangelogUiState, ChangelogAction, DocEffect>(ChangelogUiState()) {
    private var copiedReset: Job? = null

    init {
        load()
    }

    override fun handle(action: ChangelogAction) {
        when (action) {
            is ChangelogAction.PickModule -> updateState { copy(module = action.key) }

            ChangelogAction.ToggleBreaking -> updateState { copy(breakingOnly = !breakingOnly) }

            ChangelogAction.CopyPage -> currentState.changelog.valueOrNull?.let { changelog ->
                sendEffect(DocEffect.Copy(changelog.markdown))
            }

            is ChangelogAction.Copied -> if (action.done) showCopied()

            is ChangelogAction.OpenLink -> sendEffect(
                if (action.href.startsWith("/")) DocEffect.Navigate(action.href) else DocEffect.OpenUrl(action.href)
            )

            ChangelogAction.Retry -> load()
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
        updateState { copy(changelog = LoadState.Loading) }
        launch {
            val changelog = loadState { content.changelog(language) }
            updateState { copy(changelog = changelog) }
        }
    }

    private companion object {
        /** How long the button says the page was copied. */
        val COPIED_FOR = 2.seconds
    }
}
