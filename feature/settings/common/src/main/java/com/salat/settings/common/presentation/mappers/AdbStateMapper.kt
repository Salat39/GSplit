package com.salat.settings.common.presentation.mappers

import com.salat.adb.data.entity.AdbConnectionState
import com.salat.settings.common.presentation.entity.DisplayAdbState

fun AdbConnectionState.toDisplayAdbState(): DisplayAdbState = when (this) {
    AdbConnectionState.Connected -> DisplayAdbState.Connected
    AdbConnectionState.Disconnected -> DisplayAdbState.Disconnected
    AdbConnectionState.Connecting -> DisplayAdbState.Connecting
    is AdbConnectionState.Error -> DisplayAdbState.Error(message = message)
}
