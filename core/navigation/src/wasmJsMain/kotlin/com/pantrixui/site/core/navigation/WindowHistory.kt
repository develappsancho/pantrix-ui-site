// The browser's history is read and written with JavaScript's own values: an entry's id is a JavaScript number.
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.pantrixui.site.core.navigation

import kotlinx.browser.window
import org.w3c.dom.PopStateEvent
import org.w3c.dom.events.Event
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsNumber
import kotlin.js.toInt
import kotlin.js.toJsNumber

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

    override val entryId: Int?
        get() = (window.history.state as? JsNumber)?.toInt()

    override fun push(
        id: Int,
        address: String
    ) {
        window.history.pushState(id.toJsNumber(), "", address)
    }

    override fun replace(
        id: Int,
        address: String
    ) {
        window.history.replaceState(id.toJsNumber(), "", address)
    }

    override fun go(steps: Int) {
        window.history.go(steps)
    }

    override fun onPop(listener: (id: Int?, address: String) -> Unit): () -> Unit {
        val callback: (Event) -> Unit = { event ->
            val state = (event as? PopStateEvent)?.state
            listener((state as? JsNumber)?.toInt(), address)
        }
        window.addEventListener("popstate", callback)
        return { window.removeEventListener("popstate", callback) }
    }
}
