package com.salat.filedownloader.domain.usecases

import com.salat.filedownloader.domain.repository.FileDownloaderRepository

class GetSettingsFromFileUseCase(private val repository: FileDownloaderRepository) {
    suspend fun execute(uriString: String) = repository.getSettingsFromFile(uriString)
}
