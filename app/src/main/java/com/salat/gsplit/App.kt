package com.salat.gsplit

import android.app.Application
import android.content.ComponentName
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.salat.adb.data.entity.AdbConnectionState
import com.salat.adb.domain.repository.AdbRepository
import com.salat.firebase.domain.repository.FirebaseRepository
import com.salat.gsplit.presentation.AutoLaunchAccessibilityService
import com.salat.gsplit.presentation.logs.ExecTraceTree
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {

    private companion object {
        const val ACCESSIBILITY_SERVICES_KEY = "enabled_accessibility_services"
        const val ACCESSIBILITY_RESTART_CHECK_DELAY = 2_000L
        const val ACCESSIBILITY_RESTART_TOGGLE_DELAY = 500L
    }

    @Inject
    lateinit var firebase: FirebaseRepository

    @Inject
    lateinit var stateKeeper: StateKeeperRepository

    @Inject
    lateinit var adb: AdbRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        timberInit()
        analyticsInit()
        appScope.initAccessibilityRestartWatchdog()
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

    private fun CoroutineScope.initAccessibilityRestartWatchdog() = launch {
        combine(stateKeeper.accessibilityServiceEnabled, adb.connectionState) { isServiceEnabled, adbState ->
            !isServiceEnabled && adbState is AdbConnectionState.Connected
        }.distinctUntilChanged().collectLatest { needsRestart ->
            if (!needsRestart) return@collectLatest

            while (true) {
                delay(ACCESSIBILITY_RESTART_CHECK_DELAY)
                restartAccessibilityService()
            }
        }
    }

    private suspend fun restartAccessibilityService() {
        val services = readEnabledAccessibilityServices() ?: return
        val ownService = ComponentName(this, AutoLaunchAccessibilityService::class.java)
        val otherServices = services.filter { ComponentName.unflattenFromString(it) != ownService }

        Timber.d("[AS] Restart accessibility service via shell")
        // Remove the own service first. A put of the same list does not bind the service again
        if (otherServices.isEmpty()) {
            adb.execute("settings delete secure $ACCESSIBILITY_SERVICES_KEY")
        } else {
            adb.execute("settings put secure $ACCESSIBILITY_SERVICES_KEY '${otherServices.joinToString(":")}'")
        }
        delay(ACCESSIBILITY_RESTART_TOGGLE_DELAY)

        val enabledServices = (otherServices + ownService.flattenToString()).joinToString(":")
        adb.execute("settings put secure $ACCESSIBILITY_SERVICES_KEY '$enabledServices'")
        adb.execute("settings put secure accessibility_enabled 1")
    }

    private suspend fun readEnabledAccessibilityServices(): List<String>? {
        val current = adb.execute("settings get secure $ACCESSIBILITY_SERVICES_KEY").trim()
        if (current == "null" || current.isEmpty()) return emptyList()
        // The output is an ADB error message, not a component list
        if (current.contains(' ') || !current.contains('/')) return null
        return current.split(':').filter { it.isNotBlank() }
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
