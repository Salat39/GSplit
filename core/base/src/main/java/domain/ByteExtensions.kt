package domain

fun ByteArray.asString(): String = toString(Charsets.UTF_8)

fun ByteArray.shift(): ByteArray = mapIndexed { index, byte ->
    (byte.toInt() - ((index + 1) % 8)).toByte()
}.toByteArray()

fun ByteArray.flip(): ByteArray = reversedArray()

fun ByteArray.toStringWithShifting(): String {
    if (isEmpty()) return ""
    val compile = mapIndexed { index, byte ->
        (byte.toInt() - ((index + 1) % 8)).toByte()
    }.toByteArray()
    return compile.toString(Charsets.UTF_8)
}
