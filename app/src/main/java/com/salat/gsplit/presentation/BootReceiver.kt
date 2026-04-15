package com.salat.gsplit.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.salat.gsplit.BuildConfig
import com.salat.gsplit.presentation.entity.ExtraIntent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == ExtraIntent.ACTION_QUICKBOOT_POWERON
        ) {
            // Skip boot
            if (BuildConfig.BOOT_VIA_ACCESSIBILITY_SERVICE) return

            // Starting a foreground service to perform necessary operations
            val serviceIntent = Intent(context, WakeUpForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
