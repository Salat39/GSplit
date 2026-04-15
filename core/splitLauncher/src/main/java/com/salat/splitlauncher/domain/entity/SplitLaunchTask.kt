package com.salat.splitlauncher.domain.entity

data class SplitLaunchTask(
    val firstApp: SplitLaunchApp?,
    val type: SplitLaunchType,
    val secondApp: SplitLaunchApp?,
    val autoStart: Boolean,
    val darkBackground: Boolean,
    val bottomWindowShift: Boolean,
    val id: Long
)
