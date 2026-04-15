package com.salat.preferences.domain.entity

import com.salat.preferences.BuildConfig

private object BoolPrefKey {
    const val MINIMIZE_BY_START = "MINIMIZE_BY_START"
    const val MINIMIZE_BY_AUTOSTART = "MINIMIZE_BY_AUTOSTART"
    const val AUTO_START_MINIMIZE_DELAY = "AUTO_START_MINIMIZE_DELAY"
    const val CONTEXT_ADAPTIVE_SIZES = "CONTEXT_ADAPTIVE_SIZES2"
    const val EXPERIMENTAL_NATIVE_SPLIT = "EXPERIMENTAL_NATIVE_SPLIT"
    const val SOFT_KILL_APP = "SOFT_KILL_APP"
    const val SELF_AUTOSTART = "SELF_AUTOSTART"
    const val SELF_AUTOSTART_IN_BG = "SELF_AUTOSTART_IN_BG"
    const val SELF_AUTOSTART_BY_CONNECT = "SELF_AUTOSTART_BY_CONNECT"
    const val YM_COMPAT_PLAY = "YM_COMPAT_PLAY"
    const val MURGLAR_COMPAT_PLAY = "MURGLAR_COMPAT_PLAY"
    const val VKX_COMPAT_PLAY = "VKX_COMPAT_PLAY"

    const val DARK_SCREEN_AUTO_CLOSE = "DARK_SCREEN_AUTO_CLOSE"
    const val AUTO_REFOCUS_WHEN_BOTTOM_WINDOW_SHIFT = "AUTO_REFOCUS_WHEN_BOTTOM_WINDOW_SHIFT"
    const val CLOSE_WINDOW_DODGE_SYSTEM_GES = "CLOSE_WINDOW_DODGE_SYSTEM_GES"
    const val CLOSE_WINDOW_SEQUENTIAL = "CLOSE_WINDOW_SEQUENTIAL"
    const val DARK_SCREEN_BACK_BUTTON = "DARK_SCREEN_BACK_BUTTON"
    const val EXTERNAL_APP_EVENT_SYNC = "MACRO_DROID_EVENT_SYNC"
    const val STANDBY_MODE = "STANDBY_MODE"
    const val SHOW_LAST_LAUNCHED_SPLIT = "SHOW_LAST_LAUNCHED_SPLIT"
    const val LAST_LAUNCHED_SPLIT_CONTRAST = "LAST_LAUNCHED_SPLIT_CONTRAST"
    const val ENABLE_ADB_HELPER = "ENABLE_ADB_HELPER"
    const val ENABLE_ADB_FORCE_STOP = "ENABLE_ADB_FORCE_STOP"
    const val ENABLE_ADB_OVERLAY_FUN = "ENABLE_ADB_OVERLAY_FUN"

    const val ENABLE_OVERLAYS = "ENABLE_OVERLAYS" // Both overlay toggle
}

sealed class BoolPref(override val key: String, override val default: Boolean) : AnyPref {
    data object MinimizeByStart : BoolPref(BoolPrefKey.MINIMIZE_BY_START, false)
    data object MinimizeByAutostart : BoolPref(BoolPrefKey.MINIMIZE_BY_AUTOSTART, false)
    data object AutoStartMinimizeDelay : BoolPref(BoolPrefKey.AUTO_START_MINIMIZE_DELAY, false)
    data object ContextAdaptiveSizes : BoolPref(BoolPrefKey.CONTEXT_ADAPTIVE_SIZES, true)
    data object ExperimentalNativeSplit : BoolPref(BoolPrefKey.EXPERIMENTAL_NATIVE_SPLIT, false)
    data object SoftKillApp : BoolPref(BoolPrefKey.SOFT_KILL_APP, false)
    data object SelfAutostart : BoolPref(BoolPrefKey.SELF_AUTOSTART, false)
    data object SelfAutostartInBg : BoolPref(BoolPrefKey.SELF_AUTOSTART_IN_BG, false)
    data object SelfAutostartByConnect : BoolPref(BoolPrefKey.SELF_AUTOSTART_BY_CONNECT, false)
    data object YmCompatPlay : BoolPref(BoolPrefKey.YM_COMPAT_PLAY, BuildConfig.COMPAT_PLAY)
    data object MurglarCompatPlay : BoolPref(BoolPrefKey.MURGLAR_COMPAT_PLAY, BuildConfig.COMPAT_PLAY)
    data object VkxCompatPlay : BoolPref(BoolPrefKey.VKX_COMPAT_PLAY, BuildConfig.COMPAT_PLAY)

    data object DarkScreenAutoClose : BoolPref(BoolPrefKey.DARK_SCREEN_AUTO_CLOSE, false)
    data object AutoRefocusWhenBottomWindowShift : BoolPref(BoolPrefKey.AUTO_REFOCUS_WHEN_BOTTOM_WINDOW_SHIFT, false)

    // Shift before close
    data object CloseWindowDodgeSystemGes :
        BoolPref(BoolPrefKey.CLOSE_WINDOW_DODGE_SYSTEM_GES, BuildConfig.SHIFT_BEFORE_CLOSE)

    data object CloseWindowSequential : BoolPref(BoolPrefKey.CLOSE_WINDOW_SEQUENTIAL, false)
    data object DarkScreenBackButton : BoolPref(BoolPrefKey.DARK_SCREEN_BACK_BUTTON, true)
    data object ExternalAppEventSync : BoolPref(BoolPrefKey.EXTERNAL_APP_EVENT_SYNC, false)
    data object StandbyMode : BoolPref(BoolPrefKey.STANDBY_MODE, true)
    data object ShowLastLaunchedSplit : BoolPref(BoolPrefKey.SHOW_LAST_LAUNCHED_SPLIT, false)
    data object LastLaunchedSplitContrast : BoolPref(BoolPrefKey.LAST_LAUNCHED_SPLIT_CONTRAST, false)
    data object EnableAdbHelper : BoolPref(BoolPrefKey.ENABLE_ADB_HELPER, false)
    data object EnableAdbForceStop : BoolPref(BoolPrefKey.ENABLE_ADB_FORCE_STOP, false)
    data object EnableAdbOverlayFun : BoolPref(BoolPrefKey.ENABLE_ADB_OVERLAY_FUN, false)

    data object EnableOverlays : BoolPref(BoolPrefKey.ENABLE_OVERLAYS, false) // Both overlay toggle
}
