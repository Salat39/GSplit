package com.salat.settings.adb.mappers

import com.salat.adb.data.entity.AdbConnectionState
import com.salat.settings.adb.entity.DisplayAdbState

fun AdbConnectionState.toDisplayAdbState(): DisplayAdbState = when (this) {
    AdbConnectionState.Connected -> DisplayAdbState.Connected
    AdbConnectionState.Disconnected -> DisplayAdbState.Disconnected
    AdbConnectionState.Connecting -> DisplayAdbState.Connecting
    is AdbConnectionState.Error -> DisplayAdbState.Error(message = message)
}
