package com.salat.systemapps.domain.usecases

import com.salat.systemapps.domain.repository.SystemAppsRepository

class FindAllInstalledAppsUseCase(private val repository: SystemAppsRepository) {
    suspend fun execute() = repository.getAllApps()
}
