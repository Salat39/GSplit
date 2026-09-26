@file:Suppress("unused")

package com.salat.splitlauncher.data.repository

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import com.salat.adb.data.entity.AdbConnectionState
import com.salat.adb.domain.repository.AdbRepository
import com.salat.firebase.domain.entity.FirebasePresetData
import com.salat.firebase.domain.repository.FirebaseRepository
import com.salat.launchhistory.domain.entity.LastLaunchedApp
import com.salat.launchhistory.domain.entity.LastLaunchedTask
import com.salat.launchhistory.domain.entity.LastLaunchedType
import com.salat.launchhistory.domain.entity.LastLaunchedWindow
import com.salat.launchhistory.domain.repository.LaunchHistoryRepository
import com.salat.mediamonitor.domain.repository.MediaMonitorRepository
import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.screenspecs.domain.repository.ScreenSpecsRepository
import com.salat.splitlauncher.data.entity.AutoPlayConfig
import com.salat.splitlauncher.data.entity.WindowDirection
import com.salat.splitlauncher.data.entity.WindowMode
import com.salat.splitlauncher.data.entity.WindowType
import com.salat.splitlauncher.domain.entity.SplitLaunchApp
import com.salat.splitlauncher.domain.entity.SplitLaunchSource
import com.salat.splitlauncher.domain.entity.SplitLaunchTask
import com.salat.splitlauncher.domain.entity.SplitLaunchType
import com.salat.splitlauncher.domain.entity.SplitLaunchWindow
import com.salat.splitlauncher.domain.repository.SplitLauncherRepository
import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.entity.LaunchedSplitType
import com.salat.statekeeper.domain.entity.LaunchedWindowsConfig
import com.salat.statekeeper.domain.entity.SplitLauncherEvent
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.hiddenapibypass.HiddenApiBypass
import presentation.sendMurglarAutoPlayCompat
import presentation.sendPlayerAutoPlay
import presentation.sendVkxAutoPlayCompat
import presentation.sendYmAutoPlayCompat
import timber.log.Timber

class SplitLauncherRepositoryImpl(
    private val context: Context,
    private val stateKeeper: StateKeeperRepository,
    private val dataStore: DataStoreRepository,
    private val screenSpecs: ScreenSpecsRepository,
    private val launchHistory: LaunchHistoryRepository,
    private val mediaMonitor: MediaMonitorRepository,
    private val firebase: FirebaseRepository,
    private val adbHelper: AdbRepository
) : SplitLauncherRepository {
    private val ioScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    private val _freeformHackFlow = MutableSharedFlow<Boolean>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val freeformHackFlow = _freeformHackFlow.asSharedFlow()

    private val _darkBackgroundFlow = MutableSharedFlow<Boolean>()
    override val darkBackgroundFlow = _darkBackgroundFlow.asSharedFlow()

    private val _splitStartedFlow = MutableSharedFlow<Pair<SplitLaunchSource, SplitLaunchTask>>()
    override val splitStartedFlow = _splitStartedFlow.asSharedFlow()

    private val _nativeSplitLaunchTaskFlow = MutableSharedFlow<Pair<Any, Any>>()
    override val nativeSplitLaunchTaskFlow = _nativeSplitLaunchTaskFlow.asSharedFlow()

    override val noCaptionWindowsFlow = combine(
        dataStore.getBooleanPrefFlow(BoolPref.NoCaptionWindows),
        adbHelper.connectionState,
        ::isNoCaptionWindowsActive
    ).stateIn(ioScope, SharingStarted.Eagerly, false)

    private val multiWindowSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    private fun isNoCaptionWindowsActive(enabled: Boolean, connection: AdbConnectionState) =
        enabled && connection is AdbConnectionState.Connected && multiWindowSupported

    // A shared pref flow instance drops a repeated value and first() does not return. Get a new flow for each call
    private suspend fun resolveNoCaptionWindows(): Boolean {
        val enabled = dataStore.getBooleanPrefFlow(BoolPref.NoCaptionWindows).first()
        return enabled && multiWindowSupported && adbHelper.ensureConnected()
    }

    companion object {
        private const val BASE_PATH = "com.salat.gsplit"
        const val YAM_PACKAGE = "ru.yandex.music"
        const val MURGLAR_PACKAGE = "com.badmanners.murglar2"
        const val VKX_PACKAGE = "ua.itaysonlab.vkx"
    }

    private var plannedMediaTask = ""

    init {
        ioScope.launch {
            collectMediaState()
            collectSharedEvents()
        }
    }

    override suspend fun launchSplit(task: SplitLaunchTask, source: SplitLaunchSource) {
        val isFree = task.type == SplitLaunchType.FREE
        // Pinned windows start last. The window tracking expects the last launched window on top
        val freeWindows = task.windows.distinctBy { it.app.packageName }.sortedBy { it.alwaysOnTop }
        val launchApps = if (isFree) freeWindows.map { it.app } else listOfNotNull(task.firstApp, task.secondApp)
        val noCaptionWindows = resolveNoCaptionWindows()
        // Windows without caption do not overlap, the bottom window shift is only for the legacy freeform flow
        val shiftBottomWindow = task.bottomWindowShift && !noCaptionWindows

        // Set launched config to accessibility service
        val launchConfig = if (isFree) {
            LaunchedWindowsConfig(
                firstAppPackage = "",
                secondAppPackage = "",
                autoStart = task.autoStart,
                darkBackground = task.darkBackground,
                bottomWindowShift = false,
                type = task.type.toLaunchedType(),
                presetId = task.id,
                sessionId = System.currentTimeMillis(),
                freeWindowPackages = launchApps.map { it.packageName }
            )
        } else {
            LaunchedWindowsConfig(
                firstAppPackage = task.firstApp?.packageName ?: stateKeeper.getLaunchedWindows()?.firstAppPackage ?: "",
                secondAppPackage = task.secondApp?.packageName
                    ?: stateKeeper.getLaunchedWindows()?.secondAppPackage ?: "",
                autoStart = task.autoStart,
                darkBackground = task.darkBackground,
                bottomWindowShift = shiftBottomWindow,
                type = task.type.toLaunchedType(),
                presetId = task.id,
                sessionId = System.currentTimeMillis(),
                ratio = task.ratio
            )
        }
        stateKeeper.setLaunchedWindows(launchConfig)
        saveLastLaunchedTask(task)

        bypassHiddenApiRestrictions()
        delay(25L)

        val prefData = dataStore.getAnyPrefsFlow(
            IntPref.BypassDelay,
            IntPref.SecondWindowDelay,
            IntPref.AutoPlayDelay,
            BoolPref.ExperimentalNativeSplit,
            BoolPref.SoftKillApp,
            BoolPref.MinimizeByStart,
            BoolPref.MinimizeByAutostart,
            BoolPref.YmCompatPlay,
            BoolPref.MurglarCompatPlay,
            BoolPref.VkxCompatPlay,
            IntPref.BottomWindowShiftSize,
            IntPref.HeightCorrector,
            BoolPref.AutoRefocusWhenBottomWindowShift,
            BoolPref.EnableAdbHelper,
            BoolPref.EnableAdbForceStop,
            BoolPref.ExternalAppEventSync
        ).firstOrNull() ?: return

        val bypassDelay = (prefData[0] as Int).toLong()
        val secondWindowDelay = (prefData[1] as Int).toLong()
        val autoPlayDelay = (prefData[2] as Int).toLong()
        val experimentalNativeSplit = prefData[3] as Boolean
        val softKillApp = prefData[4] as Boolean
        val minimizeByStart = prefData[5] as Boolean
        val minimizeByAutostart = prefData[6] as Boolean
        val ymCompatMode = prefData[7] as Boolean
        val murglarCompatMode = prefData[8] as Boolean
        val vkxCompatMode = prefData[9] as Boolean
        val bottomWindowShiftSize = prefData[10] as Int
        val heightCorrector = prefData[11] as Int
        val autoRefocusWithBottomWindowShift = prefData[12] as Boolean
        val enableAdbHelper = prefData[13] as Boolean
        val enableAdbForceStop = prefData[14] as Boolean
        val externalAppEventSync = prefData[15] as Boolean

        // Force stop via ADB helper
        if (enableAdbHelper && enableAdbForceStop) {
            val targetPkg = launchApps
                .map { it.packageName }
                .filter { adbHelper.isAppInFreeform(it) == false }
                .toSet()
                .toTypedArray()
            adbHelper.forceStop(*targetPkg)

            delay(50L)

            // Soft stop
        } else if (softKillApp) {
            launchApps.forEach { killBackgroundProcesses(it.packageName) }
            delay(50L)
        }

        // Experimental native split method via Accessibility API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && experimentalNativeSplit && !isFree) {
            withContext(Dispatchers.Main) { launchNativeSplit(task) }
            stateKeeper.setPlacedWindowsSessionId(launchConfig.sessionId)
            _splitStartedFlow.emit(Pair(source, task))
            return
        }

        // Cast dark screen by preset settings
        if (task.darkBackground) {
            val hideByCloseDarkScreen = when (source) {
                SplitLaunchSource.CLICK -> minimizeByStart
                SplitLaunchSource.SHORTCUT -> false
                SplitLaunchSource.OVERLAY -> false
                SplitLaunchSource.AUTO_START -> minimizeByAutostart
                SplitLaunchSource.BROADCAST -> false
            }
            _darkBackgroundFlow.emit(hideByCloseDarkScreen)
        }

        _freeformHackFlow.emit(true)

        delay(bypassDelay)

        val bottomWindowShift = if (shiftBottomWindow) bottomWindowShiftSize else 0
        val inReverseOrder = shiftBottomWindow
        val windowsDirection = if (inReverseOrder) {
            listOf(WindowDirection.SECOND, WindowDirection.FIRST)
        } else listOf(WindowDirection.FIRST, WindowDirection.SECOND)

        val playBetweenWindows = ymCompatMode && shiftBottomWindow && !autoRefocusWithBottomWindowShift &&
            task.secondApp?.packageName == YAM_PACKAGE && task.secondApp.autoPlay == true

        if (isFree) {
            launchFreeWindows(freeWindows, heightCorrector, secondWindowDelay, noCaptionWindows)
        } else {
            var isFirst = true
            windowsDirection.forEach { windowType ->

                if (!isFirst && playBetweenWindows) {
                    delay((autoPlayDelay - secondWindowDelay).coerceAtLeast(0))
                    if (launchConfig.sessionId != stateKeeper.getClosedSessionId() &&
                        launchConfig.secondAppPackage == task.secondApp.packageName
                    ) {
                        sendYandexMusicCompatPlay(false)
                    }
                }

                when (windowType) {
                    WindowDirection.FIRST -> {
                        task.firstApp?.let { app ->
                            val firstSpec = when (task.type) {
                                SplitLaunchType.HALF -> WindowType.HALF_LEFT
                                SplitLaunchType.ONE_TO_THREE -> WindowType.ONE_TO_THREE_LEFT
                                SplitLaunchType.TWO_TO_THREE -> WindowType.TWO_TO_THREE_LEFT
                                SplitLaunchType.THREE_TO_FOUR -> WindowType.THREE_TO_FOUR_LEFT
                                SplitLaunchType.THREE_TO_TWO -> WindowType.THREE_TO_TWO_LEFT
                                SplitLaunchType.FOUR_TO_THREE -> WindowType.FOUR_TO_THREE_LEFT
                                SplitLaunchType.CUSTOM -> WindowType.CUSTOM_LEFT
                                SplitLaunchType.FREE -> return@let
                            }

                            launchAppInWindow(
                                context = context,
                                packageName = app.packageName,
                                windowSize = firstSpec,
                                customRatio = task.ratio,
                                bottomWindowShift = bottomWindowShift,
                                heightCorrector = heightCorrector,
                                noCaption = noCaptionWindows
                            )
                        }

                        isFirst = false
                    }

                    WindowDirection.SECOND -> {
                        task.secondApp?.let { app ->
                            val secondSpec = when (task.type) {
                                SplitLaunchType.HALF -> WindowType.HALF_RIGHT
                                SplitLaunchType.ONE_TO_THREE -> WindowType.ONE_TO_THREE_RIGHT
                                SplitLaunchType.TWO_TO_THREE -> WindowType.TWO_TO_THREE_RIGHT
                                SplitLaunchType.THREE_TO_FOUR -> WindowType.THREE_TO_FOUR_RIGHT
                                SplitLaunchType.THREE_TO_TWO -> WindowType.THREE_TO_TWO_RIGHT
                                SplitLaunchType.FOUR_TO_THREE -> WindowType.FOUR_TO_THREE_RIGHT
                                SplitLaunchType.CUSTOM -> WindowType.CUSTOM_RIGHT
                                SplitLaunchType.FREE -> return@let
                            }

                            launchAppInWindow(
                                context = context,
                                packageName = app.packageName,
                                windowSize = secondSpec,
                                customRatio = task.ratio,
                                bottomWindowShift = bottomWindowShift,
                                heightCorrector = heightCorrector,
                                noCaption = noCaptionWindows
                            )
                        }
                        isFirst = false
                    }
                }

                delay(secondWindowDelay)
            }
        }

        // Split launch notify
        stateKeeper.setPlacedWindowsSessionId(launchConfig.sessionId)
        _splitStartedFlow.emit(Pair(source, task))

        // Notify external apps
        if (externalAppEventSync) sendInitSplitBroadcast(task)

        val autoPlayApps = launchApps.filter { it.autoPlay == true }
        if (!playBetweenWindows && autoPlayApps.isNotEmpty()) {
            val autoPlayConfig = AutoPlayConfig(
                ymCompatMode = ymCompatMode,
                murglarCompatMode = murglarCompatMode,
                vkxCompatMode = vkxCompatMode
            )
            delay(autoPlayDelay)
            autoPlayApps.forEachIndexed { index, app ->
                if (index > 0) delay(25L)
                if (launchConfig.sessionId != stateKeeper.getClosedSessionId()) {
                    launchAutoPlay(app.packageName, autoPlayConfig)
                }
            }
        }

        // Firebase log
        val firebaseLogData = FirebasePresetData(
            firstPackage = task.firstApp?.packageName ?: "",
            secondPackage = task.secondApp?.packageName ?: "",
            type = task.type.getTitle(),
            source = source.getTitle(),
            autoStart = task.autoStart,
            darkBackground = task.darkBackground,
            bottomWindowShift = task.bottomWindowShift,
            softKillApp = softKillApp,
            minimizeByStart = minimizeByStart,
            minimizeByAutostart = minimizeByAutostart,
            ymCompatPlay = ymCompatMode // todo other compat log
        )
        firebase.logOpenPreset(firebaseLogData)
    }

    /**
     * Launches an application by its packageName in a window with specified dimensions.
     *
     * In portrait mode, windows are split vertically (left/right),
     * in landscape mode – horizontally (top/bottom).
     *
     * @param context The context used to launch the activity.
     * @param packageName The package name of the application to launch.
     * @param windowSize Window type
     */
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private suspend fun launchAppInWindow(
        context: Context,
        packageName: String,
        windowSize: WindowType,
        customRatio: Float,
        bottomWindowShift: Int,
        heightCorrector: Int,
        noCaption: Boolean = false
    ) {
        // Calculate status bar height
        val statusBarHeight = screenSpecs.getStatusBarHeight()

        val screenWidth = screenSpecs.getFreeScreenWidth()
        val screenHeight = screenSpecs.getFreeScreenHeight() + heightCorrector
        // val (leftInset, rightInset) = getScreenHorizontalInsets()

        // Determine device orientation
        val orientation = context.resources.configuration.orientation
        val isPortrait = orientation == Configuration.ORIENTATION_PORTRAIT
        val heightOffset = statusBarHeight // if (isPortrait) statusBarHeight else navBarHeight

        // Calculate window bounds taking into account the obtained system offsets
        val bounds = if (isPortrait) {
            // Portrait: divide screen vertically (top/bottom)
            when (windowSize) {
                // 1x1
                WindowType.HALF_LEFT -> Rect(
                    0,
                    heightOffset,
                    screenWidth,
                    (screenHeight / 2) + heightOffset
                )

                WindowType.HALF_RIGHT -> Rect(
                    0,
                    (screenHeight / 2) + heightOffset - bottomWindowShift,
                    screenWidth,
                    screenHeight + heightOffset
                )

                // 1x2
                WindowType.ONE_TO_THREE_LEFT -> Rect(
                    0,
                    heightOffset,
                    screenWidth,
                    (screenHeight / 3) + heightOffset
                )

                WindowType.ONE_TO_THREE_RIGHT -> Rect(
                    0,
                    screenHeight / 3 + heightOffset - bottomWindowShift,
                    screenWidth,
                    screenHeight + heightOffset
                )

                // 2x1
                WindowType.TWO_TO_THREE_LEFT -> Rect(
                    0,
                    heightOffset,
                    screenWidth,
                    ((screenHeight * 2) / 3) + heightOffset
                )

                WindowType.TWO_TO_THREE_RIGHT -> Rect(
                    0,
                    ((screenHeight * 2) / 3) + heightOffset - bottomWindowShift,
                    screenWidth,
                    screenHeight + heightOffset
                )

                // 3x4
                WindowType.THREE_TO_FOUR_LEFT -> Rect(
                    0,
                    heightOffset,
                    screenWidth,
                    ((screenHeight * 3) / 7) + heightOffset
                )

                WindowType.THREE_TO_FOUR_RIGHT -> Rect(
                    0,
                    ((screenHeight * 3) / 7) + heightOffset - bottomWindowShift,
                    screenWidth,
                    screenHeight + heightOffset
                )

                // 4x3
                WindowType.FOUR_TO_THREE_LEFT -> Rect(
                    0,
                    heightOffset,
                    screenWidth,
                    ((screenHeight * 4) / 7) + heightOffset
                )

                WindowType.FOUR_TO_THREE_RIGHT -> Rect(
                    0,
                    ((screenHeight * 4) / 7) + heightOffset - bottomWindowShift,
                    screenWidth,
                    screenHeight + heightOffset
                )

                // 3x2
                WindowType.THREE_TO_TWO_LEFT -> Rect(
                    0,
                    heightOffset,
                    screenWidth,
                    ((screenHeight * 3) / 5) + heightOffset
                )

                WindowType.THREE_TO_TWO_RIGHT -> Rect(
                    0,
                    ((screenHeight * 3) / 5) + heightOffset - bottomWindowShift,
                    screenWidth,
                    screenHeight + heightOffset
                )

                WindowType.CUSTOM_LEFT -> Rect(
                    0,
                    heightOffset,
                    screenWidth,
                    (screenHeight * customRatio).roundToInt() + heightOffset
                )

                WindowType.CUSTOM_RIGHT -> Rect(
                    0,
                    (screenHeight * customRatio).roundToInt() + heightOffset - bottomWindowShift,
                    screenWidth,
                    screenHeight + heightOffset
                )

                WindowType.FULLSCREEN -> Rect(0, heightOffset, screenWidth, screenHeight + heightOffset)
            }
        } else {
            // Landscape: divide screen horizontally (left/right)
            when (windowSize) {
                // 1x1
                WindowType.HALF_LEFT -> Rect(
                    0,
                    heightOffset,
                    (screenWidth / 2),
                    screenHeight + heightOffset
                )

                WindowType.HALF_RIGHT -> Rect(
                    (screenWidth / 2),
                    heightOffset,
                    screenWidth,
                    screenHeight + heightOffset
                )

                // 1x2
                WindowType.ONE_TO_THREE_LEFT -> Rect(
                    0,
                    heightOffset,
                    (screenWidth / 3),
                    screenHeight + heightOffset
                )

                WindowType.ONE_TO_THREE_RIGHT -> Rect(
                    (screenWidth / 3),
                    heightOffset,
                    screenWidth,
                    screenHeight + heightOffset
                )

                // 2x1
                WindowType.TWO_TO_THREE_LEFT -> Rect(
                    0,
                    heightOffset,
                    ((screenWidth * 2) / 3),
                    screenHeight + heightOffset
                )

                WindowType.TWO_TO_THREE_RIGHT -> Rect(
                    ((screenWidth * 2) / 3),
                    heightOffset,
                    screenWidth,
                    screenHeight + heightOffset
                )

                // 3x4
                WindowType.THREE_TO_FOUR_LEFT -> Rect(
                    0,
                    heightOffset,
                    ((screenWidth * 3) / 7),
                    screenHeight + heightOffset
                )

                WindowType.THREE_TO_FOUR_RIGHT -> Rect(
                    ((screenWidth * 3) / 7),
                    heightOffset,
                    screenWidth,
                    screenHeight + heightOffset
                )

                // 4x3
                WindowType.FOUR_TO_THREE_LEFT -> Rect(
                    0,
                    heightOffset,
                    ((screenWidth * 4) / 7),
                    screenHeight + heightOffset
                )

                WindowType.FOUR_TO_THREE_RIGHT -> Rect(
                    ((screenWidth * 4) / 7),
                    heightOffset,
                    screenWidth,
                    screenHeight + heightOffset
                )

                // 3x2
                WindowType.THREE_TO_TWO_LEFT -> Rect(
                    0,
                    heightOffset,
                    ((screenWidth * 3) / 5),
                    screenHeight + heightOffset
                )

                WindowType.THREE_TO_TWO_RIGHT -> Rect(
                    ((screenWidth * 3) / 5),
                    heightOffset,
                    screenWidth,
                    screenHeight + heightOffset
                )

                WindowType.CUSTOM_LEFT -> Rect(
                    0,
                    heightOffset,
                    (screenWidth * customRatio).roundToInt(),
                    screenHeight + heightOffset
                )

                WindowType.CUSTOM_RIGHT -> Rect(
                    (screenWidth * customRatio).roundToInt(),
                    heightOffset,
                    screenWidth,
                    screenHeight + heightOffset
                )

                WindowType.FULLSCREEN -> Rect(0, heightOffset, screenWidth, screenHeight + heightOffset)
            }
        }

        startAppInBounds(context, packageName, bounds, noCaption = noCaption)
    }

    private suspend fun launchFreeWindows(
        windows: List<SplitLaunchWindow>,
        heightCorrector: Int,
        windowDelay: Long,
        noCaption: Boolean
    ) {
        val statusBarHeight = screenSpecs.getStatusBarHeight()
        val (leftInset, _) = screenSpecs.getScreenHorizontalInsets()
        val screenWidth = screenSpecs.getFreeScreenWidth()
        val screenHeight = screenSpecs.getFreeScreenHeight() + heightCorrector

        windows.forEach { window ->
            val bounds = Rect(
                leftInset + (window.left * screenWidth).roundToInt(),
                statusBarHeight + (window.top * screenHeight).roundToInt(),
                leftInset + (window.right * screenWidth).roundToInt(),
                statusBarHeight + (window.bottom * screenHeight).roundToInt()
            )
            // A pinned window keeps the caption. The user moves and closes the window with the caption
            val windowNoCaption = noCaption && !window.alwaysOnTop
            startAppInBounds(context, window.app.packageName, bounds, window.alwaysOnTop, windowNoCaption)
            delay(windowDelay)
        }
    }

    private suspend fun startAppInBounds(
        context: Context,
        packageName: String,
        bounds: Rect,
        alwaysOnTop: Boolean = false,
        noCaption: Boolean = false
    ) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            Timber.d("Application not found")
            return
        }
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        launchIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
        )

        // Multi-window mode has no caption but ignores launch bounds - the task gets its bounds through ADB after start
        val windowMode = if (noCaption) WindowMode.MULTI_WINDOW else WindowMode.FREEFORM
        try {
            val options = ActivityOptions
                .makeCustomAnimation(context, 0, 0)
                .setLaunchWindowingMode(windowMode.id)
                .setTaskAlwaysOnTop(alwaysOnTop)
            if (!noCaption) options.setLaunchBounds(bounds)
            withContext(Dispatchers.Main) {
                context.startActivity(launchIntent, options.toBundle())
            }
            if (noCaption && !adbHelper.resizeNewTask(packageName, bounds)) {
                Timber.e("New task of $packageName not found, the window keeps the full screen size")
            }
            return
        } catch (e: Exception) {
            Timber.e(e)
        }
        // If launch bounds couldn't be set, launch the app in standard way
        withContext(Dispatchers.Main) {
            context.startActivity(launchIntent)
        }
    }

    private fun saveLastLaunchedTask(task: SplitLaunchTask) = ioScope.launch {
        val isFullTask = task.firstApp?.packageName?.isNotEmpty() == true &&
            task.secondApp?.packageName?.isNotEmpty() == true
        if (task.type == SplitLaunchType.FREE || isFullTask) {
            launchHistory.saveLastConfig(task.toLastLaunchedTask())
        } else {
            val firstApp = if (task.firstApp?.packageName?.isNotEmpty() == true) {
                LastLaunchedApp(
                    title = task.firstApp.title,
                    packageName = task.firstApp.packageName,
                    autoPlay = task.firstApp.autoPlay
                )
            } else null
            val secondApp = if (task.secondApp?.packageName?.isNotEmpty() == true) {
                LastLaunchedApp(
                    title = task.secondApp.title,
                    packageName = task.secondApp.packageName,
                    autoPlay = task.secondApp.autoPlay
                )
            } else null
            launchHistory.patchLastConfig(firstApp, secondApp)
        }
    }

    private fun ActivityOptions.setTaskAlwaysOnTop(alwaysOnTop: Boolean): ActivityOptions {
        if (alwaysOnTop && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val method = this.javaClass.getMethod("setTaskAlwaysOnTop", Boolean::class.javaPrimitiveType)
                method.invoke(this, true)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return this
    }

    /**
     * Extension to set the launch windowing mode via hidden API.
     * For freeform mode, the value 5 is used; for fullscreen mode, the value 1 is used.
     */
    private fun ActivityOptions.setLaunchWindowingMode(mode: Int): ActivityOptions {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val method = this.javaClass.getMethod("setLaunchWindowingMode", Int::class.javaPrimitiveType)
                method.invoke(this, mode)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return this
    }

    /**
     * Invokes the method to bypass hidden API restrictions.
     * Adds exemptions for the ActivityOptions class to allow the use of setLaunchBounds.
     */
    private fun bypassHiddenApiRestrictions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // For example, allowing access to the ActivityOptions class API
            val result = HiddenApiBypass.addHiddenApiExemptions("Landroid/app/ActivityOptions;")
            Timber.d("Hidden API bypass result: $result")
        }
    }

    private suspend fun launchAutoPlay(mainPackageName: String, config: AutoPlayConfig) {
        try {
            if (config.ymCompatMode && mainPackageName == YAM_PACKAGE) {
                sendYandexMusicCompatPlay(true)
                return
            }

            withContext(Dispatchers.Main) { context.sendPlayerAutoPlay(mainPackageName) }

            // Play and add play task
            if (config.murglarCompatMode && mainPackageName == MURGLAR_PACKAGE) {
                sendMurglarCompatPlay()
                if (!mediaMonitor.isPlaying()) {
                    plannedMediaTask = mainPackageName
                }
            }

            // Play or add play task
            if (config.vkxCompatMode && mainPackageName == VKX_PACKAGE) {
                if (mediaMonitor.isPlaying()) {
                    sendVkxCompatPlay()
                } else {
                    plannedMediaTask = mainPackageName
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun CoroutineScope.collectMediaState() = launch {
        mediaMonitor.mediaStateFlow.collect { isPlaying ->
            if (isPlaying && plannedMediaTask.isNotEmpty()) {
                delay(200L)
                if (plannedMediaTask == MURGLAR_PACKAGE) sendMurglarCompatPlay()
                if (plannedMediaTask == VKX_PACKAGE) sendVkxCompatPlay()
                plannedMediaTask = ""
            }
        }
    }

    private fun CoroutineScope.collectSharedEvents() = launch {
        stateKeeper.splitLauncherEvents.collect { event ->
            when (event) {
                is SplitLauncherEvent.LaunchWindow -> {
                    if (event.type == LaunchedSplitType.FREE) return@collect

                    val window = SplitLaunchApp(
                        title = event.packageName,
                        packageName = event.packageName,
                        autoPlay = event.autoPlay
                    )

                    stateKeeper.getLaunchedWindows()?.let { currentConfig ->
                        launchSplit(
                            SplitLaunchTask(
                                firstApp = if (event.index == 0) window else null,
                                type = event.type.toSplitType(),
                                secondApp = if (event.index == 1) window else null,
                                autoStart = currentConfig.autoStart,
                                darkBackground = false, // So you don't get spammed with black windows
                                bottomWindowShift = currentConfig.bottomWindowShift,
                                id = currentConfig.presetId,
                                ratio = currentConfig.ratio
                            ),
                            SplitLaunchSource.BROADCAST
                        )
                    }
                }
            }
        }
    }

    private suspend fun sendInitSplitBroadcast(task: SplitLaunchTask) {
        val intent = Intent().apply {
            action = "$BASE_PATH.INIT_SPLIT"
            putExtra("first_window", task.firstApp?.packageName ?: "")
            putExtra("second_window", task.secondApp?.packageName ?: "")
        }
        withContext(Dispatchers.Main) { context.sendBroadcast(intent) }
        Timber.d("sending $BASE_PATH.INIT_SPLIT broadcast with ${intent.extras}")
    }

    private suspend fun sendYandexMusicCompatPlay(refocus: Boolean = false) = try {
        // Save focus before play intent
        if (refocus) {
            stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.RememberFocus)
        }

        // Set focus before open
        // stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.FocusWindow(YAM_PACKAGE))

        withContext(Dispatchers.Main) { context.sendYmAutoPlayCompat() }

        // Restore focus
        if (refocus) {
            stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.RestoreFocus)
        } else Unit
    } catch (e: Exception) {
        Timber.e(e)
    }

    private suspend fun sendMurglarCompatPlay() = try {
        // Save focus before play intent
        stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.RememberFocus)

        // Set focus before open
        // stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.FocusWindow(MURGLAR_PACKAGE))

        // Send play
        withContext(Dispatchers.Main) { context.sendMurglarAutoPlayCompat() }

        // Restore focus
        stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.RestoreFocus)
    } catch (e: Exception) {
        Timber.e(e)
    }

    private suspend fun sendVkxCompatPlay() = try {
        // Save focus before play intent
        stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.RememberFocus)

        // Set focus before open
        // stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.FocusWindow(VKX_PACKAGE))

        // Send play
        withContext(Dispatchers.Main) { context.sendVkxAutoPlayCompat() }

        // Restore focus
        stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.RestoreFocus)
    } catch (e: Exception) {
        Timber.e(e)
    }

    @SuppressLint("PrivateApi")
    fun forceStopPackage(context: Context, packageName: String) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        try {
            val method = activityManager.javaClass.getDeclaredMethod(
                "forceStopPackage",
                String::class.java
            )
            method.isAccessible = true
            method.invoke(activityManager, packageName)
            Timber.d("forceStopPackage success")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Terminates the background processes of the specified application.
     *
     * @param packageName The package name of the application whose processes need to be terminated.
     */
    private fun killBackgroundProcesses(packageName: String) {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.killBackgroundProcesses(packageName)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private suspend fun launchNativeSplit(task: SplitLaunchTask) {
        if (task.firstApp == null || task.secondApp == null) return

        // Retrieve launch intents for the apps; exit if either is null
        val intent1 = context.packageManager.getLaunchIntentForPackage(task.firstApp.packageName) ?: return
        val intent2 = context.packageManager.getLaunchIntentForPackage(task.secondApp.packageName) ?: return

        // Add flags for split-screen mode
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)

        // Convert intents to URI strings and then back to intents
        val first = Intent.parseUri(intent1.toUri(0), 0)
        val second = Intent.parseUri(intent2.toUri(0), 0)

        // Emit the result through the flow
        _nativeSplitLaunchTaskFlow.emit(first to second)
    }

    private fun SplitLaunchType.getTitle() = when (this) {
        SplitLaunchType.HALF -> "1x1"
        SplitLaunchType.ONE_TO_THREE -> "1x2"
        SplitLaunchType.TWO_TO_THREE -> "2x1"
        SplitLaunchType.THREE_TO_FOUR -> "3x4"
        SplitLaunchType.THREE_TO_TWO -> "3x2"
        SplitLaunchType.FOUR_TO_THREE -> "4x3"
        SplitLaunchType.FREE -> "free"
        SplitLaunchType.CUSTOM -> "custom"
    }

    private fun SplitLaunchSource.getTitle() = when (this) {
        SplitLaunchSource.CLICK -> "click"
        SplitLaunchSource.SHORTCUT -> "shortcut"
        SplitLaunchSource.AUTO_START -> "auto_start"
        SplitLaunchSource.OVERLAY -> "overlay"
        SplitLaunchSource.BROADCAST -> "broadcast"
    }

    private fun SplitLaunchType.toLaunchedType() = when (this) {
        SplitLaunchType.HALF -> LaunchedSplitType.HALF
        SplitLaunchType.ONE_TO_THREE -> LaunchedSplitType.ONE_TO_THREE
        SplitLaunchType.TWO_TO_THREE -> LaunchedSplitType.TWO_TO_THREE
        SplitLaunchType.THREE_TO_FOUR -> LaunchedSplitType.THREE_TO_FOUR
        SplitLaunchType.THREE_TO_TWO -> LaunchedSplitType.THREE_TO_TWO
        SplitLaunchType.FOUR_TO_THREE -> LaunchedSplitType.FOUR_TO_THREE
        SplitLaunchType.FREE -> LaunchedSplitType.FREE
        SplitLaunchType.CUSTOM -> LaunchedSplitType.CUSTOM
    }

    private fun LaunchedSplitType.toSplitType() = when (this) {
        LaunchedSplitType.HALF -> SplitLaunchType.HALF
        LaunchedSplitType.ONE_TO_THREE -> SplitLaunchType.ONE_TO_THREE
        LaunchedSplitType.TWO_TO_THREE -> SplitLaunchType.TWO_TO_THREE
        LaunchedSplitType.THREE_TO_FOUR -> SplitLaunchType.THREE_TO_FOUR
        LaunchedSplitType.THREE_TO_TWO -> SplitLaunchType.THREE_TO_TWO
        LaunchedSplitType.FOUR_TO_THREE -> SplitLaunchType.FOUR_TO_THREE
        LaunchedSplitType.FREE -> SplitLaunchType.FREE
        LaunchedSplitType.CUSTOM -> SplitLaunchType.CUSTOM
    }

    private fun SplitLaunchTask.toLastLaunchedTask(): LastLaunchedTask {
        return LastLaunchedTask(
            firstApp = this.firstApp?.toLastLaunchedApp(),
            secondApp = this.secondApp?.toLastLaunchedApp(),
            type = this.type.toLastLaunchedType(),
            autoStart = this.autoStart,
            darkBackground = this.darkBackground,
            bottomWindowShift = this.bottomWindowShift,
            id = this.id,
            windows = this.windows.map { it.toLastLaunchedWindow() },
            ratio = this.ratio
        )
    }

    private fun SplitLaunchWindow.toLastLaunchedWindow() = LastLaunchedWindow(
        app = app.toLastLaunchedApp(),
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        alwaysOnTop = alwaysOnTop
    )

    private fun SplitLaunchApp.toLastLaunchedApp() = LastLaunchedApp(
        title = this.title,
        packageName = this.packageName,
        autoPlay = this.autoPlay
    )

    private fun SplitLaunchType.toLastLaunchedType() = LastLaunchedType.entries.first { it.id == this.id }
}
