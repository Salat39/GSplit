package com.salat.schedulerstorage

import com.salat.schedulerstorage.domain.repository.SchedulerStorageRepository
import com.salat.schedulerstorage.domain.useCases.AddScheduledItemUseCase
import com.salat.schedulerstorage.domain.useCases.DeleteScheduledItemUseCase
import com.salat.schedulerstorage.domain.useCases.GetSchedulerFlowUseCase
import com.salat.schedulerstorage.domain.useCases.GetSchedulerFreeIdUseCase
import com.salat.schedulerstorage.domain.useCases.GetSchedulersUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object SchedulerStorageDomainModule {

    @Provides
    fun provideAddScheduledItemUseCase(repository: SchedulerStorageRepository) = AddScheduledItemUseCase(repository)

    @Provides
    fun provideDeleteScheduledItemUseCase(repository: SchedulerStorageRepository) =
        DeleteScheduledItemUseCase(repository)

    @Provides
    fun provideGetSchedulerFlowUseCase(repository: SchedulerStorageRepository) = GetSchedulerFlowUseCase(repository)

    @Provides
    fun provideGetSchedulerFreeIdUseCase(repository: SchedulerStorageRepository) = GetSchedulerFreeIdUseCase(repository)

    @Provides
    fun provideGetSchedulersUseCase(repository: SchedulerStorageRepository) = GetSchedulersUseCase(repository)
}
