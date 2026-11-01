package com.pantrixui.site

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultPredictivePopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.pantrixui.site.core.model.Route

/**
 * The three ways one page gives way to the next, as the library's demo has them.
 *
 * @property forward a new page opening: the page that comes in and the one it covers.
 * @property back going back to the page below, by a button or a key.
 * @property swipeBack the same, by a finger, given the edge the gesture started from. The finger's progress plays it,
 *   not a clock.
 */
internal class PageTransitions(
    val forward: AnimatedContentTransitionScope<Scene<Route>>.() -> ContentTransform,
    val back: AnimatedContentTransitionScope<Scene<Route>>.() -> ContentTransform,
    val swipeBack: AnimatedContentTransitionScope<Scene<Route>>.(Int) -> ContentTransform
) {
    companion object {
        /**
         * Navigation 3's own for each platform: on iOS a page slides in from the side and a finger draws it back, on
         * Android it fades in, on the desktop and the web it changes in place.
         */
        val Default: PageTransitions = PageTransitions(
            forward = defaultTransitionSpec(),
            back = defaultPopTransitionSpec(),
            swipeBack = defaultPredictivePopTransitionSpec()
        )

        /** Nothing moves: in a wide window, and when the system asks for less motion. */
        val InPlace: PageTransitions = PageTransitions(
            forward = { inPlace() },
            back = { inPlace() },
            swipeBack = { inPlace() }
        )
    }
}

/** A change in place: the page that comes in appears, the one it replaces goes, neither moves. */
internal fun inPlace(): ContentTransform = ContentTransform(EnterTransition.None, ExitTransition.None)

/**
 * The parts of the site open as tabs do: they appear in place rather than slide over the page before. A page's own
 * data decides how it comes in and goes; these say in place.
 */
internal val SectionSwitch: Map<String, Any> =
    NavDisplay.transitionSpec { inPlace() } +
        NavDisplay.popTransitionSpec { inPlace() } +
        NavDisplay.predictivePopTransitionSpec { inPlace() }

/**
 * The page on top of [backStack], drawn by Navigation 3's [NavDisplay] with the ways of [transitions].
 *
 * [NavDisplay] takes three transitions and decides which one to ask as a change starts. A change cut short leaves that
 * choice stale: when a finger goes back while a page is still coming in, the page leaving is asked of the forward
 * transition again, and instead of following the finger it slides the other way, like the page covered by a forward
 * one. So the transition given as the forward one looks at what it is asked: when the page gone to is under the page
 * left, it is a way back, and it answers as going back does, or as a finger does while a back gesture is under way.
 */
@Composable
internal fun PageDisplay(
    backStack: List<Route>,
    transitions: PageTransitions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    entryDecorators: List<NavEntryDecorator<Route>>,
    entryProvider: (Route) -> NavEntry<Route>
) {
    // Where a back gesture is now. It is read each time a transition is asked, not kept from an earlier answer.
    val gesture = checkNotNull(LocalNavigationEventDispatcherOwner.current).navigationEventDispatcher.transitionState
    val forwardOrBack: AnimatedContentTransitionScope<Scene<Route>>.() -> ContentTransform =
        remember(transitions, gesture) {
            {
                val swipe = gesture.value as? NavigationEventTransitionState.InProgress
                when {
                    !targetState.isUnder(initialState) -> transitions.forward(this)
                    swipe != null -> transitions.swipeBack(this, swipe.latestEvent.swipeEdge)
                    else -> transitions.back(this)
                }
            }
        }
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = onBack,
        entryDecorators = entryDecorators,
        transitionSpec = forwardOrBack,
        popTransitionSpec = transitions.back,
        predictivePopTransitionSpec = transitions.swipeBack,
        entryProvider = entryProvider
    )
}

/** Whether this scene's page is one of the pages under [other]'s: whether going from [other] to here is going back. */
private fun Scene<Route>.isUnder(other: Scene<Route>): Boolean {
    val page = entries.lastOrNull()?.contentKey
    return other.previousEntries.any { it.contentKey == page }
}
