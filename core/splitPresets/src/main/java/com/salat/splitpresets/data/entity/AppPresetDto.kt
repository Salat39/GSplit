package com.salat.splitpresets.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class AppPresetDto(
    val title: String,
    val packageName: String,
    val autoPlay: Boolean? = null
)
