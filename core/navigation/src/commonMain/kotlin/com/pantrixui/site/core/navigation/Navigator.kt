package com.pantrixui.site.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * A back stack of screens, each named by a key of type [K]: the app's own record of where the user has been. The
 * last key is the screen on show. A [NavigationHistory] keeps a platform's own history, the browser's address bar
 * and its Back and Forward buttons, in step with it.
 *
 * Its keys are values the app chooses, usually a sealed hierarchy of routes; a key that compares equal is the same
 * screen. The screen on show may be at one of its parts, its [section], as a web page is at the heading its address
 * names after a `#`.
 *
 * @param initial the stack it opens with, oldest first; it has at least one key.
 * @param section the part of the last screen it opens at, or null for none.
 */
@Stable
class Navigator<K : Any>(
    initial: List<K>,
    section: String? = null
) {
    init {
        require(initial.isNotEmpty()) { "A back stack starts with a screen." }
    }

    /** The keys, oldest first: what a `NavDisplay` draws from. */
    val backStack: SnapshotStateList<K> = mutableStateListOf<K>().apply { addAll(initial) }

    /** The screen on show. */
    val current: K
        get() = backStack.last()

    /**
     * The part of the screen on show the user is at, as an address's `#symptom` names it, or null for none: a screen
     * with parts scrolls to it, and a browser shows it in the address. A screen opened, replaced or gone back to is at
     * none, unless one is asked for.
     */
    var section: String? by mutableStateOf(section)
        private set

    /** Whether there is a screen to go back to. */
    val canGoBack: Boolean
        get() = backStack.size > 1

    /**
     * Shows [key] on top of the screen on show, at its [section] if one is given. Showing the screen already on show
     * only moves it to the section given, and does nothing without one.
     */
    fun navigate(
        key: K,
        section: String? = null
    ) {
        if (current != key) {
            backStack.add(key)
            this.section = section
        } else if (section != null) {
            this.section = section
        }
    }

    /** Shows [key] in place of the screen on show, which is not kept to go back to. */
    fun replace(key: K) {
        if (current == key) return
        backStack[backStack.lastIndex] = key
        section = null
    }

    /** Goes back to the screen before; at the first screen it does nothing and says so by returning false. */
    fun back(): Boolean {
        if (!canGoBack) return false
        backStack.removeAt(backStack.lastIndex)
        section = null
        return true
    }

    /** Moves the screen on show to its part [section], or to none when it is null. */
    fun showSection(section: String?) {
        this.section = section
    }

    /** Leaves [key] as the only screen: what the stack is after a jump that has nothing to go back to. */
    fun resetTo(key: K) {
        setAll(listOf(key))
    }

    /** Puts [keys] in place of the whole stack, the last at its [section], as a platform's history asks. */
    internal fun setAll(
        keys: List<K>,
        section: String? = null
    ) {
        require(keys.isNotEmpty()) { "A back stack keeps a screen." }
        this.section = section
        if (backStack.toList() == keys) return
        backStack.clear()
        backStack.addAll(keys)
    }
}

/**
 * A [Navigator] that opens at [start] and is kept when the screen is set up again, as after an Android activity is
 * recreated: its keys are saved as JSON with [serializer].
 *
 * @param start the stack it opens with, oldest first.
 * @param serializer what writes a key as JSON and reads it back.
 * @param section the part of the last screen it opens at, or null for none. It is not kept with the keys.
 */
@Composable
fun <K : Any> rememberNavigator(
    start: List<K>,
    serializer: KSerializer<K>,
    section: String? = null
): Navigator<K> {
    val stackSerializer = ListSerializer(serializer)
    val saver = Saver<Navigator<K>, String>(
        save = { Json.encodeToString(stackSerializer, it.backStack.toList()) },
        restore = { Navigator(Json.decodeFromString(stackSerializer, it)) }
    )
    return rememberSaveable(saver = saver) { Navigator(start, section) }
}
