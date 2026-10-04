package com.salat.statekeeper.domain.entity

sealed class QuickSplitTarget {
    // The app in full screen gets a second window
    data class FullscreenApp(val packageName: String) : QuickSplitTarget()

    // The open quick split closes and its app returns to full screen
    data object OpenSplit : QuickSplitTarget()
}
