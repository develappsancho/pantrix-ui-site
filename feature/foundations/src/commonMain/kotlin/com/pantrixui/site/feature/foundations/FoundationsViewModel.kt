package com.pantrixui.site.feature.foundations

import com.pantrixui.site.core.base.BaseViewModel

/** The model of the foundations page: which colours it shows and where the motion's dots stand. */
class FoundationsViewModel : BaseViewModel<FoundationsUiState, FoundationsAction, FoundationsEffect>(
    FoundationsUiState()
) {
    override fun handle(action: FoundationsAction) {
        when (action) {
            is FoundationsAction.ShowDark -> updateState { copy(dark = action.dark) }
            FoundationsAction.Play -> updateState { copy(moved = !moved) }
        }
    }
}
