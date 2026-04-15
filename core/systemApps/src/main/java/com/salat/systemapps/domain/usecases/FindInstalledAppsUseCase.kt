package com.salat.systemapps.domain.usecases

import com.salat.systemapps.domain.repository.SystemAppsRepository

class FindInstalledAppsUseCase(private val repository: SystemAppsRepository) {
    suspend fun execute(vararg packageNames: String) = repository.getApps(*packageNames)
}
