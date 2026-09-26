package com.salat.launchhistory.domain.entity

data class LastLaunchedWindow(
    val app: LastLaunchedApp,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val alwaysOnTop: Boolean
)
