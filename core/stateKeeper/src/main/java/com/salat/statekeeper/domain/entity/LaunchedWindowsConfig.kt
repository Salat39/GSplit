package com.salat.statekeeper.domain.entity

data class LaunchedWindowsConfig(
    val firstAppPackage: String,
    val secondAppPackage: String,
    val autoStart: Boolean,
    val darkBackground: Boolean,
    val bottomWindowShift: Boolean,
    val type: LaunchedSplitType,
    val presetId: Long,
    // launch time + session id
    val sessionId: Long
)
