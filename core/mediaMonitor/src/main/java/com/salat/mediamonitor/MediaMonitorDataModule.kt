package com.salat.mediamonitor

import android.content.Context
import com.salat.mediamonitor.data.repository.MediaMonitorRepositoryImpl
import com.salat.mediamonitor.domain.repository.MediaMonitorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaMonitorDataModule {

    @Provides
    @Singleton
    fun provideMediaMonitorRepository(@ApplicationContext context: Context): MediaMonitorRepository =
        MediaMonitorRepositoryImpl(context)
}
