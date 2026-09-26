package com.salat.settings.add.presentation.entity

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

// presetRatio is the share of the first window. CUSTOM takes the share from the manual setup
@Immutable
enum class SizeFormat(val id: Int, val presetRatio: Float?) {
    HALF(1, 1 / 2f),
    ONE_TO_THREE(2, 1 / 3f),
    TWO_TO_THREE(3, 2 / 3f),
    THREE_TO_FOUR(4, 3 / 7f),
    THREE_TO_TWO(5, 3 / 5f),
    FOUR_TO_THREE(6, 4 / 7f),
    CUSTOM(8, null)
}

internal fun presetFormatOf(ratio: Float) = SizeFormat.entries.firstOrNull { format ->
    format.presetRatio?.let { it.toPercent() == ratio.toPercent() } == true
}

private fun Float.toPercent() = (this * 100).roundToInt()
