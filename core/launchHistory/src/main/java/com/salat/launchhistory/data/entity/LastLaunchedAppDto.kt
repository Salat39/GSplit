package com.salat.launchhistory.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class LastLaunchedAppDto(
    val title: String,
    val packageName: String,
    val autoPlay: Boolean? = null
)
