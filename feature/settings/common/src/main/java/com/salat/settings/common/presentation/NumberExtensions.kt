package com.salat.settings.common.presentation

import android.content.Context
import com.salat.resources.R
import java.util.Locale

fun Int.toDecimalSecondString(context: Context, decimal: Int = 1) =
    String.format(Locale.US, "%.${decimal}f ${context.getString(R.string.sec)}", this / 1000.0)

fun Int.toSecondString(context: Context) =
    String.format(Locale.US, "%.0f ${context.getString(R.string.sec)}", this / 1000.0)

fun Int.toShiftString() = when {
    this > 0 -> "↓ $this"
    this < 0 -> "↑ ${-this}"
    else -> "0"
}

fun Float.toDoubleString(decimal: Int = 1) = String.format(Locale.US, "%.${decimal}f", this)
