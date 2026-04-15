package com.salat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit

val TextUnit.toPxFloat: Float
    @Composable get() {
        val density = LocalDensity.current
        return with(density) { this@toPxFloat.toPx() }
    }

fun TextUnit.toPxFloat(density: Density): Float {
    return with(density) { this@toPxFloat.toPx() }
}
