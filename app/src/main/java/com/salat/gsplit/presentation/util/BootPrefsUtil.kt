@file:Suppress("unused")

package com.salat.gsplit.presentation.util

import android.content.Context
import android.os.SystemClock
import androidx.core.content.edit
import kotlin.math.abs

object BootPrefsUtil {
    private const val PREFS_NAME = "auto_launch_prefs"
    private const val KEY_BOOT_TIME = "boot_time"
    private const val KEY_CODE_EXECUTED = "code_executed"

    /**
     * Calculates the approximate boot time of the device.
     */
    private fun getCurrentBootTime(): Long {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime()
    }

    /**
     * Returns the stored boot time of the device.
     */
    private fun getBootTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_BOOT_TIME, 0L)
    }

    /**
     * Saves the boot time of the device.
     */
    fun setBootTime(context: Context, bootTime: Long = getCurrentBootTime()) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putLong(KEY_BOOT_TIME, bootTime)
        }
    }

    /**
     * Checks whether the auto-launch code has already been executed for the current session.
     */
    fun isCodeExecuted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_CODE_EXECUTED, false)
    }

    /**
     * Sets the flag indicating whether the auto-launch code has been executed for the current session.
     */
    fun setCodeExecuted(context: Context, executed: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_CODE_EXECUTED, executed)
        }
    }

    /**
     * Checks if the current boot session is new,
     * based on whether the difference between the current and stored boot times exceeds the tolerance.
     */
    fun isNewBootSession(context: Context, tolerance: Long): Boolean {
        val currentBootTime = getCurrentBootTime()
        val storedBootTime = getBootTime(context)
        return storedBootTime == 0L || abs(currentBootTime - storedBootTime) > tolerance
    }

    /**
     * Updates the boot session data: if the current session is new (considering tolerance),
     * saves the new boot time and resets the execution flag.
     * Returns true if a new session was detected.
     */
    fun updateBootSession(context: Context, tolerance: Long): Boolean {
        val currentBootTime = getCurrentBootTime()
        val storedBootTime = getBootTime(context)
        return if (storedBootTime == 0L || abs(currentBootTime - storedBootTime) > tolerance) {
            setBootTime(context, currentBootTime)
            setCodeExecuted(context, false)
            true
        } else {
            false
        }
    }
}
