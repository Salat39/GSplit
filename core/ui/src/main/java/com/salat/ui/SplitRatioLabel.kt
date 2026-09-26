package com.salat.ui

import kotlin.math.roundToInt

fun splitRatioLabel(ratio: Float): String {
    val firstPercent = (ratio * 100).roundToInt()
    return "$firstPercent : ${100 - firstPercent}"
}
