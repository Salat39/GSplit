package com.salat.settings.general.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.usecases.CreateSettingsSnapshotUseCase
import com.salat.statekeeper.domain.usecases.RequestImportSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.encodeBase64Jvm
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

@HiltViewModel
class SettingsGeneralViewModel @Inject constructor(
    private val createSettingsSnapshotUseCase: CreateSettingsSnapshotUseCase,
    private val requestImportSettingsUseCase: RequestImportSettingsUseCase
) : BaseSyncViewModel<SettingsGeneralViewModel.ViewState, SettingsGeneralViewModel.Action>(ViewState()) {

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.ExportAllSettings -> {
            viewModelScope.launch(Dispatchers.IO) {
                val exportBackup = encodeBase64Jvm(createSettingsSnapshotUseCase.execute())
                sendAction(Action.SetExportBackup(exportBackup))
            }
            state.value
        }

        Action.RequestImportSettings -> {
            viewModelScope.launch(Dispatchers.IO) { requestImportSettingsUseCase.execute() }
            state.value
        }

        is Action.SetExportBackup -> state.value.copy(exportBackup = viewAction.export)
    }

    @Immutable
    data class ViewState(
        val exportBackup: String = ""
    ) : MviViewState

    sealed class Action : MviAction {
        class SetExportBackup(val export: String) : Action()
        internal data object ExportAllSettings : Action()
        internal data object RequestImportSettings : Action()
    }
}
