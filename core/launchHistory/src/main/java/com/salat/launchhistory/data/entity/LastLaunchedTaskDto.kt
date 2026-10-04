package com.salat.launchhistory.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class LastLaunchedTaskDto(
    val firstApp: LastLaunchedAppDto?,
    val type: LastLaunchedTypeDto,
    val secondApp: LastLaunchedAppDto?,
    val autoStart: Boolean,
    val darkBackground: Boolean,
    val bottomWindowShift: Boolean,
    val id: Long,
    val windows: List<LastLaunchedWindowDto> = emptyList(),
    val ratio: Float = .5f,
    val quickSplitOpenPackage: String = ""
)
