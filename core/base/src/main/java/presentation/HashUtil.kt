package presentation

/**
 * Function to generate a unique Long value from a variable number of arguments
 */
fun uniqueLongForArgsByHash(vararg args: Any?): Long {
    var hash = 0L // Initialize the hash value to 0
    for (arg in args) { // Iterate through each argument
        // Calculate the hash code for each argument, convert it to Long, and add it to the hash
        // Multiply the current hash by 31 (a prime number) for better distribution of hash values
        // “31” is a common coefficient in hashing to reduce collisions.
        hash = 31 * hash + (arg?.hashCode()?.toLong() ?: 0)
    }
    return hash // Return the calculated hash value
}

/**
 * Function to generate a unique Long value based on simple types
 */
fun uniqueLongForArgsByClip(vararg args: Any?) = buildString {
    for (arg in args) {
        when (arg) {
            is Boolean -> append(if (arg) 1 else 2)
            is Int -> append(arg + 1)
            is Long -> append(arg + 1)
            is Float -> append((arg + 1f).toInt())
            is Double -> append((arg + 1).toInt())
            is String -> arg.toCharArray().forEach { append(it.code) }
            else -> println("!!! WRONG HASH TYPE")
        }
    }
}.toLong()

fun uniqueIntForArgsByClip(vararg args: Any?) = buildString {
    for (arg in args) {
        when (arg) {
            is Boolean -> append(if (arg) 1 else 2)
            is Int -> append(arg + 1)
            is Long -> append(arg + 1)
            is Float -> append((arg + 1f).toInt())
            is Double -> append((arg + 1).toInt())
            is String -> arg.toCharArray().forEach { append(it.code) }
            else -> println("!!! WRONG HASH TYPE")
        }
    }
}.toInt()
