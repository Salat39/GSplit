package com.salat.launchhistory.domain.entity

data class LastLaunchedTask(
    val firstApp: LastLaunchedApp?,
    val type: LastLaunchedType,
    val secondApp: LastLaunchedApp?,
    val autoStart: Boolean,
    val darkBackground: Boolean,
    val bottomWindowShift: Boolean,
    val id: Long,
    val windows: List<LastLaunchedWindow> = emptyList(),
    val ratio: Float = .5f,
    val quickSplitOpenPackage: String = ""
)
