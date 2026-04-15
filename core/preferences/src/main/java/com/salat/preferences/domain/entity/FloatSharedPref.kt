package com.salat.preferences.domain.entity

import com.salat.preferences.BuildConfig

private object FloatSharedPrefKey {
    const val CLOSE_OVERLAY_SCALE = "CLOSE_OVERLAY_SCALE"
    const val CLOSE_OVERLAY_BG_ALPHA = "CLOSE_OVERLAY_BG_ALPHA"
    const val CLOSE_OVERLAY_ICON_ALPHA = "CLOSE_OVERLAY_ICON_ALPHA"
    const val REPLACE_OVERLAY_SCALE = "REPLACE_OVERLAY_SCALE"
    const val REPLACE_WINDOW_SCALE = "REPLACE_WINDOW_SCALE"
    const val REPLACE_OVERLAY_BG_ALPHA = "REPLACE_OVERLAY_BG_ALPHA"
    const val REPLACE_OVERLAY_ICON_ALPHA = "REPLACE_OVERLAY_ICON_ALPHA"
}

sealed class FloatSharedPref(val key: String, val default: Float) {
    data object CloseOverlayScale : FloatSharedPref(FloatSharedPrefKey.CLOSE_OVERLAY_SCALE, BuildConfig.OVERLAY_SCALE)
    data object CloseOverlayBgAlpha : FloatSharedPref(FloatSharedPrefKey.CLOSE_OVERLAY_BG_ALPHA, .8f)
    data object CloseOverlayIconAlpha : FloatSharedPref(FloatSharedPrefKey.CLOSE_OVERLAY_ICON_ALPHA, .9f)
    data object ReplaceOverlayScale : FloatSharedPref(
        FloatSharedPrefKey.REPLACE_OVERLAY_SCALE,
        BuildConfig.OVERLAY_SCALE
    )
    data object ReplaceWindowScale : FloatSharedPref(
        FloatSharedPrefKey.REPLACE_WINDOW_SCALE,
        BuildConfig.OVERLAY_WINDOW_SCALE
    )
    data object ReplaceOverlayBgAlpha : FloatSharedPref(FloatSharedPrefKey.REPLACE_OVERLAY_BG_ALPHA, .8f)
    data object ReplaceOverlayIconAlpha : FloatSharedPref(FloatSharedPrefKey.REPLACE_OVERLAY_ICON_ALPHA, .9f)
}
