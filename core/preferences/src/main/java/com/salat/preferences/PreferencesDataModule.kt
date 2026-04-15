package com.salat.preferences

import android.content.Context
import com.salat.preferences.data.DataStoreRepositoryImpl
import com.salat.preferences.data.PreferencesRepositoryImpl
import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesDataModule {

    @Provides
    @Singleton
    fun providePreferencesRepository(@ApplicationContext context: Context): PreferencesRepository =
        PreferencesRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideDataStoreRepository(@ApplicationContext context: Context): DataStoreRepository =
        DataStoreRepositoryImpl(context)
}
