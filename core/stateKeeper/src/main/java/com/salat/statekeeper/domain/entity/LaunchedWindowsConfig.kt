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
    val sessionId: Long,
    // Launch order - the last launched window is on top
    val freeWindowPackages: List<String> = emptyList(),
    // Preset order - the window number in the preset editor is the index plus one
    val freePresetWindowPackages: List<String> = emptyList(),
    val firstWithCaption: Boolean = false,
    val secondWithCaption: Boolean = false,
    val ratio: Float = .5f
)
