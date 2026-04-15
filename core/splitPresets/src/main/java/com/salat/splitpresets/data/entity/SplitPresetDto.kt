package com.salat.splitpresets.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class SplitPresetDto(
    val firstApp: AppPresetDto,
    val type: PresetTypeDto,
    val secondApp: AppPresetDto,
    val autoStart: Boolean,
    val darkBackground: Boolean,
    val bottomWindowShift: Boolean = false,
    val quickAccess: Boolean = false,
    val id: Long
)
