package com.salat.settings.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
import com.salat.preferences.domain.usecases.SaveFloatPrefUseCase
import com.salat.preferences.domain.usecases.SaveIntPrefUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

internal const val MAX_UI_SCALE = 2.5f
internal const val MAX_TOOLBAR_EXTRA_SPACE = 230

internal const val DEFAULT_UI_SCALE = 1f
internal const val DEFAULT_TOOLBAR_EXTRA_SPACE = 0

@HiltViewModel
class SettingsUiViewModel @Inject constructor(
    private val saveIntPrefUseCase: SaveIntPrefUseCase,
    private val saveFloatPrefUseCase: SaveFloatPrefUseCase,
    private val saveBoolPrefUseCase: SaveBoolPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase
) : BaseSyncViewModel<SettingsUiViewModel.ViewState, SettingsUiViewModel.Action>(ViewState()) {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val collectedPreferences = flowPrefsUseCase.execute(
                FloatPref.UiScale,
                IntPref.ToolbarExtraSpace,
                BoolPref.ShowLastLaunchedSplit,
                BoolPref.LastLaunchedSplitContrast
            ).firstOrNull()

            collectedPreferences?.let { prefs ->
                sendAction(
                    Action.InitPrefs(
                        uiScale = prefs[0] as Float,
                        toolbarExtraSpace = prefs[1] as Int,
                        showLastLaunchedSplit = prefs[2] as Boolean,
                        lastLaunchedSplitContrast = prefs[3] as Boolean,
                    )
                )
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

        is Action.SetToolbarExtraSpace -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveIntPrefUseCase.execute(
                    IntPref.ToolbarExtraSpace,
                    viewAction.value
                )
            }
            state.value.copy(toolbarExtraSpace = viewAction.value)
        }

        is Action.SetShowLastLaunchedSplit -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.ShowLastLaunchedSplit,
                    viewAction.value
                )
            }
            state.value.copy(showLastLaunchedSplit = viewAction.value)
        }

        is Action.SetLastLaunchedSplitContrast -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(
                    BoolPref.LastLaunchedSplitContrast,
                    viewAction.value
                )
            }
            state.value.copy(lastLaunchedSplitContrast = viewAction.value)
        }

        is Action.InitPrefs -> state.value.copy(
            uiScale = viewAction.uiScale,
            toolbarExtraSpace = viewAction.toolbarExtraSpace,
            showLastLaunchedSplit = viewAction.showLastLaunchedSplit,
            lastLaunchedSplitContrast = viewAction.lastLaunchedSplitContrast
        )
    }

    @Immutable
    data class ViewState(
        val uiScale: Float = DEFAULT_UI_SCALE,
        val toolbarExtraSpace: Int = DEFAULT_TOOLBAR_EXTRA_SPACE,
        val showLastLaunchedSplit: Boolean = false,
        val lastLaunchedSplitContrast: Boolean = false,
    ) : MviViewState

    sealed class Action : MviAction {
        internal class InitPrefs(
            val uiScale: Float,
            val toolbarExtraSpace: Int,
            val showLastLaunchedSplit: Boolean,
            val lastLaunchedSplitContrast: Boolean,
        ) : Action()

        internal class SetUiScale(val scale: Float) : Action()

        internal class SetToolbarExtraSpace(val value: Int) : Action()

        internal class SetShowLastLaunchedSplit(val value: Boolean) : Action()

        internal class SetLastLaunchedSplitContrast(val value: Boolean) : Action()
    }
}
