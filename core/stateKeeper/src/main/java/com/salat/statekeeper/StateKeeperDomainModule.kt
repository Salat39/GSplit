package com.salat.statekeeper

import com.salat.statekeeper.domain.repository.StateKeeperRepository
import com.salat.statekeeper.domain.usecases.CheckAccessibilityServiceEnabledUseCase
import com.salat.statekeeper.domain.usecases.CloseDarkScreenFlowUseCase
import com.salat.statekeeper.domain.usecases.FindQuickSplitTargetUseCase
import com.salat.statekeeper.domain.usecases.GetImportSettingsRequestUseCase
import com.salat.statekeeper.domain.usecases.GetSkipAutoLaunchUseCase
import com.salat.statekeeper.domain.usecases.LaunchLastFromPanelUseCase
import com.salat.statekeeper.domain.usecases.LaunchPresetFromPanelUseCase
import com.salat.statekeeper.domain.usecases.RequestImportSettingsUseCase
import com.salat.statekeeper.domain.usecases.SetLaunchRunningUseCase
import com.salat.statekeeper.domain.usecases.SetPresetPanelShownUseCase
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

    @Provides
    fun provideRequestImportSettingsUseCase(repository: StateKeeperRepository) =
        RequestImportSettingsUseCase(repository)

    @Provides
    fun provideGetImportSettingsRequestUseCase(repository: StateKeeperRepository) =
        GetImportSettingsRequestUseCase(repository)

    @Provides
    fun provideSetPresetPanelShownUseCase(repository: StateKeeperRepository) = SetPresetPanelShownUseCase(repository)

    @Provides
    fun provideLaunchPresetFromPanelUseCase(repository: StateKeeperRepository) =
        LaunchPresetFromPanelUseCase(repository)

    @Provides
    fun provideLaunchLastFromPanelUseCase(repository: StateKeeperRepository) = LaunchLastFromPanelUseCase(repository)

    @Provides
    fun provideSetLaunchRunningUseCase(repository: StateKeeperRepository) = SetLaunchRunningUseCase(repository)

    @Provides
    fun provideFindQuickSplitTargetUseCase(repository: StateKeeperRepository) = FindQuickSplitTargetUseCase(repository)
}
