package com.salat.settings.windowshiftmode.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
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

internal const val MAX_BOTTOM_WINDOW_SHIFT_SIZE = 230

internal const val DEFAULT_BOTTOM_WINDOW_SHIFT_SIZE = 0
internal const val DEFAULT_UI_SCALE = 1f

@HiltViewModel
class SettingsWindowShiftModeViewModel @Inject constructor(
    private val saveIntPrefUseCase: SaveIntPrefUseCase,
    private val saveBoolPrefUseCase: SaveBoolPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase,
    private val checkAccessibilityServiceEnabledUseCase: CheckAccessibilityServiceEnabledUseCase
) : BaseSyncViewModel<SettingsWindowShiftModeViewModel.ViewState, SettingsWindowShiftModeViewModel.Action>(
    ViewState()
) {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                val collectedPreferences = flowPrefsUseCase.execute(
                    FloatPref.UiScale,
                    IntPref.BottomWindowShiftSize,
                    BoolPref.AutoRefocusWhenBottomWindowShift
                ).firstOrNull()

                collectedPreferences?.let { prefs ->
                    sendAction(
                        Action.InitPrefs(
                            uiScale = prefs[0] as Float,
                            bottomWindowShiftSize = prefs[1] as Int,
                            autoRefocusWhenBottomWindowShift = prefs[2] as Boolean,
                        )
                    )
                }
            }

            // Collect Accessibility Service enabled
            launch {
                checkAccessibilityServiceEnabledUseCase.flow.collect {
                    sendAction(Action.SetAccessibilityServiceEnabled(it))
                }
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.SetBottomWindowShiftSize -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveIntPrefUseCase.execute(
                    IntPref.BottomWindowShiftSize,
                    viewAction.value
                )
            }
            state.value.copy(bottomWindowShiftSize = viewAction.value)
        }

        is Action.SetAutoRefocusWhenBottomWindowShift -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.AutoRefocusWhenBottomWindowShift,
                    viewAction.value
                )
            }
            state.value.copy(autoRefocusWhenBottomWindowShift = viewAction.value)
        }

        is Action.InitPrefs -> state.value.copy(
            uiScale = viewAction.uiScale,
            bottomWindowShiftSize = viewAction.bottomWindowShiftSize,
            autoRefocusWhenBottomWindowShift = viewAction.autoRefocusWhenBottomWindowShift
        )

        is Action.SetAccessibilityServiceEnabled -> state.value.copy(
            accessibilityServiceEnabled = viewAction.value
        )
    }

    @Immutable
    data class ViewState(
        val accessibilityServiceEnabled: Boolean = false,
        val uiScale: Float = DEFAULT_UI_SCALE,
        val bottomWindowShiftSize: Int = DEFAULT_BOTTOM_WINDOW_SHIFT_SIZE,
        val autoRefocusWhenBottomWindowShift: Boolean? = null,
    ) : MviViewState

    sealed class Action : MviAction {
        internal class InitPrefs(
            val uiScale: Float,
            val bottomWindowShiftSize: Int,
            val autoRefocusWhenBottomWindowShift: Boolean
        ) : Action()

        internal class SetAccessibilityServiceEnabled(val value: Boolean) : Action()

        internal class SetBottomWindowShiftSize(val value: Int) : Action()

        internal class SetAutoRefocusWhenBottomWindowShift(val value: Boolean) : Action()
    }
}
