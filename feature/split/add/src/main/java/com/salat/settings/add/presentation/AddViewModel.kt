package com.salat.settings.add.presentation

import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.LoadFloatPrefUseCase
import com.salat.settings.add.presentation.entity.DeviceAppInfo
import com.salat.settings.add.presentation.entity.DisplayFreeWindow
import com.salat.settings.add.presentation.entity.SizeFormat
import com.salat.settings.add.presentation.mappers.toDisplay
import com.salat.settings.add.presentation.mappers.toDomainPreset
import com.salat.settings.add.presentation.route.SplitAddNavRoute
import com.salat.splitlauncher.domain.usecases.GetNoCaptionWindowsFlowUseCase
import com.salat.splitpresets.domain.entity.PresetType
import com.salat.splitpresets.domain.usecases.AddSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.GetPresetByIdUseCase
import com.salat.splitpresets.domain.usecases.GetPresetFreeIdUseCase
import com.salat.splitpresets.domain.usecases.UpdateSplitPresetUseCase
import com.salat.systemapps.domain.usecases.FindAllInstalledAppsUseCase
import com.salat.systemapps.domain.usecases.FindInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt
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
class AddViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val findAllInstalledAppsUseCase: FindAllInstalledAppsUseCase,
    private val findInstalledAppsUseCase: FindInstalledAppsUseCase,
    private val getPresetByIdUseCase: GetPresetByIdUseCase,
    private val addSplitPresetUseCase: AddSplitPresetUseCase,
    private val updateSplitPresetUseCase: UpdateSplitPresetUseCase,
    private val getPresetFreeIdUseCase: GetPresetFreeIdUseCase,
    private val loadFloatPrefUseCase: LoadFloatPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase,
    private val getNoCaptionWindowsFlowUseCase: GetNoCaptionWindowsFlowUseCase
) : BaseSyncViewModel<AddViewModel.ViewState, AddViewModel.Action>(
    savedStateHandle.toRoute<SplitAddNavRoute>().let { data ->
        data.type?.let { type ->
            ViewState(
                editId = data.editId,
                splitForm = SizeFormat.entries.find { it.id == type } ?: SizeFormat.HALF,
                freeMode = type == PresetType.FREE.id,
                isFreePresetEdit = data.editId != null && type == PresetType.FREE.id
            )
        } ?: ViewState()
    }
) {
    private val _uiScaleState = MutableStateFlow(1f)
    val uiScaleState = _uiScaleState.asStateFlow()

    private val navData by lazy { savedStateHandle.toRoute<SplitAddNavRoute>() }

    private val isCommitInProgress = AtomicBoolean(false)

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

            launch {
                getNoCaptionWindowsFlowUseCase.flow.collect {
                    sendAction(Action.SetWindowTypeAvailable(it))
                }
            }

            launch {
                flowPrefsUseCase.execute(BoolPref.ExperimentalNativeSplit, BoolPref.NoCaptionWindows).collect { prefs ->
                    sendAction(
                        Action.SetWindowTypePrefs(
                            isNativeSplitEnabled = prefs[0] as Boolean,
                            isWindowTypeEnabled = prefs[1] as Boolean &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                        )
                    )
                }
            }
        }
    }

    private fun CoroutineScope.catchEditItemTask() = launch {
        navData.editId?.let { editId ->
            getPresetByIdUseCase.execute(editId)?.let { item ->
                if (item.type == PresetType.FREE) {
                    val packages = item.windows.map { it.app.packageName }.distinct().toTypedArray()
                    val installedApps = findInstalledAppsUseCase.execute(*packages)
                    sendAction(Action.SetFreeWindows(item.windows.toDisplay(installedApps)))
                    return@let
                }

                val installedApps =
                    findInstalledAppsUseCase.execute(item.firstApp.packageName, item.secondApp.packageName)

                val (firstApp, secondApp) = item.toDisplay(installedApps)
                val type = item.type.toDisplay() ?: SizeFormat.HALF

                sendAction(Action.SetEditData(editId, type, firstApp, secondApp, item.ratio))
            }
        }
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.SetSplitForm -> if (viewAction.format == SizeFormat.CUSTOM) {
            state.value.copy(splitForm = SizeFormat.CUSTOM, customRatio = state.value.windowRatio)
        } else {
            state.value.copy(splitForm = viewAction.format)
        }

        is Action.SetCustomRatio -> state.value.copy(
            customRatio = viewAction.ratio.coerceIn(MIN_CUSTOM_RATIO, MAX_CUSTOM_RATIO)
        )

        Action.RoundCustomRatio -> state.value.copy(
            customRatio = (state.value.customRatio * PERCENTS).roundToInt() / PERCENTS
        )

        Action.ShowPresets -> state.value.copy(splitForm = state.value.customRatio.nearestPresetFormat())

        is Action.SetDeviceApps -> state.value.copy(deviceApps = viewAction.apps)
        is Action.SetBottomApp -> state.value.copy(
            bottomApp = viewAction.app?.copy(withCaption = state.value.bottomApp?.withCaption ?: false)
        )

        is Action.SetTopApp -> state.value.copy(
            topApp = viewAction.app?.copy(withCaption = state.value.topApp?.withCaption ?: false)
        )

        is Action.SetTopWithCaption -> state.value.copy(
            topApp = state.value.topApp?.copy(withCaption = viewAction.value)
        )

        is Action.SetBottomWithCaption -> state.value.copy(
            bottomApp = state.value.bottomApp?.copy(withCaption = viewAction.value)
        )

        is Action.SetWindowTypeAvailable -> state.value.copy(isWindowTypeAvailable = viewAction.value)

        is Action.SetWindowTypePrefs -> state.value.copy(
            isNativeSplitEnabled = viewAction.isNativeSplitEnabled,
            isWindowTypeEnabled = viewAction.isWindowTypeEnabled
        )

        Action.SwapApps -> state.value.copy(topApp = state.value.bottomApp, bottomApp = state.value.topApp)

        is Action.SetEditData -> state.value.copy(
            editId = viewAction.editId,
            splitForm = viewAction.splitForm,
            topApp = viewAction.topApp,
            bottomApp = viewAction.bottomApp,
            customRatio = viewAction.customRatio
        )

        Action.CommitPreset -> {
            if (isCommitInProgress.compareAndSet(false, true)) {
                val currentState = state.value
                viewModelScope.launch(Dispatchers.IO) {
                    val committed = runCatching { commitPreset(currentState) }
                        .onFailure { Timber.e(it) }
                        .getOrDefault(false)
                    if (!committed) isCommitInProgress.set(false)
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

        is Action.SetFreeMode -> state.value.copy(freeMode = viewAction.enabled)

        is Action.SetFreeWindows -> state.value.copy(freeWindows = viewAction.windows)

        is Action.AddFreeWindow -> state.value.withFreeWindow(viewAction.app)

        is Action.UpdateFreeWindow -> state.value.copy(
            freeWindows = state.value.freeWindows.map { if (it.id == viewAction.window.id) viewAction.window else it }
        )

        is Action.RemoveFreeWindow -> state.value.copy(
            freeWindows = state.value.freeWindows.filterNot { it.id == viewAction.id }
        )
    }

    private suspend fun commitPreset(viewState: ViewState): Boolean {
        val editItem = viewState.editId?.let { getPresetByIdUseCase.execute(it) ?: return false }
        val preset = viewState.toDomainPreset(editItem?.id ?: getPresetFreeIdUseCase.execute()) ?: return false

        if (editItem == null) {
            addSplitPresetUseCase.execute(preset)
        } else {
            val isFree = preset.type == PresetType.FREE
            updateSplitPresetUseCase.execute(
                preset.copy(
                    autoStart = editItem.autoStart,
                    darkBackground = editItem.darkBackground,
                    bottomWindowShift = editItem.bottomWindowShift && !isFree,
                    quickAccess = editItem.quickAccess
                )
            )
        }
        sendAction(Action.SetCloseScreenSingleEvent(true))
        return true
    }

    private fun Float.nearestPresetFormat() = SizeFormat.entries
        .mapNotNull { format -> format.presetRatio?.let { format to abs(it - this) } }
        .minBy { it.second }
        .first

    private fun ViewState.withFreeWindow(app: DeviceAppInfo): ViewState {
        if (freeWindows.any { it.app.packageName == app.packageName }) return this
        return copy(freeWindows = freeWindows + freeWindows.newWindow(app))
    }

    private fun List<DisplayFreeWindow>.newWindow(app: DeviceAppInfo): DisplayFreeWindow {
        val offset = NEW_WINDOW_OFFSET + (size % NEW_WINDOW_CASCADE_STEPS) * NEW_WINDOW_CASCADE_SHIFT
        return DisplayFreeWindow(
            id = (maxOfOrNull { it.id } ?: 0) + 1,
            app = app,
            left = offset,
            top = offset,
            right = offset + NEW_WINDOW_WIDTH,
            bottom = offset + NEW_WINDOW_HEIGHT,
            alwaysOnTop = false
        )
    }

    @Immutable
    data class ViewState(
        val editId: Long? = null,
        val splitForm: SizeFormat = SizeFormat.HALF,
        val deviceApps: List<DeviceAppInfo> = emptyList(),
        val topApp: DeviceAppInfo? = null,
        val bottomApp: DeviceAppInfo? = null,
        val freeMode: Boolean = false,
        val isFreePresetEdit: Boolean = false,
        val freeWindows: List<DisplayFreeWindow> = emptyList(),
        val closeScreenSingleEvent: Boolean? = null,
        val customRatio: Float = .5f,
        val isWindowTypeEnabled: Boolean = false,
        val isWindowTypeAvailable: Boolean = false,
        val isNativeSplitEnabled: Boolean = false
    ) : MviViewState {
        val windowRatio: Float get() = splitForm.presetRatio ?: customRatio
    }

    sealed class Action : MviAction {
        internal class SetSplitForm(val format: SizeFormat) : Action()
        internal class SetDeviceApps(val apps: List<DeviceAppInfo>) : Action()
        internal class SetTopApp(val app: DeviceAppInfo?) : Action()
        internal class SetBottomApp(val app: DeviceAppInfo?) : Action()
        internal class SetTopWithCaption(val value: Boolean) : Action()
        internal class SetBottomWithCaption(val value: Boolean) : Action()
        internal class SetWindowTypeAvailable(val value: Boolean) : Action()
        internal class SetWindowTypePrefs(
            val isNativeSplitEnabled: Boolean,
            val isWindowTypeEnabled: Boolean
        ) : Action()
        internal class SetCloseScreenSingleEvent(val value: Boolean?) : Action()

        internal class SetEditData(
            val editId: Long?,
            val splitForm: SizeFormat,
            val topApp: DeviceAppInfo?,
            val bottomApp: DeviceAppInfo?,
            val customRatio: Float
        ) : Action()

        internal class SetCustomRatio(val ratio: Float) : Action()
        internal class SetFreeMode(val enabled: Boolean) : Action()
        internal class SetFreeWindows(val windows: List<DisplayFreeWindow>) : Action()
        internal class AddFreeWindow(val app: DeviceAppInfo) : Action()
        internal class UpdateFreeWindow(val window: DisplayFreeWindow) : Action()
        internal class RemoveFreeWindow(val id: Int) : Action()

        data object CommitPreset : Action()
        data object ShowPresets : Action()
        data object RoundCustomRatio : Action()
        data object ToggleTopAutoPlay : Action()
        data object ToggleBottomAutoPlay : Action()
        data object SwapApps : Action()
    }

    private companion object {
        const val NEW_WINDOW_OFFSET = .1f
        const val NEW_WINDOW_CASCADE_SHIFT = .05f
        const val NEW_WINDOW_CASCADE_STEPS = 6
        const val NEW_WINDOW_WIDTH = .5f
        const val NEW_WINDOW_HEIGHT = .4f
        const val MIN_CUSTOM_RATIO = .2f
        const val MAX_CUSTOM_RATIO = .8f
        const val PERCENTS = 100f
    }
}
