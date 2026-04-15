package com.salat.settings.list.presentation.entity

import androidx.compose.runtime.Immutable

@Immutable
data class DisplayAppUpdate(
    val version: String,
    val size: String,
    val text: String,
    val code: Int,
    val downloadUrl: String,
    val infoUrl: String,
    val mandatory: Boolean
)
