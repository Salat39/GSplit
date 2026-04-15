package com.salat.launchhistory.domain.entity

data class LastLaunchedApp(
    val title: String,
    val packageName: String,
    val autoPlay: Boolean? = null
)
