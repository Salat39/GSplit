package com.salat.overlay.presentation.entity

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

@Immutable
data class DisplayAppPreset(
    val title: String,
    val packageName: String,
    val icon: Drawable?,
    val autoPlay: Boolean? = null
)
