package com.salat.remoteconfig.domain.usecases

import com.salat.remoteconfig.domain.repository.RemoteConfigRepository

class GetAppUpdateFlowUseCase(repository: RemoteConfigRepository) {
    val flow = repository.appUpdateFlow
}
