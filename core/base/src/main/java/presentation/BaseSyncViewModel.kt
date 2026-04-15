package presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

abstract class BaseSyncViewModel<ViewState : MviViewState, ViewAction : MviAction>(initialState: ViewState) :
    ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val actionQueue = MutableSharedFlow<ViewAction>()

    init {
        viewModelScope.launch {
            actionQueue.collect { viewAction ->
                val newState = onReduceState(viewAction)
                _state.update { newState }
            }
        }
    }

    fun sendAction(viewAction: ViewAction) {
        viewModelScope.launch {
            actionQueue.emit(viewAction)
        }
    }

    fun sendActionUpdate(viewAction: ViewAction) {
        val newState = onReduceState(viewAction)
        _state.update { newState }
    }

    protected abstract fun onReduceState(viewAction: ViewAction): ViewState
}
