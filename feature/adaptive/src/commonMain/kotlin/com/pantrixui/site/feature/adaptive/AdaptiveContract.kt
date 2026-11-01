package com.pantrixui.site.feature.adaptive

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.base.loadState
import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.codeByHeading

/**
 * The adaptive page, as its screen draws it.
 *
 * @property device the window the simulator draws its app in.
 * @property app the app it draws there.
 * @property code the code the page shows, by the anchor of its heading in the page's content.
 */
data class AdaptiveUiState(
    val device: SimDevice = SimDevice.Laptop,
    val app: SimApp = SimApp.Inbox,
    val code: LoadState<Map<String, Block.Code>> = LoadState.Loading
) : UiState

/** The apps the simulator draws. */
enum class SimApp {
    /** A shop's inbox: navigation, a list and its detail, and a history beside them where there is room. */
    Inbox,

    /** A video call: the picture and its controls, which a foldable on a table parts at its fold. */
    VideoCall
}

/** What the user does on the adaptive page. */
sealed interface AdaptiveAction : UiAction {
    /** Draws the simulator's app in [device]'s window. */
    data class PickDevice(val device: SimDevice) : AdaptiveAction

    /** Draws [app] in the simulator. */
    data class PickApp(val app: SimApp) : AdaptiveAction
}

/** The adaptive page asks nothing of the platform: its links are the site's frame's. */
sealed interface AdaptiveEffect : UiEffect

/**
 * The model of the adaptive page: the window and the app its simulator draws, and the code its sections show, read in
 * [language].
 *
 * @param language the language the code's comments are read in.
 * @param content where the code is read from.
 */
class AdaptiveViewModel(
    private val language: Language,
    private val content: ContentRepository
) : BaseViewModel<AdaptiveUiState, AdaptiveAction, AdaptiveEffect>(AdaptiveUiState()) {
    init {
        launch {
            val code = loadState { codeOf(content.page(language, Route.Adaptive)) }
            updateState { copy(code = code) }
        }
    }

    override fun handle(action: AdaptiveAction) {
        when (action) {
            is AdaptiveAction.PickDevice -> updateState { copy(device = action.device) }
            is AdaptiveAction.PickApp -> updateState { copy(app = action.app) }
        }
    }

    internal companion object {
        /** The code of [page]: the block under each of its headings, by the heading's anchor. */
        fun codeOf(page: Page?): Map<String, Block.Code> = page?.codeByHeading().orEmpty()
            .also { check(it.isNotEmpty()) { "The adaptive page has no code" } }
    }
}
