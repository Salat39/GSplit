package presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

abstract class BaseAndroidViewModel<ViewState : MviViewState, ViewAction : MviAction>(
    application: Application,
    initialState: ViewState
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    fun sendAction(viewAction: ViewAction) {
        val newState = onReduceState(viewAction)
        _state.update { newState }
    }

    protected abstract fun onReduceState(viewAction: ViewAction): ViewState
}
