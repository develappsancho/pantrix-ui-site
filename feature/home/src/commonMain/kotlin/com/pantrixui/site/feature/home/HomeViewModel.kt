package com.pantrixui.site.feature.home

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.loadState
import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Route
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * The model of the landing page: it reads the code the page shows in [language] and answers what the user does on
 * it.
 *
 * @param language the language the page is read in.
 * @param content where its code is read from.
 */
class HomeViewModel(
    private val language: Language,
    private val content: ContentRepository
) : BaseViewModel<HomeUiState, HomeAction, HomeEffect>(HomeUiState()) {
    private var copiedReset: Job? = null

    init {
        launch {
            val code = loadState { codeOf(content.page(language, Route.Home)) }
            updateState { copy(code = code) }
        }
    }

    override fun handle(action: HomeAction) {
        when (action) {
            is HomeAction.PickPlatform -> updateState { copy(platform = action.type) }

            is HomeAction.PickCodeTab -> updateState { copy(codeTab = action.index) }

            HomeAction.CopyInstall -> sendEffect(HomeEffect.Copy(HomeSay.INSTALL))

            is HomeAction.Copied -> if (action.done) showCopied()

            is HomeAction.OpenLink -> sendEffect(
                if (action.href.startsWith("/")) HomeEffect.Navigate(action.href) else HomeEffect.OpenUrl(action.href)
            )
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

    internal companion object {
        /** How long the button says the line was copied. */
        private val COPIED_FOR = 2.seconds

        /**
         * The code of [page], the landing page's own: the block under each of its headings, by the heading's anchor.
         * A page without one of them is a content tool that wrote the page wrong, and fails.
         */
        fun codeOf(page: Page?): HomeCode {
            val blocks = page?.blocks.orEmpty()

            fun under(anchor: String): Block.Code {
                val heading = blocks.indexOfFirst { it is Block.Heading && it.anchor == anchor }
                return blocks.drop(heading + 1).filterIsInstance<Block.Code>().firstOrNull()
                    ?.takeIf { heading >= 0 }
                    ?: error("The landing page has no code under #$anchor")
            }
            return HomeCode(
                form = under("review-form"),
                wideForm = under("review-form-wide"),
                shortForm = under("review-form-short"),
                preset = under("preset")
            )
        }
    }
}
