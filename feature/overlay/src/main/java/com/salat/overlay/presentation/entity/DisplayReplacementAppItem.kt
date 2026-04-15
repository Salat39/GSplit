package com.salat.overlay.presentation.entity

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

@Immutable
data class DisplayReplacementAppItem(
    val title: String,
    val packageName: String,
    val firstWindow: Boolean,
    val secondWindow: Boolean,
    val autoPlay: Boolean,
    val icon: Drawable?,
    val id: Long
)
