package com.pantrixui.site.core.base

import kotlin.coroutines.cancellation.CancellationException

/** Where something a screen waits for stands: on its way, here, or not to be had. */
sealed interface LoadState<out T> {
    /** On its way. */
    data object Loading : LoadState<Nothing>

    /** Here: [value] is what was loaded. */
    data class Loaded<out T>(val value: T) : LoadState<T>

    /** Not to be had: [reason] says why, for a log or a test; a screen tells the user in its own words. */
    data class Failed(val reason: String) : LoadState<Nothing>
}

/** The value, if it has been loaded. */
val <T> LoadState<T>.valueOrNull: T?
    get() = (this as? LoadState.Loaded)?.value

/**
 * Runs [load] and says how it went: [LoadState.Loaded] with its result, or [LoadState.Failed] when it throws. A
 * cancellation is not a failure: it goes on, so that the work stops.
 */
suspend fun <T> loadState(load: suspend () -> T): LoadState<T> =
    try {
        LoadState.Loaded(load())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (@Suppress("TooGenericExceptionCaught") failure: Exception) {
        // A load can fail in any way a source can.
        LoadState.Failed(failure.message ?: failure::class.simpleName.orEmpty())
    }
