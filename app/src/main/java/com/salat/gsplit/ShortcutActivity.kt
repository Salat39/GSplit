package com.salat.gsplit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.salat.settings.list.presentation.entity.DisplayPresetType
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.ui.splitRatioLabel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShortcutActivity : ComponentActivity() {
    private val viewModel: ShortcutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setPresetSelectContent(
            viewModel = viewModel,
            lastLaunchedHint = com.salat.resources.R.string.last_launched_shortcut_hint,
            onSelectPreset = ::performLaunchPresetAction,
            onSelectLastLaunched = ::performLastLaunchedAction,
            onSelectPresetPanel = ::performPresetPanelAction
        )
    }

    private fun DisplaySplitPreset.toShortcutTitle(): String {
        if (type == DisplayPresetType.FREE) return toFreeTitle()

        return buildString {
            append("ID")
            append(id)
            append(": [")
            append(firstApp.title)
            append("]")

            if (firstApp.autoPlay == true) {
                append("[▶]")
            }

            append(" - ")
            append(typeTitle())

            if (darkBackground || bottomWindowShift) {
                append(
                    listOfNotNull(
                        if (darkBackground) "D" else null,
                        if (bottomWindowShift) "S" else null
                    ).joinToString(prefix = "[", postfix = "]", separator = ",")
                )
            }

            append(" - ")

            if (secondApp.autoPlay == true) {
                append("[▶]")
            }

            append("[")
            append(secondApp.title)
            append("]")
        }
    }

    private fun DisplaySplitPreset.typeTitle() = when (type) {
        DisplayPresetType.HALF -> "1x1"
        DisplayPresetType.ONE_TO_THREE -> "1x2"
        DisplayPresetType.TWO_TO_THREE -> "2x1"
        DisplayPresetType.THREE_TO_FOUR -> "3x4"
        DisplayPresetType.THREE_TO_TWO -> "3x2"
        DisplayPresetType.FOUR_TO_THREE -> "4x3"
        DisplayPresetType.FREE -> "free"
        DisplayPresetType.CUSTOM -> splitRatioLabel(ratio)
    }

    private fun DisplaySplitPreset.toFreeTitle() = buildString {
        append("ID")
        append(id)
        append(": ")
        append(
            windows.joinToString(separator = ", ") { window ->
                if (window.app.autoPlay == true) "[${window.app.title}][▶]" else "[${window.app.title}]"
            }
        )
        append(" - ")
        append(typeTitle())
        if (darkBackground) append("[D]")
    }

    @Suppress("DEPRECATION")
    private fun performLaunchPresetAction(preset: DisplaySplitPreset) {
        val shortcutIntent = Intent(this, PresetLauncherActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("id", preset.id)
        }
        val iconRes = if (preset.type == DisplayPresetType.FREE) {
            com.salat.resources.R.mipmap.ic_launcher_free
        } else com.salat.resources.R.mipmap.ic_launcher

        val legacyShortcutIntent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, preset.toShortcutTitle())
            putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this@ShortcutActivity, iconRes)
            )
        }
        setResult(RESULT_OK, legacyShortcutIntent)
        finish()
    }

    @Suppress("DEPRECATION")
    private fun performLastLaunchedAction() {
        val shortcutIntent = Intent(this, PresetLauncherActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("launch_last", true)
        }

        val legacyShortcutIntent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(com.salat.resources.R.string.last_launched))
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
    private fun performPresetPanelAction() {
        val shortcutIntent = Intent(this, PresetPanelActivity::class.java).setAction(Intent.ACTION_VIEW)

        val legacyShortcutIntent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(com.salat.resources.R.string.preset_panel))
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
