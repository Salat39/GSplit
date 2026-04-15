import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64

fun String.asField(): String = "\"$this\""

fun String.toBytes(): ByteArray = this.toByteArray(Charsets.UTF_8)

fun ByteArray.shift(): ByteArray = mapIndexed { index, byte ->
    (byte.toInt() + ((index + 1) % 8)).toByte()
}.toByteArray()

fun ByteArray.flip(): ByteArray = reversedArray()

fun ByteArray.asField(): String = "new byte[]${this.contentToString().replace('[', '{').replace(']', '}')}"

fun String.asBytes(): String =
    "new byte[]${this.toByteArray(Charsets.UTF_8).contentToString().replace('[', '{').replace(']', '}')}"

fun String.asShiftedBytes(): String {
    val bytes = this.toByteArray(Charsets.UTF_8)
    val shiftedBytes = bytes.mapIndexed { index, byte ->
        (byte.toInt() + ((index + 1) % 8)).toByte()
    }.toByteArray()
    return "new byte[]${shiftedBytes.contentToString().replace('[', '{').replace(']', '}')}"
}

fun String.toBase64(): String = try {
    val encode = toByteArray(StandardCharsets.UTF_8)
    Base64.getEncoder().encodeToString(encode)
} catch (_: Exception) {
    ""
}

fun String.fromBase64(): String = try {
    val decode = Base64.getDecoder().decode(this)
    String(decode, StandardCharsets.UTF_8)
} catch (_: Exception) {
    ""
}

fun String.flipEverySecondChar(): String {
    val chars = toCharArray()
    val reversed = CharArray(chars.size)
    val isEvent = chars.size % 2 == 0

    chars.forEachIndexed { i, _ ->
        if (i % 2 == 1) {
            reversed[i] = chars[chars.size - i - (if (isEvent) 0 else 1)]
        } else {
            reversed[i] = chars[i]
        }
    }
    return String(reversed)
}

fun getBuildNumber(): Int = System.getenv()["BUILD_NUMBER"]?.toInt() ?: 0

fun getVersionCode(): Int {
    val major = ProjectConfig.VERSION_MAJOR * 10_000
    val minor = ProjectConfig.VERSION_MINOR * 1_000
    val patch = ProjectConfig.VERSION_PATCH * 100
    val fix = ProjectConfig.VERSION_FIX
    return major + minor + patch + fix + getBuildNumber()
}

fun getVersionName(): String = "${ProjectConfig.VERSION_MAJOR}.${ProjectConfig.VERSION_MINOR}." +
    "${ProjectConfig.VERSION_PATCH}${ProjectConfig.VERSION_POSTFIX}"

fun getSimpleVersionName(): String = "${ProjectConfig.VERSION_MAJOR}.${ProjectConfig.VERSION_MINOR}." +
    "${ProjectConfig.VERSION_PATCH}"

fun getTestProfiles(): String {
    val filePath = System.getProperty("user.dir") + "\\..\\POTestProfiles.txt"
    val file = File(filePath)

    if (!file.exists() || !file.canRead()) {
        println("! Users file not found or wrong format")
        return "new String[]{}"
    }

    val users = file.readLines()
        .filter { it.isNotBlank() && !it.startsWith("#") }

    return users.joinToString(
        prefix = "new String[]{",
        postfix = "}",
        separator = ", ",
        transform = { line ->
            "\"${line}\""
        }
    )
}

fun List<String>.asField(): String = joinToString(
    prefix = "new String[]{",
    postfix = "}",
    separator = ", ",
    transform = { "\"$it\"" }
)
