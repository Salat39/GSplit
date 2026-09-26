package com.salat.splitlauncher.domain.entity

// Bounds are fractions of the free screen area between the status bar and the navigation bar
data class SplitLaunchWindow(
    val app: SplitLaunchApp,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val alwaysOnTop: Boolean
)
