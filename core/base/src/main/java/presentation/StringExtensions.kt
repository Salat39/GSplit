package presentation

import android.net.Uri
import android.text.Html
import android.text.Spanned
import android.util.Base64
import android.util.Patterns
import java.nio.charset.StandardCharsets
import java.text.BreakIterator

private const val LINE_SEPARATOR = "\n"
private const val HTML_LINE_SEPARATOR = "<br>"

fun String.spannedFromHtml(): Spanned {
    return fromHtml() as Spanned
}

fun String.fromHtml(): CharSequence {
    // Keep original line breakers
    val htmlString = trim().replace(LINE_SEPARATOR, HTML_LINE_SEPARATOR)
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION") Html.fromHtml(htmlString)
    }
}

fun String.stripHtml(): String {
    // Keep original line breakers
    val htmlString = trim().replace(LINE_SEPARATOR, HTML_LINE_SEPARATOR)
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        @Suppress("DEPRECATION") Html.fromHtml(htmlString).toString()
    }
}

fun String.parseUrlParams(): Map<String, String> {
    val params = mutableMapOf<String, String>()
    this.split("?").filter { "&" in it || "=" in it }.forEach {
        it.split("&").filter { sub -> "=" in sub }.forEach { sub ->
            sub.split("=").takeIf { p -> p.size == 2 }?.let { (key, value) -> params[key] = value }
        }
    }
    return params.toMap()
}

fun String.removeUrlParameter(paramName: String): String {
    try {
        // Parse the URL into a Uri object
        val uri = Uri.parse(this)

        // Build a new URI without the paramName parameter
        val newUri = uri.buildUpon().clearQuery()
        // Re-add all parameters except paramName
        uri.queryParameterNames.forEach { param ->
            if (param != paramName) {
                uri.getQueryParameter(param)?.let {
                    newUri.appendQueryParameter(param, it)
                }
            }
        }
        // Convert the Uri back to a string
        return newUri.build().toString()
    } catch (_: Exception) {
        return this
    }
}

fun String.addUrlParameter(name: String, value: String) = try {
    buildString {
        append(this@addUrlParameter)
        append(if (contains("?")) '&' else '?')
        append(name)
        append('=')
        append(value.urlEncode())
    }
} catch (_: Exception) {
    this
}

fun String.toBase64(): String = try {
    val encode = toByteArray(StandardCharsets.UTF_8)
    Base64.encodeToString(encode, Base64.DEFAULT)
} catch (_: Exception) {
    ""
}

fun String.fromBase64(): String = try {
    val decode = Base64.decode(this, Base64.DEFAULT)
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

fun String.isRtlText(): Boolean {
    if (this.isEmpty()) {
        return false
    }
    val trimmed = this.trim()
    if (trimmed.isEmpty()) {
        return false
    }
    return trimmed[0].code in 0x590..0x6ff && trimmed[trimmed.length - 1].code in 0x590..0x6ff
}

fun String?.isEmailValid(): Boolean {
    return this?.let {
        it.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(it).matches()
    } ?: false
}

fun String.isPasswordValid(min: Int = 6, max: Int = 24): Boolean = !isPasswordShort(min) && !isPasswordLong(max)

fun String?.isPasswordShort(min: Int = 6): Boolean = this != null && this.trim().length < min

fun String?.isPasswordLong(max: Int = 24): Boolean = this != null && this.trim().length > max

fun String.urlEncode(): String = if (isEmpty()) this else java.net.URLEncoder.encode(this, "utf-8")

fun String.urlDecode(): String {
    if (isEmpty()) return this
    return try {
        replace(Regex("%(?![0-9a-fA-F]{2})"), "%25")
            .let { java.net.URLDecoder.decode(it, "utf-8") }
    } catch (_: Exception) {
        this
    }
}

fun Spanned.hasSpans(): Boolean = this.getSpans(0, this.length, Any::class.java).isNotEmpty()

fun String.removeUrlVersionParameter(): String {
    val paramIndex = indexOf("?v=")
    val ampIndex = indexOf("&v=")

    return when {
        paramIndex != -1 -> {
            val nextParamIndex = indexOf('&', paramIndex + 1)
            if (nextParamIndex != -1) {
                removeRange(paramIndex, nextParamIndex + 1)
            } else {
                substring(0, paramIndex)
            }
        }

        ampIndex != -1 -> {
            val nextParamIndex = indexOf('&', ampIndex + 1)
            if (nextParamIndex != -1) {
                removeRange(ampIndex, nextParamIndex + 1)
            } else {
                substring(0, ampIndex)
            }
        }

        else -> this
    }
}

fun String.removeLanguageSegmentFromUrl(): String {
    try {
        // Parse the URL using Uri
        val uri = Uri.parse(this)

        // Get the path segments
        val pathSegments = uri.pathSegments

        // Check if the first segment is a language code (2-letter segment)
        val newPathSegments = if (pathSegments.isNotEmpty() && pathSegments[0].length == 2) {
            pathSegments.drop(1) // Drop the first segment
        } else {
            pathSegments
        }

        // Reconstruct the path without the language segment
        val newPath = newPathSegments.joinToString("/", prefix = "/")

        // Build the new Uri with the modified path
        val newUri = uri.buildUpon().path(newPath).build()

        return newUri.toString()
    } catch (_: Exception) {
        return this
    }
}

fun String.withDollar(): String {
    if (isEmpty()) return "0"
    return if (startsWith("-")) {
        "-$${drop(1)}"
    } else {
        "$$this"
    }
}

fun String.formatCurrency(): String {
    if (isEmpty()) return "0"
    return (toFloatOrNull() ?: 0f).toFormattedString()
}

private fun isEmojiCodePoint(codePoint: Int): Boolean {
    return (
        codePoint in 0x1F600..0x1F64F || // Emoticons
            codePoint in 0x1F300..0x1F5FF || // Miscellaneous Symbols and Pictographs
            codePoint in 0x1F680..0x1F6FF || // Transport and Map Symbols
            codePoint in 0x1F700..0x1F77F || // Alchemical Symbols
            codePoint in 0x1F780..0x1F7FF || // Geometric Shapes Extended
            codePoint in 0x1F800..0x1F8FF || // Supplemental Arrows-C
            codePoint in 0x1F900..0x1F9FF || // Supplemental Symbols and Pictographs
            codePoint in 0x1FA00..0x1FA6F || // Chess Symbols
            codePoint in 0x1FA70..0x1FAFF || // Symbols and Pictographs Extended-A
            codePoint in 0x2600..0x26FF || // Miscellaneous Symbols
            codePoint in 0x2700..0x27BF || // Dingbats
            codePoint in 0xFE00..0xFE0F || // Variation Selectors
            codePoint in 0x1F1E6..0x1F1FF || // Regional Indicator Symbols
            codePoint in 0x1F201..0x1F251 || // Enclosed Ideographic Supplement
            codePoint in 0x1F004..0x1F0CF || // Playing Cards
            codePoint in 0x1F018..0x1F270 // Miscellaneous Symbols and Pictographs
        )
}

fun String.containsOnlyEmojis(maxEmojiLength: Int = 3): Int {
    var emojiCount = 0
    var isValid = true

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        val graphemeIterator = BreakIterator.getCharacterInstance()
        graphemeIterator.setText(this)

        var start = graphemeIterator.first()
        var end = graphemeIterator.next()

        while (end != BreakIterator.DONE && isValid) {
            val grapheme = this.substring(start, end)
            val codePoints = grapheme.codePoints().toArray()

            if (codePoints.isEmpty() || !isEmojiCodePoint(codePoints[0])) {
                isValid = false
            } else {
                emojiCount++

                if (maxEmojiLength in 1..<emojiCount) {
                    isValid = false
                }
            }

            start = end
            end = graphemeIterator.next()
        }
    } else {
        var index = 0
        while (index < this.length && isValid) {
            val codePoint = this.codePointAt(index)
            if (!isEmojiCodePoint(codePoint)) {
                isValid = false
            } else {
                emojiCount++
                if (maxEmojiLength in 1..<emojiCount) {
                    isValid = false
                }
            }
            index += Character.charCount(codePoint)
        }
    }

    return if (isValid) emojiCount else 0
}

fun String.generateYouTubeLink() = "https://www.youtube.com/watch?v=$this"

fun String.capitalizeFirstLetter(): String {
    if (this.isEmpty()) return this
    return this[0].uppercaseChar() + this.substring(1).lowercase()
}
