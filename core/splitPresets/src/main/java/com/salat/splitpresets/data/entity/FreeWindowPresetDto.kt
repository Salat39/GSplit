package com.salat.splitpresets.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class FreeWindowPresetDto(
    val app: AppPresetDto,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val alwaysOnTop: Boolean = false
)
