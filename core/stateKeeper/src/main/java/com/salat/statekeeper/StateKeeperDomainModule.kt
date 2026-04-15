package com.salat.statekeeper

import com.salat.statekeeper.domain.repository.StateKeeperRepository
import com.salat.statekeeper.domain.usecases.CheckAccessibilityServiceEnabledUseCase
import com.salat.statekeeper.domain.usecases.CloseDarkScreenFlowUseCase
import com.salat.statekeeper.domain.usecases.GetSkipAutoLaunchUseCase
import com.salat.statekeeper.domain.usecases.SetSkipAutoLaunchUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object StateKeeperDomainModule {

    @Provides
    fun provideSetSkipAutoLaunchUseCase(repository: StateKeeperRepository) = SetSkipAutoLaunchUseCase(repository)

    @Provides
    fun provideGetSkipAutoLaunchUseCase(repository: StateKeeperRepository) = GetSkipAutoLaunchUseCase(repository)

    @Provides
    fun provideCloseDarkScreenFlowUseCase(repository: StateKeeperRepository) = CloseDarkScreenFlowUseCase(repository)

    @Provides
    fun provideCheckAccessibilityServiceEnabledUseCase(repository: StateKeeperRepository) =
        CheckAccessibilityServiceEnabledUseCase(repository)
}
