package com.salat.preferences.domain.entity

private object IntSharedPrefKey {
    const val CLOSE_OVERLAY_X = "CLOSE_OVERLAY_X"
    const val CLOSE_OVERLAY_Y = "CLOSE_OVERLAY_Y"
    const val REPLACE_OVERLAY_X = "REPLACE_OVERLAY_X"
    const val REPLACE_OVERLAY_Y = "REPLACE_OVERLAY_Y"
}

sealed class IntSharedPref(val key: String, val default: Int) {
    data object CloseOverlayX : IntSharedPref(IntSharedPrefKey.CLOSE_OVERLAY_X, -1)
    data object CloseOverlayY : IntSharedPref(IntSharedPrefKey.CLOSE_OVERLAY_Y, -1)
    data object ReplaceOverlayX : IntSharedPref(IntSharedPrefKey.REPLACE_OVERLAY_X, -1)
    data object ReplaceOverlayY : IntSharedPref(IntSharedPrefKey.REPLACE_OVERLAY_Y, -1)
}
