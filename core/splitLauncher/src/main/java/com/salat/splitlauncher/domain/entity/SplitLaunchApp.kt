package com.salat.splitlauncher.domain.entity

data class SplitLaunchApp(
    val title: String,
    val packageName: String,
    val autoPlay: Boolean? = null
)
