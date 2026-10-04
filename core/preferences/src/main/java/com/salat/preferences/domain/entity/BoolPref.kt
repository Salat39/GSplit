package com.salat.preferences.domain.entity

import android.os.Build
import com.salat.preferences.BuildConfig

// Only Android 11 moves a running app into a window without a restart
private val taskResizeSupported = Build.VERSION.SDK_INT == Build.VERSION_CODES.R

private object BoolPrefKey {
    const val MINIMIZE_BY_START = "MINIMIZE_BY_START"
    const val MINIMIZE_BY_AUTOSTART = "MINIMIZE_BY_AUTOSTART"
    const val AUTO_START_MINIMIZE_DELAY = "AUTO_START_MINIMIZE_DELAY"
    const val EXPERIMENTAL_NATIVE_SPLIT = "EXPERIMENTAL_NATIVE_SPLIT"
    const val SOFT_KILL_APP = "SOFT_KILL_APP"
    const val SELF_AUTOSTART = "SELF_AUTOSTART"
    const val SELF_AUTOSTART_IN_BG = "SELF_AUTOSTART_IN_BG"
    const val SELF_AUTOSTART_BY_CONNECT = "SELF_AUTOSTART_BY_CONNECT"
    const val SELF_AUTOSTART_AFTER_PAUSE = "SELF_AUTOSTART_AFTER_PAUSE"
    const val YM_COMPAT_PLAY = "YM_COMPAT_PLAY"
    const val MURGLAR_COMPAT_PLAY = "MURGLAR_COMPAT_PLAY"
    const val VKX_COMPAT_PLAY = "VKX_COMPAT_PLAY"

    const val DARK_SCREEN_AUTO_CLOSE = "DARK_SCREEN_AUTO_CLOSE"
    const val AUTO_REFOCUS_WHEN_BOTTOM_WINDOW_SHIFT = "AUTO_REFOCUS_WHEN_BOTTOM_WINDOW_SHIFT"
    const val CLOSE_WINDOW_DODGE_SYSTEM_GES = "CLOSE_WINDOW_DODGE_SYSTEM_GES"
    const val CLOSE_WINDOW_SEQUENTIAL = "CLOSE_WINDOW_SEQUENTIAL"
    const val DARK_SCREEN_BACK_BUTTON = "DARK_SCREEN_BACK_BUTTON"
    const val EXTERNAL_APP_EVENT_SYNC = "MACRO_DROID_EVENT_SYNC"
    const val SHOW_LAST_LAUNCHED_SPLIT = "SHOW_LAST_LAUNCHED_SPLIT"
    const val LAST_LAUNCHED_SPLIT_CONTRAST = "LAST_LAUNCHED_SPLIT_CONTRAST"
    const val ENABLE_ADB_HELPER = "ENABLE_ADB_HELPER"
    const val ENABLE_ADB_FORCE_STOP = "ENABLE_ADB_FORCE_STOP"
    const val ENABLE_ADB_TASK_RESIZE = "ENABLE_ADB_TASK_RESIZE"
    const val NO_CAPTION_WINDOWS = "NO_CAPTION_WINDOWS"
    const val CLOSE_OLD_SPLIT_WINDOWS = "CLOSE_OLD_SPLIT_WINDOWS"
    const val QUICK_SPLIT_INSERT_FIRST = "QUICK_SPLIT_INSERT_FIRST"
    const val QUICK_SPLIT_FIRST_CAPTION = "QUICK_SPLIT_FIRST_CAPTION"
    const val QUICK_SPLIT_SECOND_CAPTION = "QUICK_SPLIT_SECOND_CAPTION"
    const val QUICK_SPLIT_SKIP_SIDE_STEP = "QUICK_SPLIT_SKIP_SIDE_STEP"
    const val QUICK_SPLIT_DARK_BACKGROUND = "QUICK_SPLIT_DARK_BACKGROUND"

    const val ENABLE_OVERLAYS = "ENABLE_OVERLAYS" // Both overlay toggle
}

sealed class BoolPref(override val key: String, override val default: Boolean) : AnyPref {
    data object MinimizeByStart : BoolPref(BoolPrefKey.MINIMIZE_BY_START, false)
    data object MinimizeByAutostart : BoolPref(BoolPrefKey.MINIMIZE_BY_AUTOSTART, false)
    data object AutoStartMinimizeDelay : BoolPref(BoolPrefKey.AUTO_START_MINIMIZE_DELAY, false)
    data object ExperimentalNativeSplit : BoolPref(BoolPrefKey.EXPERIMENTAL_NATIVE_SPLIT, false)
    data object SoftKillApp : BoolPref(BoolPrefKey.SOFT_KILL_APP, false)
    data object SelfAutostart : BoolPref(BoolPrefKey.SELF_AUTOSTART, false)
    data object SelfAutostartInBg : BoolPref(BoolPrefKey.SELF_AUTOSTART_IN_BG, false)
    data object SelfAutostartByConnect : BoolPref(BoolPrefKey.SELF_AUTOSTART_BY_CONNECT, false)
    data object SelfAutostartAfterPause :
        BoolPref(BoolPrefKey.SELF_AUTOSTART_AFTER_PAUSE, BuildConfig.AUTOSTART_AFTER_PAUSE)
    data object YmCompatPlay : BoolPref(BoolPrefKey.YM_COMPAT_PLAY, BuildConfig.COMPAT_PLAY)
    data object MurglarCompatPlay : BoolPref(BoolPrefKey.MURGLAR_COMPAT_PLAY, BuildConfig.COMPAT_PLAY)
    data object VkxCompatPlay : BoolPref(BoolPrefKey.VKX_COMPAT_PLAY, BuildConfig.COMPAT_PLAY)

    data object DarkScreenAutoClose : BoolPref(BoolPrefKey.DARK_SCREEN_AUTO_CLOSE, true)
    data object AutoRefocusWhenBottomWindowShift : BoolPref(BoolPrefKey.AUTO_REFOCUS_WHEN_BOTTOM_WINDOW_SHIFT, false)

    // Shift before close
    data object CloseWindowDodgeSystemGes :
        BoolPref(BoolPrefKey.CLOSE_WINDOW_DODGE_SYSTEM_GES, BuildConfig.SHIFT_BEFORE_CLOSE)

    data object CloseWindowSequential : BoolPref(BoolPrefKey.CLOSE_WINDOW_SEQUENTIAL, false)
    data object DarkScreenBackButton : BoolPref(BoolPrefKey.DARK_SCREEN_BACK_BUTTON, true)
    data object ExternalAppEventSync : BoolPref(BoolPrefKey.EXTERNAL_APP_EVENT_SYNC, false)
    data object ShowLastLaunchedSplit : BoolPref(BoolPrefKey.SHOW_LAST_LAUNCHED_SPLIT, false)
    data object LastLaunchedSplitContrast : BoolPref(BoolPrefKey.LAST_LAUNCHED_SPLIT_CONTRAST, false)
    data object EnableAdbHelper : BoolPref(BoolPrefKey.ENABLE_ADB_HELPER, false)
    data object EnableAdbForceStop : BoolPref(BoolPrefKey.ENABLE_ADB_FORCE_STOP, false)
    data object EnableAdbTaskResize : BoolPref(BoolPrefKey.ENABLE_ADB_TASK_RESIZE, taskResizeSupported)
    data object NoCaptionWindows : BoolPref(BoolPrefKey.NO_CAPTION_WINDOWS, true)
    data object CloseOldSplitWindows : BoolPref(BoolPrefKey.CLOSE_OLD_SPLIT_WINDOWS, true)

    // Quick split - the new window takes the first slot. The open app takes the second slot
    data object QuickSplitInsertFirst : BoolPref(BoolPrefKey.QUICK_SPLIT_INSERT_FIRST, false)
    data object QuickSplitFirstCaption : BoolPref(BoolPrefKey.QUICK_SPLIT_FIRST_CAPTION, false)
    data object QuickSplitSecondCaption : BoolPref(BoolPrefKey.QUICK_SPLIT_SECOND_CAPTION, false)
    data object QuickSplitSkipSideStep : BoolPref(BoolPrefKey.QUICK_SPLIT_SKIP_SIDE_STEP, false)
    data object QuickSplitDarkBackground : BoolPref(BoolPrefKey.QUICK_SPLIT_DARK_BACKGROUND, false)

    data object EnableOverlays : BoolPref(BoolPrefKey.ENABLE_OVERLAYS, false) // Both overlay toggle
}
