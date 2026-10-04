package com.salat.preferences.domain.entity

private object PrivateStringSharedPrefKey {
    const val ADB_PRIVATE_KEY = "ADB_PRIVATE_KEY"
    const val ADB_PUBLIC_KEY = "ADB_PUBLIC_KEY"
    const val ADB_KEY_FINGERPRINT = "ADB_KEY_FINGERPRINT"
}

// Values of a separate file. The settings export and the system backup do not include this file
sealed class PrivateStringSharedPref(val key: String) {
    data object AdbPrivateKey : PrivateStringSharedPref(PrivateStringSharedPrefKey.ADB_PRIVATE_KEY)
    data object AdbPublicKey : PrivateStringSharedPref(PrivateStringSharedPrefKey.ADB_PUBLIC_KEY)
    data object AdbKeyFingerprint : PrivateStringSharedPref(PrivateStringSharedPrefKey.ADB_KEY_FINGERPRINT)
}
