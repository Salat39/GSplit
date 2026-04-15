package com.salat.firebase.domain.entity

data class FirebasePresetData(
    val firstPackage: String,
    val secondPackage: String,
    val type: String,
    val source: String,
    val autoStart: Boolean,
    val darkBackground: Boolean,
    val bottomWindowShift: Boolean,
    val softKillApp: Boolean,
    val minimizeByStart: Boolean,
    val minimizeByAutostart: Boolean,
    val ymCompatPlay: Boolean
)
