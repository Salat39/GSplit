package com.salat.firebase

import com.salat.firebase.domain.repository.FirebaseRepository
import com.salat.firebase.domain.useCases.LogLaunchTypeUseCase
import com.salat.firebase.domain.useCases.LogScreenUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object FirebaseDomainModule {

    @Provides
    fun provideLogScreenUseCase(repository: FirebaseRepository) = LogScreenUseCase(repository)

    @Provides
    fun provideLogLaunchTypeUseCase(repository: FirebaseRepository) = LogLaunchTypeUseCase(repository)
}
