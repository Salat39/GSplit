package domain

fun Float.normalizeToRange(
    originalMin: Float,
    originalMax: Float,
    targetMin: Float = 0f,
    targetMax: Float = 1f,
    coerceIn: Boolean = false
): Float {
    return if (this == originalMin || (coerceIn && this < originalMin)) {
        targetMin
    } else if (this == originalMax || (coerceIn && this > originalMax)) {
        targetMax
    } else {
        targetMin + ((this - originalMin) / (originalMax - originalMin)) * (targetMax - targetMin)
    }
}
