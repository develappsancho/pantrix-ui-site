package com.pantrixui.site.feature.references

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.base.loadState
import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.PlatformKind
import com.pantrixui.site.core.model.Reference
import com.pantrixui.site.core.routing.SiteRoutes

/**
 * The references page, as its screen draws it.
 *
 * @property projects the apps built with Pantrix UI, the site first, once they are read.
 * @property platform the platform the list of every app is narrowed to, or null for all of them.
 */
data class ReferencesUiState(
    val projects: LoadState<List<Reference>> = LoadState.Loading,
    val platform: PlatformKind? = null
) : UiState {
    /**
     * The apps the list shows: every one but the first, which the page features, of the [platform] where one is
     * set.
     */
    val listed: List<Reference>
        get() = when (val loaded = projects) {
            is LoadState.Loaded -> loaded.value.drop(1).filter { platform == null || it.runsOn(platform) }
            else -> emptyList()
        }
}

/** Whether this app runs on [kind]. */
fun Reference.runsOn(kind: PlatformKind): Boolean = platforms.any { it.kind == kind }

/** What the user does on the references page. */
sealed interface ReferencesAction : UiAction {
    /**
     * Opens [address]: an app's own, outside the site, or one of the site's pages, written without its language,
     * `/download`.
     */
    data class Open(val address: String) : ReferencesAction

    /** Narrows the list to the apps that run on [platform], or shows all of them where it is null. */
    data class PickPlatform(val platform: PlatformKind?) : ReferencesAction

    /** Reads the apps again, after they could not be read. */
    data object Retry : ReferencesAction
}

/** What the references page asks of the app. */
sealed interface ReferencesEffect : UiEffect {
    /** Opens the site's page at [path], in the page's language. */
    data class Navigate(val path: String) : ReferencesEffect

    /** Opens [url] outside the site. */
    data class OpenUrl(val url: String) : ReferencesEffect
}

/**
 * The model of the references page: it reads the apps built with Pantrix UI in [language] as it opens, and narrows
 * their list by platform.
 *
 * @param language the language of the page.
 * @param content where the apps are read from.
 */
class ReferencesViewModel(
    private val language: Language,
    private val content: ContentRepository
) : BaseViewModel<ReferencesUiState, ReferencesAction, ReferencesEffect>(ReferencesUiState()) {
    init {
        load()
    }

    override fun handle(action: ReferencesAction) {
        when (action) {
            is ReferencesAction.Open -> sendEffect(
                if (action.address.startsWith("/")) {
                    ReferencesEffect.Navigate(SiteRoutes.addressOf(SiteRoutes.routeOf(action.address), language))
                } else {
                    ReferencesEffect.OpenUrl(action.address)
                }
            )

            is ReferencesAction.PickPlatform -> updateState { copy(platform = action.platform) }

            ReferencesAction.Retry -> load()
        }
    }

    private fun load() {
        updateState { copy(projects = LoadState.Loading) }
        launch {
            val projects = loadState { content.references(language).projects }
            updateState { copy(projects = projects) }
        }
    }
}
