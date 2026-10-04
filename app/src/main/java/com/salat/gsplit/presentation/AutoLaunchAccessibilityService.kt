package com.salat.gsplit.presentation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.salat.adb.data.entity.AdbConnectionState
import com.salat.adb.domain.repository.AdbRepository
import com.salat.gsplit.PresetLauncherActivity
import com.salat.gsplit.presentation.entity.FreeFormPosition
import com.salat.gsplit.presentation.entity.FreeFormWindow
import com.salat.gsplit.presentation.entity.SessionWindows
import com.salat.gsplit.presentation.entity.SplitStateBroadcastData
import com.salat.gsplit.presentation.util.PauseDetector
import com.salat.overlay.presentation.startOverlay
import com.salat.overlay.presentation.stopOverlay
import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.resources.R
import com.salat.screenspecs.domain.repository.ScreenSpecsRepository
import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.entity.LaunchedSplitType
import com.salat.statekeeper.domain.entity.LaunchedWindowsConfig
import com.salat.statekeeper.domain.entity.QuickSplitTarget
import com.salat.statekeeper.domain.entity.SplitLauncherEvent
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import dagger.hilt.android.AndroidEntryPoint
import domain.launchWithRetry
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import presentation.toast
import timber.log.Timber

@AndroidEntryPoint
class AutoLaunchAccessibilityService : AccessibilityService() {

    companion object {
        private const val INIT_WINDOWS_DELAY = 1500L

        private const val CLOSE_WINDOW_DRAG_SIZE = 48f
        private const val CLOSE_WINDOW_DRAG_TIME = 100L // 80L
        private const val CLOSE_WINDOW_DELAY_BEFORE_CLICK = 100L
        private const val CLOSE_WINDOW_CLICK_TIME = 100L
        private const val AWAIT_TIMEOUT = 5_000L
        private const val FREE_WINDOWS_SETTLE_TIMEOUT = 1_000L
        private const val HIDDEN_WINDOW_AWAIT_TIMEOUT = 1_500L
        private const val FULLSCREEN_CHECK_INTERVAL = 100L
        private const val SLEEP_DELAY = 300_000L

        private const val RETRY_WHEN_ATTEMPTS = 3

        private const val BASE_PATH = "com.salat.gsplit"
        // private const val MACRO_DROID_PACKAGE = "com.arlosoft.macrodroid"
    }

    private val handler = CoroutineExceptionHandler { _, e -> Timber.e(e) }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + handler)
    private val windowBounds = Rect()
    private val actionBounds = Rect()

    @Inject
    lateinit var stateKeeper: StateKeeperRepository

    @Inject
    lateinit var dataStore: DataStoreRepository

    @Inject
    lateinit var adb: AdbRepository

    @Inject
    lateinit var screenSpecs: ScreenSpecsRepository

    private val _freeFormWindows = MutableStateFlow<Pair<FreeFormWindow?, FreeFormWindow?>>(Pair(null, null))
    private val freeFormWindows = _freeFormWindows.asStateFlow()

    private val _freePresetWindows = MutableStateFlow<List<FreeFormWindow>>(emptyList())
    private val freePresetWindows = _freePresetWindows.asStateFlow()

    private val shownWindows = combine(freeFormWindows, freePresetWindows) { (top, bottom), freeWindows ->
        listOfNotNull(top, bottom) + freeWindows
    }

    // Null until the first scan of this service instance
    private val _sessionWindows = MutableStateFlow<SessionWindows?>(null)
    private val sessionWindows = _sessionWindows.asStateFlow()

    private val _splitStateBroadcastData = MutableStateFlow<SplitStateBroadcastData?>(null)
    private val splitStateBroadcastData = _splitStateBroadcastData.asStateFlow()

    private var taskSleep: Job? = null
    private var sleepTaskSessionId = -2L

    private var pauseDetector: PauseDetector? = null
    private var selfAutostart = false
    private var autostartAfterPause = false
    private var autostartPauseThreshold = IntPref.AutostartPauseThreshold.default

    // Local flag whether to handle dark screen close events
    private var darkScreenAutoClose = false
    private var autoRefocusWhenBottomWindowShift = false
    private var enableOverlays = false

    private var sequentialClosing = false
    private var dodgeSystemGesWhenClosing = true

    // If true, wait until there are no windows on the screen to close the black screen
    private var enableDarkScreenCloseTracking = false

    private var settledSessionId = 0L

    // Save current focus for restore after
    private var memorizedFocusPackageName = ""

    // delay between windows closing
    private var windowClosingExtraPause = 100

    // External app notifications
    private var externalAppEventSync = false

    // To avoid processing events when the split is not running
    private var splitWasLaunched = false

    // The window list does not show the windows under the open preset panel
    private var presetPanelCoveredPackages = emptyList<String>()
    private var isPresetPanelCover = false
    private var replaceMenuCoveredPackages = emptyList<String>()

    // add this at the top of the class
    private val stateChangeFlow = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val contentChangeFlow = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var screenWidth = 0
    private var screenHeight = 0
    private var isLandscape = false
    private var density = 0f
    private var touchSlop = 0f
    private var safePx = 0f
    private var minDragPx = 0f

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateScreenMetrics()
    }

    @OptIn(FlowPreview::class)
    override fun onCreate() {
        super.onCreate()
        Timber.d("[AS] Created")

        // One collector runs the debounced window scans in sequence
        serviceScope.launch {
            merge(stateChangeFlow.debounce(60), contentChangeFlow.debounce(300))
                .collect { collectFreeFormWindows() }
        }

        serviceScope.launch {
            collectBasePrefs()
            collectSplitStateBroadcasts()
            collectWindowsChanges()
        }
        serviceScope.launch { collectSharedEvents() }
        serviceScope.launch { collectPresetPanelCoveredWindows() }
        serviceScope.launch { collectReplaceMenuCoveredWindows() }
        serviceScope.launch { collectSettledSessions() }
        serviceScope.launch { collectQuickSplitTargetRequests() }

        pauseDetector = PauseDetector(this, serviceScope, ::onPauseEnded)
    }

    private fun CoroutineScope.collectBasePrefs() = launchWithRetry(RETRY_WHEN_ATTEMPTS) {
        dataStore.getAnyPrefsFlow(
            BoolPref.DarkScreenAutoClose,
            BoolPref.EnableOverlays,
            BoolPref.CloseWindowDodgeSystemGes,
            BoolPref.CloseWindowSequential,
            IntPref.WindowClosingExtraPause,
            BoolPref.ExternalAppEventSync,
            BoolPref.SelfAutostart,
            BoolPref.SelfAutostartAfterPause,
            IntPref.AutostartPauseThreshold,
            BoolPref.AutoRefocusWhenBottomWindowShift
        ).collect { prefs ->
            darkScreenAutoClose = prefs[0] as Boolean
            enableOverlays = prefs[1] as Boolean
            dodgeSystemGesWhenClosing = prefs[2] as Boolean
            sequentialClosing = prefs[3] as Boolean
            windowClosingExtraPause = prefs[4] as Int
            externalAppEventSync = prefs[5] as Boolean
            selfAutostart = prefs[6] as Boolean
            autostartAfterPause = prefs[7] as Boolean
            autostartPauseThreshold = prefs[8] as Int
            autoRefocusWhenBottomWindowShift = prefs[9] as Boolean
        }
    }

    private fun onPauseEnded(durationMs: Long) {
        if (!selfAutostart || !autostartAfterPause || durationMs < autostartPauseThreshold) return
        serviceScope.launch {
            delay(FREE_WINDOWS_SETTLE_TIMEOUT)
            if (hasFreeFormAppWindows()) return@launch
            Timber.d("[AS] Autostart after pause of ${durationMs / 1000} s")
            startForegroundService(
                Intent(this@AutoLaunchAccessibilityService, WakeUpForegroundService::class.java)
                    .putExtra(WakeUpForegroundService.EXTRA_AFTER_PAUSE, true)
            )
        }
    }

    private fun hasFreeFormAppWindows(): Boolean {
        val bounds = Rect()
        return windows.orEmpty().any { window ->
            window.getBoundsInScreen(bounds)
            window.type == AccessibilityWindowInfo.TYPE_APPLICATION &&
                (bounds.width() < screenWidth || bounds.height() < screenHeight)
        }
    }

    private fun CoroutineScope.collectSharedEvents() = launchWithRetry(RETRY_WHEN_ATTEMPTS) {
        stateKeeper.accessibilityServiceEvents.collect { event ->
            try {
                when (event) {
                    is AccessibilityServiceEvent.FocusWindow -> setFocusWindow(event.packageName)

                    AccessibilityServiceEvent.RememberFocus -> saveCurrentFocus()

                    AccessibilityServiceEvent.RestoreFocus -> restoreCurrentFocus()

                    AccessibilityServiceEvent.CloseSplit -> closeWindows()

                    is AccessibilityServiceEvent.CloseQuickSplit -> closeQuickSplit(event.sessionId)

                    is AccessibilityServiceEvent.ReplaceWindow -> replaceWindow(
                        event.index,
                        event.packageName,
                        event.autoPlay
                    )

                    is AccessibilityServiceEvent.ReplacePreset -> replacePreset(event.presetId, event.fromPresetPanel)

                    is AccessibilityServiceEvent.ReplaceSplit -> replaceSplit(
                        event.firstPackage,
                        event.firstAutoPlay,
                        event.secondPackage,
                        event.secondAutoPlay,
                        event.type,
                        event.darkBackground,
                        event.windowShift,
                        event.firstCaption,
                        event.secondCaption
                    )

                    is AccessibilityServiceEvent.LaunchLast -> launchLast(event.fromPresetPanel)

                    is AccessibilityServiceEvent.CloseCurrentWindows -> closeCurrentWindowsTask(event.postAction)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    // The panel sets the flag before its window appears. The window list still shows the windows under it
    // The panel stops before the window list shows the covered windows again. The cover stays until they return
    private fun CoroutineScope.collectPresetPanelCoveredWindows() = launch {
        stateKeeper.presetPanelShown.collectLatest { isShown ->
            if (isShown) {
                presetPanelCoveredPackages = shownWindows.first().map { it.packageName }
            } else {
                withTimeoutOrNull(HIDDEN_WINDOW_AWAIT_TIMEOUT) {
                    shownWindows.first { windows ->
                        windows.map { it.packageName }.containsAll(presetPanelCoveredPackages)
                    }
                }
            }
            isPresetPanelCover = isShown
            stateChangeFlow.tryEmit(Unit)
        }
    }

    // The menu sets the flag before its window appears. The window list still shows the windows under it
    // The window replacement starts right after the menu closes. A later replacement does not use this list
    private fun CoroutineScope.collectReplaceMenuCoveredWindows() = launch {
        stateKeeper.replaceMenuShown.collectLatest { isShown ->
            if (isShown) {
                replaceMenuCoveredPackages = shownWindows.first().map { it.packageName }
            } else {
                delay(AWAIT_TIMEOUT)
                replaceMenuCoveredPackages = emptyList()
            }
        }
    }

    // A window without caption opens full screen and gets its bounds later. The window list is not stable until then
    private fun CoroutineScope.collectSettledSessions() = launch {
        stateKeeper.launchedWindows.mapNotNull { it?.sessionId }.distinctUntilChanged().collectLatest { sessionId ->
            stateKeeper.placedWindowsSessionId.first { it >= sessionId }
            delay(INIT_WINDOWS_DELAY)
            settledSessionId = sessionId
            stateChangeFlow.tryEmit(Unit)
        }
    }

    // Quick split shows its own window only after this answer, so the window list still has the app
    // A second activation of the shortcut closes the open quick split in the queue of the window events
    private fun CoroutineScope.collectQuickSplitTargetRequests() = launch {
        stateKeeper.quickSplitTargetRequests.collect { reply ->
            try {
                val openSplit = openQuickSplit()
                val target = if (openSplit != null) {
                    QuickSplitTarget.OpenSplit
                } else findFullscreenAppPackage()?.let { QuickSplitTarget.FullscreenApp(it) }
                if (reply.complete(target) && openSplit != null) {
                    stateKeeper.sendAccessibilityServiceEvent(
                        AccessibilityServiceEvent.CloseQuickSplit(openSplit.sessionId)
                    )
                }
            } catch (e: Exception) {
                reply.complete(null)
                Timber.e(e)
            }
        }
    }

    // The quick split is open while a window of it shows
    private fun openQuickSplit() = stateKeeper.getLaunchedWindows()?.takeIf { config ->
        config.quickSplitOpenPackage.isNotEmpty() && config.sessionId != stateKeeper.getClosedSessionId() &&
            _freeFormWindows.value.toList().any { it != null }
    }

    // With ADB the open app covers the screen first and the inserted window closes under it
    // Without ADB the window list does not show a window under a full screen app, so the inserted window closes first
    private suspend fun closeQuickSplit(sessionId: Long) {
        val config = openQuickSplit()?.takeIf { it.sessionId == sessionId } ?: return
        val previousClosedSessionId = stateKeeper.getClosedSessionId()
        stateKeeper.setClosedSessionId(sessionId)
        val openPackage = config.quickSplitOpenPackage
        val splitWindows = _freeFormWindows.value.toList().filterNotNull()
        val openWindow = splitWindows.find { it.packageName == openPackage }
        val insertedWindows = splitWindows.filter { it.packageName != openPackage }

        // A covered dark screen does not close, so it closes while it shows under the windows
        stateKeeper.sendCloseDarkScreenEvent()

        // Android 8 keeps the windows in one stack, so ADB cannot close one window
        val closesTasks = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && adb.ensureConnected()
        val isClosed = if (closesTasks) {
            val isOpenAppShown = openWindow == null || moveToFullscreen(openWindow)
            if (!isOpenAppShown) Timber.w("[AS] Open app $openPackage stays in its window")
            if (stateKeeper.getLaunchedWindows()?.sessionId == sessionId) {
                insertedWindows.forEach { window -> adb.getTaskId(window.packageName)?.let { adb.minimize(it) } }
            }
            insertedWindows.all { awaitWindowGone(it) }
        } else {
            val areInsertedClosed = insertedWindows.all { tapCloseAndAwait(it) }
            if (areInsertedClosed) openWindow?.let { moveToFullscreen(it) }
            areInsertedClosed
        }
        // The split stays open, so the next activation of the shortcut can try again
        if (!isClosed) {
            Timber.w("[AS] Quick split of $openPackage stays open")
            stateKeeper.setClosedSessionId(previousClosedSessionId)
        }
    }

    // A window without caption moves through ADB. Returns true when the app shows in full screen
    private suspend fun moveToFullscreen(window: FreeFormWindow): Boolean {
        if (!tapMaximize(window)) {
            stateKeeper.sendSplitLauncherEvent(SplitLauncherEvent.MoveToFullscreen(window.packageName))
        }
        return withTimeoutOrNull(AWAIT_TIMEOUT) {
            while (findFullscreenAppPackage() != window.packageName) delay(FULLSCREEN_CHECK_INTERVAL)
        } != null
    }

    // Returns true when the window is not in the window list after the tap
    private suspend fun tapCloseAndAwait(window: FreeFormWindow): Boolean {
        withTimeoutOrNull(AWAIT_TIMEOUT) {
            suspendCancellableCoroutine { cont -> closeWindow(window) { cont.resume(it) } }
        }
        return awaitWindowGone(window)
    }

    private suspend fun awaitWindowGone(window: FreeFormWindow) = withTimeoutOrNull(AWAIT_TIMEOUT) {
        freeFormWindows.first { pair -> pair.toList().none { it?.packageName == window.packageName } }
    } != null

    // The caption button moves the window to full screen without a restart of the app
    private suspend fun tapMaximize(window: FreeFormWindow): Boolean {
        val node = window.data.root?.findAccessibilityNodeInfosByViewId("android:id/maximize_window")?.firstOrNull()
            ?: return false
        val bounds = Rect().also { node.getBoundsInScreen(it) }
        val tapPath = Path().apply {
            moveTo(
                bounds.exactCenterX().coerceIn(0f, screenWidth.toFloat()),
                bounds.exactCenterY().coerceIn(0f, screenHeight.toFloat())
            )
        }
        val tapGesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(tapPath, 0, CLOSE_WINDOW_CLICK_TIME))
            .build()
        return withTimeoutOrNull(AWAIT_TIMEOUT) {
            suspendCancellableCoroutine { cont ->
                dispatchGesture(
                    tapGesture,
                    object : GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) = cont.resume(true)

                        override fun onCancelled(gestureDescription: GestureDescription?) = cont.resume(false)
                    },
                    null
                )
            }
        } ?: false
    }

    // The top app can show a dialog over its full screen window. An own window on top means no target app
    private fun findFullscreenAppPackage(): String? {
        val appWindows = windows.orEmpty()
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION && !it.isInPictureInPictureMode }
            .mapNotNull { window -> window.root?.packageName?.toString()?.let { it to window } }
        val topPackage = appWindows.maxByOrNull { (_, window) -> window.layer }?.first
        if (topPackage == null || topPackage == packageName || topPackage == homePackageName()) return null

        val appArea = splitWindowsArea()
        val bounds = Rect()
        return topPackage.takeIf {
            appWindows.any { (pkg, window) ->
                window.getBoundsInScreen(bounds)
                pkg == topPackage && bounds.contains(appArea)
            }
        }
    }

    // The launcher places the split windows in this area. A window of the split is smaller than the area
    private fun splitWindowsArea(): Rect {
        val left = screenSpecs.getScreenHorizontalInsets().first
        val top = screenSpecs.getStatusBarHeight()
        return Rect(left, top, left + screenSpecs.getFreeScreenWidth(), top + screenSpecs.getFreeScreenHeight())
    }

    private fun homePackageName() = packageManager.resolveActivity(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
        PackageManager.MATCH_DEFAULT_ONLY
    )?.activityInfo?.packageName

    // Notify other app
    private fun CoroutineScope.collectSplitStateBroadcasts() = launch(Dispatchers.IO) {
        splitStateBroadcastData.collect { data -> data?.let { sendSplitStateBroadcast(it) } }
    }

    private fun CoroutineScope.collectWindowsChanges() = launchWithRetry(RETRY_WHEN_ATTEMPTS) {
        sessionWindows.filterNotNull().collect { windows ->
            val (topWindow, bottomWindow, freeWindows, isSessionCovered) = windows
            val isAnyWindowShown = topWindow != null || bottomWindow != null || freeWindows.isNotEmpty()
            if (isAnyWindowShown || isSessionCovered) {
                stopSleepTask()
                splitWasLaunched = true
            }
            // Own windows cover the session windows. The overlay and the broadcast keep the last state
            // The full screen main window is the exception, the split controls must not show over it
            if (!isAnyWindowShown && isSessionCovered) {
                if (enableOverlays && windows.isMainWindowFullScreen) stopOverlay(this@AutoLaunchAccessibilityService)
                return@collect
            }

            // At least one window appears, enable dark screen closing processing
            if (isAnyWindowShown) {
                enableDarkScreenCloseTracking = true
            }

            // Display overlay management
            if (enableOverlays) {
                if ((topWindow != null && bottomWindow != null) || isLastFreeWindowShown(freeWindows)) {
                    startOverlay(this@AutoLaunchAccessibilityService)
                } else if (!isAnyWindowShown) {
                    stopOverlay(this@AutoLaunchAccessibilityService)
                }
            }

            // Notify external apps
            if (externalAppEventSync) {
                _splitStateBroadcastData.update {
                    SplitStateBroadcastData(
                        isShown = isAnyWindowShown,
                        firstPackageName = topWindow?.packageName ?: "",
                        secondPackageName = bottomWindow?.packageName ?: ""
                    )
                }
            }

            if (!isAnyWindowShown && splitWasLaunched) {
                sleepTaskSessionId = stateKeeper.getLaunchedWindows()?.sessionId ?: -2L
                startSleepTask()
            }

            // TODO windows screen configuration changed
            // Timber.d("[AS] Windows config: $topWindow $bottomWindow")
        }
    }

    // A covered window is not in the window list. The last launched window is on top
    private fun isLastFreeWindowShown(freeWindows: List<FreeFormWindow>): Boolean {
        val lastPackage = stateKeeper.getLaunchedWindows()?.freeWindowPackages?.lastOrNull() ?: return false
        return freeWindows.any { it.packageName == lastPackage }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        // initialize screen dimensions
        updateScreenMetrics()

        val dm = resources.displayMetrics
        density = dm.density
        touchSlop = ViewConfiguration.get(this).scaledTouchSlop.toFloat()
        safePx = CLOSE_WINDOW_DRAG_SIZE * density
        minDragPx = maxOf(touchSlop * 2f, 50f)

        configureAccessibilityService()
        serviceScope.launch { stateKeeper.setAccessibilityServiceEnabled(true) }
        Timber.d("[AS] Connected")
    }

    @Suppress("ReturnCount")
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        when (event.eventType) {
            // handle content-changed only when exactly one freeform window is shown
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> stateChangeFlow.tryEmit(Unit)

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val cfg = stateKeeper.getLaunchedWindows() ?: return

                // emit into debounced flow only when exactly one freeform window is shown
                val (top, bottom) = _freeFormWindows.value
                val oneWindow = (top != null).xor(bottom != null)
                if (!oneWindow) return

                // filter by desired packages
                val pkg = event.packageName?.toString() ?: return
                if (pkg != cfg.firstAppPackage && pkg != cfg.secondAppPackage) return

                // enqueue a debounced re-check
                contentChangeFlow.tryEmit(Unit)
            }

            // ignore everything else
            else -> Unit
        }
    }

    private suspend fun collectFreeFormWindows() {
        val split = stateKeeper.getLaunchedWindows() ?: return

        // Get the list of windows, if none – emit Pair(null, null)
        val currentWindows = windows
        if (currentWindows == null) {
            _freeFormWindows.emit(null to null)
            _freePresetWindows.emit(emptyList())
            _sessionWindows.value = SessionWindows(null, null, emptyList(), isCovered = false)
            return
        }

        // Collect the list of freeform windows (condition determined by window size)
        var topWindowCandidate: AccessibilityWindowInfo? = null
        var bottomWindowCandidate: AccessibilityWindowInfo? = null
        val freePresetCandidates = mutableListOf<FreeFormWindow>()
        var isMainWindowFullScreen = false

        for (window in currentWindows) {
            // Check is no system app
            if (window.type != AccessibilityWindowInfo.TYPE_APPLICATION) continue

            window.getBoundsInScreen(windowBounds)

            // Check is free form
            if (windowBounds.width() >= screenWidth && windowBounds.height() >= screenHeight) {
                if (split.mainWindowPackage.isNotEmpty() && !isMainWindowFullScreen) {
                    isMainWindowFullScreen = window.root?.packageName?.toString() == split.mainWindowPackage
                }
                continue
            }

            // Check window in current split config
            window.root?.packageName?.let { wPcg ->
                when (wPcg) {
                    // Application in freeform mode
                    split.firstAppPackage -> topWindowCandidate = window
                    split.secondAppPackage -> bottomWindowCandidate = window
                    in split.freeWindowPackages ->
                        freePresetCandidates += FreeFormWindow(wPcg.toString(), FreeFormPosition.FREE, window)

                    else -> Unit
                }
            }

            // Break if already collected
            if (topWindowCandidate != null && bottomWindowCandidate != null) break
        }

        val freePresetWindows = freePresetCandidates.distinctBy { it.packageName }
        if (_freePresetWindows.value != freePresetWindows) {
            _freePresetWindows.emit(freePresetWindows)
        }

        // Get split params
        val desiredBottomWindowShift = split.bottomWindowShift

        // Create FreeFormWindow objects if the corresponding window is found
        val freeFormTop = topWindowCandidate?.let { FreeFormWindow(split.firstAppPackage, FreeFormPosition.TOP, it) }
        val freeFormBottom = bottomWindowCandidate?.let {
            FreeFormWindow(split.secondAppPackage, FreeFormPosition.BOTTOM, it)
        }

        val (currentTop, currentBottom) = _freeFormWindows.value
        if (currentTop != freeFormTop || currentBottom != freeFormBottom) {
            _freeFormWindows.emit(Pair(freeFormTop, freeFormBottom))
        }
        _sessionWindows.value = SessionWindows(
            top = freeFormTop,
            bottom = freeFormBottom,
            free = freePresetWindows,
            isCovered = split.isCoveredByOwnWindow(isMainWindowFullScreen),
            isMainWindowFullScreen = isMainWindowFullScreen
        )

        // The preset panel covers the windows but the split stays open
        val trackDarkScreenClose = enableDarkScreenCloseTracking && !isPresetPanelCover
        // If darkScreenAutoClose is enabled, there are no freeform windows and the full-screen application
        // is in the list of windows, send the dark screen close event
        if (darkScreenAutoClose && trackDarkScreenClose && split.sessionId == settledSessionId &&
            topWindowCandidate == null && bottomWindowCandidate == null && freePresetWindows.isEmpty() &&
            currentWindows.hasOwnAppWindow()
        ) {
            // Check this last. The launcher sets the flag before its dark screen comes on screen
            if (!stateKeeper.isLaunchRunning()) {
                stateKeeper.sendCloseDarkScreenEvent()
                enableDarkScreenCloseTracking = false
            }
        }

        // Force focus top window. Refocus only if the second window has focus
        val isSplitClosing = split.sessionId == stateKeeper.getClosedSessionId()
        if (autoRefocusWhenBottomWindowShift && desiredBottomWindowShift &&
            freeFormTop != null && freeFormBottom != null && !isSplitClosing &&
            freeFormBottom.data.isFocused
        ) {
            setFocusWindow(freeFormTop.packageName)
        }
    }

    // The overlay of GSplit does not count. It stays on screen when a full screen app hides the dark screen
    private fun List<AccessibilityWindowInfo>.hasOwnAppWindow() = any { window ->
        window.type == AccessibilityWindowInfo.TYPE_APPLICATION && window.root?.packageName?.toString() == packageName
    }

    // A new window without caption is full screen until it gets its bounds. The main window can expand to full screen
    private fun LaunchedWindowsConfig.isCoveredByOwnWindow(isMainWindowFullScreen: Boolean) =
        sessionId != stateKeeper.getClosedSessionId() &&
            (sessionId != settledSessionId || isMainWindowFullScreen || isPresetPanelCover)

    override fun onInterrupt() {
        Timber.d("[AS] Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        pauseDetector?.release()
        serviceScope.launch {
            stateKeeper.setAccessibilityServiceEnabled(false)
            serviceScope.cancel()
        }
        Timber.d("[AS] Destroyed")
    }

    /**
     * Configures the Accessibility Service parameters.
     */
    private fun configureAccessibilityService() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    private fun getCurrentSessionId() = stateKeeper.getLaunchedWindows()?.sessionId ?: 0L

    private suspend fun closeWindows() {
        stateKeeper.setClosedSessionId(getCurrentSessionId())

        val expandedConfig = stateKeeper.getLaunchedWindows()?.takeIf { it.mainWindowExpanded }
        if (expandedConfig != null && adb.ensureConnected()) {
            closeSessionTasks(expandedConfig)
            return
        }

        val hasFreePresetWindows = _freePresetWindows.value.isNotEmpty()
        if (hasFreePresetWindows) awaitFreePresetWindows()

        when {
            adb.ensureConnected() -> adbCloseWindows()

            sequentialClosing || hasFreePresetWindows -> sequentiallyCloseWindows()

            else -> parallelCloseWindows()
        }
    }

    private fun isAdbConnected() = adb.connectionState.value is AdbConnectionState.Connected

    private fun launchedFreePresetPackages() = stateKeeper.getLaunchedWindows()?.freeWindowPackages.orEmpty()

    // The replace menu covers the windows. The window list shows them again after the menu closes
    private suspend fun awaitFreePresetWindows() {
        val count = launchedFreePresetPackages().size
        withTimeoutOrNull(FREE_WINDOWS_SETTLE_TIMEOUT) {
            freePresetWindows.first { it.size >= count }
        }
    }

    // The full screen main window covers the other windows of the session. The window list does not show them
    private suspend fun closeSessionTasks(config: LaunchedWindowsConfig) {
        (listOf(config.firstAppPackage, config.secondAppPackage) + config.freeWindowPackages)
            .filter { it.isNotEmpty() }
            .distinct()
            .forEach { pkg -> adb.getTaskId(pkg)?.let { adb.minimize(it) } }
        stateKeeper.setLaunchedWindows(config.copy(mainWindowExpanded = false))
    }

    private suspend fun adbCloseWindows() {
        val (first, second) = _freeFormWindows.value
        val p1 = first?.packageName ?: ""
        val p2 = second?.packageName ?: ""
        adb.getTaskId(p1)?.let { adb.minimize(it) }
        adb.getTaskId(p2)?.let { adb.minimize(it) }
        freePresetClosingQueue().forEach { pkg -> adb.getTaskId(pkg)?.let { adb.minimize(it) } }
    }

    @Suppress("unused")
    private suspend fun sequentiallyCloseWindows() {
        val queue = windowsClosingQueue()
        val shownPackages = shownWindows.first().map { it.packageName }

        for (pkg in queue) {
            val appearTimeout = if (pkg in shownPackages) AWAIT_TIMEOUT else HIDDEN_WINDOW_AWAIT_TIMEOUT
            val win = withTimeoutOrNull(appearTimeout) {
                shownWindows
                    .map { list -> list.find { it.packageName == pkg } }
                    .filterNotNull()
                    .first()
            }
            if (win == null) continue

            closeWindow(win)

            withTimeoutOrNull(AWAIT_TIMEOUT) {
                shownWindows
                    .filter { list -> list.none { it.packageName == pkg } }
                    .first()
            }
        }
    }

    private suspend fun parallelCloseWindows() {
        val queue = windowsClosingQueue()

        for (pkg in queue) {
            val win = withTimeoutOrNull(AWAIT_TIMEOUT) {
                shownWindows
                    .map { list -> list.find { it.packageName == pkg } }
                    .filterNotNull()
                    .first()
            } ?: continue

            val closed = withTimeoutOrNull(AWAIT_TIMEOUT) {
                suspendCancellableCoroutine<Boolean> { cont ->
                    closeWindow(win) { result ->
                        cont.resume(result)
                    }
                }
            } ?: false

            if (!closed) {
                Timber.w("[AS] Closing $pkg was not confirmed within $AWAIT_TIMEOUT ms")
            }

            delay(windowClosingExtraPause.toLong())
        }
    }

    private fun windowsClosingQueue(): List<String> {
        val splitPackages = _freeFormWindows.value.toList().mapNotNull { it?.packageName }
        return (if (isLandscape) splitPackages.reversed() else splitPackages) + freePresetClosingQueue()
    }

    // Close the upper window first. An upper window can cover the close button of a lower window
    private fun freePresetClosingQueue(): List<String> {
        val layers = windows.orEmpty().associate { it.id to it.layer }
        val shownPackages = _freePresetWindows.value
            .sortedByDescending { layers[it.data.id] ?: Int.MIN_VALUE }
            .map { it.packageName }
        if (shownPackages.isEmpty()) return emptyList()
        // A covered window is not in the window list. The last launched window is on top
        val hiddenPackages = launchedFreePresetPackages().reversed() - shownPackages.toSet()
        return shownPackages + hiddenPackages
    }

    @Suppress("UnnecessaryVariable")
    private fun closeWindow(window: FreeFormWindow, onClosed: (result: Boolean) -> Unit = {}) {
        val root = window.data.root ?: run {
            onClosed(false)
            return
        }

        // Find buttons in one pass
        val nodes = root.findAccessibilityNodeInfosByViewId("android:id/close_window") to
            root.findAccessibilityNodeInfosByViewId("android:id/maximize_window")
        val closeNode = nodes.first.firstOrNull() ?: run {
            Timber.d("[AS] Close-button not found for ${window.packageName}")
            onClosed(false)
            return
        }
        val maxNode = nodes.second.firstOrNull() ?: run {
            Timber.d("[AS] Maximize-button not found for ${window.packageName}")
            onClosed(false)
            return
        }

        // Get coordinates of the close button
        closeNode.getBoundsInScreen(actionBounds)
        val screenW = screenWidth.toFloat()
        val rawShift = when {
            actionBounds.left < safePx -> safePx - actionBounds.left
            actionBounds.right > screenW - safePx -> (screenW - safePx) - actionBounds.right
            else -> 0f
        }
        val shiftPx = when {
            rawShift > 0f -> rawShift.coerceAtLeast(minDragPx)
            rawShift < 0f -> rawShift.coerceAtMost(-minDragPx)
            else -> 0f
        }

        if (shiftPx == 0f || !dodgeSystemGesWhenClosing) {
            tapClose(window, onClosed)
            return
        }

        // Prepare drag gesture
        maxNode.getBoundsInScreen(actionBounds)
        val startX = (actionBounds.left - 20f).coerceIn(0f, screenW)
        val startY = actionBounds.exactCenterY()
        val endX = (startX + shiftPx).coerceIn(0f, screenW)
        val endY = startY

        val dragPath = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val dragGesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(dragPath, 0, CLOSE_WINDOW_DRAG_TIME))
            .build()

        dispatchGesture(
            dragGesture,
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    Timber.d("[AS] Drag success for ${window.packageName}")
                    serviceScope.launch {
                        delay(CLOSE_WINDOW_DELAY_BEFORE_CLICK)
                        tapClose(window, onClosed)
                    }
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    Timber.w("[AS] Drag cancelled for ${window.packageName}, retrying tap")
                    tapClose(window, onClosed)
                }
            },
            null
        )
    }

    private fun tapClose(window: FreeFormWindow, onClosed: (result: Boolean) -> Unit = {}) {
        val root = window.data.root ?: run {
            onClosed(false)
            return
        }
        val node = root.findAccessibilityNodeInfosByViewId("android:id/close_window")
            ?.firstOrNull()
            ?: run {
                onClosed(false)
                return
            }

        node.getBoundsInScreen(actionBounds)
        val x = actionBounds.exactCenterX().coerceIn(0f, screenWidth.toFloat())
        val y = actionBounds.exactCenterY().coerceIn(0f, screenHeight.toFloat())
        val tapPath = Path().apply { moveTo(x, y) }
        val tapGesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(tapPath, 0, CLOSE_WINDOW_CLICK_TIME))
            .build()

        dispatchGesture(
            tapGesture,
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    onClosed(true)
                    Timber.d("[AS] Close-tap completed for ${window.packageName}")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    onClosed(false)
                    Timber.w("[AS] Close-tap cancelled for ${window.packageName}")
                }
            },
            null
        )
    }

    private suspend fun replaceWindow(index: Int, packageName: String, autoPlay: Boolean) {
        val menuCoveredPackages = replaceMenuCoveredPackages
        replaceMenuCoveredPackages = emptyList()
        val currentConfig = stateKeeper.getLaunchedWindows()
        if (currentConfig?.type == LaunchedSplitType.FREE) {
            replaceFreeWindow(currentConfig, index, packageName, autoPlay)
            return
        }

        // It's open in the neighboring window
        if ((index == 1 && currentConfig?.firstAppPackage == packageName) ||
            (index == 0 && currentConfig?.secondAppPackage == packageName)
        ) {
            withContext(Dispatchers.Main) {
                toast(getString(R.string.opened_in_adjacent_window))
            }
            return
        }

        stateKeeper.setClosedSessionId(getCurrentSessionId())
        try {
            // Check: if there is no window at this index — immediately execute onReplaceWindowTask
            val currentPair = _freeFormWindows.value
            val maybeExisting = if (index == 0) currentPair.first else currentPair.second
            val slotPackage = currentConfig?.run { if (index == 0) firstAppPackage else secondAppPackage }.orEmpty()
            // The replace menu covers the slot window. The window list shows it again after the menu closes
            val isCoveredByMenu = maybeExisting == null && slotPackage in menuCoveredPackages
            if (maybeExisting == null && !isCoveredByMenu) {
                if (currentConfig?.mainWindowExpanded == true && adb.ensureConnected()) {
                    adb.getTaskId(slotPackage)?.let { adb.minimize(it) }
                    delay(150L)
                }
                onReplaceWindowTask(index, packageName, autoPlay)
                return
            }

            // wait until the desired position in Pair becomes non-null
            val targetWindow = withTimeoutOrNull(if (isCoveredByMenu) HIDDEN_WINDOW_AWAIT_TIMEOUT else AWAIT_TIMEOUT) {
                freeFormWindows
                    .map { pair ->
                        if (index == 0) pair.first
                        else pair.second
                    }
                    .filterNotNull() // remove nulls
                    .first() // wait for the first non-null FreeFormWindow
            } ?: run {
                // The covered window does not show again. Replace it as an empty slot
                if (isCoveredByMenu) onReplaceWindowTask(index, packageName, autoPlay)
                return
            }

            if (adb.ensureConnected()) {
                targetWindow.packageName.takeIf { it.isNotEmpty() && it != "unknown" }?.let { targetPackage ->
                    // adb.forceStop(targetPackage)
                    adb.getTaskId(targetPackage)?.let { taskId -> adb.minimize(taskId) }
                    delay(150L)
                    onReplaceWindowTask(index, packageName, autoPlay)
                }
            } else {
                // focus window before closing
                setFocusWindow(targetWindow.packageName)
                delay(150L)

                closeWindow(targetWindow) {
                    serviceScope.launch { onReplaceWindowTask(index, packageName, autoPlay) }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private suspend fun replaceFreeWindow(
        config: LaunchedWindowsConfig,
        index: Int,
        packageName: String,
        autoPlay: Boolean
    ) {
        val targetPackage = config.freePresetWindowPackages.getOrNull(index) ?: return
        if (packageName != targetPackage && packageName in config.freeWindowPackages) {
            withContext(Dispatchers.Main) {
                toast(getString(R.string.opened_in_adjacent_window))
            }
            return
        }

        stateKeeper.setClosedSessionId(config.sessionId)
        // The replaced window can be the only window of the preset. Keep the dark screen until the new session settles
        settledSessionId = 0L

        if (adb.ensureConnected()) {
            adb.getTaskId(targetPackage)?.let { adb.minimize(it) }
            delay(150L)
        } else {
            closeShownFreeWindow(targetPackage)
        }
        onReplaceWindowTask(index, packageName, autoPlay)
    }

    // The replace menu covers the windows. The window list shows them again after the menu closes
    private suspend fun closeShownFreeWindow(packageName: String) {
        val window = withTimeoutOrNull(FREE_WINDOWS_SETTLE_TIMEOUT) {
            freePresetWindows
                .map { list -> list.find { it.packageName == packageName } }
                .filterNotNull()
                .first()
        } ?: return

        setFocusWindow(packageName)
        delay(150L)

        withTimeoutOrNull(AWAIT_TIMEOUT) {
            suspendCancellableCoroutine<Boolean> { cont -> closeWindow(window) { cont.resume(it) } }
        }
    }

    private suspend fun replacePreset(presetId: Long, fromPresetPanel: Boolean) {
        if (switchOpenMainWindow(presetId) { putExtra("id", presetId) }) return

        if (fromPresetPanel && !awaitPresetPanelCoveredWindows()) {
            startPresetLauncher { putExtra("id", presetId) }
            return
        }

        stateKeeper.setClosedSessionId(getCurrentSessionId())

        if (isAdbConnected()) {
            closeWindows()
            delay(350L)
        } else {
            // focus window before closing
            _freeFormWindows.value.first?.let {
                setFocusWindow(it.packageName)
                delay(150L)
            }

            closeWindows()
            delay(200L)
        }

        startPresetLauncher { putExtra("id", presetId) }
    }

    // The launcher switches the main window of the open preset and keeps its windows open. Returns true for it
    private suspend fun switchOpenMainWindow(presetId: Long, launcherExtras: Intent.() -> Unit): Boolean {
        if (stateKeeper.getLaunchedWindows()?.isOpenMainWindowPreset(presetId) != true) return false
        startPresetLauncher(launcherExtras)
        return true
    }

    private fun LaunchedWindowsConfig.isOpenMainWindowPreset(presetId: Long) = this.presetId == presetId &&
        mainWindowPackage.isNotEmpty() && sessionId != stateKeeper.getClosedSessionId()

    private suspend fun replaceSplit(
        firstPackage: String,
        firstAutoPlay: Int,
        secondPackage: String,
        secondAutoPlay: Int,
        type: String,
        darkBackground: Int,
        windowShift: Int,
        firstCaption: Int,
        secondCaption: Int
    ) {
        stateKeeper.setClosedSessionId(getCurrentSessionId())

        if (isAdbConnected()) {
            closeWindows()
            delay(350L)
        } else {
            // focus window before closing
            _freeFormWindows.value.first?.let {
                setFocusWindow(it.packageName)
                delay(150L)
            }

            closeWindows()
            delay(200L)
        }

        startPresetLauncher {
            putExtra("first_package", firstPackage)
            putExtra("second_package", secondPackage)
            putExtra("first_auto_play", firstAutoPlay)
            putExtra("second_auto_play", secondAutoPlay)
            putExtra("type", type)
            putExtra("dark_background", darkBackground)
            putExtra("window_shift", windowShift)
            putExtra("first_caption", firstCaption)
            putExtra("second_caption", secondCaption)
        }
    }

    private suspend fun launchLast(fromPresetPanel: Boolean) {
        // The last launched split is the open session
        val lastPresetId = stateKeeper.getLaunchedWindows()?.presetId
        if (lastPresetId != null && switchOpenMainWindow(lastPresetId) { putExtra("launch_last", true) }) return

        if (fromPresetPanel && !awaitPresetPanelCoveredWindows()) {
            startPresetLauncher { putExtra("launch_last", true) }
            return
        }

        stateKeeper.setClosedSessionId(getCurrentSessionId())

        // focus window before closing
        _freeFormWindows.value.first?.let {
            setFocusWindow(it.packageName)
            delay(150L)
        }

        closeWindows()
        delay(200L)

        startPresetLauncher { putExtra("launch_last", true) }
    }

    // The windows return to the window list after the preset panel closes. Returns true if a split is open
    private suspend fun awaitPresetPanelCoveredWindows(): Boolean {
        val coveredPackages = presetPanelCoveredPackages
        if (coveredPackages.isEmpty()) return false

        withTimeoutOrNull(AWAIT_TIMEOUT) { stateKeeper.presetPanelShown.first { !it } }
        withTimeoutOrNull(HIDDEN_WINDOW_AWAIT_TIMEOUT) {
            shownWindows.first { windows -> windows.map { it.packageName }.containsAll(coveredPackages) }
        }
        return shownWindows.first().isNotEmpty()
    }

    private suspend fun startPresetLauncher(extras: Intent.() -> Unit) {
        val intent = Intent(this@AutoLaunchAccessibilityService, PresetLauncherActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .apply(extras)
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        withContext(Dispatchers.Main) {
            startActivity(intent, options.toBundle())
        }
    }

    private suspend fun closeCurrentWindowsTask(postAction: suspend () -> Unit) {
        stateKeeper.setClosedSessionId(getCurrentSessionId())

        val (first, second) = _freeFormWindows.value
        val hasExpandedMainWindow = stateKeeper.getLaunchedWindows()?.mainWindowExpanded == true
        if (first != null || second != null || _freePresetWindows.value.isNotEmpty() || hasExpandedMainWindow) {
            closeWindows()
            delay(200L)
        }

        postAction()
    }

    private fun onReplaceWindowTask(index: Int, packageName: String, autoPlay: Boolean) =
        stateKeeper.getLaunchedWindows()?.let { currentSplit ->
            serviceScope.launch(Dispatchers.IO) {
                stateKeeper.sendSplitLauncherEvent(
                    SplitLauncherEvent.LaunchWindow(
                        index = index,
                        type = currentSplit.type,
                        packageName = packageName,
                        autoPlay = autoPlay
                    )
                )
            }
        }

    private fun setFocusWindow(packageName: String) {
        if (packageName.isEmpty()) return

        val (firstWindow, secondWindow) = _freeFormWindows.value

        val targetWindow: AccessibilityWindowInfo? = when {
            firstWindow?.packageName == packageName -> firstWindow.data
            secondWindow?.packageName == packageName -> secondWindow.data
            else -> windows.find { window ->
                window.root?.packageName?.toString() == packageName
            }
        }

        if (targetWindow == null) {
            Timber.d("[AS] No window found for package: $packageName")
            return
        }

        // Attempt to set focus via ACTION_FOCUS / ACTION_CLICK on the root node
        targetWindow.root?.let { rootNode ->
            if (rootNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)) {
                Timber.d("[AS] Focus action performed for package: $packageName via ACTION_FOCUS")
            }
            if (rootNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Timber.d("[AS] Focus action performed for package: $packageName via ACTION_CLICK")
            }
        }

        // Refocus via intent
        // Link Tv relaunch every intent bug
        if (packageName != "com.ottplay.ottplas") {
            try {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        // If ACTION_FOCUS does not work, imitate the gesture with increased duration
        /*val bounds = Rect()
        targetWindow.getBoundsInScreen(bounds)
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()

        val path = Path().apply {
            moveTo(centerX, centerY)
        }

        // Increased gesture duration (e.g. 200 ms)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 200))
        val gesture = gestureBuilder.build()

        dispatchGesture(
            gesture,
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    Timber.d("[AS] Focus gesture completed for package: $packageName")
                    super.onCompleted(gestureDescription)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Timber.d("[AS] Focus gesture cancelled for package: $packageName. Retrying...")
                    super.onCancelled(gestureDescription)
                }
            },
            null
        )*/
    }

    private fun saveCurrentFocus() {
        val (firstWindow, secondWindow) = _freeFormWindows.value
        val focusedWindow = if (firstWindow?.data?.isFocused == true) {
            firstWindow.packageName
        } else if (secondWindow?.data?.isFocused == true) {
            secondWindow.packageName
        } else ""
        memorizedFocusPackageName = focusedWindow.ifEmpty { "" }
    }

    private fun restoreCurrentFocus() {
        setFocusWindow(memorizedFocusPackageName)
        memorizedFocusPackageName = ""
    }

    private suspend fun sendSplitStateBroadcast(data: SplitStateBroadcastData) {
        val intent = Intent().apply {
            action = "$BASE_PATH.SPLIT_STATE"
            // `package` = MACRO_DROID_PACKAGE
            putExtra("is_active", if (data.isShown) "1" else "0")
            putExtra("first_window", data.firstPackageName)
            putExtra("second_window", data.secondPackageName)
        }
        withContext(Dispatchers.Main) { sendBroadcast(intent) }
        Timber.d("sending $BASE_PATH.SPLIT_STATE broadcast with $data")
    }

    private fun updateScreenMetrics() {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(dm)
            screenWidth = dm.widthPixels
            screenHeight = dm.heightPixels
        }
        isLandscape = screenWidth > screenHeight
    }

    @Suppress("unused")
    private fun startSleepTask() {
        if (taskSleep?.isActive == true) {
            // Task is already running, exit the method
            return
        }

        taskSleep = serviceScope.launch {
            delay(SLEEP_DELAY)

            // Reset split active data by sessionId
            stateKeeper.getLaunchedWindows()?.let { config ->
                if (config.sessionId == sleepTaskSessionId) {
                    sleepTaskSessionId = -2L
                    stateKeeper.setLaunchedWindows(null)
                    splitWasLaunched = false
                }
            }

            Timber.d("[AS] Event scanner stopped by sleep task")
            stopSleepTask()
        }
    }

    private fun stopSleepTask() {
        taskSleep?.cancel()
        taskSleep = null
    }
}
