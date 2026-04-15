package com.salat.mediamonitor.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Build
import com.salat.mediamonitor.domain.repository.MediaMonitorRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MediaMonitorRepositoryImpl(private val context: Context) : MediaMonitorRepository {

    @SuppressLint("ObsoleteSdkInt")
    override val mediaStateFlow: Flow<Boolean> = callbackFlow {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For API 26+ we use AudioManager.AudioPlaybackCallback
            // Using a wildcard instead of a specific type to avoid referencing AudioPlaybackConfiguration.
            val callback = object : AudioManager.AudioPlaybackCallback() {
                override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>?) {
                    super.onPlaybackConfigChanged(configs)
                    val isPlaying = configs?.any { config ->
                        config.audioAttributes.usage == AudioAttributes.USAGE_MEDIA
                    } ?: false
                    trySend(isPlaying)
                }
            }
            audioManager.registerAudioPlaybackCallback(callback, null)
            // Send the initial state
            trySend(audioManager.isMusicActive)
            awaitClose {
                audioManager.unregisterAudioPlaybackCallback(callback)
            }
        } else {
            // For older API versions, use polling every 500 ms
            var lastState = audioManager.isMusicActive
            trySend(lastState)
            val pollingJob = launch {
                while (true) {
                    delay(500)
                    val currentState = audioManager.isMusicActive
                    if (currentState != lastState) {
                        trySend(currentState)
                        lastState = currentState
                    }
                }
            }
            awaitClose { pollingJob.cancel() }
        }
    }.distinctUntilChanged()

    override suspend fun isPlaying() = mediaStateFlow.firstOrNull() ?: false
}
