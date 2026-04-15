package com.salat.gsplit.presentation.entity

import android.view.accessibility.AccessibilityWindowInfo

internal data class FreeFormWindow(
    val packageName: String,
    val position: FreeFormPosition,
    val data: AccessibilityWindowInfo
)
