package com.salat.gsplit

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import timber.log.Timber

/**
 * MultiWindowHeatingActivity — invisible activity used to initialize freeform mode.
 * It creates a window of minimal size and terminates immediately.
 */
class MultiWindowHeatingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set window size to 1x1 pixel so that it remains unobtrusive
        window.setLayout(1, 1)
        // Disable interaction with the window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        // Additional freeform mode initialization can be performed here, if necessary

        // Terminate the activity immediately after creation
        Timber.d("Freedom Hack activated")
        finish()
    }
}
