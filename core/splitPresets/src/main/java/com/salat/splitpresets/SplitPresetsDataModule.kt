package com.salat.splitpresets

import com.salat.preferences.domain.DataStoreRepository
import com.salat.splitpresets.data.repository.SplitPresetsRepositoryImpl
import com.salat.splitpresets.domain.repository.SplitPresetsRepository
import com.salat.systemapps.domain.repository.SystemAppsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SplitPresetsDataModule {

    @Provides
    @Singleton
    fun provideSplitPresetsRepository(
        dataStore: DataStoreRepository,
        systemApps: SystemAppsRepository
    ): SplitPresetsRepository = SplitPresetsRepositoryImpl(dataStore, systemApps)
}
