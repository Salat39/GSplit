package com.salat.gsplit

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import com.salat.gsplit.presentation.WakeUpForegroundService
import com.salat.gsplit.presentation.util.BootPrefsUtil
import timber.log.Timber

class BootContentProvider : ContentProvider() {

    companion object {
        // Period during which we consider the device has just booted (e.g., 5 minutes)
        private const val BOOT_THRESHOLD = 5 * 60 * 1000L

        // Acceptable margin of error when comparing boot times (40 sec)
        private const val TOLERANCE = 40 * 1000L
    }

    // Local flag to prevent repeated execution within the same service instance
    private var hasExecutedAfterBoot = false

    override fun onCreate(): Boolean {
        // Boot via AccessibilityService for car
        if (BuildConfig.BOOT_VIA_ACCESSIBILITY_SERVICE) checkAndHandleBootSession()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        throw UnsupportedOperationException("Not allowed")
    }

    override fun getType(uri: Uri): String? {
        throw UnsupportedOperationException("Not allowed")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Not allowed")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Not allowed")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Not allowed")
    }

    /**
     * Checks if the boot session is new (considering TOLERANCE) and performs an action
     * if the device has recently booted.
     */
    private fun checkAndHandleBootSession() {
        context?.apply {
            if (BootPrefsUtil.updateBootSession(this, TOLERANCE)) {
                Timber.d("[CP] New boot detected")
            } else {
                Timber.d("[CP] Same boot session")
            }

            if (SystemClock.elapsedRealtime() < BOOT_THRESHOLD) {
                if (!BootPrefsUtil.isCodeExecuted(this) && !hasExecutedAfterBoot) {
                    launchAction()
                    BootPrefsUtil.setCodeExecuted(this, true)
                    hasExecutedAfterBoot = true
                }
            } else {
                Timber.d("[CP] Boot threshold exceeded. No action will be taken.")
            }
        }
    }

    /**
     * Launches the foreground service. Wraps the launch in a try-catch block for reliable error logging.
     */
    private fun launchAction() {
        context?.apply {
            try {
                val serviceIntent = Intent(this, WakeUpForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                Timber.d("[CP] Foreground service started successfully.")
            } catch (ex: Exception) {
                Timber.e(ex, "[CP] Failed to start foreground service")
            }
        }
    }
}
