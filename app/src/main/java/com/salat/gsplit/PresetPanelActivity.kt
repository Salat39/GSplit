package com.salat.gsplit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PresetPanelActivity : ComponentActivity() {
    private val viewModel: ShortcutViewModel by viewModels()
    private val panelViewModel: PresetPanelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setPresetSelectContent(
            viewModel = viewModel,
            lastLaunchedHint = com.salat.resources.R.string.last_launched_panel_hint,
            onSelectPreset = { preset ->
                if (panelViewModel.launchPresetViaService(preset.id)) {
                    finish()
                } else {
                    launchSplit { putExtra("id", preset.id) }
                }
            },
            onSelectLastLaunched = {
                if (panelViewModel.launchLastViaService()) {
                    finish()
                } else {
                    launchSplit { putExtra("launch_last", true) }
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        panelViewModel.setPanelShown(true)
    }

    override fun onStop() {
        panelViewModel.setPanelShown(false)
        super.onStop()
    }

    // A second launch from the shortcut or the TOGGLE_PRESET_PANEL broadcast closes the open panel
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        finish()
    }

    private fun launchSplit(extras: Intent.() -> Unit) {
        startActivity(
            Intent(this, PresetLauncherActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .apply(extras)
        )
        finish()
    }
}
