package com.salat.settings.list.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.salat.filedownloader.domain.usecases.ClearDownloadedFilesUseCase
import com.salat.filedownloader.domain.usecases.DownloadFileUseCase
import com.salat.launchhistory.domain.entity.LastLaunchedTask
import com.salat.launchhistory.domain.usecases.GetHistoryFlowUseCase
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.LoadFloatPrefUseCase
import com.salat.remoteconfig.domain.usecases.GetAppUpdateFlowUseCase
import com.salat.settings.list.presentation.entity.DisplayAppUpdate
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.settings.list.presentation.entity.UiDownloadState
import com.salat.settings.list.presentation.mappers.toDisplay
import com.salat.settings.list.presentation.mappers.toDomain
import com.salat.settings.list.presentation.mappers.toUi
import com.salat.settings.list.presentation.route.SplitListNavRoute
import com.salat.split.list.BuildConfig
import com.salat.splitlauncher.domain.entity.SplitLaunchSource
import com.salat.splitlauncher.domain.usecases.LaunchSplitUseCase
import com.salat.splitpresets.domain.usecases.DeleteSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.GetPresetsFlowUseCase
import com.salat.splitpresets.domain.usecases.SetAutoStartSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.SetDarkBackgroundSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.SetQuickAccessSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.SetWindowShiftSplitPresetUseCase
import com.salat.systemapps.domain.usecases.FindInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import timber.log.Timber

@HiltViewModel
class ListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val launchSplitUseCase: LaunchSplitUseCase,
    private val getPresetsFlowUseCase: GetPresetsFlowUseCase,
    private val getHistoryFlowUseCase: GetHistoryFlowUseCase,
    private val findInstalledAppsUseCase: FindInstalledAppsUseCase,
    private val deleteSplitPresetUseCase: DeleteSplitPresetUseCase,
    private val setAutoStartSplitPresetUseCase: SetAutoStartSplitPresetUseCase,
    private val setDarkBackgroundSplitPresetUseCase: SetDarkBackgroundSplitPresetUseCase,
    private val setWindowShiftSplitPresetUseCase: SetWindowShiftSplitPresetUseCase,
    private val setQuickAccessSplitPresetUseCase: SetQuickAccessSplitPresetUseCase,
    private val loadFloatPrefUseCase: LoadFloatPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase,
    private val getAppUpdateFlowUseCase: GetAppUpdateFlowUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val clearDownloadedFilesUseCase: ClearDownloadedFilesUseCase
) : BaseSyncViewModel<ListViewModel.ViewState, ListViewModel.Action>(
    ViewState(toolbarExtraSize = savedStateHandle.toRoute<SplitListNavRoute>().toolbarExtraSize)
) {
    private val _uiScaleState = MutableStateFlow(1f)
    val uiScaleState = _uiScaleState.asStateFlow()

    private val _toolbarExtraSizeState = MutableStateFlow(0)
    val toolbarExtraSizeState = _toolbarExtraSizeState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {

            // Collect ui scale
            launch { _uiScaleState.emit(loadFloatPrefUseCase.execute(FloatPref.UiScale)) }
            launch {
                flowPrefsUseCase.execute(
                    FloatPref.UiScale,
                    IntPref.ToolbarExtraSpace,
                    BoolPref.ShowLastLaunchedSplit,
                    BoolPref.LastLaunchedSplitContrast,
                ).collect { prefs ->
                    if (prefs[0] is Float) {
                        _uiScaleState.update { prefs[0] as Float }
                    }
                    if (prefs[1] is Int) {
                        _toolbarExtraSizeState.update { prefs[1] as Int }
                    }

                    sendAction(
                        Action.UpdatePref(
                            showLastLaunchedSplit = prefs[2] as Boolean,
                            lastLaunchedSplitContrast = prefs[3] as Boolean
                        )
                    )
                }
            }

            launch {
                getPresetsFlowUseCase.flow.collect {
                    sendAction(Action.UpdateItems(it.toDisplay(), true))
                }
            }

            launch {
                getHistoryFlowUseCase.flow.collect { collectHistoryItem(it) }
            }

            launch {
                checkAppUpdate()
                launch {
                    Timber.d("Temp files deleted: ${clearDownloadedFilesUseCase.execute()}")
                }
            }
        }
    }

    private suspend fun collectHistoryItem(historyItem: LastLaunchedTask?) {
        if (historyItem == null) {
            sendAction(Action.UpdateHistory(null))
            return
        }

        val packages = buildList(2) {
            historyItem.firstApp?.packageName?.let { add(it) }
            historyItem.secondApp?.packageName?.let { add(it) }
        }.toTypedArray()
        val appsInfo = findInstalledAppsUseCase.execute(*packages)

        if (appsInfo.size == 2) {
            val lastLaunchPreset = try {
                historyItem.toDisplay(appsInfo, false) // TODO autoStart?
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
            sendAction(Action.UpdateHistory(lastLaunchPreset))
        } else {
            sendAction(Action.UpdateHistory(null))
        }
    }

    private fun CoroutineScope.checkAppUpdate() = launch {
        getAppUpdateFlowUseCase.flow.collect { (isSuccess, info) ->
            if (!isSuccess || info == null) return@collect

            val versionCode: Int = BuildConfig.VERSION_CODE
            if (info.code > versionCode || BuildConfig.DEBUG) {
                sendAction(Action.SetAppUpdateInfo(info.toDisplay()))
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.UpdateItems -> state.value.copy(items = viewAction.items, dataLoaded = viewAction.dataLoaded)

        is Action.UpdateHistory -> state.value.copy(history = viewAction.item)

        is Action.PrepareOpenSplit -> {
            viewModelScope.launch(Dispatchers.IO) {
                launchSplitUseCase.execute(viewAction.task.toDomain(), SplitLaunchSource.CLICK)
            }
            state.value
        }

        is Action.DeletePreset -> {
            viewModelScope.launch(Dispatchers.IO) {
                deleteSplitPresetUseCase.execute(viewAction.id)
            }
            state.value
        }

        is Action.MarkAutoStartupPreset -> {
            viewModelScope.launch(Dispatchers.IO) {
                setAutoStartSplitPresetUseCase.execute(viewAction.id, viewAction.value)
            }
            state.value
        }

        is Action.MarkDarkBackgroundPreset -> {
            viewModelScope.launch(Dispatchers.IO) {
                setDarkBackgroundSplitPresetUseCase.execute(viewAction.id, viewAction.value)
            }
            state.value
        }

        is Action.MarkWindowShiftPreset -> {
            viewModelScope.launch(Dispatchers.IO) {
                setWindowShiftSplitPresetUseCase.execute(viewAction.id, viewAction.value)
            }
            state.value
        }

        is Action.MarkQuickAccessPreset -> {
            viewModelScope.launch(Dispatchers.IO) {
                setQuickAccessSplitPresetUseCase.execute(viewAction.id, viewAction.value)
            }
            state.value
        }

        is Action.UpdatePref -> state.value.copy(
            showLastLaunchedSplit = viewAction.showLastLaunchedSplit,
            lastLaunchedSplitContrast = viewAction.lastLaunchedSplitContrast
        )

        is Action.StartDownloadUpdate -> {
            viewModelScope.launch(Dispatchers.IO) {
                Timber.d("Start download: ${viewAction.url}")
                val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
                val timestamp = formatter.format(Date())
                downloadFileUseCase.execute(viewAction.url, "${timestamp}_update.apk", "")
                    .collect { result -> sendAction(Action.SetDownloadUpdateState(result.toUi())) }
            }
            state.value
        }

        is Action.SetDownloadUpdateState -> state.value.copy(
            updateDownloadState = viewAction.value
        )

        is Action.SetAppUpdateInfo -> state.value.copy(
            appUpdateInfo = viewAction.update
        )
    }

    @Immutable
    data class ViewState(
        val items: List<DisplaySplitPreset> = emptyList(),
        val history: DisplaySplitPreset? = null,
        val toolbarExtraSize: Int = 0,
        val dataLoaded: Boolean = false,
        val showLastLaunchedSplit: Boolean = false,
        val lastLaunchedSplitContrast: Boolean = false,
        val updateDownloadState: UiDownloadState? = null,
        val appUpdateInfo: DisplayAppUpdate? = null
    ) : MviViewState

    sealed class Action : MviAction {
        internal class UpdateItems(val items: List<DisplaySplitPreset>, val dataLoaded: Boolean) : Action()
        internal class UpdateHistory(val item: DisplaySplitPreset?) : Action()
        internal class PrepareOpenSplit(val task: DisplaySplitPreset) : Action()
        internal class DeletePreset(val id: Long) : Action()
        internal class MarkAutoStartupPreset(val id: Long, val value: Boolean) : Action()
        internal class MarkDarkBackgroundPreset(val id: Long, val value: Boolean) : Action()
        internal class MarkWindowShiftPreset(val id: Long, val value: Boolean) : Action()
        internal class MarkQuickAccessPreset(val id: Long, val value: Boolean) : Action()
        internal class UpdatePref(val showLastLaunchedSplit: Boolean, val lastLaunchedSplitContrast: Boolean) : Action()
        internal class StartDownloadUpdate(val url: String) : Action()
        internal class SetDownloadUpdateState(val value: UiDownloadState?) : Action()
        internal class SetAppUpdateInfo(val update: DisplayAppUpdate?) : Action()
    }
}
