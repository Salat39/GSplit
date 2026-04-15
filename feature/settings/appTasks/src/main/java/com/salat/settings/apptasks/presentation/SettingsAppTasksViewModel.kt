package com.salat.settings.apptasks.presentation

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
class SettingsAppTasksViewModel @Inject constructor(
    private val saveBoolPrefUseCase: SaveBoolPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase
) : BaseSyncViewModel<SettingsAppTasksViewModel.ViewState, SettingsAppTasksViewModel.Action>(ViewState()) {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val collectedPreferences = flowPrefsUseCase.execute(
                BoolPref.YmCompatPlay,
                BoolPref.MurglarCompatPlay,
                BoolPref.VkxCompatPlay
            ).firstOrNull()

            collectedPreferences?.let { prefs ->
                sendAction(
                    Action.InitPrefs(
                        ymCompatPlay = prefs[0] as Boolean,
                        murglarCompatPlay = prefs[1] as Boolean,
                        vkxCompatPlay = prefs[2] as Boolean,
                    )
                )
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.SetYmCompatPlay -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.YmCompatPlay,
                    viewAction.value ?: BoolPref.YmCompatPlay.default
                )
            }
            state.value.copy(ymCompatPlay = viewAction.value)
        }

        is Action.SetMurglarCompatPlay -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.MurglarCompatPlay,
                    viewAction.value ?: BoolPref.MurglarCompatPlay.default
                )
            }
            state.value.copy(murglarCompatPlay = viewAction.value)
        }

        is Action.SetVkxCompatPlay -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.VkxCompatPlay,
                    viewAction.value ?: BoolPref.VkxCompatPlay.default
                )
            }
            state.value.copy(vkxCompatPlay = viewAction.value)
        }

        is Action.InitPrefs -> state.value.copy(
            ymCompatPlay = viewAction.ymCompatPlay,
            murglarCompatPlay = viewAction.murglarCompatPlay,
            vkxCompatPlay = viewAction.vkxCompatPlay
        )
    }

    @Immutable
    data class ViewState(
        val ymCompatPlay: Boolean? = null,
        val murglarCompatPlay: Boolean? = null,
        val vkxCompatPlay: Boolean? = null,
    ) : MviViewState

    sealed class Action : MviAction {
        internal class InitPrefs(
            val ymCompatPlay: Boolean?,
            val murglarCompatPlay: Boolean?,
            val vkxCompatPlay: Boolean?
        ) : Action()

        internal class SetYmCompatPlay(val value: Boolean?) : Action()
        internal class SetMurglarCompatPlay(val value: Boolean?) : Action()
        internal class SetVkxCompatPlay(val value: Boolean?) : Action()
    }
}
