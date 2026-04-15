package com.salat.replacementappsstorage.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class ReplacementAppItemDto(
    val title: String,
    val packageName: String,
    val firstWindow: Boolean,
    val secondWindow: Boolean,
    val autoPlay: Boolean,
    val id: Long
)
