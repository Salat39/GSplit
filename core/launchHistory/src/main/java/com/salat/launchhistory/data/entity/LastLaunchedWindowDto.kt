package com.salat.launchhistory.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class LastLaunchedWindowDto(
    val app: LastLaunchedAppDto,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val alwaysOnTop: Boolean = false
)
