package com.salat.settings.quicksplit.presentation

import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.StringPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
import com.salat.preferences.domain.usecases.SaveStringPrefUseCase
import com.salat.settings.quicksplit.presentation.entity.DisplayQuickSplitApp
import com.salat.settings.quicksplit.presentation.mappers.toQuickSplitAppsPref
import com.salat.settings.quicksplit.presentation.mappers.toQuickSplitDisplay
import com.salat.settings.quicksplit.presentation.mappers.toQuickSplitPackages
import com.salat.systemapps.domain.usecases.FindAllInstalledAppsUseCase
import com.salat.systemapps.domain.usecases.FindInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

@HiltViewModel
class SettingsQuickSplitViewModel @Inject constructor(
    private val flowPrefsUseCase: FlowPrefsUseCase,
    private val saveBoolPrefUseCase: SaveBoolPrefUseCase,
    private val saveStringPrefUseCase: SaveStringPrefUseCase,
    private val findInstalledAppsUseCase: FindInstalledAppsUseCase,
    private val findAllInstalledAppsUseCase: FindAllInstalledAppsUseCase
) : BaseSyncViewModel<SettingsQuickSplitViewModel.ViewState, SettingsQuickSplitViewModel.Action>(ViewState()) {
    private val _uiScaleState = MutableStateFlow(1f)
    val uiScaleState = _uiScaleState.asStateFlow()

    // Saves run in the order of the changes, so the last change stays
    private val savedPackages = Channel<List<String>>(Channel.UNLIMITED)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                for (packages in savedPackages) {
                    saveStringPrefUseCase.execute(StringPref.QuickSplitApps, packages.toQuickSplitAppsPref())
                }
            }

            // The default ratio editor changes these values while this screen stays in the back stack
            launch {
                flowPrefsUseCase.execute(
                    FloatPref.UiScale,
                    FloatPref.QuickSplitRatio,
                    BoolPref.QuickSplitInsertFirst,
                    BoolPref.QuickSplitFirstCaption,
                    BoolPref.QuickSplitSecondCaption,
                    BoolPref.QuickSplitSkipSideStep,
                    BoolPref.NoCaptionWindows,
                    BoolPref.QuickSplitDarkBackground
                ).collect { prefs ->
                    _uiScaleState.value = prefs[0] as Float
                    sendAction(
                        Action.InitPrefs(
                            ratio = prefs[1] as Float,
                            insertFirst = prefs[2] as Boolean,
                            firstCaption = prefs[3] as Boolean,
                            secondCaption = prefs[4] as Boolean,
                            skipSideStep = prefs[5] as Boolean,
                            showWindowTypes = prefs[6] as Boolean && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
                            darkBackground = prefs[7] as Boolean
                        )
                    )
                }
            }

            // The reducer owns the set after this load. A later read of the store can be older than the last change
            launch {
                val packages = (flowPrefsUseCase.execute(StringPref.QuickSplitApps).firstOrNull()?.get(0) as? String)
                    .orEmpty()
                    .toQuickSplitPackages()
                val apps = findInstalledAppsUseCase.execute(*packages.toTypedArray()).toQuickSplitDisplay()
                sendAction(Action.SetApps(packages, apps))
            }

            launch {
                sendAction(Action.SetDeviceApps(findAllInstalledAppsUseCase.execute().toQuickSplitDisplay()))
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.InitPrefs -> state.value.copy(
            ratio = viewAction.ratio,
            insertFirst = viewAction.insertFirst,
            firstCaption = viewAction.firstCaption,
            secondCaption = viewAction.secondCaption,
            skipSideStep = viewAction.skipSideStep,
            showWindowTypes = viewAction.showWindowTypes,
            darkBackground = viewAction.darkBackground
        )

        is Action.SetSkipSideStep -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(BoolPref.QuickSplitSkipSideStep, viewAction.value)
            }
            state.value.copy(skipSideStep = viewAction.value)
        }

        is Action.SetDarkBackground -> {
            viewModelScope.launch(Dispatchers.IO) {
                saveBoolPrefUseCase.execute(BoolPref.QuickSplitDarkBackground, viewAction.value)
            }
            state.value.copy(darkBackground = viewAction.value)
        }

        is Action.SetApps -> state.value.copy(packages = viewAction.packages, apps = viewAction.apps)

        is Action.SetDeviceApps -> state.value.copy(deviceApps = viewAction.apps)

        is Action.AddApps -> {
            val addedApps = viewAction.apps
                .distinctBy { it.packageName }
                .filter { it.packageName !in state.value.packages }
            if (addedApps.isEmpty()) {
                state.value
            } else {
                val packages = state.value.packages + addedApps.map { it.packageName }
                savedPackages.trySend(packages)
                state.value.copy(packages = packages, apps = state.value.apps.orEmpty() + addedApps)
            }
        }

        is Action.MoveApp -> {
            val apps = state.value.apps.orEmpty()
            val from = apps.indexOfFirst { it.packageName == viewAction.fromPackage }
            val to = apps.indexOfFirst { it.packageName == viewAction.toPackage }
            if (from < 0 || to < 0 || from == to) {
                state.value
            } else {
                val moved = apps.toMutableList().apply { add(to, removeAt(from)) }
                state.value.copy(packages = moved.map { it.packageName }, apps = moved)
            }
        }

        is Action.SaveAppsOrder -> {
            savedPackages.trySend(state.value.packages)
            state.value
        }

        is Action.RemoveApp -> {
            val packages = state.value.packages - viewAction.packageName
            savedPackages.trySend(packages)
            state.value.copy(
                packages = packages,
                apps = state.value.apps?.filterNot { it.packageName == viewAction.packageName }
            )
        }
    }

    @Immutable
    data class ViewState(
        val ratio: Float = FloatPref.QuickSplitRatio.default,
        val insertFirst: Boolean = BoolPref.QuickSplitInsertFirst.default,
        val firstCaption: Boolean = false,
        val secondCaption: Boolean = false,
        val showWindowTypes: Boolean = false,
        val skipSideStep: Boolean? = null,
        val darkBackground: Boolean? = null,
        val packages: List<String> = emptyList(),
        // Null until the set loads. The grid shows the loaded set at once, without the placement animation
        val apps: List<DisplayQuickSplitApp>? = null,
        val deviceApps: List<DisplayQuickSplitApp> = emptyList()
    ) : MviViewState

    sealed class Action : MviAction {
        internal class InitPrefs(
            val ratio: Float,
            val insertFirst: Boolean,
            val firstCaption: Boolean,
            val secondCaption: Boolean,
            val skipSideStep: Boolean,
            val showWindowTypes: Boolean,
            val darkBackground: Boolean
        ) : Action()

        internal class SetSkipSideStep(val value: Boolean) : Action()
        internal class SetDarkBackground(val value: Boolean) : Action()
        internal class SetApps(val packages: List<String>, val apps: List<DisplayQuickSplitApp>) : Action()
        internal class SetDeviceApps(val apps: List<DisplayQuickSplitApp>) : Action()
        internal class AddApps(val apps: List<DisplayQuickSplitApp>) : Action()
        internal class RemoveApp(val packageName: String) : Action()
        internal class MoveApp(val fromPackage: String, val toPackage: String) : Action()
        internal data object SaveAppsOrder : Action()
    }
}
