package com.app.mobile.presentation.ui.components

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, Event>(initialState: State) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    protected val currentState: State
        get() = _uiState.value

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(
            this::class.simpleName,
            "Error captured in BaseViewModel: ${exception.message}",
            exception
        )
        handleError(exception)
    }

    protected abstract fun handleError(exception: Throwable)

    protected fun updateState(transform: (State) -> State) {
        _uiState.update(transform)
    }

    protected fun sendEvent(event: Event) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    protected fun launch(
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(errorHandler) {
            block()
        }
    }
}