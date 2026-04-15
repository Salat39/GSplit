package com.salat.gsplit

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.salat.firebase.domain.repository.FirebaseRepository
import com.salat.gsplit.presentation.logs.ExecTraceTree
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {

    @Inject
    lateinit var firebase: FirebaseRepository

    override fun onCreate() {
        super.onCreate()
        timberInit()
        analyticsInit()
    }

    private fun timberInit() {
        if (BuildConfig.DEBUG) {
            Timber.plant(ExecTraceTree())
        } else {
            // For release builds, consider using a different tree, like Crashlytics
            // Timber.plant(new CrashlyticsTree());
        }
    }

    private fun analyticsInit() {
        firebase.init()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader(this)
            .newBuilder()
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% of the available memory
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .maxSizePercent(0.15) // 15% of the available disk space
                    .directory(cacheDir)
                    .build()
            }
            // .logger(if (BuildConfig.DEBUG) DebugLogger() else null)
            .build()
    }
}
