package com.salat.statekeeper.domain.entity

sealed class SplitLauncherEvent {
    data class LaunchWindow(
        val index: Int, // 0 or 1, first or second window
        val type: LaunchedSplitType,
        val packageName: String,
        val autoPlay: Boolean
    ) : SplitLauncherEvent()
}
