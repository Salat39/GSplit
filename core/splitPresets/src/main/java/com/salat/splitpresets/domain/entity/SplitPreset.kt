package com.salat.splitpresets.domain.entity

data class SplitPreset(
    val firstApp: AppPreset,
    val type: PresetType,
    val secondApp: AppPreset,
    val autoStart: Boolean,
    val darkBackground: Boolean,
    val bottomWindowShift: Boolean,
    val quickAccess: Boolean,
    val id: Long,
    // FREE type only - firstApp and secondApp then contain the first and the last window app
    val windows: List<FreeWindowPreset> = emptyList(),
    // CUSTOM type only - share of the free screen area for the first window
    val ratio: Float = .5f
)
