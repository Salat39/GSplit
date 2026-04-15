package com.salat.gsplit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.salat.splitpresets.domain.entity.PresetType
import com.salat.splitpresets.domain.entity.SplitPreset
import com.salat.ui.observeLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShortcutActivity : AppCompatActivity() {
    private val viewModel: ShortcutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeLifecycleFlow(viewModel.presetsListState) {
            if (it.isEmpty()) {
                finish()
            } else {
                showOptionDialog(it)
            }
        }
    }

    private fun showOptionDialog(splitPresets: List<SplitPreset>) {
        val dialogContext: Context = ContextThemeWrapper(this, R.style.Theme_GSplit_AppTheme)

        val titles = splitPresets.map {
            buildString {
                append("ID")
                append(it.id)
                append(": [")
                append(it.firstApp.title)
                append("]")

                if (it.firstApp.autoPlay == true) {
                    append("[▶]")
                }

                append(" - ")
                append(it.type.toDisplay())

                if (it.darkBackground || it.bottomWindowShift) {
                    append(
                        listOfNotNull(
                            if (it.darkBackground) "D" else null,
                            if (it.bottomWindowShift) "S" else null
                        ).joinToString(prefix = "[", postfix = "]", separator = ",")
                    )
                }

                append(" - ")

                if (it.secondApp.autoPlay == true) {
                    append("[▶]")
                }

                append("[")
                append(it.secondApp.title)
                append("]")
            }
        }

        val lastLaunchTitle = getString(com.salat.resources.R.string.last_launched)
        AlertDialog.Builder(dialogContext)
            .setTitle(getString(com.salat.resources.R.string.select_an_preset))
            .setItems((titles + lastLaunchTitle).toTypedArray()) { _, which ->
                if (which == splitPresets.size) {
                    performLastLaunchedAction(lastLaunchTitle)
                } else {
                    performLaunchPresetAction(splitPresets[which].id, titles[which])
                }
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }

    private fun PresetType.toDisplay() = when (this) {
        PresetType.HALF -> "1x1"
        PresetType.ONE_TO_THREE -> "1x2"
        PresetType.TWO_TO_THREE -> "2x1"
        PresetType.THREE_TO_FOUR -> "3x4"
        PresetType.THREE_TO_TWO -> "3x2"
        PresetType.FOUR_TO_THREE -> "4x3"
    }

    @Suppress("DEPRECATION")
    private fun performLaunchPresetAction(id: Long, title: String) {
        val shortcutIntent = Intent(this, PresetLauncherActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("id", id)
        }

        val legacyShortcutIntent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, title) // TODO
            putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(
                    this@ShortcutActivity,
                    com.salat.resources.R.mipmap.ic_launcher
                )
            )
        }
        setResult(RESULT_OK, legacyShortcutIntent)
        finish()
    }

    @Suppress("DEPRECATION")
    private fun performLastLaunchedAction(title: String) {
        val shortcutIntent = Intent(this, PresetLauncherActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("launch_last", true)
        }

        val legacyShortcutIntent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, title) // TODO
            putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(
                    this@ShortcutActivity,
                    com.salat.resources.R.mipmap.ic_launcher
                )
            )
        }
        setResult(RESULT_OK, legacyShortcutIntent)
        finish()
    }
}
