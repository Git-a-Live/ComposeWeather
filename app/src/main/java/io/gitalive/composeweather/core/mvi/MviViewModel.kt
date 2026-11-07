package io.gitalive.composeweather.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 页面意图，由 UI 单向发出。 */
interface UiIntent

/** 一次性事件，例如提示条、导航，不进入 State。 */
interface UiEffect

/**
 * MVI 基类：State 用 StateFlow 暴露，Intent 单向进入队列，Effect 用 Channel 保证只消费一次。
 */
abstract class MviViewModel<S, I : UiIntent, E : UiEffect>(initialState: S) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    protected val currentState: S get() = _uiState.value

    protected fun setState(reducer: S.() -> S) {
        _uiState.update(reducer)
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }

    fun dispatch(intent: I) {
        handleIntent(intent)
    }

    protected abstract fun handleIntent(intent: I)
}
