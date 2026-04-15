package com.salat.preferences.domain.entity

import com.salat.preferences.BuildConfig

private object IntPrefKey {
    const val BYPASS_DELAY = "BYPASS_DELAY"
    const val SECOND_WINDOW_DELAY = "SECOND_WINDOW_DELAY"
    const val AUTO_PLAY_DELAY = "AUTO_PLAY_DELAY"
    const val TOOLBAR_EXTRA_SPACE = "TOOLBAR_EXTRA_SPACE"
    const val BOTTOM_WINDOW_SHIFT_SIZE = "BOTTOM_WINDOW_SHIFT_SIZE"
    const val AUTOSTART_DELAY = "AUTOSTART_DELAY"
    const val HEIGHT_CORRECTOR = "HEIGHT_CORRECTOR"
    const val WINDOW_CLOSING_EXTRA_PAUSE = "WINDOW_CLOSING_EXTRA_PAUSE"
    const val ADB_HELPER_PORT = "ADB_HELPER_PORT"
}

sealed class IntPref(override val key: String, override val default: Int) : AnyPref {
    data object BypassDelay : IntPref(IntPrefKey.BYPASS_DELAY, 100)
    data object SecondWindowDelay : IntPref(IntPrefKey.SECOND_WINDOW_DELAY, 200)
    data object AutoPlayDelay : IntPref(IntPrefKey.AUTO_PLAY_DELAY, 3000)
    data object ToolbarExtraSpace : IntPref(
        IntPrefKey.TOOLBAR_EXTRA_SPACE,
        BuildConfig.TOOLBAR_EXTRA_SPACE
    )

    data object BottomWindowShiftSize : IntPref(
        IntPrefKey.BOTTOM_WINDOW_SHIFT_SIZE,
        BuildConfig.BOTTOM_WINDOW_SHIFT_SIZE
    )

    data object AutostartDelay : IntPref(IntPrefKey.AUTOSTART_DELAY, 2000)

    data object HeightCorrector : IntPref(IntPrefKey.HEIGHT_CORRECTOR, 0)

    data object WindowClosingExtraPause : IntPref(IntPrefKey.WINDOW_CLOSING_EXTRA_PAUSE, 100)

    data object AdbHelperPort : IntPref(IntPrefKey.ADB_HELPER_PORT, 5555)
}
