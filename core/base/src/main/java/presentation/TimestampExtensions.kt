package presentation

import java.util.Calendar
import java.util.concurrent.TimeUnit

fun Long.getDayOfYearFromTimeStamp(): Int {
    if (this == 0L) {
        return 0
    }
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@getDayOfYearFromTimeStamp * 1000
    }
    return calendar.get(Calendar.DAY_OF_YEAR)
}

fun Long.isCurrentTimeWithinRange(rangeInMinutes: Long): Boolean {
    if (this == 0L) {
        return false
    }
    // Current time in milliseconds
    val now = Calendar.getInstance().apply { clear(Calendar.ZONE_OFFSET) }.timeInMillis

    // Calculate the range in milliseconds
    val rangeInMillis = TimeUnit.MINUTES.toMillis(rangeInMinutes)
    val lowerBound = (this * 1000) - rangeInMillis
    val upperBound = (this * 1000) + rangeInMillis

    // Check if the current time is within the range
    return now in lowerBound..upperBound
}
