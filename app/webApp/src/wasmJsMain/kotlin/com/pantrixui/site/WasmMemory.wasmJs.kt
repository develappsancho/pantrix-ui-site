@file:OptIn(ExperimentalWasmJsInterop::class)

package com.pantrixui.site

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.wasm.unsafe.WebAssembly
import kotlin.wasm.unsafe.wasmMemory

/**
 * Gives Compose's resource reader the Wasm memory by the way Kotlin supports, before the first resource is read: the
 * library's web demo does the same.
 *
 * Compose Multiplatform 1.12.2 copies a font's or an image's bytes into the Wasm memory through `wasmExports.memory`.
 * Kotlin 2.4.20 deprecated that way, and the first use of it writes an error to the console: "Accessing `memory` via
 * `wasmExports` is deprecated". The supported way is `kotlin.wasm.unsafe.wasmMemory`. Compose fixed it on its side
 * (JetBrains/compose-multiplatform #5724); once the site is on a Compose with that fix, this goes, with its call.
 */
internal actual fun shareWasmMemoryWithResources() = shareMemory(wasmMemory)

/**
 * The compiler writes the bodies of `js(...)` into one module and hands them the memory through a variable called
 * `wasmExports`, which in Kotlin 2.4.20 is a proxy that writes the error on first use. It is replaced here with a
 * plain object holding the same memory, taken the supported way. Where the variable is gone, or cannot be changed,
 * nothing is done.
 */
@Suppress("UnusedParameter") // The JavaScript body reads it by its name.
private fun shareMemory(memory: WebAssembly.Memory): Unit = js(
    """{
        try {
            if (typeof wasmExports !== 'undefined') wasmExports = { memory: memory };
        } catch (unchangeable) {
        }
    }"""
)
