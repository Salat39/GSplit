package presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

abstract class BaseViewModel<ViewState : MviViewState, ViewAction : MviAction>(initialState: ViewState) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    fun sendAction(viewAction: ViewAction) {
        val newState = onReduceState(viewAction)
        _state.update { newState }
    }

    protected abstract fun onReduceState(viewAction: ViewAction): ViewState
}
