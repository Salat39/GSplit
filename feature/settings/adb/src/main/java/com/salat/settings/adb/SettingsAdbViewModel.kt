package com.salat.settings.adb

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.adb.domain.usecases.AdbConnectionStateUseCase
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
import com.salat.preferences.domain.usecases.SaveIntPrefUseCase
import com.salat.settings.adb.entity.DisplayAdbState
import com.salat.settings.adb.mappers.toDisplayAdbState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

@HiltViewModel
class SettingsAdbViewModel @Inject constructor(
    private val saveBoolPrefUseCase: SaveBoolPrefUseCase,
    private val saveIntPrefUseCase: SaveIntPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase,
    private val adbConnectionStateUseCase: AdbConnectionStateUseCase
) : BaseSyncViewModel<SettingsAdbViewModel.ViewState, SettingsAdbViewModel.Action>(ViewState()) {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                val collectedPreferences = flowPrefsUseCase.execute(
                    BoolPref.EnableAdbHelper,
                    BoolPref.EnableAdbForceStop,
                    BoolPref.EnableAdbOverlayFun,
                    IntPref.AdbHelperPort,
                    FloatPref.UiScale
                ).firstOrNull()

                collectedPreferences?.let { prefs ->
                    sendAction(
                        Action.InitPrefs(
                            enableAdbHelper = prefs[0] as Boolean,
                            enableAdbForceStop = prefs[1] as Boolean,
                            enableAdbOverlayFun = prefs[2] as Boolean,
                            adbPort = prefs[3] as Int,
                            uiScale = prefs[4] as Float
                        )
                    )
                }
            }
            launch {
                adbConnectionStateUseCase.flow.collect { status ->
                    sendAction(Action.SetAdbConnectionState(status.toDisplayAdbState()))
                }
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.InitPrefs -> state.value.copy(
            enableAdbHelper = viewAction.enableAdbHelper,
            enableAdbForceStop = viewAction.enableAdbForceStop,
            enableAdbOverlayFun = viewAction.enableAdbOverlayFun,
            adbHelperPort = viewAction.adbPort,
            uiScale = viewAction.uiScale
        )

        is Action.SetEnableAdbHelper -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(BoolPref.EnableAdbHelper, viewAction.value)
            }
            state.value.copy(enableAdbHelper = viewAction.value)
        }

        is Action.SetEnableAdbForceStop -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(BoolPref.EnableAdbForceStop, viewAction.value)
            }
            state.value.copy(enableAdbForceStop = viewAction.value)
        }

        is Action.SetEnableAdbOverlayFun -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(BoolPref.EnableAdbOverlayFun, viewAction.value)
            }
            state.value.copy(enableAdbOverlayFun = viewAction.value)
        }

        is Action.SetPort -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveIntPrefUseCase.execute(IntPref.AdbHelperPort, viewAction.port)
            }
            state.value.copy(adbHelperPort = viewAction.port)
        }

        is Action.SetAdbConnectionState -> state.value.copy(
            adbConnectionState = viewAction.state
        )
    }

    @Immutable
    data class ViewState(
        val enableAdbHelper: Boolean = false,
        val enableAdbForceStop: Boolean = false,
        val enableAdbOverlayFun: Boolean = false,
        val adbHelperPort: Int = -1,
        val adbConnectionState: DisplayAdbState = DisplayAdbState.Disconnected,
        val uiScale: Float = 1f,
    ) : MviViewState

    sealed class Action : MviAction {
        class InitPrefs(
            val enableAdbHelper: Boolean,
            val enableAdbForceStop: Boolean,
            val enableAdbOverlayFun: Boolean,
            val adbPort: Int,
            val uiScale: Float
        ) : Action()

        data class SetEnableAdbHelper(val value: Boolean) : Action()

        data class SetEnableAdbForceStop(val value: Boolean) : Action()

        data class SetEnableAdbOverlayFun(val value: Boolean) : Action()

        data class SetPort(val port: Int) : Action()

        data class SetAdbConnectionState(val state: DisplayAdbState) : Action()
    }
}
