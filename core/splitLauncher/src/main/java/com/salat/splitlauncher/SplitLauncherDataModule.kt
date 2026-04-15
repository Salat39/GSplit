package com.salat.splitlauncher

import android.content.Context
import com.salat.adb.domain.repository.AdbRepository
import com.salat.firebase.domain.repository.FirebaseRepository
import com.salat.launchhistory.domain.repository.LaunchHistoryRepository
import com.salat.mediamonitor.domain.repository.MediaMonitorRepository
import com.salat.preferences.domain.DataStoreRepository
import com.salat.screenspecs.domain.repository.ScreenSpecsRepository
import com.salat.splitlauncher.data.repository.SplitLauncherRepositoryImpl
import com.salat.splitlauncher.domain.repository.SplitLauncherRepository
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SplitLauncherDataModule {

    @Provides
    @Singleton
    fun provideSplitLauncherRepository(
        @ApplicationContext context: Context,
        stateKeeper: StateKeeperRepository,
        dataStore: DataStoreRepository,
        screenSpecs: ScreenSpecsRepository,
        launchHistory: LaunchHistoryRepository,
        mediaMonitor: MediaMonitorRepository,
        firebase: FirebaseRepository,
        adbHelper: AdbRepository
    ): SplitLauncherRepository = SplitLauncherRepositoryImpl(
        context,
        stateKeeper,
        dataStore,
        screenSpecs,
        launchHistory,
        mediaMonitor,
        firebase,
        adbHelper
    )
}
