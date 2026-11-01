// The browser's history is read and written with JavaScript's own values: an entry's state is a JavaScript string.
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.pantrixui.site.core.navigation

import kotlinx.browser.window
import org.w3c.dom.PopStateEvent
import org.w3c.dom.events.Event
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsNumber
import kotlin.js.JsString
import kotlin.js.toInt
import kotlin.js.toJsString

/**
 * The browser's own history, for an app whose screens have addresses: see [BrowserNavigationHistory].
 *
 * @param keyOf the key an address names; an address the app has no screen for is the app's "not found" key.
 */
fun <K : Any> browserNavigationHistory(keyOf: (String) -> K): NavigationHistory<K> =
    BrowserNavigationHistory(WindowLocation, keyOf)

/** The window's location and history. */
private object WindowLocation : BrowserLocation {
    override val address: String
        get() = window.location.run { pathname + search + hash }

    override val entry: EntryState?
        get() = stateOf(window.history.state)

    override fun push(
        state: EntryState,
        address: String
    ) {
        window.history.pushState(state.write().toJsString(), "", address)
    }

    override fun replace(
        state: EntryState,
        address: String
    ) {
        window.history.replaceState(state.write().toJsString(), "", address)
    }

    override fun go(steps: Int) {
        window.history.go(steps)
    }

    override fun onPop(listener: (state: EntryState?, address: String) -> Unit): () -> Unit {
        val callback: (Event) -> Unit = { event ->
            listener(stateOf((event as? PopStateEvent)?.state), address)
        }
        window.addEventListener("popstate", callback)
        return { window.removeEventListener("popstate", callback) }
    }

    /**
     * The app's record in an entry's [state]. An id alone, as the site wrote before an entry kept its stack, is the
     * screen on show alone; anything else is no record.
     */
    private fun stateOf(state: JsAny?): EntryState? = when (state) {
        is JsString -> EntryState.read(state.toString())
        is JsNumber -> EntryState(listOf(EntryState.Page(state.toInt(), address)))
        else -> null
    }
}
