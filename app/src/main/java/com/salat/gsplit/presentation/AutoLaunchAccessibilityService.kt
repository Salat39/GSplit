package com.salat.gsplit.presentation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.app.ActivityOptions
import android.content.Intent
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
import com.salat.adb.domain.repository.AdbRepository
import com.salat.gsplit.PresetLauncherActivity
import com.salat.gsplit.presentation.entity.FreeFormPosition
import com.salat.gsplit.presentation.entity.FreeFormWindow
import com.salat.gsplit.presentation.entity.SplitStateBroadcastData
import com.salat.overlay.presentation.startOverlay
import com.salat.overlay.presentation.stopOverlay
import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.resources.R
import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.entity.SplitLauncherEvent
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import dagger.hilt.android.AndroidEntryPoint
import domain.launchWithRetry
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    private val _freeFormWindows = MutableStateFlow<Pair<FreeFormWindow?, FreeFormWindow?>>(Pair(null, null))
    private val freeFormWindows = _freeFormWindows.asStateFlow()

    private val _splitStateBroadcastData = MutableStateFlow<SplitStateBroadcastData?>(null)
    private val splitStateBroadcastData = _splitStateBroadcastData.asStateFlow()

    private var taskSleep: Job? = null
    private var sleepTaskSessionId = -2L

    // Local flag whether to handle dark screen close events
    private var darkScreenAutoClose = false
    private var autoRefocusWhenBottomWindowShift = false
    private var enableOverlays = false

    private var sequentialClosing = false
    private var dodgeSystemGesWhenClosing = true

    private var enableAdbHelper = false
    private var enableAdbOverlayFun = false

    // If true, wait until there are no windows on the screen to close the black screen
    private var enableDarkScreenCloseTracking = false

    // Save current focus for restore after
    private var memorizedFocusPackageName = ""

    // delay between windows closing
    private var windowClosingExtraPause = 100

    // External app notifications
    private var externalAppEventSync = false

    // Start-Stop tasks
    private var standbyMode = false

    // To avoid processing events when the split is not running
    private var splitWasLaunched = false

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

        // start debounced collector for content-changed events
        serviceScope.launch {
            launch {
                stateChangeFlow
                    .debounce(60)
                    .collect { collectFreeFormWindows() }
            }
            launch {
                contentChangeFlow
                    .debounce(300)
                    .collect { collectFreeFormWindows() }
            }
        }

        serviceScope.launch {
            collectBasePrefs()
            collectSplitStateBroadcasts()
            collectWindowsChanges()
        }
        serviceScope.launch { collectSharedEvents() }
    }

    private fun CoroutineScope.collectBasePrefs() = launchWithRetry(RETRY_WHEN_ATTEMPTS) {
        dataStore.getAnyPrefsFlow(
            BoolPref.DarkScreenAutoClose,
            BoolPref.AutoRefocusWhenBottomWindowShift,
            BoolPref.EnableOverlays,
            BoolPref.CloseWindowDodgeSystemGes,
            BoolPref.CloseWindowSequential,
            IntPref.WindowClosingExtraPause,
            BoolPref.ExternalAppEventSync,
            BoolPref.StandbyMode,
            BoolPref.EnableAdbHelper,
            BoolPref.EnableAdbOverlayFun
        ).collect { prefs ->
            darkScreenAutoClose = prefs[0] as Boolean
            autoRefocusWhenBottomWindowShift = prefs[1] as Boolean
            enableOverlays = prefs[2] as Boolean
            dodgeSystemGesWhenClosing = prefs[3] as Boolean
            sequentialClosing = prefs[4] as Boolean
            windowClosingExtraPause = prefs[5] as Int
            externalAppEventSync = prefs[6] as Boolean

            val sm = prefs[7] as Boolean
            if (sm != standbyMode && !sm) stopSleepTask()
            standbyMode = sm

            enableAdbHelper = prefs[8] as Boolean
            enableAdbOverlayFun = prefs[9] as Boolean
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

                    is AccessibilityServiceEvent.ReplaceWindow -> replaceWindow(
                        event.index,
                        event.packageName,
                        event.autoPlay
                    )

                    is AccessibilityServiceEvent.ReplacePreset -> replacePreset(event.presetId)

                    is AccessibilityServiceEvent.ReplaceSplit -> replaceSplit(
                        event.firstPackage,
                        event.firstAutoPlay,
                        event.secondPackage,
                        event.secondAutoPlay,
                        event.type,
                        event.darkBackground,
                        event.windowShift,
                    )

                    AccessibilityServiceEvent.LaunchLast -> launchLast()

                    is AccessibilityServiceEvent.CloseCurrentWindows -> closeCurrentWindowsTask(event.postAction)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    // Notify other app
    private fun CoroutineScope.collectSplitStateBroadcasts() = launch(Dispatchers.IO) {
        splitStateBroadcastData.collect { data -> data?.let { sendSplitStateBroadcast(it) } }
    }

    private fun CoroutineScope.collectWindowsChanges() = launchWithRetry(RETRY_WHEN_ATTEMPTS) {
        freeFormWindows.collect { (topWindow, bottomWindow) ->

            // At least one window appears, enable dark screen closing processing
            if (topWindow != null || bottomWindow != null) {
                enableDarkScreenCloseTracking = true
            }

            // Display overlay management
            if (enableOverlays) {
                if (topWindow != null && bottomWindow != null) {
                    startOverlay(this@AutoLaunchAccessibilityService)
                } else if (topWindow == null && bottomWindow == null) {
                    stopOverlay(this@AutoLaunchAccessibilityService)
                }
            }

            // Notify external apps
            if (externalAppEventSync) {
                _splitStateBroadcastData.update {
                    SplitStateBroadcastData(
                        isShown = topWindow != null || bottomWindow != null,
                        firstPackageName = topWindow?.packageName ?: "",
                        secondPackageName = bottomWindow?.packageName ?: ""
                    )
                }
            }

            if (standbyMode) {
                // Handle "split" was launched
                if (topWindow != null || bottomWindow != null) {
                    stopSleepTask()
                    splitWasLaunched = true
                }

                // Reset current launched config
                if (topWindow == null && bottomWindow == null && splitWasLaunched) {
                    sleepTaskSessionId = stateKeeper.getLaunchedWindows()?.sessionId ?: -2L
                    startSleepTask()
                }
            }

            // TODO windows screen configuration changed
            // Timber.d("[AS] Windows config: $topWindow $bottomWindow")
        }
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
            return
        }

        // Collect the list of freeform windows (condition determined by window size)
        var topWindowCandidate: AccessibilityWindowInfo? = null
        var bottomWindowCandidate: AccessibilityWindowInfo? = null

        for (window in currentWindows) {
            // Check is no system app
            if (window.type != AccessibilityWindowInfo.TYPE_APPLICATION) continue

            window.getBoundsInScreen(windowBounds)

            // Check is free form
            if (windowBounds.width() >= screenWidth && windowBounds.height() >= screenHeight) continue

            // Check window in current split config
            window.root?.packageName?.let { wPcg ->
                when (wPcg) {
                    // Application in freeform mode
                    split.firstAppPackage -> topWindowCandidate = window
                    split.secondAppPackage -> bottomWindowCandidate = window

                    else -> Unit
                }
            }

            // Break if already collected
            if (topWindowCandidate != null && bottomWindowCandidate != null) break
        }

        // Return if no windows
        val (checkTop, checkBottom) = _freeFormWindows.value
        if (topWindowCandidate == null && bottomWindowCandidate == null && checkTop == null && checkBottom == null &&
            !enableDarkScreenCloseTracking
        ) return

        // Get split params
        val desiredBottomWindowShift = split.bottomWindowShift
        val desiredLaunchTime = split.sessionId

        // Create FreeFormWindow objects if the corresponding window is found
        val freeFormTop = topWindowCandidate?.let {
            FreeFormWindow(it.root?.packageName?.toString() ?: "unknown", FreeFormPosition.TOP, it)
        }
        val freeFormBottom = bottomWindowCandidate?.let {
            FreeFormWindow(it.root?.packageName?.toString() ?: "unknown", FreeFormPosition.BOTTOM, it)
        }

        val (currentTop, currentBottom) = _freeFormWindows.value
        if (currentTop != freeFormTop || currentBottom != freeFormBottom) {
            _freeFormWindows.emit(Pair(freeFormTop, freeFormBottom))
        }

        // If darkScreenAutoClose is enabled, there are no freeform windows and the full-screen application
        // is in the list of windows, send the dark screen close event
        val currentTime = System.currentTimeMillis()
        if (darkScreenAutoClose && enableDarkScreenCloseTracking &&
            (currentTime - desiredLaunchTime) >= INIT_WINDOWS_DELAY &&
            topWindowCandidate == null && bottomWindowCandidate == null &&
            // Check if your full-screen self app is present in the window hierarchy
            currentWindows.any { window ->
                window.root?.packageName?.toString() == packageName
            }
        ) {
            stateKeeper.sendCloseDarkScreenEvent()
            enableDarkScreenCloseTracking = false
        }

        // Force focus top window. Refocus only if the second window has focus
        if (autoRefocusWhenBottomWindowShift && desiredBottomWindowShift &&
            freeFormTop != null && freeFormBottom != null && !stateKeeper.inProcessClosingWindows() &&
            bottomWindowCandidate.isFocused
        ) {
            setFocusWindow(freeFormTop.packageName)
        }
    }

    override fun onInterrupt() {
        Timber.d("[AS] Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
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
        when {
            enableAdbHelper && enableAdbOverlayFun -> adbCloseWindows()

            sequentialClosing -> sequentiallyCloseWindows()

            else -> parallelCloseWindows()
        }
    }

    private suspend fun adbCloseWindows() {
        try {
            // disable force refocus
            stateKeeper.setInProcessClosingWindows(true, getCurrentSessionId())

            val (first, second) = _freeFormWindows.value
            val p1 = first?.packageName ?: ""
            val p2 = second?.packageName ?: ""
            // adb.forceStop(p1, p2)
            adb.getTaskId(p1)?.let { adb.minimize(it) }
            adb.getTaskId(p2)?.let { adb.minimize(it) }
        } finally {
            stateKeeper.setInProcessClosingWindows(false)
        }
    }

    @Suppress("unused")
    private suspend fun sequentiallyCloseWindows() {
        try {
            // disable force refocus
            stateKeeper.setInProcessClosingWindows(true, getCurrentSessionId())

            // Save the packages of free-form windows at the start
            val packagesToClose = _freeFormWindows.value
                .toList()
                .mapNotNull { it?.packageName }

            val queue = if (isLandscape) packagesToClose.reversed() else packagesToClose

            for (pkg in queue) {
                // Wait for the current window with this package to appear
                val win = withTimeoutOrNull(AWAIT_TIMEOUT) {
                    freeFormWindows
                        .map { pair -> listOfNotNull(pair.first, pair.second) }
                        .map { list -> list.find { it.packageName == pkg } }
                        .filterNotNull()
                        .first()
                }
                if (win == null) continue

                // Close this live instance
                closeWindow(win)

                // Wait until the window disappears from the list
                withTimeoutOrNull(AWAIT_TIMEOUT) {
                    freeFormWindows
                        .filter { (t, b) ->
                            t?.packageName != pkg && b?.packageName != pkg
                        }
                        .first()
                }
            }

            // enable force refocus
            delay(100L)
        } finally {
            stateKeeper.setInProcessClosingWindows(false)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun parallelCloseWindows() {
        try {
            stateKeeper.setInProcessClosingWindows(true, getCurrentSessionId())

            val packagesToClose = _freeFormWindows.value
                .toList()
                .mapNotNull { it?.packageName }

            val queue = if (isLandscape) packagesToClose.reversed() else packagesToClose

            for (pkg in queue) {
                // Wait for the window to appear (up to AWAIT_TIMEOUT)
                val win = withTimeoutOrNull(AWAIT_TIMEOUT) {
                    freeFormWindows
                        .map { (a, b) -> listOfNotNull(a, b) }
                        .map { list -> list.find { it.packageName == pkg } }
                        .filterNotNull()
                        .first()
                } ?: continue // If timeout — skip the package

                // Close the window, but don't wait for callback indefinitely
                val closed = withTimeoutOrNull(AWAIT_TIMEOUT) {
                    suspendCancellableCoroutine<Boolean> { cont ->
                        closeWindow(win) { result ->
                            cont.resume(result) { /* ignore */ }
                        }
                    }
                } ?: false

                if (!closed) {
                    Timber.w("[AS] Closing $pkg was not confirmed within $AWAIT_TIMEOUT ms")
                }

                // Short pause between iterations
                delay(windowClosingExtraPause.toLong())
            }
        } finally {
            stateKeeper.setInProcessClosingWindows(false)
        }
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
        // It's open in the neighboring window
        val currentConfig = stateKeeper.getLaunchedWindows()
        if ((index == 1 && currentConfig?.firstAppPackage == packageName) ||
            (index == 0 && currentConfig?.secondAppPackage == packageName)
        ) {
            withContext(Dispatchers.Main) {
                toast(getString(R.string.opened_in_adjacent_window))
            }
            return
        }

        try {
            stateKeeper.setInProcessClosingWindows(true, getCurrentSessionId())

            // Check: if there is no window at this index — immediately execute onReplaceWindowTask
            val currentPair = _freeFormWindows.value
            val maybeExisting = if (index == 0) currentPair.first else currentPair.second
            if (maybeExisting == null) {
                onReplaceWindowTask(index, packageName, autoPlay)

                // enable force refocus
                delay(300L)
                stateKeeper.setInProcessClosingWindows(false)
                return
            }

            // wait until the desired position in Pair becomes non-null
            val targetWindow = withTimeoutOrNull(AWAIT_TIMEOUT) {
                freeFormWindows
                    .map { pair ->
                        if (index == 0) pair.first
                        else pair.second
                    }
                    .filterNotNull() // remove nulls
                    .first() // wait for the first non-null FreeFormWindow
            }
            if (targetWindow == null) {
                delay(300L)
                stateKeeper.setInProcessClosingWindows(false)
                return
            }

            if (enableAdbHelper && enableAdbOverlayFun) {
                targetWindow.packageName.takeIf { it.isNotEmpty() && it != "unknown" }?.let { targetPackage ->
                    // adb.forceStop(targetPackage)
                    adb.getTaskId(targetPackage)?.let { taskId -> adb.minimize(taskId) }
                    delay(150L)
                    onReplaceWindowTask(index, packageName, autoPlay)
                }

                stateKeeper.setInProcessClosingWindows(false)
            } else {
                // focus window before closing
                setFocusWindow(targetWindow.packageName)
                delay(150L)

                closeWindow(targetWindow) {
                    serviceScope.launch {
                        onReplaceWindowTask(index, packageName, autoPlay)

                        // enable force refocus
                        delay(300L)
                        stateKeeper.setInProcessClosingWindows(false)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            stateKeeper.setInProcessClosingWindows(false)
        }
    }

    private suspend fun replacePreset(presetId: Long) {
        stateKeeper.setInProcessClosingWindows(true, getCurrentSessionId())

        if (enableAdbHelper && enableAdbOverlayFun) {
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

        val intent = Intent(this@AutoLaunchAccessibilityService, PresetLauncherActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("id", presetId)
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        withContext(Dispatchers.Main) {
            startActivity(intent, options.toBundle())
        }
    }

    private suspend fun replaceSplit(
        firstPackage: String,
        firstAutoPlay: Int,
        secondPackage: String,
        secondAutoPlay: Int,
        type: String,
        darkBackground: Int,
        windowShift: Int
    ) {
        stateKeeper.setInProcessClosingWindows(true, getCurrentSessionId())

        if (enableAdbHelper && enableAdbOverlayFun) {
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

        val intent = Intent(this@AutoLaunchAccessibilityService, PresetLauncherActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("first_package", firstPackage)
        intent.putExtra("second_package", secondPackage)
        intent.putExtra("first_auto_play", firstAutoPlay)
        intent.putExtra("second_auto_play", secondAutoPlay)
        intent.putExtra("type", type)
        intent.putExtra("dark_background", darkBackground)
        intent.putExtra("window_shift", windowShift)
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        withContext(Dispatchers.Main) {
            startActivity(intent, options.toBundle())
        }
    }

    private suspend fun launchLast() {
        stateKeeper.setInProcessClosingWindows(true, getCurrentSessionId())

        // focus window before closing
        _freeFormWindows.value.first?.let {
            setFocusWindow(it.packageName)
            delay(150L)
        }

        closeWindows()
        delay(200L)

        val intent = Intent(this@AutoLaunchAccessibilityService, PresetLauncherActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("launch_last", true)
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        withContext(Dispatchers.Main) {
            startActivity(intent, options.toBundle())
        }
    }

    private suspend fun closeCurrentWindowsTask(postAction: suspend () -> Unit) {
        stateKeeper.setInProcessClosingWindows(true, getCurrentSessionId())

        val (first, second) = _freeFormWindows.value
        if (first != null || second != null) {
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
