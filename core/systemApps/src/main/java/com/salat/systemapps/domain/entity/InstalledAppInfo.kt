package com.salat.systemapps.domain.entity

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Any?,
    val isMedia: Boolean
)
