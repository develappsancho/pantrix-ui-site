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
 * rewrites, and the entries the user goes back and forward to. Each entry carries the app's record of it, an
 * [EntryState]: the screens the app's stack held when it was written.
 */
internal interface BrowserLocation {
    /** The address on show: its path, its query and its fragment. */
    val address: String

    /** What the app wrote in the entry on show, or null for one the app never wrote. */
    val entry: EntryState?

    /** Adds an entry after the one on show, which drops the entries forward of it. */
    fun push(
        state: EntryState,
        address: String
    )

    /** Rewrites the entry on show. */
    fun replace(
        state: EntryState,
        address: String
    )

    /** Goes [steps] entries forward, or back when it is negative; the browser answers with [onPop]. */
    fun go(steps: Int)

    /** Calls [listener] with the entry's state and address when the user goes back or forward; returns how to stop. */
    fun onPop(listener: (state: EntryState?, address: String) -> Unit): () -> Unit
}

/**
 * The app's record of one entry of the browser's history: the screens of its stack when the entry was written, oldest
 * first, the last the entry's own.
 *
 * @property pages each screen's address and the id of the browser's entry it is, or [NOT_AN_ENTRY] for a screen the
 *   browser has no entry of.
 */
internal data class EntryState(val pages: List<Page>) {
    init {
        require(pages.isNotEmpty()) { "An entry is a screen." }
    }

    /** The id of the entry itself. */
    val id: Int
        get() = pages.last().id

    /**
     * One screen of the stack.
     *
     * @property id the id of the browser's entry it is, or [NOT_AN_ENTRY].
     * @property address its address, at its part where it is the entry's own screen.
     */
    data class Page(
        val id: Int,
        val address: String
    )

    /** The state as the browser keeps it: a line for each screen, its id and its address. */
    fun write(): String = pages.joinToString("\n") { "${it.id} ${it.address}" }

    companion object {
        /** The state [text] is, as [write] writes it, or null for any other text. */
        fun read(text: String): EntryState? {
            val pages = text.lines().map { line ->
                val id = line.substringBefore(' ').toIntOrNull()
                val address = line.substringAfter(' ', "")
                if (id == null || address.isEmpty()) null else Page(id, address)
            }
            return if (null in pages) null else EntryState(pages.filterNotNull())
        }
    }
}

/** The id of a screen the browser has no entry of, as one below a page a jump opened. */
internal const val NOT_AN_ENTRY = -1

/**
 * A browser's history kept in step with a [Navigator], by the History API: each screen the app opens is an entry with
 * the screen's own address (`/docs/installation`, not `#installation`), and the browser's Back and Forward buttons
 * move the app's stack.
 *
 * - **Opening.** The stack opens with the screen the address names ([keyOf]); the address is the page's own, so a
 *   link to it can be shared.
 * - **Each entry its stack.** An entry keeps the whole stack it was written with. A reload opens that stack again, and
 *   an entry gone back or forward to shows the stack it had, so the app's own back goes on where the browser's would.
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

    override fun startStack(default: List<K>): List<K> = sync.startStack()

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
            val stop = browser.onPop { state, address -> sync.onPop(state, address, navigator) }
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

    /** The stack the entry on show was written with, its last screen the one its address names. */
    fun startStack(): List<K> {
        val below = browser.entry?.pages?.dropLast(1).orEmpty()
        return below.map { keyOf(it.address) } + keyOf(browser.address)
    }

    /** Takes [stack], at [section], as what the browser's entry on show is, and gives the entry an id. */
    fun attach(
        stack: List<K>,
        section: String?,
        pathOf: (K) -> String
    ) {
        this.pathOf = pathOf
        val entry = browser.entry
        val id = entry?.id ?: 0
        nextId = (entry?.pages.orEmpty().maxOfOrNull { it.id } ?: id) + 1
        keys = stack
        this.section = section
        // The entry's record names the entries of the screens below, as after a reload. Of a stack it does not
        // describe, as one the app restored itself, only the screen on show is an entry the browser has.
        val recorded = entry?.pages?.takeIf { it.size == stack.size }?.map { it.id }
        ids = recorded ?: (List(stack.size - 1) { NOT_AN_ENTRY } + id)
        browser.replace(state(), addressOf(stack.last(), section))
    }

    /** The app changed its stack to [stack], the last screen at [section]: the browser's history follows. */
    fun onStackChanged(
        stack: List<K>,
        section: String? = null
    ) {
        if (stack.isEmpty() || (stack == keys && section == this.section)) return
        val before = keys
        val shared = before.zip(stack).takeWhile { (old, new) -> old == new }.size
        keys = stack
        this.section = section
        when {
            // The screen on show moved to another of its parts: the entry is written again, not added.
            stack == before -> {
                browser.replace(state(), addressOf(stack.last(), section))
            }

            // A screen opened on top.
            stack.size == before.size + 1 && shared == before.size -> {
                ids = ids + nextId++
                browser.push(state(), addressOf(stack.last(), section))
            }

            // Back: the browser goes back as many entries, and its answer finds the stack already there.
            stack.size < before.size && shared == stack.size && ids[stack.lastIndex] != NOT_AN_ENTRY -> {
                ids = ids.take(stack.size)
                browser.go(stack.size - before.size)
            }

            // The screen on show replaced.
            stack.size == before.size && shared == before.size - 1 -> {
                browser.replace(state(), addressOf(stack.last(), section))
            }

            // Any other jump is a new entry, with nothing of the browser's behind it.
            else -> {
                ids = List(stack.size - 1) { NOT_AN_ENTRY } + nextId++
                browser.push(state(), addressOf(stack.last(), section))
            }
        }
    }

    /**
     * The user went back or forward to the entry of [state], at [address]: the app's stack becomes the one the entry
     * was written with. An entry the app never wrote, as an address typed in the bar, is its screen alone.
     */
    fun onPop(
        state: EntryState?,
        address: String,
        navigator: Navigator<K>
    ) {
        if (state == null) {
            keys = listOf(keyOf(address))
            ids = listOf(nextId++)
        } else {
            keys = state.pages.dropLast(1).map { keyOf(it.address) } + keyOf(address)
            ids = state.pages.map { it.id }
            nextId = maxOf(nextId, ids.max() + 1)
        }
        section = sectionOf(address)
        navigator.setAll(keys, section)
        // An entry written in another language, or never written by the app, is written as the app has it now.
        val expected = addressOf(keys.last(), section)
        val written = state()
        if (address != expected || state != written) browser.replace(written, expected)
    }

    /** The addresses changed, as the app's language did: the entry on show is written again. */
    fun rewriteAddress() {
        if (keys.isEmpty()) return
        browser.replace(state(), addressOf(keys.last(), section))
    }

    /** The record of the entry on show: each key's id and address, the last at its part. */
    private fun state(): EntryState =
        EntryState(
            keys.mapIndexed { index, key ->
                EntryState.Page(ids[index], if (index == keys.lastIndex) addressOf(key, section) else pathOf(key))
            }
        )

    /** The address of [key], at its part [section] where there is one. */
    private fun addressOf(
        key: K,
        section: String?
    ): String = if (section == null) pathOf(key) else "${pathOf(key)}#$section"
}

/** The part of a page [address] names after a `#`, or null where it names none. */
internal fun sectionOf(address: String): String? = address.substringAfter('#', "").ifEmpty { null }
