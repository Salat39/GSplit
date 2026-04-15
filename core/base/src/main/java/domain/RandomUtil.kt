package domain

import kotlin.random.Random

fun randomTitleString(min: Int = 4, max: Int = 10): String {
    val allowedChars = ('a'..'z')
    val size = (min..max).random()
    return (1..size)
        .map { allowedChars.random() }
        .joinToString("")
        .replaceFirstChar(Char::titlecase)
}

fun randomString(min: Int = 4, max: Int = 10): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    val size = (min..max).random()
    return (1..size)
        .map { allowedChars.random() }
        .joinToString("")
}

fun String.withRandomSpaces(from: Int = 4, to: Int = 10): String = buildString {
    var currentIndex = 0
    while (currentIndex < this@withRandomSpaces.length) {
        val step = (from..to).random()
        val endIndex = (currentIndex + step).coerceAtMost(this@withRandomSpaces.length)
        append(this@withRandomSpaces.substring(currentIndex, endIndex))
        if (endIndex < this@withRandomSpaces.length) append(" ")
        currentIndex = endIndex
    }
}

fun getRandomChance(percent: Int = 50): Boolean = (0..100).random() <= percent

fun randomRoll(start: Int = 0, end: Int = 100): Int = (start..end).random()

fun randomRoll(start: Float = 0f, end: Float = 1f): Float = start + (end - start) * Random.nextFloat()
