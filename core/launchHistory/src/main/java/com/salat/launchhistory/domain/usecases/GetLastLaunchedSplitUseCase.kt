package com.salat.launchhistory.domain.usecases

import com.salat.launchhistory.domain.repository.LaunchHistoryRepository

class GetLastLaunchedSplitUseCase(private val repository: LaunchHistoryRepository) {
    suspend fun execute() = repository.loadLastConfig()
}
