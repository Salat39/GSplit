package com.salat.settings.closingOverlay.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.BoolSharedPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.FloatSharedPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.entity.IntSharedPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.LoadBoolSharedPrefUseCase
import com.salat.preferences.domain.usecases.LoadFloatSharedPrefUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
import com.salat.preferences.domain.usecases.SaveBoolSharedPrefUseCase
import com.salat.preferences.domain.usecases.SaveFloatSharedPrefUseCase
import com.salat.preferences.domain.usecases.SaveIntPrefUseCase
import com.salat.preferences.domain.usecases.SaveIntSharedPrefUseCase
import com.salat.statekeeper.domain.usecases.CheckAccessibilityServiceEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

@HiltViewModel
class SettingsClosingOverlayViewModel @Inject constructor(
    private val saveBoolPrefUseCase: SaveBoolPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase,
    private val saveIntPrefUseCase: SaveIntPrefUseCase,
    private val loadFloatSharedPrefUseCase: LoadFloatSharedPrefUseCase,
    private val saveFloatSharedPrefUseCase: SaveFloatSharedPrefUseCase,
    private val loadBoolSharedPrefUseCase: LoadBoolSharedPrefUseCase,
    private val saveBoolSharedPrefUseCase: SaveBoolSharedPrefUseCase,
    private val saveIntSharedPrefUseCase: SaveIntSharedPrefUseCase,
    private val checkAccessibilityServiceEnabledUseCase: CheckAccessibilityServiceEnabledUseCase
) : BaseSyncViewModel<SettingsClosingOverlayViewModel.ViewState, SettingsClosingOverlayViewModel.Action>(ViewState()) {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                sendAction(
                    Action.InitPref(
                        overlayEnabled = loadBoolSharedPrefUseCase.execute(BoolSharedPref.CloseOverlayEnabled),
                        overlayLock = loadBoolSharedPrefUseCase.execute(BoolSharedPref.CloseOverlayLock),
                        overlayScale = loadFloatSharedPrefUseCase.execute(FloatSharedPref.CloseOverlayScale),
                        overlayBgAlpha = loadFloatSharedPrefUseCase.execute(FloatSharedPref.CloseOverlayBgAlpha),
                        overlayIconAlpha = loadFloatSharedPrefUseCase.execute(FloatSharedPref.CloseOverlayIconAlpha)
                    )
                )
            }

            launch {
                val collectedPreferences = flowPrefsUseCase.execute(
                    BoolPref.CloseWindowDodgeSystemGes,
                    BoolPref.CloseWindowSequential,
                    IntPref.WindowClosingExtraPause,
                    FloatPref.UiScale
                ).firstOrNull()
                collectedPreferences?.let { prefs ->
                    sendAction(
                        Action.InitDSPrefs(
                            closeWindowDodgeSystemGes = prefs[0] as Boolean,
                            closeWindowSequential = prefs[1] as Boolean,
                            windowClosingExtraPause = prefs[2] as Int,
                            uiScale = prefs[3] as Float
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
        is Action.InitPref -> state.value.copy(
            overlayEnabled = viewAction.overlayEnabled,
            overlayLock = viewAction.overlayLock,
            overlayScale = viewAction.overlayScale,
            overlayBgAlpha = viewAction.overlayBgAlpha,
            overlayIconAlpha = viewAction.overlayIconAlpha
        )

        is Action.InitDSPrefs -> state.value.copy(
            closeWindowDodgeSystemGes = viewAction.closeWindowDodgeSystemGes,
            closeWindowSequential = viewAction.closeWindowSequential,
            windowClosingExtraPause = viewAction.windowClosingExtraPause,
            uiScale = viewAction.uiScale
        )

        is Action.SetOverlayEnabled -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolSharedPrefUseCase.execute(BoolSharedPref.CloseOverlayEnabled, viewAction.value)

                // Determining whether to run the overlay service. If at least one overlay is enabled
                val otherOverlayValue = loadBoolSharedPrefUseCase.execute(BoolSharedPref.ReplaceOverlayEnabled)
                saveBoolPrefUseCase.execute(BoolPref.EnableOverlays, otherOverlayValue || viewAction.value)
            }
            state.value.copy(overlayEnabled = viewAction.value)
        }

        is Action.SetOverlayLock -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolSharedPrefUseCase.execute(BoolSharedPref.CloseOverlayLock, viewAction.value)
            }
            state.value.copy(overlayLock = viewAction.value)
        }

        is Action.SetOverlayBgAlpha -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveFloatSharedPrefUseCase.execute(FloatSharedPref.CloseOverlayBgAlpha, viewAction.value)
            }
            state.value.copy(overlayBgAlpha = viewAction.value)
        }

        is Action.SetOverlayIconAlpha -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveFloatSharedPrefUseCase.execute(FloatSharedPref.CloseOverlayIconAlpha, viewAction.value)
            }
            state.value.copy(overlayIconAlpha = viewAction.value)
        }

        is Action.SetOverlayScale -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveFloatSharedPrefUseCase.execute(FloatSharedPref.CloseOverlayScale, viewAction.value)
            }
            state.value.copy(overlayScale = viewAction.value)
        }

        is Action.SetCloseWindowDodgeSystemGes -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(BoolPref.CloseWindowDodgeSystemGes, viewAction.value)
            }
            state.value.copy(closeWindowDodgeSystemGes = viewAction.value)
        }

        is Action.SetCloseWindowSequential -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(BoolPref.CloseWindowSequential, viewAction.value)
            }
            state.value.copy(closeWindowSequential = viewAction.value)
        }

        is Action.SetWindowClosingExtraPause -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveIntPrefUseCase.execute(IntPref.WindowClosingExtraPause, viewAction.value)
            }
            state.value.copy(windowClosingExtraPause = viewAction.value)
        }

        Action.ResetOverlayPosition -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveIntSharedPrefUseCase.execute(IntSharedPref.CloseOverlayY, -1)
                saveIntSharedPrefUseCase.execute(IntSharedPref.CloseOverlayX, -1)
            }
            state.value
        }

        is Action.SetAccessibilityServiceEnabled -> state.value.copy(
            accessibilityServiceEnabled = viewAction.value
        )
    }

    @Immutable
    data class ViewState(
        val accessibilityServiceEnabled: Boolean = false,
        val overlayEnabled: Boolean? = null,
        val overlayLock: Boolean? = null,
        val overlayScale: Float = 0f,
        val overlayBgAlpha: Float = 0f,
        val overlayIconAlpha: Float = 0f,
        val closeWindowDodgeSystemGes: Boolean? = null,
        val closeWindowSequential: Boolean? = null,
        val windowClosingExtraPause: Int = 100,
        val uiScale: Float = 1f
    ) : MviViewState

    sealed class Action : MviAction {
        internal class InitPref(
            val overlayEnabled: Boolean,
            val overlayLock: Boolean,
            val overlayScale: Float,
            val overlayBgAlpha: Float,
            val overlayIconAlpha: Float
        ) : Action()

        internal class InitDSPrefs(
            val closeWindowDodgeSystemGes: Boolean,
            val closeWindowSequential: Boolean,
            val windowClosingExtraPause: Int,
            val uiScale: Float
        ) : Action()

        internal class SetAccessibilityServiceEnabled(val value: Boolean) : Action()
        data class SetOverlayEnabled(val value: Boolean) : Action()
        data class SetOverlayLock(val value: Boolean) : Action()
        data class SetOverlayScale(val value: Float) : Action()
        data class SetOverlayBgAlpha(val value: Float) : Action()
        data class SetOverlayIconAlpha(val value: Float) : Action()
        data class SetCloseWindowDodgeSystemGes(val value: Boolean) : Action()
        data class SetCloseWindowSequential(val value: Boolean) : Action()
        data class SetWindowClosingExtraPause(val value: Int) : Action()
        data object ResetOverlayPosition : Action()
    }
}
