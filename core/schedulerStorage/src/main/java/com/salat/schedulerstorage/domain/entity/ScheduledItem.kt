package com.salat.schedulerstorage.domain.entity

data class ScheduledItem(
    val title: String,
    val packageName: String,
    val delay: Int,
    val icon: Any?,
    val preTask: Boolean = false,
    val autoPlay: Boolean = false,
    val id: Long
)
