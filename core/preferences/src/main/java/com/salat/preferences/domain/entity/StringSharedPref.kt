package com.salat.preferences.domain.entity

private object StringSharedPrefKey {
    const val DUMMY = "dummy"
}

sealed class StringSharedPref(val key: String, val default: String) {
    data object Dummy : StringSharedPref(StringSharedPrefKey.DUMMY, "")
}
