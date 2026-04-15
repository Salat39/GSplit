package com.salat.settings.general.presentation

import androidx.compose.runtime.Immutable
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

@HiltViewModel
class SettingsGeneralViewModel @Inject constructor() : BaseSyncViewModel<
    SettingsGeneralViewModel.ViewState,
    SettingsGeneralViewModel.Action
    >(ViewState()) {
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.Dummy -> state.value
    }

    @Immutable
    data class ViewState(
        val dummy: Boolean? = null
    ) : MviViewState

    sealed class Action : MviAction {
        internal data object Dummy : Action()
    }
}
