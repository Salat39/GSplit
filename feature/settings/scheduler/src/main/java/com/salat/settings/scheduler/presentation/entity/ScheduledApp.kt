package com.salat.settings.scheduler.presentation.entity

data class ScheduledApp(
    val app: DeviceAppInfo,
    val time: Int,
    val isPreTask: Boolean,
    val isAutoPlay: Boolean
)
