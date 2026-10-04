package com.salat.gsplit

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.BoolSharedPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.StringPref
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.LoadBoolSharedPrefUseCase
import com.salat.resources.R
import com.salat.settings.quicksplit.presentation.entity.DisplayQuickSplitApp
import com.salat.settings.quicksplit.presentation.mappers.toQuickSplitDisplay
import com.salat.settings.quicksplit.presentation.mappers.toQuickSplitPackages
import com.salat.statekeeper.domain.entity.QuickSplitTarget
import com.salat.statekeeper.domain.usecases.CheckAccessibilityServiceEnabledUseCase
import com.salat.statekeeper.domain.usecases.FindQuickSplitTargetUseCase
import com.salat.systemapps.domain.usecases.FindAllInstalledAppsUseCase
import com.salat.systemapps.domain.usecases.FindInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class QuickSplitViewModel @Inject constructor(
    private val findQuickSplitTargetUseCase: FindQuickSplitTargetUseCase,
    private val checkAccessibilityServiceEnabledUseCase: CheckAccessibilityServiceEnabledUseCase,
    private val flowPrefsUseCase: FlowPrefsUseCase,
    private val loadBoolSharedPrefUseCase: LoadBoolSharedPrefUseCase,
    private val findInstalledAppsUseCase: FindInstalledAppsUseCase,
    private val findAllInstalledAppsUseCase: FindAllInstalledAppsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _events = Channel<Event>()
    val events = _events.receiveAsFlow()

    private var openAppPackage = ""
    private var firstCaption = false
    private var secondCaption = false
    private var darkBackground = false
    private var allAppsJob: Job? = null
    private var isLaunched = false

    // The activity keeps its window hidden until the step is set, so the window list still has the open app
    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (!checkAccessibilityServiceEnabledUseCase.flow.value) {
                _events.send(Event.Close(R.string.accessibility_permission_prompt))
                return@launch
            }
            val openApp = when (val target = findQuickSplitTargetUseCase.execute()) {
                is QuickSplitTarget.FullscreenApp -> target.packageName

                // The accessibility service returns the app of the open quick split to full screen
                QuickSplitTarget.OpenSplit -> {
                    _events.send(Event.Close(null))
                    return@launch
                }

                null -> {
                    _events.send(Event.Close(R.string.quick_split_no_fullscreen_app))
                    return@launch
                }
            }
            openAppPackage = openApp

            val prefs = flowPrefsUseCase.execute(
                FloatPref.UiScale,
                FloatPref.QuickSplitRatio,
                BoolPref.QuickSplitInsertFirst,
                BoolPref.QuickSplitFirstCaption,
                BoolPref.QuickSplitSecondCaption,
                BoolPref.QuickSplitSkipSideStep,
                StringPref.QuickSplitApps,
                BoolPref.QuickSplitDarkBackground
            ).firstOrNull() ?: run {
                _events.send(Event.Close(null))
                return@launch
            }
            firstCaption = prefs[3] as Boolean
            secondCaption = prefs[4] as Boolean
            darkBackground = prefs[7] as Boolean
            val packages = (prefs[6] as String).toQuickSplitPackages() - openApp
            val setApps = findInstalledAppsUseCase.execute(*packages.toTypedArray()).toQuickSplitDisplay()

            _state.update {
                it.copy(
                    step = if (prefs[5] as Boolean) Step.APP else Step.SIDE,
                    ratio = (prefs[1] as Float).coerceIn(MIN_RATIO, MAX_RATIO),
                    insertFirst = prefs[2] as Boolean,
                    setApps = setApps,
                    showAllApps = setApps.isEmpty(),
                    uiScale = prefs[0] as Float,
                    darkTheme = loadBoolSharedPrefUseCase.execute(BoolSharedPref.DarkTheme)
                )
            }
            if (setApps.isEmpty()) loadAllApps()
        }
    }

    fun setRatio(ratio: Float) = _state.update { it.copy(ratio = ratio.coerceIn(MIN_RATIO, MAX_RATIO)) }

    fun roundRatio() = _state.update { it.copy(ratio = (it.ratio * PERCENTS).roundToInt() / PERCENTS) }

    fun selectSide(insertFirst: Boolean) = _state.update { it.copy(step = Step.APP, insertFirst = insertFirst) }

    fun backToSide() = _state.update { it.copy(step = Step.SIDE) }

    fun showAllApps() {
        _state.update { it.copy(showAllApps = true) }
        loadAllApps()
    }

    // The new app takes the selected side. The open app takes the other side
    fun selectApp(app: DisplayQuickSplitApp) {
        if (isLaunched) return
        isLaunched = true
        val current = _state.value
        val (firstPackage, secondPackage) = if (current.insertFirst) {
            app.packageName to openAppPackage
        } else openAppPackage to app.packageName
        viewModelScope.launch {
            _events.send(
                Event.Launch(
                    firstPackage = firstPackage,
                    secondPackage = secondPackage,
                    openPackage = openAppPackage,
                    ratio = current.ratio,
                    firstCaption = firstCaption,
                    secondCaption = secondCaption,
                    darkBackground = darkBackground
                )
            )
        }
    }

    fun close() = viewModelScope.launch { _events.send(Event.Close(null)) }

    private fun loadAllApps() {
        if (allAppsJob != null) return
        allAppsJob = viewModelScope.launch(Dispatchers.IO) {
            val apps = findAllInstalledAppsUseCase.execute().filter { it.packageName != openAppPackage }
            _state.update { it.copy(allApps = apps.toQuickSplitDisplay()) }
        }
    }

    enum class Step { SIDE, APP }

    @Immutable
    data class State(
        val step: Step? = null,
        val ratio: Float = FloatPref.QuickSplitRatio.default,
        val insertFirst: Boolean = BoolPref.QuickSplitInsertFirst.default,
        val setApps: List<DisplayQuickSplitApp> = emptyList(),
        val allApps: List<DisplayQuickSplitApp>? = null,
        val showAllApps: Boolean = false,
        val uiScale: Float = 1f,
        val darkTheme: Boolean = true
    )

    sealed class Event {
        class Launch(
            val firstPackage: String,
            val secondPackage: String,
            val openPackage: String,
            val ratio: Float,
            val firstCaption: Boolean,
            val secondCaption: Boolean,
            val darkBackground: Boolean
        ) : Event()

        class Close(@StringRes val message: Int?) : Event()
    }

    private companion object {
        const val MIN_RATIO = .2f
        const val MAX_RATIO = .8f
        const val PERCENTS = 100f
    }
}
