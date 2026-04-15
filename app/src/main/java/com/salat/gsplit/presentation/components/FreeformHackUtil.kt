package com.salat.gsplit.presentation.components

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.provider.Settings.canDrawOverlays
import com.salat.gsplit.MultiWindowHeatingActivity
import timber.log.Timber

/**
 * Launches the freeform hack, i.e., an invisible activity to initialize freeform mode.
 *
 * @param context The context used to launch the activity.
 * @param checkMultiWindow If true, passes the "check_multiwindow" parameter
 * (if it's necessary to check if multiwindow is running).
 */
internal fun Context.startFreeformHack(checkMultiWindow: Boolean = false) {
    // Create an intent for MultiWindowHeatingActivity
    val freeformHackIntent = Intent(this, MultiWindowHeatingActivity::class.java).apply {
        // Flags to launch in a separate task without animation and with adjacent launch
        addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                Intent.FLAG_ACTIVITY_NO_ANIMATION
        )
        if (checkMultiWindow) {
            putExtra("check_multiwindow", true)
        }
    }

    // Check permission to draw over other apps, as it is required for freeform mode
    if (canDrawOverlays(this)) {
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        startActivity(freeformHackIntent, options.toBundle())
    }
    Timber.d("FreeformHack applied")
}
