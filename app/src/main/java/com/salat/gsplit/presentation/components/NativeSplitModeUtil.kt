@file:Suppress("unused")

package com.salat.gsplit.presentation.components

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/** The primary container driving the screen to be in split-screen mode. */
private const val WINDOWING_MODE_SPLIT_SCREEN_PRIMARY = 3

/**
 * The containers adjacent to the {@link #WINDOWING_MODE_SPLIT_SCREEN_PRIMARY} container in
 * split-screen mode.
 * NOTE: Containers launched with the windowing mode with APIs like
 * {@link ActivityOptions#setLaunchWindowingMode(int)} will be launched in
 * {@link #WINDOWING_MODE_FULLSCREEN} if the display isn't currently in split-screen windowing
 * mode
 * @see #WINDOWING_MODE_FULLSCREEN_OR_SPLIT_SCREEN_SECONDARY
 */
private const val WINDOWING_MODE_SPLIT_SCREEN_SECONDARY = 4

/**
 * Parameter to {@link android.app.IActivityManager#setTaskWindowingModeSplitScreenPrimary}
 * which specifies the position of the created docked stack at the top half of the screen if
 * in portrait mode or at the left half of the screen if in landscape mode.
 * @hide
 */
private const val SPLIT_SCREEN_CREATE_MODE_TOP_OR_LEFT = 0

/**
 * Parameter to {@link android.app.IActivityManager#setTaskWindowingModeSplitScreenPrimary}
 * which specifies the position of the created docked stack at the bottom half of the screen if
 * in portrait mode or at the right half of the screen if in landscape mode.
 * @hide
 */
private const val SPLIT_SCREEN_CREATE_MODE_BOTTOM_OR_RIGHT = 1

/**
 * The windowing mode the activity should be launched into.
 * @hide
 */
private const val KEY_LAUNCH_WINDOWING_MODE = "android.activity.windowingMode"

/**
 * Where the split-screen-primary stack should be positioned.
 * @hide
 */
private const val KEY_SPLIT_SCREEN_CREATE_MODE = "android:activity.splitScreenCreateMode"

/**
 * Launch both intents in split-screen mode with a delay
 */
private const val SPLIT_LAUNCH_DELAY = 400L

/**
 * Utility class to launch two supported apps in Split-screen mode without
 * using the Accessibility API
 */
class NativeSplitModeUtil {

    @RequiresApi(28)
    fun launchSplitScreenMode(parent: ComponentActivity, split: Pair<Intent, Intent>) {
        val (intentTop, intentBottom) = split

        // Declare the observer variable beforehand
        var observer: DefaultLifecycleObserver? = null
        observer = object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                // Reset the current window mode by launching the home screen
                parent.startActivity(
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    }
                )

                // Launch both intents in split-screen mode with a delay
                Handler(Looper.getMainLooper()).postDelayed({
                    // Configure intents to launch in new tasks
                    intentTop.apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    }
                    intentBottom.apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    }

                    // Prepare options for split-screen mode
                    val options = ActivityOptionsCompat.makeBasic().toBundle()?.apply {
                        putInt(KEY_LAUNCH_WINDOWING_MODE, WINDOWING_MODE_SPLIT_SCREEN_PRIMARY)
                        putInt(KEY_SPLIT_SCREEN_CREATE_MODE, SPLIT_SCREEN_CREATE_MODE_TOP_OR_LEFT)
                    }

                    // Launch activities: first the bottom one, then the top one
                    parent.startActivities(arrayOf(intentBottom, intentTop), options)

                    // Remove the observer after the work is done
                    observer?.let { parent.lifecycle.removeObserver(it) }
                }, SPLIT_LAUNCH_DELAY)
            }
        }

        // Add the observer to the activity's lifecycle
        parent.lifecycle.addObserver(observer)

        // Launch the top activity to reset the current window mode
        parent.startActivity(
            intentTop.apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_TASK_ON_HOME
            }
        )
    }
}
