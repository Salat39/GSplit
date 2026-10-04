package com.salat.settings.adb

import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.adb.domain.usecases.AdbConnectionStateUseCase
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
import com.salat.preferences.domain.usecases.SaveIntPrefUseCase
import com.salat.settings.common.presentation.entity.DisplayAdbState
import com.salat.settings.common.presentation.mappers.toDisplayAdbState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val fullscreenAppModesMutex = Mutex()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                val collectedPreferences = flowPrefsUseCase.execute(
                    BoolPref.EnableAdbHelper,
                    BoolPref.EnableAdbForceStop,
                    BoolPref.EnableAdbTaskResize,
                    IntPref.AdbHelperPort,
                    FloatPref.UiScale,
                    BoolPref.CloseOldSplitWindows
                ).firstOrNull()

                collectedPreferences?.let { prefs ->
                    sendAction(
                        Action.InitPrefs(
                            enableAdbHelper = prefs[0] as Boolean,
                            enableAdbForceStop = prefs[1] as Boolean,
                            enableAdbTaskResize = prefs[2] as Boolean,
                            adbPort = prefs[3] as Int,
                            uiScale = prefs[4] as Float,
                            closeOldSplitWindows = prefs[5] as Boolean
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
            // The launcher prefers the task resize mode when a restored backup enables both modes
            enableAdbForceStop = viewAction.enableAdbForceStop &&
                !(viewAction.enableAdbTaskResize && state.value.taskResizeSupported),
            enableAdbTaskResize = viewAction.enableAdbTaskResize,
            adbHelperPort = viewAction.adbPort,
            uiScale = viewAction.uiScale,
            closeOldSplitWindows = viewAction.closeOldSplitWindows
        )

        is Action.SetEnableAdbHelper -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(BoolPref.EnableAdbHelper, viewAction.value)
            }
            state.value.copy(enableAdbHelper = viewAction.value)
        }

        is Action.SetEnableAdbForceStop -> setFullscreenAppModes(
            forceStop = viewAction.value,
            taskResize = state.value.enableAdbTaskResize && !viewAction.value
        )

        is Action.SetEnableAdbTaskResize -> setFullscreenAppModes(
            forceStop = state.value.enableAdbForceStop && !viewAction.value,
            taskResize = viewAction.value
        )

        is Action.SetCloseOldSplitWindows -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(BoolPref.CloseOldSplitWindows, viewAction.value)
            }
            state.value.copy(closeOldSplitWindows = viewAction.value)
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

    private fun setFullscreenAppModes(forceStop: Boolean, taskResize: Boolean): ViewState {
        viewModelScope.launch(Dispatchers.IO) {
            fullscreenAppModesMutex.withLock {
                saveBoolPrefUseCase.execute(BoolPref.EnableAdbForceStop, forceStop)
                saveBoolPrefUseCase.execute(BoolPref.EnableAdbTaskResize, taskResize)
            }
        }
        return state.value.copy(enableAdbForceStop = forceStop, enableAdbTaskResize = taskResize)
    }

    @Immutable
    data class ViewState(
        val enableAdbHelper: Boolean = false,
        val enableAdbForceStop: Boolean = BoolPref.EnableAdbForceStop.default,
        val enableAdbTaskResize: Boolean = BoolPref.EnableAdbTaskResize.default,
        val taskResizeSupported: Boolean = Build.VERSION.SDK_INT == Build.VERSION_CODES.R,
        val closeOldSplitWindows: Boolean = BoolPref.CloseOldSplitWindows.default,
        val closeOldSplitWindowsSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
        val adbHelperPort: Int = -1,
        val adbConnectionState: DisplayAdbState = DisplayAdbState.Disconnected,
        val uiScale: Float = 1f,
    ) : MviViewState

    sealed class Action : MviAction {
        class InitPrefs(
            val enableAdbHelper: Boolean,
            val enableAdbForceStop: Boolean,
            val enableAdbTaskResize: Boolean,
            val adbPort: Int,
            val uiScale: Float,
            val closeOldSplitWindows: Boolean
        ) : Action()

        data class SetEnableAdbHelper(val value: Boolean) : Action()

        data class SetEnableAdbForceStop(val value: Boolean) : Action()

        data class SetEnableAdbTaskResize(val value: Boolean) : Action()

        data class SetCloseOldSplitWindows(val value: Boolean) : Action()

        data class SetPort(val port: Int) : Action()

        data class SetAdbConnectionState(val state: DisplayAdbState) : Action()
    }
}
