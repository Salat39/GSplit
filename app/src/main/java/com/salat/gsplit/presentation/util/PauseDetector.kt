package com.salat.gsplit.presentation.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// Screen events cover a screen-off pause, the clock gap covers a CPU suspend that a firmware reports no other way
class PauseDetector(
    private val context: Context,
    scope: CoroutineScope,
    private val onPauseEnded: (durationMs: Long) -> Unit
) {
    private companion object {
        const val TICK_DELAY = 5_000L
        const val MIN_SUSPEND_GAP = 1_000L
    }

    private val powerManager = context.getSystemService(PowerManager::class.java)
    private var pauseStartedAt: Long? = null
    private var lastSuspendOffset = suspendOffset()

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> beginPause(SystemClock.elapsedRealtime())
                Intent.ACTION_SCREEN_ON -> endPause()
            }
        }
    }

    private val tickJob = scope.launch(Dispatchers.Main) {
        while (isActive) {
            delay(TICK_DELAY)
            tick()
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        ContextCompat.registerReceiver(context, screenReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    fun release() {
        tickJob.cancel()
        runCatching { context.unregisterReceiver(screenReceiver) }
    }

    // uptimeMillis stops while the CPU sleeps, elapsedRealtime does not
    private fun suspendOffset() = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()

    private fun tick() {
        val offset = suspendOffset()
        val suspended = offset - lastSuspendOffset
        lastSuspendOffset = offset
        when {
            suspended >= MIN_SUSPEND_GAP -> beginPause(SystemClock.elapsedRealtime() - suspended)
            powerManager.isInteractive -> endPause()
        }
    }

    private fun beginPause(startedAt: Long) {
        if (pauseStartedAt == null) pauseStartedAt = startedAt
    }

    private fun endPause() {
        val startedAt = pauseStartedAt ?: return
        pauseStartedAt = null
        lastSuspendOffset = suspendOffset()
        onPauseEnded(SystemClock.elapsedRealtime() - startedAt)
    }
}
