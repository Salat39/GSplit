package com.salat.splitpresets

import com.salat.splitpresets.domain.repository.SplitPresetsRepository
import com.salat.splitpresets.domain.usecases.AddSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.DeleteSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.GetAutoPlayPresetUseCase
import com.salat.splitpresets.domain.usecases.GetPresetByIdUseCase
import com.salat.splitpresets.domain.usecases.GetPresetFreeIdUseCase
import com.salat.splitpresets.domain.usecases.GetPresetsFlowUseCase
import com.salat.splitpresets.domain.usecases.GetPresetsUseCase
import com.salat.splitpresets.domain.usecases.SetAutoStartSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.SetDarkBackgroundSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.SetQuickAccessSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.SetWindowShiftSplitPresetUseCase
import com.salat.splitpresets.domain.usecases.UpdateSplitPresetUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object SplitPresetsDomainModule {

    @Provides
    fun provideGetPresetsFlowUseCase(repository: SplitPresetsRepository) = GetPresetsFlowUseCase(repository)

    @Provides
    fun provideAddSplitPresetUseCase(repository: SplitPresetsRepository) = AddSplitPresetUseCase(repository)

    @Provides
    fun provideUpdateSplitPresetUseCase(repository: SplitPresetsRepository) = UpdateSplitPresetUseCase(repository)

    @Provides
    fun provideGetPresetFreeIdUseCase(repository: SplitPresetsRepository) = GetPresetFreeIdUseCase(repository)

    @Provides
    fun provideDeleteSplitPresetUseCase(repository: SplitPresetsRepository) = DeleteSplitPresetUseCase(repository)

    @Provides
    fun provideSetAutoStartSplitPresetUseCase(repository: SplitPresetsRepository) =
        SetAutoStartSplitPresetUseCase(repository)

    @Provides
    fun provideGetPresetByIdUseCase(repository: SplitPresetsRepository) = GetPresetByIdUseCase(repository)

    @Provides
    fun provideGetPresetsUseCase(repository: SplitPresetsRepository) = GetPresetsUseCase(repository)

    @Provides
    fun provideGetAutoPlayPresetUseCase(repository: SplitPresetsRepository) = GetAutoPlayPresetUseCase(repository)

    @Provides
    fun provideSetDarkBackgroundSplitPresetUseCase(repository: SplitPresetsRepository) =
        SetDarkBackgroundSplitPresetUseCase(repository)

    @Provides
    fun provideSetWindowShiftSplitPresetUseCase(repository: SplitPresetsRepository) =
        SetWindowShiftSplitPresetUseCase(repository)

    @Provides
    fun provideSetQuickAccessSplitPresetUseCase(repository: SplitPresetsRepository) =
        SetQuickAccessSplitPresetUseCase(repository)
}
