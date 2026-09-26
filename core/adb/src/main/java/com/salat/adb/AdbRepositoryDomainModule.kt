package com.salat.adb

import com.salat.adb.domain.repository.AdbRepository
import com.salat.adb.domain.usecases.AdbConnectionStateUseCase
import com.salat.adb.domain.usecases.ApplyRequiredSystemSettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object AdbRepositoryDomainModule {

    @Provides
    fun provideAdbConnectionStateUseCase(repository: AdbRepository) = AdbConnectionStateUseCase(repository)

    @Provides
    fun provideApplyRequiredSystemSettingsUseCase(repository: AdbRepository) =
        ApplyRequiredSystemSettingsUseCase(repository)
}
