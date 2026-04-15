package com.salat.uikit.entity

import androidx.compose.runtime.Immutable

@Immutable
data class SegmentTogglerItem(
    val text: String,
    val subtitle: String? = null
)
