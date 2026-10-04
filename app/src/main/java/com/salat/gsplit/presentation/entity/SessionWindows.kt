package com.salat.gsplit.presentation.entity

// One window scan of the launched session
internal data class SessionWindows(
    val top: FreeFormWindow?,
    val bottom: FreeFormWindow?,
    val free: List<FreeFormWindow>,
    // Own windows of GSplit hide the session windows but the session stays open
    val isCovered: Boolean,
    // The main window of the session is in full screen over the other windows
    val isMainWindowFullScreen: Boolean = false
)
