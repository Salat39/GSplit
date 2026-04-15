package com.salat.settings.scheduler.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.LoadFloatPrefUseCase
import com.salat.schedulerstorage.domain.entity.ScheduledItem
import com.salat.schedulerstorage.domain.useCases.AddScheduledItemUseCase
import com.salat.schedulerstorage.domain.useCases.DeleteScheduledItemUseCase
import com.salat.schedulerstorage.domain.useCases.GetSchedulerFlowUseCase
import com.salat.schedulerstorage.domain.useCases.GetSchedulerFreeIdUseCase
import com.salat.settings.scheduler.presentation.entity.DeviceAppInfo
import com.salat.settings.scheduler.presentation.entity.DisplayScheduledItem
import com.salat.settings.scheduler.presentation.entity.ScheduledApp
import com.salat.settings.scheduler.presentation.mappers.toAppsDisplay
import com.salat.settings.scheduler.presentation.mappers.toDisplay
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
class SettingsSchedulerViewModel @Inject constructor(
    private val findAllInstalledAppsUseCase: FindAllInstalledAppsUseCase,
    private val loadFloatPrefUseCase: LoadFloatPrefUseCase,
    private val getSchedulerFlowUseCase: GetSchedulerFlowUseCase,
    private val addScheduledItemUseCase: AddScheduledItemUseCase,
    private val deleteScheduledItemUseCase: DeleteScheduledItemUseCase,
    private val getSchedulerFreeIdUseCase: GetSchedulerFreeIdUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase
) : BaseSyncViewModel<SettingsSchedulerViewModel.ViewState, SettingsSchedulerViewModel.Action>(ViewState()) {
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
                getSchedulerFlowUseCase.flow.collect {
                    sendAction(Action.UpdateItems(it.toDisplay(), true))
                }
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.UpdateItems -> state.value.copy(items = viewAction.items, dataLoaded = viewAction.dataLoaded)

        is Action.SetDeviceApps -> state.value.copy(deviceApps = viewAction.apps)

        is Action.AddScheduledItem -> {
            viewModelScope.launch(Dispatchers.IO) {
                addScheduledItemUseCase.execute(
                    ScheduledItem(
                        title = viewAction.scheduledData.app.appName,
                        packageName = viewAction.scheduledData.app.packageName,
                        delay = viewAction.scheduledData.time,
                        icon = viewAction.scheduledData.app.icon,
                        preTask = viewAction.scheduledData.isPreTask,
                        autoPlay = viewAction.scheduledData.isAutoPlay,
                        id = getSchedulerFreeIdUseCase.execute()
                    )
                )
            }
            state.value
        }

        is Action.DeleteScheduledItem -> {
            viewModelScope.launch(Dispatchers.IO) {
                deleteScheduledItemUseCase.execute(viewAction.id)
            }
            state.value
        }
    }

    @Immutable
    data class ViewState(
        val deviceApps: List<DeviceAppInfo> = emptyList(),
        val items: List<DisplayScheduledItem> = emptyList(),
        val dataLoaded: Boolean = false
    ) : MviViewState

    sealed class Action : MviAction {
        internal class UpdateItems(val items: List<DisplayScheduledItem>, val dataLoaded: Boolean) : Action()
        internal class SetDeviceApps(val apps: List<DeviceAppInfo>) : Action()
        internal class AddScheduledItem(val scheduledData: ScheduledApp) : Action()
        internal class DeleteScheduledItem(val id: Long) : Action()
    }
}
