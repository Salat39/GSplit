package com.salat.filedownloader.domain.repository

import com.salat.filedownloader.domain.entity.DownloadState
import kotlinx.coroutines.flow.Flow

interface FileDownloaderRepository {
    suspend fun download(url: String, fileName: String, userAgent: String): Flow<DownloadState>

    suspend fun clear(): Int
}
