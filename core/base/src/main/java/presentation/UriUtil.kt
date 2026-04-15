package presentation

import android.net.Uri

fun List<Uri>.toUriString(): String {
    return this.joinToString(separator = ",") { it.toString() }
}

fun String.toUriList(): List<Uri> {
    return this.split(",").map { Uri.parse(it) }
}

fun Uri.toUriString(): String {
    return this.toString()
}

fun String.toUri(): Uri {
    return Uri.parse(this)
}
