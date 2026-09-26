package com.salat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
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

// The app UI scale applies to text two times. This is the second factor. It is 1 at UI scale 1
val appTextScale: Float
    @Composable get() = LocalDensity.current.fontScale / LocalConfiguration.current.fontScale
