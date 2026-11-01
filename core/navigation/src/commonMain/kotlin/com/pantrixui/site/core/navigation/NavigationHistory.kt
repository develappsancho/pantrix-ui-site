package com.pantrixui.site.core.navigation

import androidx.compose.runtime.Composable

/**
 * A platform's own history of screens, kept in step with a [Navigator]: in a browser, the address bar and the Back and
 * Forward buttons. The app sets it up once, at its root, and binds its navigator to it with [Bind]; from then on a
 * screen the app opens is an entry of the platform's history, and an entry the user goes back or forward to is the
 * screen the app shows.
 *
 * A platform with no history of its own is given [NoNavigationHistory].
 */
interface NavigationHistory<K : Any> {
    /** The stack the app opens with: in a browser, the screen its address names; elsewhere, [default]. */
    fun startStack(default: List<K>): List<K>

    /** The part of the first screen the app opens at: in a browser, the one its address names after a `#`. */
    fun startSection(): String? = null

    /**
     * Keeps the platform's history in step with [navigator] for as long as it is in the composition.
     *
     * @param navigator the app's back stack.
     * @param pathOf the address of a key. A new function, as when the app's language changes, writes the screen on
     *   show's address again: the caller remembers it for as long as the addresses it gives stay the same.
     */
    @Composable
    fun Bind(
        navigator: Navigator<K>,
        pathOf: (K) -> String
    )
}

/** The history of a platform that keeps none of its own: the app's back stack is the whole of it. */
class NoNavigationHistory<K : Any> : NavigationHistory<K> {
    override fun startStack(default: List<K>): List<K> = default

    @Composable
    override fun Bind(
        navigator: Navigator<K>,
        pathOf: (K) -> String
    ) = Unit
}
