package com.salat.preferences.domain.entity

private object BoolSharedPrefKey {
    const val DARK_THEME = "dark_theme"
    const val CLOSE_OVERLAY_ENABLED = "close_overlay_enabled"
    const val REPLACE_OVERLAY_ENABLED = "replace_overlay_enabled"
    const val CLOSE_OVERLAY_LOCK = "close_overlay_lock"
    const val REPLACE_OVERLAY_LOCK = "replace_overlay_lock"
    const val REPLACE_OVERLAY_APP_NAMES = "replace_overlay_app_names"
    const val REPLACE_OVERLAY_PRESETS_NAMES = "replace_overlay_presets_names"
}

sealed class BoolSharedPref(val key: String, val default: Boolean) {
    data object DarkTheme : BoolSharedPref(BoolSharedPrefKey.DARK_THEME, true)
    data object CloseOverlayEnabled : BoolSharedPref(BoolSharedPrefKey.CLOSE_OVERLAY_ENABLED, false)
    data object CloseOverlayLock : BoolSharedPref(BoolSharedPrefKey.CLOSE_OVERLAY_LOCK, false)
    data object ReplaceOverlayEnabled : BoolSharedPref(BoolSharedPrefKey.REPLACE_OVERLAY_ENABLED, false)
    data object ReplaceOverlayLock : BoolSharedPref(BoolSharedPrefKey.REPLACE_OVERLAY_LOCK, false)
    data object ReplaceOverlayAppNames : BoolSharedPref(BoolSharedPrefKey.REPLACE_OVERLAY_APP_NAMES, true)
    data object ReplaceOverlayPresetsNames : BoolSharedPref(BoolSharedPrefKey.REPLACE_OVERLAY_PRESETS_NAMES, false)
}
