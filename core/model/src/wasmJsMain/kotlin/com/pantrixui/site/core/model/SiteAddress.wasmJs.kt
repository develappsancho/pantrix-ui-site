package com.pantrixui.site.core.model

/** The page's own address, `window.location.origin`: `"null"` for a page opened from a file, which is no address. */
internal actual fun servedOrigin(): String? = locationOrigin()

@OptIn(ExperimentalWasmJsInterop::class)
private fun locationOrigin(): String = js("window.location.origin")
