package com.salat.settings.list.presentation.entity

import androidx.compose.runtime.Immutable

@Immutable
data class DisplayFreeWindow(
    val app: DisplayAppPreset,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val alwaysOnTop: Boolean
)
