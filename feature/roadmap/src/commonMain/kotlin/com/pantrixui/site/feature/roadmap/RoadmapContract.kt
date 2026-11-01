package com.pantrixui.site.feature.roadmap

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState

/**
 * The roadmap, as its screen draws it.
 *
 * @property filter the stage whose column the board shows alone, or null for all of them.
 * @property sample the description the server-driven part shows.
 */
data class RoadmapUiState(
    val filter: Stage? = null,
    val sample: Sample = Sample.Campaign
) : UiState

/** What the user does on the roadmap. */
sealed interface RoadmapAction : UiAction {
    /** Shows the column of [stage] alone, or every column for null. */
    data class PickFilter(val stage: Stage?) : RoadmapAction

    /** Shows the description [sample]. */
    data class PickSample(val sample: Sample) : RoadmapAction

    /** Opens [href]: a path of the site, or an address outside it. */
    data class OpenLink(val href: String) : RoadmapAction
}

/** What the roadmap asks of the platform. */
sealed interface RoadmapEffect : UiEffect {
    /** Opens the site's page at [path]. */
    data class Navigate(val path: String) : RoadmapEffect

    /** Opens [url] outside the site. */
    data class OpenUrl(val url: String) : RoadmapEffect
}

/** The model of the roadmap: the board's filter and the sample description on show. */
class RoadmapViewModel : BaseViewModel<RoadmapUiState, RoadmapAction, RoadmapEffect>(RoadmapUiState()) {
    override fun handle(action: RoadmapAction) {
        when (action) {
            is RoadmapAction.PickFilter -> updateState { copy(filter = action.stage) }

            is RoadmapAction.PickSample -> updateState { copy(sample = action.sample) }

            is RoadmapAction.OpenLink -> sendEffect(
                if (action.href.startsWith(
                        "/"
                    )
                ) {
                    RoadmapEffect.Navigate(action.href)
                } else {
                    RoadmapEffect.OpenUrl(action.href)
                }
            )
        }
    }
}
