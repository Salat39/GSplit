package com.salat.launchhistory

import com.salat.launchhistory.data.repository.LaunchHistoryRepositoryImpl
import com.salat.launchhistory.domain.repository.LaunchHistoryRepository
import com.salat.preferences.domain.DataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LaunchHistoryDataModule {

    @Provides
    @Singleton
    fun provideLaunchHistoryRepository(dataStore: DataStoreRepository): LaunchHistoryRepository =
        LaunchHistoryRepositoryImpl(dataStore)
}
