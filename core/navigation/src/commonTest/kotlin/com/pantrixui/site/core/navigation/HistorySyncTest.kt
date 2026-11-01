package com.pantrixui.site.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class HistorySyncTest {
    /** A browser's history: its entries, the one on show, and what it tells the app when the user moves. */
    private class FakeBrowser(start: String) : BrowserLocation {
        val entries = mutableListOf<Pair<Int?, String>>(null to start)
        var index = 0
        private var listener: ((Int?, String) -> Unit)? = null

        override val address: String get() = entries[index].second
        override val entryId: Int? get() = entries[index].first

        override fun push(
            id: Int,
            address: String
        ) {
            while (entries.lastIndex > index) entries.removeAt(entries.lastIndex)
            entries += id to address
            index = entries.lastIndex
        }

        override fun replace(
            id: Int,
            address: String
        ) {
            entries[index] = id to address
        }

        override fun go(steps: Int) {
            index = (index + steps).coerceIn(0, entries.lastIndex)
            pop()
        }

        override fun onPop(listener: (id: Int?, address: String) -> Unit): () -> Unit {
            this.listener = listener
            return { this.listener = null }
        }

        /** The user pressing Back. */
        fun back() = go(-1)

        /** The user pressing Forward. */
        fun forward() = go(1)

        private fun pop() {
            val (id, address) = entries[index]
            listener?.invoke(id, address)
        }

        val addresses: List<String> get() = entries.map { it.second }
    }

    /** The app's addresses: a prefix the app's language gives, as `/tr`. */
    private var prefix = ""

    private fun start(address: String = "/"): Triple<FakeBrowser, Navigator<String>, HistorySync<String>> {
        val browser = FakeBrowser(address)
        val sync = HistorySync(browser) { it.substringBefore('#').removePrefix("/tr").ifEmpty { "/" } }
        val navigator = Navigator(listOf(browser.address.substringBefore('#')))
        sync.attach(navigator.backStack.toList()) { prefix + it }
        browser.onPop { id, at -> sync.onPop(id, at, navigator) }
        return Triple(browser, navigator, sync)
    }

    private fun Navigator<String>.go(
        sync: HistorySync<String>,
        change: Navigator<String>.() -> Unit
    ) {
        change()
        sync.onStackChanged(backStack.toList())
    }

    @Test
    fun the_stack_opens_with_the_page_the_address_names() {
        val (browser, navigator) = start("/docs/installation")
        assertEquals(listOf("/docs/installation"), navigator.backStack.toList())
        assertEquals(0, browser.entryId)
    }

    @Test
    fun a_page_the_app_opens_is_an_entry_of_the_browser() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components") }
        navigator.go(sync) { navigate("/components/text-area") }
        assertEquals(listOf("/", "/components", "/components/text-area"), browser.addresses)
        assertEquals(2, browser.index)
    }

    @Test
    fun the_browser_s_back_and_forward_move_the_app_s_stack() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components") }
        navigator.go(sync) { navigate("/components/text-area") }
        browser.back()
        assertEquals(listOf("/", "/components"), navigator.backStack.toList())
        browser.back()
        assertEquals(listOf("/"), navigator.backStack.toList())
        browser.forward()
        browser.forward()
        assertEquals(listOf("/", "/components", "/components/text-area"), navigator.backStack.toList())
    }

    @Test
    fun the_app_going_back_goes_back_in_the_browser_so_that_forward_returns() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components") }
        navigator.go(sync) { back() }
        assertEquals(listOf("/"), navigator.backStack.toList())
        assertEquals(0, browser.index)
        browser.forward()
        assertEquals(listOf("/", "/components"), navigator.backStack.toList())
    }

    @Test
    fun a_page_replaced_rewrites_the_entry_on_show() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components") }
        navigator.go(sync) { replace("/foundations") }
        assertEquals(listOf("/", "/foundations"), browser.addresses)
    }

    @Test
    fun back_from_a_reset_opens_the_page_before_it_alone() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components") }
        navigator.go(sync) { resetTo("/roadmap") }
        assertEquals(listOf("/", "/components", "/roadmap"), browser.addresses)
        browser.back()
        assertEquals(listOf("/components"), navigator.backStack.toList())
    }

    @Test
    fun new_addresses_rewrite_the_entry_on_show_and_an_old_entry_follows_when_reached() {
        val (browser, navigator, sync) = start("/docs/installation")
        navigator.go(sync) { navigate("/components") }
        prefix = "/tr"
        sync.rewriteAddress()
        assertEquals(listOf("/docs/installation", "/tr/components"), browser.addresses)
        browser.back()
        assertEquals(listOf("/docs/installation"), navigator.backStack.toList())
        assertEquals("/tr/docs/installation", browser.address)
    }
}
