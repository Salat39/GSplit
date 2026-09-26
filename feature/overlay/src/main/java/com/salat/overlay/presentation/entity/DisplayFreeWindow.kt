package com.salat.overlay.presentation.entity

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect

@Immutable
data class DisplayFreeWindow(
    val app: DisplayAppPreset,
    val bounds: Rect
)
