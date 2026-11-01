package com.pantrixui.site

/**
 * Gives Compose's resource reader the Wasm memory by the way Kotlin supports, before the first resource is read, where
 * the site runs as Wasm. The JavaScript build has no Wasm memory of its own to give.
 */
internal expect fun shareWasmMemoryWithResources()
