// The entry point keeps the name the template gave it, main.kt, as the library's web demo does.
@file:Suppress("ktlint:standard:filename")

package com.pantrixui.site

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Before the first resource is read: Compose's reader then does not ask for the memory the deprecated way.
    shareWasmMemoryWithResources()
    startSiteKoin()
    // The app is set in the page's own box, not its body: one set in the body removes what the body holds, the
    // loading mark with it. Set in the box, the mark stays over it until the first frame is drawn.
    ComposeViewport(viewportContainerId = APP) {
        WebSite(onShown = ::removeLoadingMark)
    }
}

/** Removes the page's loading mark: the site is drawn under it now. */
private fun removeLoadingMark() {
    document.getElementById(LOADING)?.remove()
}

/** The ids of `index.html`'s two boxes: the one the app is set in, and the loading mark over it. */
private const val APP = "app"
private const val LOADING = "loading"
