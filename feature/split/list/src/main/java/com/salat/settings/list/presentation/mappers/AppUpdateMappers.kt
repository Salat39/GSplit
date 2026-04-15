package com.salat.settings.list.presentation.mappers

import com.salat.filedownloader.domain.entity.DownloadState
import com.salat.remoteconfig.domain.entity.AppUpdateInfo
import com.salat.settings.list.presentation.entity.DisplayAppUpdate
import com.salat.settings.list.presentation.entity.UiDownloadState

fun AppUpdateInfo.toDisplay() = DisplayAppUpdate(
    version = version,
    size = size,
    text = text,
    code = code,
    downloadUrl = downloadUrl,
    infoUrl = infoUrl,
    mandatory = mandatory
)

fun DownloadState.toUi(): UiDownloadState = when (this) {
    is DownloadState.Progress -> UiDownloadState.InProgress(percent.coerceIn(0, 100))

    is DownloadState.Success -> UiDownloadState.Success(uri.toString())

    is DownloadState.Error -> UiDownloadState.Error(message)
}
