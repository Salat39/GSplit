package com.salat.schedulerstorage.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class ScheduledItemDto(
    val title: String,
    val packageName: String,
    val delay: Int,
    val preTask: Boolean = false,
    val autoPlay: Boolean = false,
    val id: Long
)
