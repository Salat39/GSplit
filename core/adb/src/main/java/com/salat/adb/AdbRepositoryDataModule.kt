package com.salat.adb

import com.salat.adb.data.repository.AdbRepositoryImpl
import com.salat.adb.domain.repository.AdbRepository
import com.salat.preferences.domain.DataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdbRepositoryDataModule {

    @Provides
    @Singleton
    fun provideAdbRepository(dataStore: DataStoreRepository): AdbRepository = AdbRepositoryImpl(dataStore)
}
