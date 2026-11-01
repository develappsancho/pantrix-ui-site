package com.pantrixui.site.core.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle

/**
 * Connects a screen to its [viewModel]: draws [content] with the model's state, sends what the user does back to the
 * model, and hands each effect to [onEffect] while the screen is on show.
 *
 * The screen in [content] is stateless: it is given the state and a function for its actions, and draws only from
 * them. A test or a preview draws the same content with a state of its own.
 *
 * @param viewModel the screen's model.
 * @param onEffect what the screen does with something it is told to do once: open a page, copy a text.
 * @param content the screen, drawn from its state, with the function its actions go to.
 */
@Composable
fun <S : UiState, A : UiAction, E : UiEffect> BaseScreen(
    viewModel: BaseViewModel<S, A, E>,
    onEffect: suspend (E) -> Unit = {},
    content: @Composable (state: S, onAction: (A) -> Unit) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effectHandler by rememberUpdatedState(onEffect)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effectHandler(it) }
        }
    }
    content(state, viewModel::onAction)
}
