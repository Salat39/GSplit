package com.salat.settings.add.presentation.entity

import androidx.compose.runtime.Immutable

// Values are fractions of the editor canvas. A window edge sticks to the nearest line in the snap distance
@Immutable
data class FreeWindowLimits(
    val minWidth: Float,
    val minHeight: Float,
    val xLines: List<Float>,
    val yLines: List<Float>,
    val xSnap: Float,
    val ySnap: Float
)
