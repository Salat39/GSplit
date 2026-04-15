package com.salat.settings.add.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.LoadFloatPrefUseCase
import com.salat.settings.add.presentation.entity.DeviceAppInfo
import com.salat.settings.add.presentation.entity.SizeFormat
import com.salat.settings.add.presentation.mappers.toDisplay
import com.salat.settings.add.presentation.mappers.toDomain
import com.salat.settings.add.presentation.route.SplitAddNavRoute
import com.salat.splitpresets.domain.entity.SplitPreset
import com.salat.splitpresets.domain.usecases.AddSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.GetPresetByIdUseCase
import com.salat.splitpresets.domain.usecases.GetPresetFreeIdUseCase
import com.salat.splitpresets.domain.usecases.UpdateSplitPresetUseCase
import com.salat.systemapps.domain.usecases.FindAllInstalledAppsUseCase
import com.salat.systemapps.domain.usecases.FindInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import presentation.BaseSyncViewModel
import presentation.mvi.MviAction
import presentation.mvi.MviViewState

@HiltViewModel
class AddViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val findAllInstalledAppsUseCase: FindAllInstalledAppsUseCase,
    private val findInstalledAppsUseCase: FindInstalledAppsUseCase,
    private val getPresetByIdUseCase: GetPresetByIdUseCase,
    private val addSplitPresetUseCase: AddSplitPresetUseCase,
    private val updateSplitPresetUseCase: UpdateSplitPresetUseCase,
    private val getPresetFreeIdUseCase: GetPresetFreeIdUseCase,
    private val loadFloatPrefUseCase: LoadFloatPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase
) : BaseSyncViewModel<AddViewModel.ViewState, AddViewModel.Action>(
    savedStateHandle.toRoute<SplitAddNavRoute>().let { data ->
        data.type?.let { type ->
            ViewState(
                editId = data.editId,
                splitForm = SizeFormat.entries.find { it.id == type } ?: SizeFormat.HALF
            )
        } ?: ViewState()
    }
) {
    private val _uiScaleState = MutableStateFlow(1f)
    val uiScaleState = _uiScaleState.asStateFlow()

    private val navData by lazy { savedStateHandle.toRoute<SplitAddNavRoute>() }

    init {
        viewModelScope.launch(Dispatchers.IO) {

            catchEditItemTask()

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
                val installedApps = findAllInstalledAppsUseCase.execute().toDisplay()
                sendAction(Action.SetDeviceApps(installedApps))
            }
        }
    }

    private fun CoroutineScope.catchEditItemTask() = launch {
        navData.editId?.let { editId ->
            getPresetByIdUseCase.execute(editId)?.let { item ->
                val installedApps =
                    findInstalledAppsUseCase.execute(item.firstApp.packageName, item.secondApp.packageName)

                val (firstApp, secondApp) = item.toDisplay(installedApps)
                val type = item.type.toDisplay()

                sendAction(Action.SetEditData(editId, type, firstApp, secondApp))
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.SetSplitForm -> state.value.copy(splitForm = viewAction.format)
        is Action.SetDeviceApps -> state.value.copy(deviceApps = viewAction.apps)
        is Action.SetBottomApp -> state.value.copy(bottomApp = viewAction.app)
        is Action.SetTopApp -> state.value.copy(topApp = viewAction.app)

        is Action.SetEditData -> state.value.copy(
            editId = viewAction.editId,
            splitForm = viewAction.splitForm,
            topApp = viewAction.topApp,
            bottomApp = viewAction.bottomApp
        )

        Action.CommitPreset -> {
            viewModelScope.launch(Dispatchers.IO) {
                val firstApp = state.value.topApp?.toDomain()
                val secondApp = state.value.bottomApp?.toDomain()

                if (firstApp != null && secondApp != null) {
                    state.value.editId?.let { editId ->
                        getPresetByIdUseCase.execute(editId)?.let { item ->
                            val buildPreset = SplitPreset(
                                firstApp = firstApp,
                                type = state.value.splitForm.toDomain(),
                                secondApp = secondApp,
                                autoStart = item.autoStart,
                                darkBackground = item.darkBackground,
                                bottomWindowShift = item.bottomWindowShift,
                                quickAccess = item.quickAccess,
                                id = editId
                            )
                            updateSplitPresetUseCase.execute(buildPreset)
                        }
                    } ?: run {
                        val buildPreset = SplitPreset(
                            firstApp = firstApp,
                            type = state.value.splitForm.toDomain(),
                            secondApp = secondApp,
                            autoStart = false,
                            darkBackground = false,
                            bottomWindowShift = false,
                            quickAccess = false,
                            id = getPresetFreeIdUseCase.execute()
                        )
                        addSplitPresetUseCase.execute(buildPreset)
                    }
                    sendAction(Action.SetCloseScreenSingleEvent(true))
                }
            }
            state.value
        }

        is Action.SetCloseScreenSingleEvent -> state.value.copy(closeScreenSingleEvent = viewAction.value)

        Action.ToggleTopAutoPlay -> state.value.copy(
            topApp = state.value.topApp?.copy(
                autoPlay = !(state.value.topApp?.autoPlay ?: true)
            )
        )

        Action.ToggleBottomAutoPlay -> state.value.copy(
            bottomApp = state.value.bottomApp?.copy(
                autoPlay = !(state.value.bottomApp?.autoPlay ?: true)
            )
        )
    }

    @Immutable
    data class ViewState(
        val editId: Long? = null,
        val splitForm: SizeFormat = SizeFormat.HALF,
        val deviceApps: List<DeviceAppInfo> = emptyList(),
        val topApp: DeviceAppInfo? = null,
        val bottomApp: DeviceAppInfo? = null,
        val closeScreenSingleEvent: Boolean? = null
    ) : MviViewState

    sealed class Action : MviAction {
        internal class SetSplitForm(val format: SizeFormat) : Action()
        internal class SetDeviceApps(val apps: List<DeviceAppInfo>) : Action()
        internal class SetTopApp(val app: DeviceAppInfo?) : Action()
        internal class SetBottomApp(val app: DeviceAppInfo?) : Action()
        internal class SetCloseScreenSingleEvent(val value: Boolean?) : Action()

        internal class SetEditData(
            val editId: Long?,
            val splitForm: SizeFormat,
            val topApp: DeviceAppInfo?,
            val bottomApp: DeviceAppInfo?
        ) : Action()

        data object CommitPreset : Action()
        data object ToggleTopAutoPlay : Action()
        data object ToggleBottomAutoPlay : Action()
    }
}
