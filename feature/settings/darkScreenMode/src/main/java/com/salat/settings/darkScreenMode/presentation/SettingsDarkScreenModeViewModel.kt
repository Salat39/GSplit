package com.salat.settings.darkScreenMode.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
import com.salat.statekeeper.domain.usecases.CheckAccessibilityServiceEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

internal const val DEFAULT_UI_SCALE = 1f

@HiltViewModel
class SettingsDarkScreenModeViewModel @Inject constructor(
    private val saveBoolPrefUseCase: SaveBoolPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase,
    private val checkAccessibilityServiceEnabledUseCase: CheckAccessibilityServiceEnabledUseCase
) : BaseSyncViewModel<SettingsDarkScreenModeViewModel.ViewState, SettingsDarkScreenModeViewModel.Action>(
    ViewState()
) {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                val collectedPreferences = flowPrefsUseCase.execute(
                    FloatPref.UiScale,
                    BoolPref.DarkScreenAutoClose,
                    BoolPref.DarkScreenBackButton,
                ).firstOrNull()

                collectedPreferences?.let { prefs ->
                    sendAction(
                        Action.InitPrefs(
                            uiScale = prefs[0] as Float,
                            darkScreenAutoClose = prefs[1] as Boolean,
                            darkScreenBackButton = prefs[2] as Boolean,
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
        is Action.SetDarkScreenAutoClose -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.DarkScreenAutoClose,
                    viewAction.value
                )
            }
            state.value.copy(darkScreenAutoClose = viewAction.value)
        }

        is Action.SetDarkScreenBackButton -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.DarkScreenBackButton,
                    viewAction.value
                )
            }
            state.value.copy(darkScreenBackButton = viewAction.value)
        }

        is Action.InitPrefs -> state.value.copy(
            uiScale = viewAction.uiScale,
            darkScreenAutoClose = viewAction.darkScreenAutoClose,
            darkScreenBackButton = viewAction.darkScreenBackButton,
        )

        is Action.SetAccessibilityServiceEnabled -> state.value.copy(
            accessibilityServiceEnabled = viewAction.value
        )
    }

    @Immutable
    data class ViewState(
        val accessibilityServiceEnabled: Boolean = false,
        val uiScale: Float = DEFAULT_UI_SCALE,
        val darkScreenAutoClose: Boolean? = null,
        val darkScreenBackButton: Boolean? = null,
    ) : MviViewState

    sealed class Action : MviAction {
        internal class InitPrefs(
            val uiScale: Float,
            val darkScreenAutoClose: Boolean,
            val darkScreenBackButton: Boolean,
        ) : Action()

        internal class SetAccessibilityServiceEnabled(val value: Boolean) : Action()

        internal class SetDarkScreenAutoClose(val value: Boolean) : Action()

        internal class SetDarkScreenBackButton(val value: Boolean) : Action()
    }
}
