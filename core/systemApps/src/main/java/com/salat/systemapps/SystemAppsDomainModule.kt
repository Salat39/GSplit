package com.salat.systemapps

import com.salat.systemapps.domain.repository.SystemAppsRepository
import com.salat.systemapps.domain.usecases.FindAllInstalledAppsUseCase
import com.salat.systemapps.domain.usecases.FindInstalledAppsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object SystemAppsDomainModule {

    @Provides
    fun provideFindAllInstalledAppsUseCase(repository: SystemAppsRepository) = FindAllInstalledAppsUseCase(repository)

    @Provides
    fun provideFindInstalledAppsUseCase(repository: SystemAppsRepository) = FindInstalledAppsUseCase(repository)
}
