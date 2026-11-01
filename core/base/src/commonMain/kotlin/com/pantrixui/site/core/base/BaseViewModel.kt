package com.pantrixui.site.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The model of a screen, in the model–view–intent way: the screen sends it [UiAction]s ([onAction]), it answers with
 * a new [state] and, for what happens once, [effects].
 *
 * A subclass says what each action does in [handle], changes the state only with [updateState], and sends an effect
 * with [sendEffect]. The state is one value, so a screen never draws half of a change.
 *
 * @param initialState what the screen draws before anything has happened.
 */
abstract class BaseViewModel<S : UiState, A : UiAction, E : UiEffect>(initialState: S) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    private val effectChannel = Channel<E>(Channel.BUFFERED)

    /** What the screen draws. */
    val state: StateFlow<S> = mutableState.asStateFlow()

    /**
     * What the screen does once: each effect reaches one collector, in the order it was sent. An effect sent while no
     * screen collects, as while an Android activity is stopped, waits for the next one.
     */
    val effects: Flow<E> = effectChannel.receiveAsFlow()

    /** The state at this moment. */
    protected val currentState: S
        get() = mutableState.value

    /** Tells the model what happened. */
    fun onAction(action: A) {
        handle(action)
    }

    /** What [action] does: a new state, an effect, or work that ends in one of them. */
    protected abstract fun handle(action: A)

    /** Gives the screen a new state, made from the one it has. */
    protected fun updateState(reduce: S.() -> S) {
        mutableState.update(reduce)
    }

    /** Sends the screen something to do once. */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    /** Runs [block] for as long as the model lives: it is cancelled when the screen's model is cleared. */
    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job = viewModelScope.launch(block = block)
}
