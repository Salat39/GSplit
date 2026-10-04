package com.salat.preferences.domain.entity

import com.salat.preferences.BuildConfig

private object FloatPrefKey {
    const val UI_SCALE = "UI_SCALE"
    const val QUICK_SPLIT_RATIO = "QUICK_SPLIT_RATIO"
}

sealed class FloatPref(override val key: String, override val default: Float) : AnyPref {
    data object UiScale : FloatPref(FloatPrefKey.UI_SCALE, BuildConfig.UI_SCALE)

    // Share of the first window in the quick split
    data object QuickSplitRatio : FloatPref(FloatPrefKey.QUICK_SPLIT_RATIO, .5f)
}
