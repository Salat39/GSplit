package com.salat.settings.replacementapps

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.LoadFloatPrefUseCase
import com.salat.replacementappsstorage.domain.entity.ReplacementAppItem
import com.salat.replacementappsstorage.domain.useCases.AddReplacementAppItemUseCase
import com.salat.replacementappsstorage.domain.useCases.DeleteReplacementAppItemUseCase
import com.salat.replacementappsstorage.domain.useCases.GetReplacementAppsFlowUseCase
import com.salat.replacementappsstorage.domain.useCases.GetReplacementAppsFreeIdUseCase
import com.salat.settings.replacementapps.entity.DeviceAppInfo
import com.salat.settings.replacementapps.entity.DisplayReplacementAppItem
import com.salat.settings.replacementapps.mappers.toAppsDisplay
import com.salat.settings.replacementapps.mappers.toDisplay
import com.salat.systemapps.domain.usecases.FindAllInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

@HiltViewModel
class SettingsReplacementAppsViewModel @Inject constructor(
    private val findAllInstalledAppsUseCase: FindAllInstalledAppsUseCase,
    private val loadFloatPrefUseCase: LoadFloatPrefUseCase,
    private val getReplacementAppsFlowUseCase: GetReplacementAppsFlowUseCase,
    private val addReplacementAppItemUseCase: AddReplacementAppItemUseCase,
    private val deleteReplacementAppItemUseCase: DeleteReplacementAppItemUseCase,
    private val getReplacementAppsFreeIdUseCase: GetReplacementAppsFreeIdUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase
) : BaseSyncViewModel<SettingsReplacementAppsViewModel.ViewState, SettingsReplacementAppsViewModel.Action>
    (ViewState()) {
    private val _uiScaleState = MutableStateFlow(1f)
    val uiScaleState = _uiScaleState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {

            // Collect ui scale
            launch { _uiScaleState.emit(loadFloatPrefUseCase.execute(FloatPref.UiScale)) }
            launch {
                flowPrefsUseCase.execute(
                    FloatPref.UiScale
                ).collect { prefs ->
                    if (prefs[0] is Float) {
                        _uiScaleState.update { prefs[0] as Float }
                    }
                }
            }

            launch {
                val installedApps = findAllInstalledAppsUseCase.execute().toAppsDisplay()
                sendAction(Action.SetDeviceApps(installedApps))
            }

            launch {
                getReplacementAppsFlowUseCase.flow.collect {
                    sendAction(Action.UpdateItems(it.toDisplay(), true))
                }
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.UpdateItems -> state.value.copy(items = viewAction.items, dataLoaded = viewAction.dataLoaded)

        is Action.SetDeviceApps -> state.value.copy(deviceApps = viewAction.apps)

        is Action.AddReplacementAppItem -> {
            viewModelScope.launch(Dispatchers.IO) {
                addReplacementAppItemUseCase.execute(
                    ReplacementAppItem(
                        title = viewAction.app.appName,
                        packageName = viewAction.app.packageName,
                        firstWindow = viewAction.firstWindow,
                        secondWindow = viewAction.secondWindow,
                        autoPlay = viewAction.autoPlay,
                        icon = viewAction.app.icon,
                        id = getReplacementAppsFreeIdUseCase.execute()
                    )
                )
            }
            state.value
        }

        is Action.DeleteReplacementAppItem -> {
            viewModelScope.launch(Dispatchers.IO) {
                deleteReplacementAppItemUseCase.execute(viewAction.id)
            }
            state.value
        }
    }

    @Immutable
    data class ViewState(
        val deviceApps: List<DeviceAppInfo> = emptyList(),
        val items: List<DisplayReplacementAppItem> = emptyList(),
        val dataLoaded: Boolean = false
    ) : MviViewState

    sealed class Action : MviAction {
        internal class UpdateItems(val items: List<DisplayReplacementAppItem>, val dataLoaded: Boolean) : Action()
        internal class SetDeviceApps(val apps: List<DeviceAppInfo>) : Action()
        internal class AddReplacementAppItem(
            val app: DeviceAppInfo,
            val firstWindow: Boolean,
            val secondWindow: Boolean,
            val autoPlay: Boolean
        ) : Action()

        internal class DeleteReplacementAppItem(val id: Long) : Action()
    }
}
