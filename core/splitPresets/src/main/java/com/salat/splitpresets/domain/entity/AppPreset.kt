package com.salat.splitpresets.domain.entity

data class AppPreset(
    val title: String,
    val packageName: String,
    val icon: Any?,
    val autoPlay: Boolean? = null
)
