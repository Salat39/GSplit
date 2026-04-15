package com.salat.settings.common.presentation

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.salat.resources.R
import java.util.Locale

fun Int.toDecimalSecondString(context: Context, decimal: Int = 1) =
    String.format(Locale.US, "%.${decimal}f ${context.getString(R.string.sec)}", this / 1000.0)

fun Int.toSecondString(context: Context) =
    String.format(Locale.US, "%.0f ${context.getString(R.string.sec)}", this / 1000.0)

fun Int.toAnnotatedPaddedString(fixedLength: Int = 3, padChar: Char = ' ', color: Color): AnnotatedString {
    val numberStr = if (this < 0) (this * -1).toString() else this.toString()
    val padCount = (fixedLength - numberStr.length).coerceAtLeast(0)

    return buildAnnotatedString {
        if (padCount > 0) {
            withStyle(style = SpanStyle(color = color)) {
                append(padChar.toString().repeat(padCount))
            }
        }
        append(numberStr)
    }
}

fun Float.toDoubleString(decimal: Int = 1) = String.format(Locale.US, "%.${decimal}f", this)
