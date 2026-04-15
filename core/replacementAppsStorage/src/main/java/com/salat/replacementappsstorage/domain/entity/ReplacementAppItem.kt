package com.salat.replacementappsstorage.domain.entity

data class ReplacementAppItem(
    val title: String,
    val packageName: String,
    val firstWindow: Boolean,
    val secondWindow: Boolean,
    val autoPlay: Boolean,
    val icon: Any?,
    val id: Long
)
