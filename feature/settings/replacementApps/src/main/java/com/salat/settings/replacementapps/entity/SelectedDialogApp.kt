package com.salat.settings.replacementapps.entity

data class SelectedDialogApp(
    val app: DeviceAppInfo,
    val first: Boolean,
    val second: Boolean,
    val autoPlay: Boolean
)
