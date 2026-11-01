package com.pantrixui.site.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow

/**
 * What [BrowserNavigationHistory] needs of a browser: the address of the entry on show, the entries it adds and
 * rewrites, and the entries the user goes back and forward to. Each entry carries an id, the app's record of which
 * screen of its stack it is.
 */
internal interface BrowserLocation {
    /** The address on show: its path, its query and its fragment. */
    val address: String

    /** The id the entry on show was given, or null for one the app never wrote. */
    val entryId: Int?

    /** Adds an entry after the one on show, which drops the entries forward of it. */
    fun push(
        id: Int,
        address: String
    )

    /** Rewrites the entry on show. */
    fun replace(
        id: Int,
        address: String
    )

    /** Goes [steps] entries forward, or back when it is negative; the browser answers with [onPop]. */
    fun go(steps: Int)

    /** Calls [listener] with the entry's id and address when the user goes back or forward; returns how to stop. */
    fun onPop(listener: (id: Int?, address: String) -> Unit): () -> Unit
}

/**
 * A browser's history kept in step with a [Navigator], by the History API: each screen the app opens is an entry with
 * the screen's own address (`/docs/installation`, not `#installation`), and the browser's Back and Forward buttons
 * move the app's stack.
 *
 * - **Opening.** The stack opens with the screen the address names ([keyOf]); the address is the page's own, so a
 *   link to it can be shared and a reload opens it again.
 * - **A part of a page.** The navigator's section is the address's fragment, `#symptom`: an address that names one
 *   opens the page at it, a section the app moves to is written in the entry on show, without an entry of its own,
 *   and an entry gone back or forward to opens at the section its address names.
 * - **The app's back.** The app going back goes back in the browser too, so that its Forward button returns.
 * - **Another language.** An entry written in another language shows the screen in the app's, and its address follows.
 *
 * @param keyOf the key an address names; an address the app has no screen for is the app's "not found" key.
 */
class BrowserNavigationHistory<K : Any> internal constructor(
    private val browser: BrowserLocation,
    private val keyOf: (String) -> K
) : NavigationHistory<K> {
    private val sync = HistorySync(browser, keyOf)

    override fun startStack(default: List<K>): List<K> = listOf(keyOf(browser.address))

    override fun startSection(): String? = sectionOf(browser.address)

    @Composable
    override fun Bind(
        navigator: Navigator<K>,
        pathOf: (K) -> String
    ) {
        val currentPathOf by rememberUpdatedState(pathOf)
        val addresses = remember { { key: K -> currentPathOf(key) } }
        DisposableEffect(navigator) {
            sync.attach(navigator.backStack.toList(), navigator.section, addresses)
            val stop = browser.onPop { id, address -> sync.onPop(id, address, navigator) }
            onDispose { stop() }
        }
        LaunchedEffect(navigator) {
            // The stack and the section in one: a screen opened at a part of it is one entry, written with its part.
            snapshotFlow { navigator.backStack.toList() to navigator.section }
                .collect { (stack, section) -> sync.onStackChanged(stack, section) }
        }
        LaunchedEffect(pathOf) {
            sync.rewriteAddress()
        }
    }
}

/** The bookkeeping of [BrowserNavigationHistory]: which entry of the browser each key of the stack is. */
internal class HistorySync<K : Any>(
    private val browser: BrowserLocation,
    private val keyOf: (String) -> K
) {
    private var keys: List<K> = emptyList()
    private var section: String? = null
    private var ids: List<Int> = emptyList()
    private var nextId = 0
    private var pathOf: (K) -> String = { "" }

    /** Takes [stack], at [section], as what the browser's entry on show is, and gives the entry an id. */
    fun attach(
        stack: List<K>,
        section: String?,
        pathOf: (K) -> String
    ) {
        this.pathOf = pathOf
        val id = browser.entryId ?: 0
        nextId = id + 1
        keys = stack
        this.section = section
        // Only the screen on show is an entry the browser has; older keys, from a stack the app restored, are not.
        ids = List(stack.size - 1) { NOT_AN_ENTRY } + id
        browser.replace(id, addressOf(stack.last(), section))
    }

    /** The app changed its stack to [stack], the last screen at [section]: the browser's history follows. */
    fun onStackChanged(
        stack: List<K>,
        section: String? = null
    ) {
        if (stack.isEmpty() || (stack == keys && section == this.section)) return
        val moved = stack == keys
        val shared = keys.zip(stack).takeWhile { (old, new) -> old == new }.size
        this.section = section
        when {
            // The screen on show moved to another of its parts: the entry is written again, not added.
            moved -> {
                browser.replace(ids.last(), addressOf(stack.last(), section))
            }

            // A screen opened on top.
            stack.size == keys.size + 1 && shared == keys.size -> {
                val id = nextId++
                browser.push(id, addressOf(stack.last(), section))
                ids = ids + id
            }

            // Back: the browser goes back as many entries, and its answer finds the stack already there.
            stack.size < keys.size && shared == stack.size && ids[stack.lastIndex] != NOT_AN_ENTRY -> {
                val steps = keys.size - stack.size
                ids = ids.take(stack.size)
                keys = stack
                browser.go(-steps)
                return
            }

            // The screen on show replaced.
            stack.size == keys.size && shared == keys.size - 1 -> {
                browser.replace(ids.last(), addressOf(stack.last(), section))
            }

            // Any other jump is a new entry, with nothing of the browser's behind it.
            else -> {
                val id = nextId++
                browser.push(id, addressOf(stack.last(), section))
                ids = List(stack.size - 1) { NOT_AN_ENTRY } + id
            }
        }
        keys = stack
    }

    /** The user went back or forward to the entry [id], at [address]: the app's stack follows. */
    fun onPop(
        id: Int?,
        address: String,
        navigator: Navigator<K>
    ) {
        val index = if (id == null) -1 else ids.indexOf(id)
        when {
            index >= 0 -> {
                keys = keys.take(index + 1)
                ids = ids.take(index + 1)
            }

            id != null && id > (ids.lastOrNull() ?: NOT_AN_ENTRY) -> {
                keys = keys + keyOf(address)
                ids = ids + id
                nextId = maxOf(nextId, id + 1)
            }

            else -> {
                val entry = id ?: nextId++
                keys = listOf(keyOf(address))
                ids = listOf(entry)
            }
        }
        section = sectionOf(address)
        navigator.setAll(keys, section)
        val expected = addressOf(keys.last(), section)
        if (address != expected || id == null) browser.replace(ids.last(), expected)
    }

    /** The addresses changed, as the app's language did: the entry on show is written again. */
    fun rewriteAddress() {
        if (keys.isEmpty()) return
        browser.replace(ids.last(), addressOf(keys.last(), section))
    }

    /** The address of [key], at its part [section] where there is one. */
    private fun addressOf(
        key: K,
        section: String?
    ): String = if (section == null) pathOf(key) else "${pathOf(key)}#$section"

    private companion object {
        /** The id of a key the browser has no entry for. */
        const val NOT_AN_ENTRY = -1
    }
}

/** The part of a page [address] names after a `#`, or null where it names none. */
internal fun sectionOf(address: String): String? = address.substringAfter('#', "").ifEmpty { null }
