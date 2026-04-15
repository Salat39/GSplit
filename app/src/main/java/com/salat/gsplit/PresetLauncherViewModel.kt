package com.salat.gsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salat.firebase.domain.useCases.LogLaunchTypeUseCase
import com.salat.gsplit.presentation.mappers.toLauncherDomain
import com.salat.launchhistory.domain.entity.LastLaunchedApp
import com.salat.launchhistory.domain.entity.LastLaunchedTask
import com.salat.launchhistory.domain.entity.LastLaunchedType
import com.salat.launchhistory.domain.usecases.GetLastLaunchedSplitUseCase
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.usecases.LoadFloatPrefUseCase
import com.salat.preferences.domain.usecases.LoadIntPrefUseCase
import com.salat.splitlauncher.domain.entity.SplitLaunchApp
import com.salat.splitlauncher.domain.entity.SplitLaunchSource
import com.salat.splitlauncher.domain.entity.SplitLaunchTask
import com.salat.splitlauncher.domain.entity.SplitLaunchType
import com.salat.splitlauncher.domain.usecases.GetFreedomHackFlowUseCase
import com.salat.splitlauncher.domain.usecases.LaunchSplitUseCase
import com.salat.splitpresets.domain.usecases.GetPresetByIdUseCase
import com.salat.statekeeper.domain.usecases.SetSkipAutoLaunchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class PresetLauncherViewModel @Inject constructor(
    private val getFreedomHackFlowUseCase: GetFreedomHackFlowUseCase,
    private val launchSplitUseCase: LaunchSplitUseCase,
    private val loadFloatPrefUseCase: LoadFloatPrefUseCase,
    private val loadIntPrefUseCase: LoadIntPrefUseCase,
    private val setSkipAutoLaunchUseCase: SetSkipAutoLaunchUseCase,
    private val logLaunchTypeUseCase: LogLaunchTypeUseCase,
    private val getPresetByIdUseCase: GetPresetByIdUseCase,
    private val getLastLaunchedSplitUseCase: GetLastLaunchedSplitUseCase
) : ViewModel() {
    private val _launchFreedomHackState = Channel<Unit>()
    val launchFreedomHackState = _launchFreedomHackState.receiveAsFlow()

    private val _finishState = Channel<Unit>()
    val finishState = _finishState.receiveAsFlow()

    private val _launchDarkScreenState = MutableStateFlow(false)
    val launchDarkScreenState = _launchDarkScreenState.asStateFlow()

    private val _uiScaleState = MutableStateFlow(1f)
    val uiScaleState = _uiScaleState.asStateFlow()

    private val _toolbarExtraSizeState = MutableStateFlow(0)
    val toolbarExtraSizeState = _toolbarExtraSizeState.asStateFlow()

    init {
        viewModelScope.launch {
            // Collect ui scale
            _uiScaleState.emit(loadFloatPrefUseCase.execute(FloatPref.UiScale))

            getFreedomHackFlowUseCase.execute().collect { _launchFreedomHackState.send(Unit) }
        }

        // App launch log
        viewModelScope.launch(Dispatchers.IO) { logLaunchTypeUseCase.execute("no_ui") }
    }

    internal fun findAndLaunchPresetById(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        getPresetByIdUseCase.execute(id)?.let { preset ->
            // show dark screen before call split
            if (preset.darkBackground) {
                // prepare stub screen prefs
                _toolbarExtraSizeState.emit(loadIntPrefUseCase.execute(IntPref.ToolbarExtraSpace))
                // cast screen
                _launchDarkScreenState.emit(true)
            }

            // init split
            launchSplitUseCase.execute(preset.toLauncherDomain(), SplitLaunchSource.SHORTCUT)

            // disable split autorun on application startup
            setSkipAutoLaunchUseCase.execute(true)

            // finish if no dark screen
            if (!preset.darkBackground) {
                _finishState.send(Unit)
            }
        } ?: run {
            _finishState.send(Unit)
        }
    }

    internal fun launchLastSplit() = viewModelScope.launch(Dispatchers.IO) {
        getLastLaunchedSplitUseCase.execute()?.let { lastLaunchedTask ->
            val task = lastLaunchedTask.toSplitLaunchTask()

            // show dark screen before call split
            if (task.darkBackground) {
                // prepare stub screen prefs
                _toolbarExtraSizeState.emit(loadIntPrefUseCase.execute(IntPref.ToolbarExtraSpace))
                // cast screen
                _launchDarkScreenState.emit(true)
            }

            // init split
            launchSplitUseCase.execute(task, SplitLaunchSource.SHORTCUT)

            // disable split autorun on application startup
            setSkipAutoLaunchUseCase.execute(true)

            // finish if no dark screen
            if (!task.darkBackground) {
                _finishState.send(Unit)
            }
        } ?: run {
            _finishState.send(Unit)
        }
    }

    internal fun launchCustomSplit(
        firstPackage: String,
        firstAutoPlay: Int,
        secondPackage: String,
        secondAutoPlay: Int,
        type: String,
        darkBackground: Int,
        windowShift: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val firstApp = SplitLaunchApp(
                title = firstPackage,
                packageName = firstPackage,
                autoPlay = firstAutoPlay == 1
            )
            val secondApp = SplitLaunchApp(
                title = secondPackage,
                packageName = secondPackage,
                autoPlay = secondAutoPlay == 1
            )
            val splitType = when (type) {
                "1x2" -> SplitLaunchType.ONE_TO_THREE
                "2x1" -> SplitLaunchType.TWO_TO_THREE
                "3x4" -> SplitLaunchType.THREE_TO_FOUR
                "3x2" -> SplitLaunchType.THREE_TO_TWO
                "4x3" -> SplitLaunchType.FOUR_TO_THREE
                else -> SplitLaunchType.HALF
            }

            val task = SplitLaunchTask(
                firstApp = firstApp,
                type = splitType,
                secondApp = secondApp,
                autoStart = false,
                darkBackground = darkBackground == 1,
                bottomWindowShift = windowShift == 1,
                id = 0L
            )

            // show dark screen before call split
            if (task.darkBackground) {
                // prepare stub screen prefs
                _toolbarExtraSizeState.emit(loadIntPrefUseCase.execute(IntPref.ToolbarExtraSpace))
                // cast screen
                _launchDarkScreenState.emit(true)
            }

            // init split
            launchSplitUseCase.execute(task, SplitLaunchSource.SHORTCUT)

            // disable split autorun on application startup
            setSkipAutoLaunchUseCase.execute(true)

            // finish if no dark screen
            if (!task.darkBackground) {
                _finishState.send(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun LastLaunchedApp.toSplitLaunchApp(): SplitLaunchApp {
        return SplitLaunchApp(
            title = this.title,
            packageName = this.packageName,
            autoPlay = this.autoPlay
        )
    }

    private fun LastLaunchedType.toSplitLaunchType(): SplitLaunchType {
        return SplitLaunchType.entries.first { it.id == this.id }
    }

    private fun LastLaunchedTask.toSplitLaunchTask(): SplitLaunchTask {
        return SplitLaunchTask(
            firstApp = this.firstApp?.toSplitLaunchApp(),
            type = this.type.toSplitLaunchType(),
            secondApp = this.secondApp?.toSplitLaunchApp(),
            autoStart = this.autoStart,
            darkBackground = this.darkBackground,
            bottomWindowShift = this.bottomWindowShift,
            id = this.id
        )
    }
}
