package com.salat.adb.data.entity

data class AdbRecentTaskInfo(
    val taskId: Int,
    val packageName: String,
    val visible: Boolean,
    val visibleRequested: Boolean,
    val topResumed: Boolean,
    val activityState: String?,
    val nowVisible: Boolean?,
    val lastVisibleTime: String?,
    val baseDir: String?,
    val dataDir: String?
)
