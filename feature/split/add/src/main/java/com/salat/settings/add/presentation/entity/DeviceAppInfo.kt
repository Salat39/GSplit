package com.salat.settings.add.presentation.entity

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

@Immutable
data class DeviceAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isMediaApp: Boolean,
    val autoPlay: Boolean? = null
)
