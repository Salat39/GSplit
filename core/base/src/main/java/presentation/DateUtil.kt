package presentation

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Extension function to convert a date string into a timestamp (milliseconds since epoch).
 *
 * This function uses the specified date format pattern to parse the string into a Date object
 * and then retrieves the time in milliseconds since the Unix epoch (January 1, 1970, 00:00:00 GMT).
 *
 * @receiver String - The date string to be converted.
 * @param format String - The format pattern of the date string (default is "yyyy-MM-dd HH:mm:ss").
 *                        The format should match the pattern of the input date string.
 *                        Uses SimpleDateFormat pattern rules.
 * @return Long - The timestamp in milliseconds since epoch.
 *                Returns 0 if the date string is invalid or cannot be parsed.
 *
 * Example usage:
 * val dateString = "2023-12-30 13:15:00"
 * val timestamp = dateString.convertToTimestamp() // Default format "yyyy-MM-dd HH:mm:ss"
 */
fun String.convertToTimestamp(format: String = "yyyy-MM-dd HH:mm:ss", shift: Long = 0L): Long {
    try {
        // Define the date format pattern
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())

        // Parse the date string to Date
        val date = dateFormat.parse(this)

        // Convert to timestamp (milliseconds since epoch)
        if (shift != 0L) {
            val millisecondsToAdd = shift * 60 * 1000L
            return (date?.time ?: 0L) + millisecondsToAdd
        } else {
            return date?.time ?: 0L
        }
    } catch (_: Exception) {
        return 0L
    }
}
