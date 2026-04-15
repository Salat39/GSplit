package com.salat.gsplit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.salat.splitpresets.domain.repository.SplitPresetsRepository
import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

@android.annotation.SuppressLint("ExportedReceiver")
@AndroidEntryPoint
class BackgroundTaskReceiver : BroadcastReceiver() {

    companion object {
        private const val BASE_PATH = "com.salat.gsplit"
    }

    @Inject
    lateinit var stateKeeper: StateKeeperRepository

    @Inject
    lateinit var splitPresets: SplitPresetsRepository

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("[BackgroundTaskReceiver] ${intent.action} received")

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        when (intent.action) {
            "$BASE_PATH.CLOSE_SPLIT" -> scope.launch {
                stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.CloseSplit)
            }

            "$BASE_PATH.REPLACE_WINDOW" -> scope.launch {
                val target = intent.getStringExtra("target") ?: "first"
                val packageName = intent.getStringExtra("package") ?: ""
                val autoPlay = intent.getIntExtra("auto_play", 0)

                if (packageName.isNotEmpty()) {
                    stateKeeper.sendAccessibilityServiceEvent(
                        AccessibilityServiceEvent.ReplaceWindow(
                            index = if (target == "second") 1 else 0,
                            packageName = packageName,
                            autoPlay = autoPlay == 1
                        )
                    )
                }
            }

            "$BASE_PATH.LAUNCH" -> scope.launch {
                val id = intent.getIntExtra("id", 0)

                stateKeeper.sendAccessibilityServiceEvent(
                    AccessibilityServiceEvent.ReplacePreset(
                        presetId = id.toLong()
                    )
                )
            }

            "$BASE_PATH.LAUNCH_LAST" -> scope.launch {
                stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.LaunchLast)
            }

            "$BASE_PATH.LAUNCH_AUTOSTART" -> scope.launch {
                val action = AccessibilityServiceEvent.CloseCurrentWindows {
                    splitPresets.getPresets().find { it.autoStart }?.let { preset ->
                        // Skip next app launch split event
                        stateKeeper.setSkipAutoLaunch(true)
                        runCatching {
                            val noUiLaunch = Intent(context, PresetLauncherActivity::class.java)
                            noUiLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            noUiLaunch.putExtra("id", preset.id)
                            context.startActivity(noUiLaunch)
                        }
                    }
                }
                stateKeeper.sendAccessibilityServiceEvent(action)
            }

            "$BASE_PATH.CUSTOM_LAUNCH" -> scope.launch {
                val firstPackage = intent.getStringExtra("first_package") ?: ""
                val secondPackage = intent.getStringExtra("second_package") ?: ""
                val firstAutoPlay = intent.getIntExtra("first_auto_play", 0)
                val secondAutoPlay = intent.getIntExtra("second_auto_play", 0)
                val type = intent.getStringExtra("type") ?: ""
                val darkBackground = intent.getIntExtra("dark_background", 0)
                val windowShift = intent.getIntExtra("window_shift", 0)

                stateKeeper.sendAccessibilityServiceEvent(
                    AccessibilityServiceEvent.ReplaceSplit(
                        firstPackage = firstPackage,
                        secondPackage = secondPackage,
                        firstAutoPlay = firstAutoPlay,
                        secondAutoPlay = secondAutoPlay,
                        type = type,
                        darkBackground = darkBackground,
                        windowShift = windowShift
                    )
                )
            }

            else -> Unit
        }
    }
}
