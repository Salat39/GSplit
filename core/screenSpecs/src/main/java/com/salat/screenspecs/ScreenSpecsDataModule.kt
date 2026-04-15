package com.salat.screenspecs

import android.content.Context
import com.salat.screenspecs.data.repository.ScreenSpecsRepositoryImpl
import com.salat.screenspecs.domain.repository.ScreenSpecsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScreenSpecsDataModule {

    @Provides
    @Singleton
    fun provideScreenSpecsRepository(@ApplicationContext context: Context): ScreenSpecsRepository =
        ScreenSpecsRepositoryImpl(context)
}
