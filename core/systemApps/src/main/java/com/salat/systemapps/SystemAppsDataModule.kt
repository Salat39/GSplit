package com.salat.systemapps

import android.content.Context
import com.salat.systemapps.data.repository.SystemAppsRepositoryImpl
import com.salat.systemapps.domain.repository.SystemAppsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SystemAppsDataModule {

    @Provides
    @Singleton
    fun provideSystemAppsRepository(@ApplicationContext context: Context): SystemAppsRepository =
        SystemAppsRepositoryImpl(context)
}
