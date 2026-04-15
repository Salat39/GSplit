package com.salat.settings.scheduler.presentation.entity

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

@Immutable
data class DisplayScheduledItem(
    val title: String,
    val packageName: String,
    val delay: Int,
    val icon: Drawable?,
    val preTask: Boolean,
    val autoPlay: Boolean,
    val id: Long
)
