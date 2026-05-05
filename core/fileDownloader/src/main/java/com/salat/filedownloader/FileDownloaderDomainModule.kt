package com.salat.filedownloader

import com.salat.filedownloader.domain.repository.FileDownloaderRepository
import com.salat.filedownloader.domain.usecases.ClearDownloadedFilesUseCase
import com.salat.filedownloader.domain.usecases.DownloadFileUseCase
import com.salat.filedownloader.domain.usecases.GetSettingsFromFileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object FileDownloaderDomainModule {

    @Provides
    fun provideDownloadFileUseCase(repository: FileDownloaderRepository) = DownloadFileUseCase(repository)

    @Provides
    fun provideClearDownloadedFilesUseCase(repository: FileDownloaderRepository) =
        ClearDownloadedFilesUseCase(repository)

    @Provides
    fun provideGetSettingsFromFileUseCase(repository: FileDownloaderRepository) = GetSettingsFromFileUseCase(repository)
}
