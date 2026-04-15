package com.salat.splitpresets.domain.entity

data class SplitPreset(
    val firstApp: AppPreset,
    val type: PresetType,
    val secondApp: AppPreset,
    val autoStart: Boolean,
    val darkBackground: Boolean,
    val bottomWindowShift: Boolean,
    val quickAccess: Boolean,
    val id: Long
)
