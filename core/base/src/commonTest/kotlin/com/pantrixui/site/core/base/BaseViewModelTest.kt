package com.pantrixui.site.core.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private data class CounterState(val count: Int = 0) : UiState

    private sealed interface CounterAction : UiAction {
        data object Add : CounterAction

        data object Announce : CounterAction
    }

    private data class Said(val text: String) : UiEffect

    private class Counter : BaseViewModel<CounterState, CounterAction, Said>(CounterState()) {
        override fun handle(action: CounterAction) {
            when (action) {
                CounterAction.Add -> updateState { copy(count = count + 1) }
                CounterAction.Announce -> sendEffect(Said("count ${currentState.count}"))
            }
        }
    }

    @Test
    fun an_action_gives_the_state_made_from_the_one_before() = runTest(dispatcher) {
        val counter = Counter()
        counter.onAction(CounterAction.Add)
        counter.onAction(CounterAction.Add)
        assertEquals(CounterState(count = 2), counter.state.value)
    }

    @Test
    fun effects_reach_the_screen_once_and_in_the_order_they_were_sent() = runTest(dispatcher) {
        val counter = Counter()
        counter.onAction(CounterAction.Announce)
        counter.onAction(CounterAction.Add)
        counter.onAction(CounterAction.Announce)
        advanceUntilIdle()
        assertEquals(listOf(Said("count 0"), Said("count 1")), counter.effects.take(2).toList())
    }

    @Test
    fun a_load_that_throws_is_a_failure_with_its_reason() = runTest(dispatcher) {
        val loaded = loadState { 42 }
        val failed = loadState<Int> { error("no such page") }
        assertEquals(LoadState.Loaded(42), loaded)
        assertIs<LoadState.Failed>(failed)
        assertEquals("no such page", failed.reason)
        assertEquals(42, loaded.valueOrNull)
    }
}
