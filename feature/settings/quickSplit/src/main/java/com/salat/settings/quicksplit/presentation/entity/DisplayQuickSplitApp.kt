package com.salat.settings.quicksplit.presentation.entity

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

@Immutable
data class DisplayQuickSplitApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)
