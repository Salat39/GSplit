package com.salat.replacementappsstorage

import com.salat.replacementappsstorage.domain.repository.ReplacementAppStorageRepository
import com.salat.replacementappsstorage.domain.useCases.AddReplacementAppItemUseCase
import com.salat.replacementappsstorage.domain.useCases.DeleteReplacementAppItemUseCase
import com.salat.replacementappsstorage.domain.useCases.GetReplacementAppsFlowUseCase
import com.salat.replacementappsstorage.domain.useCases.GetReplacementAppsFreeIdUseCase
import com.salat.replacementappsstorage.domain.useCases.GetReplacementAppsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ReplacementAppStorageDomainModule {

    @Provides
    fun provideAddReplacementAppItemUseCase(repository: ReplacementAppStorageRepository) =
        AddReplacementAppItemUseCase(repository)

    @Provides
    fun provideDeleteReplacementAppItemUseCase(repository: ReplacementAppStorageRepository) =
        DeleteReplacementAppItemUseCase(repository)

    @Provides
    fun provideGetReplacementAppsFlowUseCase(repository: ReplacementAppStorageRepository) =
        GetReplacementAppsFlowUseCase(repository)

    @Provides
    fun provideGetReplacementAppsFreeIdUseCase(repository: ReplacementAppStorageRepository) =
        GetReplacementAppsFreeIdUseCase(repository)

    @Provides
    fun provideGetReplacementAppsUseCase(repository: ReplacementAppStorageRepository) =
        GetReplacementAppsUseCase(repository)
}
