package com.salat.gsplit.presentation

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salat.firebase.domain.useCases.LogLaunchTypeUseCase
import com.salat.firebase.domain.useCases.LogScreenUseCase
import com.salat.gsplit.presentation.components.toAnalyticsRoute
import com.salat.gsplit.presentation.mappers.toLauncherDomain
import com.salat.navigation.common.extractRouteName
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.BoolSharedPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.LoadBoolPrefUseCase
import com.salat.preferences.domain.usecases.LoadBoolSharedPrefUseCase
import com.salat.splitlauncher.domain.entity.SplitLaunchSource
import com.salat.splitlauncher.domain.usecases.GetDarkBackgroundFlowUseCase
import com.salat.splitlauncher.domain.usecases.GetFreedomHackFlowUseCase
import com.salat.splitlauncher.domain.usecases.GetNativeSplitLaunchTaskFlowUseCase
import com.salat.splitlauncher.domain.usecases.GetSplitStartedFlowUseCase
import com.salat.splitlauncher.domain.usecases.LaunchSplitUseCase
import com.salat.splitpresets.domain.usecases.GetAutoPlayPresetUseCase
import com.salat.statekeeper.domain.usecases.GetSkipAutoLaunchUseCase
import com.salat.statekeeper.domain.usecases.SetSkipAutoLaunchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

private const val AUTO_START_SPLASH_DELAY = 2500L

@HiltViewModel
class MainViewModel @Inject constructor(
    private val loadBoolSharedPrefUseCase: LoadBoolSharedPrefUseCase,
    private val loadBoolPrefUseCase: LoadBoolPrefUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase,
    private val getFreedomHackFlowUseCase: GetFreedomHackFlowUseCase,
    private val getDarkBackgroundFlowUseCase: GetDarkBackgroundFlowUseCase,
    private val getSplitStartedFlowUseCase: GetSplitStartedFlowUseCase,
    private val getAutoPlayPresetUseCase: GetAutoPlayPresetUseCase,
    private val getNativeSplitLaunchTaskFlowUseCase: GetNativeSplitLaunchTaskFlowUseCase,
    private val getSkipAutoLaunchUseCase: GetSkipAutoLaunchUseCase,
    private val setSkipAutoLaunchUseCase: SetSkipAutoLaunchUseCase,
    private val logScreenUseCase: LogScreenUseCase,
    private val logLaunchTypeUseCase: LogLaunchTypeUseCase,
    private val launchSplitUseCase: LaunchSplitUseCase
) : ViewModel() {
    private val _splashScreenState = MutableStateFlow(true)
    val splashScreenState = _splashScreenState.asStateFlow()

    private val _darkTheme = MutableStateFlow(true)
    val darkTheme = _darkTheme.asStateFlow()

    private val _autoRunFiller = MutableStateFlow(false)
    val autoRunFiller = _autoRunFiller.asStateFlow()

    private val _launchFreedomHackState = Channel<Unit>()
    val launchFreedomHackState = _launchFreedomHackState.receiveAsFlow()

    private val _nativeSplitLauncherState = Channel<Pair<Intent, Intent>>()
    val nativeSplitLauncherState = _nativeSplitLauncherState.receiveAsFlow()

    private val _launchDarkScreenState = MutableStateFlow<Boolean?>(null)
    val launchDarkScreenState = _launchDarkScreenState.asStateFlow()

    private val _minimizeApp = Channel<Unit>()
    val minimizeApp = _minimizeApp.receiveAsFlow()

    private val _uiScaleState = MutableStateFlow(1f)
    val uiScaleState = _uiScaleState.asStateFlow()

    private val _toolbarExtraSpace = MutableStateFlow(0)
    val toolbarExtraSpace = _toolbarExtraSpace.asStateFlow()

    private var autoStart = true

    init {
        viewModelScope.launch {
            pickBasePrefs()
            collectBasePrefs()

            collectDarkScreen()

            collectFreedomHack()

            collectNativeSplitLaunchTask()

            val autoStartMinimizeDelay = loadBoolPrefUseCase.execute(BoolPref.AutoStartMinimizeDelay)
            collectSplitStartedEvent(autoStartMinimizeDelay)

            // Send auto start
            if (getSkipAutoLaunchUseCase.execute()) {
                autoStart = false
                setSkipAutoLaunchUseCase.execute(false)
            } else if (autoStart) {
                getAutoPlayPresetUseCase.execute()?.let { preset ->
                    _autoRunFiller.value = true
                    launch(Dispatchers.IO) {
                        launchSplitUseCase.execute(
                            task = preset.toLauncherDomain(),
                            source = SplitLaunchSource.AUTO_START
                        )
                    }
                }
                autoStart = false
            }

            // App dark mode
            _darkTheme.value = loadBoolSharedPrefUseCase.execute(BoolSharedPref.DarkTheme)

            // Disabling splashscreen
            _splashScreenState.value = false
        }

        // App launch log
        viewModelScope.launch(Dispatchers.IO) { logLaunchTypeUseCase.execute("normal") }
    }

    private suspend fun pickBasePrefs() {
        flowPrefsUseCase.execute(
            FloatPref.UiScale,
            IntPref.ToolbarExtraSpace
        ).firstOrNull()?.let { prefs ->
            // Collect ui scale
            _uiScaleState.emit(prefs[0] as Float)
            _toolbarExtraSpace.emit(prefs[1] as Int)
        }
    }

    private fun CoroutineScope.collectBasePrefs() = launch(Dispatchers.IO) {
        flowPrefsUseCase.execute(
            FloatPref.UiScale,
            IntPref.ToolbarExtraSpace
        ).collect { prefs ->
            if (prefs[0] is Float) {
                _uiScaleState.update { prefs[0] as Float }
            }
            _toolbarExtraSpace.emit(prefs[1] as Int)
        }
    }

    private fun CoroutineScope.collectFreedomHack() = launch {
        getFreedomHackFlowUseCase.execute().collect { _launchFreedomHackState.send(Unit) }
    }

    private fun CoroutineScope.collectNativeSplitLaunchTask() = launch {
        getNativeSplitLaunchTaskFlowUseCase.execute().collect { (top, bottom) ->
            if (top is Intent && bottom is Intent) {
                _nativeSplitLauncherState.send(top to bottom)
            }
        }
    }

    private fun CoroutineScope.collectDarkScreen() = launch {
        getDarkBackgroundFlowUseCase.execute().collect { minimizeAfterCloseScreen ->
            if (_launchDarkScreenState.value != minimizeAfterCloseScreen) {
                _launchDarkScreenState.emit(minimizeAfterCloseScreen)
            }
        }
    }

    private fun CoroutineScope.collectSplitStartedEvent(autoStartMinimizeDelay: Boolean) = launch(Dispatchers.IO) {
        getSplitStartedFlowUseCase.execute().collect { (source, task) ->
            when (source) {
                SplitLaunchSource.CLICK -> {
                    if (!task.darkBackground && loadBoolPrefUseCase.execute(BoolPref.MinimizeByStart)) {
                        Timber.d("App minimized by preset click")
                        _minimizeApp.send(Unit)
                    }
                }

                SplitLaunchSource.AUTO_START -> {
                    if (!task.darkBackground && loadBoolPrefUseCase.execute(BoolPref.MinimizeByAutostart)) {
                        if (autoStartMinimizeDelay) {
                            delay(AUTO_START_SPLASH_DELAY)
                            Timber.d("App minimized by auto start with delay")
                        } else {
                            Timber.d("App minimized by auto start")
                        }
                        _minimizeApp.send(Unit)
                    }
                    _autoRunFiller.value = false
                }

                else -> Unit
            }
        }
    }

    fun clearLaunchDarkScreenState() {
        viewModelScope.launch {
            _launchDarkScreenState.emit(null)
        }
    }

    fun onScreenChanged(route: String) = viewModelScope.launch(Dispatchers.IO) {
        val simpleRoute = route.extractRouteName()
        val analyticsRoute = simpleRoute.toAnalyticsRoute()
        if (analyticsRoute.isNotEmpty()) {
            logScreenUseCase.execute(analyticsRoute)
        }
    }
}
