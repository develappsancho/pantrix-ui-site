package com.pantrixui.site.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
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
 * screen.
 *
 * @param initial the stack it opens with, oldest first; it has at least one key.
 */
@Stable
class Navigator<K : Any>(initial: List<K>) {
    init {
        require(initial.isNotEmpty()) { "A back stack starts with a screen." }
    }

    /** The keys, oldest first: what a `NavDisplay` draws from. */
    val backStack: SnapshotStateList<K> = mutableStateListOf<K>().apply { addAll(initial) }

    /** The screen on show. */
    val current: K
        get() = backStack.last()

    /** Whether there is a screen to go back to. */
    val canGoBack: Boolean
        get() = backStack.size > 1

    /** Shows [key] on top of the screen on show; showing the screen already on show does nothing. */
    fun navigate(key: K) {
        if (current != key) backStack.add(key)
    }

    /** Shows [key] in place of the screen on show, which is not kept to go back to. */
    fun replace(key: K) {
        if (current != key) backStack[backStack.lastIndex] = key
    }

    /** Goes back to the screen before; at the first screen it does nothing and says so by returning false. */
    fun back(): Boolean {
        if (!canGoBack) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    /** Leaves [key] as the only screen: what the stack is after a jump that has nothing to go back to. */
    fun resetTo(key: K) {
        setAll(listOf(key))
    }

    /** Puts [keys] in place of the whole stack, as a platform's history asks. */
    internal fun setAll(keys: List<K>) {
        require(keys.isNotEmpty()) { "A back stack keeps a screen." }
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
 */
@Composable
fun <K : Any> rememberNavigator(
    start: List<K>,
    serializer: KSerializer<K>
): Navigator<K> {
    val stackSerializer = ListSerializer(serializer)
    val saver = Saver<Navigator<K>, String>(
        save = { Json.encodeToString(stackSerializer, it.backStack.toList()) },
        restore = { Navigator(Json.decodeFromString(stackSerializer, it)) }
    )
    return rememberSaveable(saver = saver) { Navigator(start) }
}
