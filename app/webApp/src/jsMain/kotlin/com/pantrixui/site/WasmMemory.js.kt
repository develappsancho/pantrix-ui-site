package com.pantrixui.site

/** The JavaScript build reads its resources with no Wasm memory of its own: there is nothing to give. */
internal actual fun shareWasmMemoryWithResources() = Unit
