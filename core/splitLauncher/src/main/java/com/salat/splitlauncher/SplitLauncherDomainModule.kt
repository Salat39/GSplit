package com.salat.splitlauncher

import com.salat.splitlauncher.domain.repository.SplitLauncherRepository
import com.salat.splitlauncher.domain.usecases.GetDarkBackgroundFlowUseCase
import com.salat.splitlauncher.domain.usecases.GetFreedomHackFlowUseCase
import com.salat.splitlauncher.domain.usecases.GetNativeSplitLaunchTaskFlowUseCase
import com.salat.splitlauncher.domain.usecases.GetSplitStartedFlowUseCase
import com.salat.splitlauncher.domain.usecases.LaunchSplitUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object SplitLauncherDomainModule {

    @Provides
    fun provideLaunchSplitUseCase(repository: SplitLauncherRepository) = LaunchSplitUseCase(repository)

    @Provides
    fun provideGetFreedomHackFlowUseCase(repository: SplitLauncherRepository) = GetFreedomHackFlowUseCase(repository)

    @Provides
    fun provideGetDarkBackgroundFlowUseCase(repository: SplitLauncherRepository) =
        GetDarkBackgroundFlowUseCase(repository)

    @Provides
    fun provideGetSplitStartedFlowUseCase(repository: SplitLauncherRepository) = GetSplitStartedFlowUseCase(repository)

    @Provides
    fun provideGetNativeSplitLaunchTaskFlowUseCase(repository: SplitLauncherRepository) =
        GetNativeSplitLaunchTaskFlowUseCase(repository)
}
