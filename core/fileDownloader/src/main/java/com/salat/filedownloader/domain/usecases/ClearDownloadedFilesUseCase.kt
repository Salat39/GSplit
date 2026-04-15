package com.salat.filedownloader.domain.usecases

import com.salat.filedownloader.domain.repository.FileDownloaderRepository

class ClearDownloadedFilesUseCase(private val repository: FileDownloaderRepository) {
    suspend fun execute() = repository.clear()
}
