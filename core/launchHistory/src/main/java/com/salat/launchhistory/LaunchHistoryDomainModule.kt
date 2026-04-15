package com.salat.launchhistory

import com.salat.launchhistory.domain.repository.LaunchHistoryRepository
import com.salat.launchhistory.domain.usecases.GetHistoryFlowUseCase
import com.salat.launchhistory.domain.usecases.GetLastLaunchedSplitUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object LaunchHistoryDomainModule {

    @Provides
    fun provideGetLastLaunchedSplitUseCase(repository: LaunchHistoryRepository) =
        GetLastLaunchedSplitUseCase(repository)

    @Provides
    fun provideGetHistoryFlowUseCase(repository: LaunchHistoryRepository) = GetHistoryFlowUseCase(repository)
}
