package com.salat.settings.presets.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
import com.salat.preferences.domain.usecases.SaveIntPrefUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

internal const val MAX_BYPASS_DELAY = 1500
internal const val MAX_SECOND_WINDOW_DELAY = 3000
internal const val MAX_AUTO_PLAY_DELAY = 9_900
internal const val MIN_HEIGHT_CORRECTOR = -300
internal const val MAX_HEIGHT_CORRECTOR = 300

internal const val DEFAULT_BYPASS_DELAY = 100
internal const val DEFAULT_SECOND_WINDOW_DELAY = 200
internal const val DEFAULT_AUTO_PLAY_DELAY = 3000
internal const val DEFAULT_HEIGHT_CORRECTOR = 0

@HiltViewModel
class SettingsPresetsViewModel @Inject constructor(
    private val saveIntPrefUseCase: SaveIntPrefUseCase,
    private val saveBoolPrefUseCase: SaveBoolPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase
) : BaseSyncViewModel<SettingsPresetsViewModel.ViewState, SettingsPresetsViewModel.Action>(ViewState()) {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val collectedPreferences = flowPrefsUseCase.execute(
                IntPref.BypassDelay,
                IntPref.SecondWindowDelay,
                IntPref.AutoPlayDelay,
                BoolPref.MinimizeByStart,
                BoolPref.MinimizeByAutostart,
                BoolPref.AutoStartMinimizeDelay,
                BoolPref.ContextAdaptiveSizes,
                BoolPref.ExperimentalNativeSplit,
                BoolPref.SoftKillApp,
                BoolPref.StandbyMode,
                IntPref.HeightCorrector
            ).firstOrNull()

            collectedPreferences?.let { prefs ->
                sendAction(
                    Action.InitPrefs(
                        bypassDelay = prefs[0] as Int,
                        secondWindowDelay = prefs[1] as Int,
                        autoPlayDelay = prefs[2] as Int,
                        minimizeByStart = prefs[3] as Boolean,
                        minimizeByAutostart = prefs[4] as Boolean,
                        autoStartMinimizeDelay = prefs[5] as Boolean,
                        contextAdaptiveSizes = prefs[6] as Boolean,
                        experimentalNativeSplit = prefs[7] as Boolean,
                        softKillApp = prefs[8] as Boolean,
                        standbyMode = prefs[9] as Boolean,
                        heightCorrector = prefs[10] as Int
                    )
                )
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.SetBypassDelay -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveIntPrefUseCase.execute(
                    IntPref.BypassDelay,
                    viewAction.delay
                )
            }
            state.value.copy(bypassDelay = viewAction.delay)
        }

        is Action.SetSecondWindowDelay -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveIntPrefUseCase.execute(
                    IntPref.SecondWindowDelay,
                    viewAction.delay
                )
            }
            state.value.copy(secondWindowDelay = viewAction.delay)
        }

        is Action.SetAutoPlayDelay -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveIntPrefUseCase.execute(
                    IntPref.AutoPlayDelay,
                    viewAction.delay
                )
            }
            state.value.copy(autoPlayDelay = viewAction.delay)
        }

        is Action.SetMinimizeByStart -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.MinimizeByStart,
                    viewAction.value ?: BoolPref.MinimizeByStart.default
                )
            }
            state.value.copy(minimizeByStart = viewAction.value)
        }

        is Action.SetMinimizeByAutostart -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.MinimizeByAutostart,
                    viewAction.value ?: BoolPref.MinimizeByAutostart.default
                )
            }
            state.value.copy(minimizeByAutostart = viewAction.value)
        }

        is Action.SetAutoStartMinimizeDelay -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.AutoStartMinimizeDelay,
                    viewAction.value ?: BoolPref.AutoStartMinimizeDelay.default
                )
            }
            state.value.copy(autoStartMinimizeDelay = viewAction.value)
        }

        is Action.SetContextAdaptiveSizes -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.ContextAdaptiveSizes,
                    viewAction.value ?: BoolPref.ContextAdaptiveSizes.default
                )
            }
            state.value.copy(contextAdaptiveSizes = viewAction.value)
        }

        is Action.SetExperimentalNativeSplit -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.ExperimentalNativeSplit,
                    viewAction.value ?: BoolPref.ExperimentalNativeSplit.default
                )
            }
            state.value.copy(experimentalNativeSplit = viewAction.value)
        }

        is Action.SetSoftKillApp -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.SoftKillApp,
                    viewAction.value ?: BoolPref.SoftKillApp.default
                )
            }
            state.value.copy(softKillApp = viewAction.value)
        }

        is Action.SetStandbyMode -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.StandbyMode,
                    viewAction.value ?: BoolPref.StandbyMode.default
                )
            }
            state.value.copy(standbyMode = viewAction.value)
        }

        is Action.SetHeightCorrector -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveIntPrefUseCase.execute(
                    IntPref.HeightCorrector,
                    viewAction.value
                )
            }
            state.value.copy(heightCorrector = viewAction.value)
        }

        is Action.InitPrefs -> state.value.copy(
            bypassDelay = viewAction.bypassDelay,
            secondWindowDelay = viewAction.secondWindowDelay,
            autoPlayDelay = viewAction.autoPlayDelay,
            minimizeByStart = viewAction.minimizeByStart,
            minimizeByAutostart = viewAction.minimizeByAutostart,
            autoStartMinimizeDelay = viewAction.autoStartMinimizeDelay,
            contextAdaptiveSizes = viewAction.contextAdaptiveSizes,
            experimentalNativeSplit = viewAction.experimentalNativeSplit,
            softKillApp = viewAction.softKillApp,
            standbyMode = viewAction.standbyMode,
            heightCorrector = viewAction.heightCorrector
        )
    }

    @Immutable
    data class ViewState(
        val bypassDelay: Int = DEFAULT_BYPASS_DELAY,
        val secondWindowDelay: Int = DEFAULT_SECOND_WINDOW_DELAY,
        val autoPlayDelay: Int = DEFAULT_AUTO_PLAY_DELAY,
        val minimizeByStart: Boolean? = null,
        val minimizeByAutostart: Boolean? = null,
        val autoStartMinimizeDelay: Boolean? = null,
        val contextAdaptiveSizes: Boolean? = null,
        val experimentalNativeSplit: Boolean? = null,
        val softKillApp: Boolean? = null,
        val standbyMode: Boolean? = null,
        val heightCorrector: Int = DEFAULT_HEIGHT_CORRECTOR,
    ) : MviViewState

    sealed class Action : MviAction {
        internal class InitPrefs(
            val bypassDelay: Int,
            val secondWindowDelay: Int,
            val autoPlayDelay: Int,
            val minimizeByStart: Boolean?,
            val minimizeByAutostart: Boolean?,
            val autoStartMinimizeDelay: Boolean?,
            val contextAdaptiveSizes: Boolean?,
            val experimentalNativeSplit: Boolean?,
            val softKillApp: Boolean?,
            val standbyMode: Boolean?,
            val heightCorrector: Int
        ) : Action()

        internal class SetBypassDelay(val delay: Int) : Action()

        internal class SetSecondWindowDelay(val delay: Int) : Action()

        internal class SetAutoPlayDelay(val delay: Int) : Action()

        internal class SetMinimizeByStart(val value: Boolean?) : Action()

        internal class SetMinimizeByAutostart(val value: Boolean?) : Action()

        internal class SetAutoStartMinimizeDelay(val value: Boolean?) : Action()

        internal class SetContextAdaptiveSizes(val value: Boolean?) : Action()

        internal class SetExperimentalNativeSplit(val value: Boolean?) : Action()

        internal class SetSoftKillApp(val value: Boolean?) : Action()

        internal class SetStandbyMode(val value: Boolean?) : Action()

        internal class SetHeightCorrector(val value: Int) : Action()
    }
}
