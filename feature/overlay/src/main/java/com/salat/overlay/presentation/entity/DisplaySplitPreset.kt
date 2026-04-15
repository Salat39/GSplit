package com.salat.overlay.presentation.entity

import androidx.compose.runtime.Immutable

@Immutable
data class DisplaySplitPreset(
    val firstApp: DisplayAppPreset,
    val type: DisplayPresetType,
    val secondApp: DisplayAppPreset,
    val autoStart: Boolean,
    val darkBackground: Boolean,
    val bottomWindowShift: Boolean,
    val quickAccess: Boolean,
    val id: Long
)
