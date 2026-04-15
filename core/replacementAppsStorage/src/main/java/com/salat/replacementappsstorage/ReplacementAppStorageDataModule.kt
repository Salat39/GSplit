package com.salat.replacementappsstorage

import com.salat.preferences.domain.DataStoreRepository
import com.salat.replacementappsstorage.data.repository.ReplacementAppStorageRepositoryImpl
import com.salat.replacementappsstorage.domain.repository.ReplacementAppStorageRepository
import com.salat.systemapps.domain.repository.SystemAppsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReplacementAppStorageDataModule {

    @Provides
    @Singleton
    fun provideReplacementAppStorageRepository(
        dataStore: DataStoreRepository,
        systemApps: SystemAppsRepository
    ): ReplacementAppStorageRepository = ReplacementAppStorageRepositoryImpl(dataStore, systemApps)
}
