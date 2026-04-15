package presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseSyncFlowViewModel<ViewState : MviViewState, ViewAction : MviAction>(initialState: ViewState) :
    ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val actionFlow = MutableSharedFlow<ViewAction>()

    init {
        viewModelScope.launch {
            actionFlow
                .flatMapConcat { viewAction ->
                    flow {
                        val newState = onReduceState(viewAction)
                        emit(newState)
                    }
                }
                .collect { newState ->
                    _state.value = newState
                }
        }
    }

    fun sendAction(viewAction: ViewAction) {
        viewModelScope.launch {
            actionFlow.emit(viewAction)
        }
    }

    fun sendActionUpdate(viewAction: ViewAction) {
        val newState = onReduceState(viewAction)
        _state.update { newState }
    }

    protected abstract fun onReduceState(viewAction: ViewAction): ViewState
}
