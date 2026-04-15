package com.salat.statekeeper

import com.salat.statekeeper.data.repository.StateKeeperRepositoryImpl
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StateKeeperDataModule {

    @Provides
    @Singleton
    fun provideStateKeeperRepository(): StateKeeperRepository = StateKeeperRepositoryImpl()
}
