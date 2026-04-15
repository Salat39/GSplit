package presentation

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun Int.toMetricPrefixes(): String = when {
    this >= 1_000_000 -> "${(this / 1_000_000.0).roundTo(1)}M"
    this >= 1000 -> "${(this / 1000.0).roundTo(1)}K"
    else -> this.toString()
}

fun Long.toFormattedString(): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val decimalFormat = DecimalFormat("#,###.##", symbols)
    return decimalFormat.format(this)
}

fun Int.toFormattedString(): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val decimalFormat = DecimalFormat("#,###.##", symbols)
    return decimalFormat.format(this)
}

fun Float.toFormattedString(): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val decimalFormat = DecimalFormat("#,###.##", symbols)
    return decimalFormat.format(this)
}

fun Double.roundTo(decimals: Int): String = "%.${decimals}f".format(Locale.US, this).removeSuffix(".0")

val Long.toIntOrNull: Int?
    get() = run {
        return if (this < Int.MIN_VALUE || this > Int.MAX_VALUE) {
            null
        } else {
            this.toInt()
        }
    }

fun Float.safeDivide(other: Float, default: Float = 1f) = if (other != .0f) this / other else default
