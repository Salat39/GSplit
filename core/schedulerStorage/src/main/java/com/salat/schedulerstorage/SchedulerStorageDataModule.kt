package com.salat.schedulerstorage

import com.salat.preferences.domain.DataStoreRepository
import com.salat.schedulerstorage.data.repository.SchedulerStorageRepositoryImpl
import com.salat.schedulerstorage.domain.repository.SchedulerStorageRepository
import com.salat.systemapps.domain.repository.SystemAppsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SchedulerStorageDataModule {

    @Provides
    @Singleton
    fun provideSplitPresetsRepository(
        dataStore: DataStoreRepository,
        systemApps: SystemAppsRepository
    ): SchedulerStorageRepository = SchedulerStorageRepositoryImpl(dataStore, systemApps)
}
