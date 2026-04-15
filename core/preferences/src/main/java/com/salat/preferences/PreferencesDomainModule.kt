package com.salat.preferences

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.PreferencesRepository
import com.salat.preferences.domain.usecases.FlowBoolPrefUseCase
import com.salat.preferences.domain.usecases.FlowBoolPrefsUseCase
import com.salat.preferences.domain.usecases.FlowPrefsUseCase
import com.salat.preferences.domain.usecases.LoadBoolPrefUseCase
import com.salat.preferences.domain.usecases.LoadBoolSharedPrefUseCase
import com.salat.preferences.domain.usecases.LoadFloatPrefUseCase
import com.salat.preferences.domain.usecases.LoadFloatSharedPrefUseCase
import com.salat.preferences.domain.usecases.LoadIntPrefUseCase
import com.salat.preferences.domain.usecases.LoadIntSharedPrefUseCase
import com.salat.preferences.domain.usecases.SaveBoolPrefUseCase
import com.salat.preferences.domain.usecases.SaveBoolSharedPrefUseCase
import com.salat.preferences.domain.usecases.SaveFloatPrefUseCase
import com.salat.preferences.domain.usecases.SaveFloatSharedPrefUseCase
import com.salat.preferences.domain.usecases.SaveIntPrefUseCase
import com.salat.preferences.domain.usecases.SaveIntSharedPrefUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object PreferencesDomainModule {

    @Provides
    fun provideSaveFloatPrefUseCase(preferences: DataStoreRepository) = SaveFloatPrefUseCase(preferences)

    @Provides
    fun provideLoadFloatPrefUseCase(preferences: DataStoreRepository) = LoadFloatPrefUseCase(preferences)

    @Provides
    fun provideLoadBoolPrefUseCase(preferences: DataStoreRepository) = LoadBoolPrefUseCase(preferences)

    @Provides
    fun provideFlowBoolPrefUseCase(preferences: DataStoreRepository) = FlowBoolPrefUseCase(preferences)

    @Provides
    fun provideFlowBoolPrefsUseCase(preferences: DataStoreRepository) = FlowBoolPrefsUseCase(preferences)

    @Provides
    fun provideSaveBoolPrefUseCase(preferences: DataStoreRepository) = SaveBoolPrefUseCase(preferences)

    @Provides
    fun provideLoadBoolSharedPrefUseCase(preferences: PreferencesRepository) = LoadBoolSharedPrefUseCase(preferences)

    @Provides
    fun provideSaveBoolSharedPrefUseCase(preferences: PreferencesRepository) = SaveBoolSharedPrefUseCase(preferences)

    @Provides
    fun provideLoadFloatSharedPrefUseCase(preferences: PreferencesRepository) = LoadFloatSharedPrefUseCase(preferences)

    @Provides
    fun provideSaveFloatSharedPrefUseCase(preferences: PreferencesRepository) = SaveFloatSharedPrefUseCase(preferences)

    @Provides
    fun provideLoadIntSharedPrefUseCase(preferences: PreferencesRepository) = LoadIntSharedPrefUseCase(preferences)

    @Provides
    fun provideSaveIntSharedPrefUseCase(preferences: PreferencesRepository) = SaveIntSharedPrefUseCase(preferences)

    @Provides
    fun provideLoadIntPrefUseCase(preferences: DataStoreRepository) = LoadIntPrefUseCase(preferences)

    @Provides
    fun provideSaveIntPrefUseCase(preferences: DataStoreRepository) = SaveIntPrefUseCase(preferences)

    @Provides
    fun provideFlowPrefsUseCase(preferences: DataStoreRepository) = FlowPrefsUseCase(preferences)
}
