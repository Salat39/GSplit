package com.salat.ui

import android.graphics.Region
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable

private const val MASK_SIZE = 100
private const val CORNER_PROBE = 12

// The system icon mask of a round shape leaves the corners empty. A square or a squircle fills them
val systemIconsAreRound: Boolean by lazy {
    runCatching {
        val icon = AdaptiveIconDrawable(ColorDrawable(), ColorDrawable())
        icon.setBounds(0, 0, MASK_SIZE, MASK_SIZE)
        val mask = Region().apply { setPath(icon.iconMask, Region(0, 0, MASK_SIZE, MASK_SIZE)) }
        val far = MASK_SIZE - CORNER_PROBE
        listOf(CORNER_PROBE to CORNER_PROBE, far to CORNER_PROBE, CORNER_PROBE to far, far to far)
            .none { (x, y) -> mask.contains(x, y) }
    }.getOrDefault(false)
}
