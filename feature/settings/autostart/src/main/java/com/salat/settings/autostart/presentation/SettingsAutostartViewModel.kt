package com.salat.settings.autostart.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
import com.salat.preferences.domain.usecases.SaveFloatPrefUseCase
import com.salat.preferences.domain.usecases.SaveIntPrefUseCase
import com.salat.statekeeper.domain.usecases.CheckAccessibilityServiceEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

internal const val MAX_AUTOSTART_DELAY = 30_000

internal const val DEFAULT_UI_SCALE = 1f
internal const val DEFAULT_AUTOSTART_DELAY = 2000

@HiltViewModel
class SettingsAutostartViewModel @Inject constructor(
    private val saveIntPrefUseCase: SaveIntPrefUseCase,
    private val saveFloatPrefUseCase: SaveFloatPrefUseCase,
    private val saveBoolPrefUseCase: SaveBoolPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase,
    private val checkAccessibilityServiceEnabledUseCase: CheckAccessibilityServiceEnabledUseCase,
) : BaseSyncViewModel<SettingsAutostartViewModel.ViewState, SettingsAutostartViewModel.Action>(ViewState()) {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val collectedPreferences = flowPrefsUseCase.execute(
                FloatPref.UiScale,
                BoolPref.SelfAutostart,
                BoolPref.SelfAutostartInBg,
                BoolPref.SelfAutostartByConnect,
                IntPref.AutostartDelay
            ).firstOrNull()

            collectedPreferences?.let { prefs ->
                sendAction(
                    Action.InitPrefs(
                        uiScale = prefs[0] as Float,
                        selfAutostart = prefs[1] as Boolean,
                        selfAutostartInBg = prefs[2] as Boolean,
                        selfAutostartByConnect = prefs[3] as Boolean,
                        autostartDelay = prefs[4] as Int,
                    )
                )
            }
        }
        // Collect Accessibility Service enabled
        viewModelScope.launch(Dispatchers.IO) {
            checkAccessibilityServiceEnabledUseCase.flow.collect {
                sendAction(Action.SetAccessibilityServiceEnabled(it))
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.SetUiScale -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveFloatPrefUseCase.execute(
                    FloatPref.UiScale,
                    viewAction.scale
                )
            }
            state.value.copy(uiScale = viewAction.scale)
        }

        is Action.SetSelfAutostart -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.SelfAutostart,
                    viewAction.value ?: BoolPref.SelfAutostart.default
                )
            }
            state.value.copy(selfAutostart = viewAction.value)
        }

        is Action.SetSelfAutostartInBg -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.SelfAutostartInBg,
                    viewAction.value ?: BoolPref.SelfAutostartInBg.default
                )
            }
            state.value.copy(selfAutostartInBg = viewAction.value)
        }

        is Action.SetSelfAutostartByConnect -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.SelfAutostartByConnect,
                    viewAction.value ?: BoolPref.SelfAutostartByConnect.default
                )
            }
            state.value.copy(selfAutostartByConnect = viewAction.value)
        }

        is Action.SetAutostartDelay -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveIntPrefUseCase.execute(
                    IntPref.AutostartDelay,
                    viewAction.value
                )
            }
            state.value.copy(autostartDelay = viewAction.value)
        }

        is Action.InitPrefs -> state.value.copy(
            uiScale = viewAction.uiScale,
            selfAutostart = viewAction.selfAutostart,
            selfAutostartInBg = viewAction.selfAutostartInBg,
            selfAutostartByConnect = viewAction.selfAutostartByConnect,
            autostartDelay = viewAction.autostartDelay,
        )

        is Action.SetAccessibilityServiceEnabled -> state.value.copy(
            accessibilityServiceEnabled = viewAction.value
        )
    }

    @Immutable
    data class ViewState(
        val accessibilityServiceEnabled: Boolean = false,
        val uiScale: Float = DEFAULT_UI_SCALE,
        val selfAutostart: Boolean? = null,
        val selfAutostartInBg: Boolean? = null,
        val selfAutostartByConnect: Boolean? = null,
        val autostartDelay: Int = DEFAULT_AUTOSTART_DELAY
    ) : MviViewState

    sealed class Action : MviAction {
        internal class InitPrefs(
            val uiScale: Float,
            val selfAutostart: Boolean?,
            val selfAutostartInBg: Boolean?,
            val selfAutostartByConnect: Boolean?,
            val autostartDelay: Int,
        ) : Action()

        internal class SetAccessibilityServiceEnabled(val value: Boolean) : Action()

        internal class SetUiScale(val scale: Float) : Action()

        internal class SetSelfAutostart(val value: Boolean?) : Action()

        internal class SetSelfAutostartInBg(val value: Boolean?) : Action()

        internal class SetSelfAutostartByConnect(val value: Boolean?) : Action()

        internal class SetAutostartDelay(val value: Int) : Action()
    }
}
