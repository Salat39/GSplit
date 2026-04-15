package com.salat.adb

import com.salat.adb.domain.repository.AdbRepository
import com.salat.adb.domain.usecases.AdbConnectionStateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object AdbRepositoryDomainModule {

    @Provides
    fun provideAdbConnectionStateUseCase(repository: AdbRepository) = AdbConnectionStateUseCase(repository)
}
