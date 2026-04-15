package com.salat.stub.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.statekeeper.domain.usecases.CloseDarkScreenFlowUseCase
import com.salat.stub.presentation.route.StubNavRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

@HiltViewModel
class StubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val closeDarkScreenFlowUseCase: CloseDarkScreenFlowUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase
) : BaseSyncViewModel<StubViewModel.ViewState, StubViewModel.Action>(
    savedStateHandle.toRoute<StubNavRoute>().let {
        ViewState(
            minimizeAfterCloseScreen = it.minimizeAfterCloseScreen,
            toolbarExtraSize = it.toolbarExtraSize
        )
    }
) {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                closeDarkScreenFlowUseCase.flow.collect {
                    sendAction(Action.Finish)
                }
            }
            launch {
                val collectedPreferences = flowPrefsUseCase.execute(
                    BoolPref.DarkScreenBackButton,
                ).firstOrNull()

                collectedPreferences?.let { prefs ->
                    sendAction(
                        Action.InitPrefs(
                            darkScreenBackButton = prefs[0] as Boolean,
                        )
                    )
                }
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.Finish -> state.value.copy(
            finishSingleEvent = true
        )

        is Action.InitPrefs -> state.value.copy(darkScreenBackButton = viewAction.darkScreenBackButton)
    }

    @Immutable
    data class ViewState(
        val minimizeAfterCloseScreen: Boolean = false,
        val toolbarExtraSize: Int = 0,
        val finishSingleEvent: Boolean = false,
        val darkScreenBackButton: Boolean = false
    ) : MviViewState

    sealed class Action : MviAction {
        data object Finish : Action()

        data class InitPrefs(
            val darkScreenBackButton: Boolean
        ) : Action()
    }
}
