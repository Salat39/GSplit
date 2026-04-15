package com.salat.settings.api.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

@HiltViewModel
class SettingsApiViewModel @Inject constructor(
    private val saveBoolPrefUseCase: SaveBoolPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase
) : BaseSyncViewModel<SettingsApiViewModel.ViewState, SettingsApiViewModel.Action>(ViewState()) {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val collectedPreferences = flowPrefsUseCase.execute(
                BoolPref.ExternalAppEventSync
            ).firstOrNull()

            collectedPreferences?.let { prefs ->
                sendAction(
                    Action.InitPrefs(
                        macroDroidEventSync = prefs[0] as Boolean
                    )
                )
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.SetMacroDroidEventSync -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.ExternalAppEventSync,
                    viewAction.value ?: BoolPref.ExternalAppEventSync.default
                )
            }
            state.value.copy(macroDroidEventSync = viewAction.value)
        }

        is Action.InitPrefs -> state.value.copy(
            macroDroidEventSync = viewAction.macroDroidEventSync
        )
    }

    @Immutable
    data class ViewState(
        val macroDroidEventSync: Boolean? = null
    ) : MviViewState

    sealed class Action : MviAction {
        internal class InitPrefs(
            val macroDroidEventSync: Boolean?
        ) : Action()

        internal class SetMacroDroidEventSync(val value: Boolean?) : Action()
    }
}
