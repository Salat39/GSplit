package com.salat.overlay.presentation

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("ObsoleteSdkInt")
suspend fun startOverlay(context: Context) {
    // Check if service is already running
    if (isServiceRunning(context, OverlayService::class.java)) {
        return // Service is already running, do nothing
    }

    // Check for overlay permission (starting from Android 6.0)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
        // If permission is not granted, redirect the user to settings
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri())
        withContext(Dispatchers.Main) {
            context.startActivity(intent)
        }
    } else {
        // If permission is granted, start the service
        val serviceIntent = Intent(context, OverlayService::class.java)
        withContext(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}

@SuppressLint("ObsoleteSdkInt")
suspend fun stopOverlay(context: Context) {
    // Check if service is running
    if (!isServiceRunning(context, OverlayService::class.java)) {
        return // Service is not running, do nothing
    }

    // Stop the service
    val serviceIntent = Intent(context, OverlayService::class.java)
    withContext(Dispatchers.Main) {
        context.stopService(serviceIntent)
    }
}

// Helper function to check if service is running
private fun <T> isServiceRunning(context: Context, serviceClass: Class<T>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    @Suppress("DEPRECATION")
    val runningServices = manager.getRunningServices(Integer.MAX_VALUE)
    return runningServices.any { it.service.className == serviceClass.name }
}
