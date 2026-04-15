package com.salat.statekeeper.domain.entity

sealed class AccessibilityServiceEvent {
    data object RememberFocus : AccessibilityServiceEvent()
    data object RestoreFocus : AccessibilityServiceEvent()
    data class FocusWindow(val packageName: String) : AccessibilityServiceEvent()
    data object CloseSplit : AccessibilityServiceEvent()
    data class ReplaceWindow(
        val index: Int, // 0 or 1, first or second window
        val packageName: String,
        val autoPlay: Boolean
    ) : AccessibilityServiceEvent()

    data class ReplacePreset(val presetId: Long) : AccessibilityServiceEvent()

    data class ReplaceSplit(
        val firstPackage: String,
        val firstAutoPlay: Int,
        val secondPackage: String,
        val secondAutoPlay: Int,
        val type: String,
        val darkBackground: Int,
        val windowShift: Int
    ) : AccessibilityServiceEvent()

    data object LaunchLast : AccessibilityServiceEvent()

    data class CloseCurrentWindows(val postAction: suspend () -> Unit) : AccessibilityServiceEvent()
}
