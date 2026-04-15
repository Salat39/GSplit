package com.salat.preferences.domain.entity

import com.salat.preferences.BuildConfig

private object FloatPrefKey {
    const val UI_SCALE = "UI_SCALE"
}

sealed class FloatPref(override val key: String, override val default: Float) : AnyPref {
    data object UiScale : FloatPref(FloatPrefKey.UI_SCALE, BuildConfig.UI_SCALE)
}
