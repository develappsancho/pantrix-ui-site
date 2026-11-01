package com.pantrixui.site.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HistorySyncTest {
    /** A browser's history: its entries, the one on show, and what it tells the app when the user moves. */
    private class FakeBrowser(start: String) : BrowserLocation {
        val entries = mutableListOf<Pair<EntryState?, String>>(null to start)
        var index = 0
        private var listener: ((EntryState?, String) -> Unit)? = null

        override val address: String get() = entries[index].second
        override val entry: EntryState? get() = entries[index].first

        override fun push(
            state: EntryState,
            address: String
        ) {
            while (entries.lastIndex > index) entries.removeAt(entries.lastIndex)
            entries += state to address
            index = entries.lastIndex
        }

        override fun replace(
            state: EntryState,
            address: String
        ) {
            entries[index] = state to address
        }

        override fun go(steps: Int) {
            index = (index + steps).coerceIn(0, entries.lastIndex)
            pop()
        }

        override fun onPop(listener: (state: EntryState?, address: String) -> Unit): () -> Unit {
            this.listener = listener
            return { this.listener = null }
        }

        /** The user pressing Back. */
        fun back() = go(-1)

        /** The user pressing Forward. */
        fun forward() = go(1)

        /** The user changing only the address's `#` in the bar: an entry with no state of the app's. */
        fun type(address: String) {
            while (entries.lastIndex > index) entries.removeAt(entries.lastIndex)
            entries += null to address
            index = entries.lastIndex
            pop()
        }

        private fun pop() {
            val (state, address) = entries[index]
            listener?.invoke(state, address)
        }

        val addresses: List<String> get() = entries.map { it.second }
    }

    /** The app's addresses: a prefix the app's language gives, as `/tr`. */
    private var prefix = ""

    private fun start(address: String = "/"): Triple<FakeBrowser, Navigator<String>, HistorySync<String>> {
        val browser = FakeBrowser(address)
        val (navigator, sync) = open(browser)
        return Triple(browser, navigator, sync)
    }

    /** The app opening on [browser]'s entry on show, as at its start and after a reload. */
    private fun open(browser: FakeBrowser): Pair<Navigator<String>, HistorySync<String>> {
        val sync = HistorySync(browser) { it.substringBefore('#').removePrefix("/tr").ifEmpty { "/" } }
        val navigator = Navigator(sync.startStack(), sectionOf(browser.address))
        sync.attach(navigator.backStack.toList(), navigator.section) { prefix + it }
        browser.onPop { state, at -> sync.onPop(state, at, navigator) }
        return navigator to sync
    }

    private fun Navigator<String>.go(
        sync: HistorySync<String>,
        change: Navigator<String>.() -> Unit
    ) {
        change()
        sync.onStackChanged(backStack.toList(), section)
    }

    @Test
    fun the_stack_opens_with_the_page_the_address_names() {
        val (browser, navigator) = start("/docs/installation")
        assertEquals(listOf("/docs/installation"), navigator.backStack.toList())
        assertEquals(0, browser.entry?.id)
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
    fun a_reload_opens_the_stack_the_entry_was_written_with_and_the_app_s_back_goes_on() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components") }
        navigator.go(sync) { navigate("/components/text-area") }
        val (reloaded, again) = open(browser)
        assertEquals(listOf("/", "/components", "/components/text-area"), reloaded.backStack.toList())
        reloaded.go(again) { back() }
        assertEquals(1, browser.index)
        assertEquals(listOf("/", "/components"), reloaded.backStack.toList())
        browser.forward()
        assertEquals(listOf("/", "/components", "/components/text-area"), reloaded.backStack.toList())
    }

    @Test
    fun after_a_reload_each_entry_gone_back_to_has_its_own_stack() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components") }
        navigator.go(sync) { navigate("/components/text-area") }
        browser.back()
        val (reloaded) = open(browser)
        assertEquals(listOf("/", "/components"), reloaded.backStack.toList())
        browser.back()
        assertEquals(listOf("/"), reloaded.backStack.toList())
        browser.forward()
        browser.forward()
        assertEquals(listOf("/", "/components", "/components/text-area"), reloaded.backStack.toList())
    }

    @Test
    fun a_page_replaced_rewrites_the_entry_on_show() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components") }
        navigator.go(sync) { replace("/foundations") }
        assertEquals(listOf("/", "/foundations"), browser.addresses)
    }

    @Test
    fun back_from_a_jump_opens_the_stack_the_page_before_it_had() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components") }
        navigator.go(sync) { resetTo("/roadmap") }
        assertEquals(listOf("/", "/components", "/roadmap"), browser.addresses)
        browser.back()
        assertEquals(listOf("/", "/components"), navigator.backStack.toList())
    }

    @Test
    fun an_entry_the_app_never_wrote_is_its_page_alone_and_is_written_as_the_app_s() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components") }
        browser.type("/docs/installation#gradle")
        assertEquals(listOf("/docs/installation"), navigator.backStack.toList())
        assertEquals("gradle", navigator.section)
        assertEquals(listOf("/docs/installation#gradle"), browser.entry?.pages?.map { it.address })
        browser.back()
        assertEquals(listOf("/", "/components"), navigator.backStack.toList())
    }

    @Test
    fun new_addresses_rewrite_the_entry_on_show_and_an_old_entry_follows_when_reached() {
        val (browser, navigator, sync) = start("/docs/installation")
        navigator.go(sync) { navigate("/components") }
        prefix = "/tr"
        sync.rewriteAddress()
        assertEquals(listOf("/docs/installation", "/tr/components"), browser.addresses)
        assertEquals(listOf("/tr/docs/installation", "/tr/components"), browser.entry?.pages?.map { it.address })
        browser.back()
        assertEquals(listOf("/docs/installation"), navigator.backStack.toList())
        assertEquals("/tr/docs/installation", browser.address)
    }

    @Test
    fun an_address_that_names_a_part_of_its_page_opens_the_page_there_and_keeps_it() {
        val (browser, navigator) = start("/docs/guides/keyboard-dismissal#fix")
        assertEquals(listOf("/docs/guides/keyboard-dismissal"), navigator.backStack.toList())
        assertEquals("fix", navigator.section)
        assertEquals("/docs/guides/keyboard-dismissal#fix", browser.address)
    }

    @Test
    fun moving_to_another_part_rewrites_the_entry_on_show_and_adds_none() {
        val (browser, navigator, sync) = start("/docs/guides/keyboard-dismissal")
        navigator.go(sync) { showSection("limits") }
        assertEquals(listOf("/docs/guides/keyboard-dismissal#limits"), browser.addresses)
        navigator.go(sync) { showSection(null) }
        assertEquals(listOf("/docs/guides/keyboard-dismissal"), browser.addresses)
    }

    @Test
    fun a_page_opened_at_a_part_is_one_entry_and_going_back_to_it_returns_there() {
        val (browser, navigator, sync) = start()
        navigator.go(sync) { navigate("/components/text-area", "api") }
        navigator.go(sync) { navigate("/roadmap") }
        assertEquals(listOf("/", "/components/text-area#api", "/roadmap"), browser.addresses)
        assertEquals(null, navigator.section)
        browser.back()
        assertEquals(listOf("/", "/components/text-area"), navigator.backStack.toList())
        assertEquals("api", navigator.section)
    }

    @Test
    fun new_addresses_keep_the_part_of_the_page_on_show() {
        val (browser, navigator, sync) = start("/docs/installation")
        navigator.go(sync) { showSection("gradle") }
        prefix = "/tr"
        sync.rewriteAddress()
        assertEquals("/tr/docs/installation#gradle", browser.address)
    }

    @Test
    fun an_entry_s_state_is_read_as_it_is_written_and_other_text_is_no_state() {
        val state = EntryState(listOf(EntryState.Page(NOT_AN_ENTRY, "/"), EntryState.Page(4, "/docs/forms#rules")))
        assertEquals("-1 /\n4 /docs/forms#rules", state.write())
        assertEquals(state, EntryState.read(state.write()))
        assertEquals(4, state.id)
        assertNull(EntryState.read(""))
        assertNull(EntryState.read("installation"))
        assertNull(EntryState.read("3"))
    }
}
