package com.pantrixui.site

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.model.Language

/**
 * The model of the site's frame: the language, the theme and the menu.
 *
 * @param language the language the site opens in: the address's on the web, the system's elsewhere.
 */
class SiteViewModel(language: Language) : BaseViewModel<SiteUiState, SiteAction, SiteEffect>(SiteUiState(language)) {
    override fun handle(action: SiteAction) {
        when (action) {
            is SiteAction.ToggleTheme -> updateState { copy(dark = !(dark ?: action.systemDark)) }
            is SiteAction.ChooseLanguage -> updateState { copy(language = action.language) }
            SiteAction.OpenMenu -> updateState { copy(menuOpen = true) }
            SiteAction.CloseMenu -> updateState { copy(menuOpen = false) }
        }
    }
}
