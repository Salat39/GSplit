package com.salat.gsplit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salat.settings.quicksplit.presentation.components.QuickSplitAppStep
import com.salat.settings.quicksplit.presentation.components.QuickSplitSideStep
import com.salat.ui.enableEdgeToEdgeKeepCutoutMode
import com.salat.ui.observeLifecycleFlow
import com.salat.uikit.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import presentation.toast

// Inserts a second window next to the app that is open in full screen
@AndroidEntryPoint
class QuickSplitActivity : ComponentActivity() {
    private val viewModel: QuickSplitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeKeepCutoutMode(
            SystemBarStyle.dark(Color.Transparent.toArgb()),
            SystemBarStyle.dark(Color.Transparent.toArgb())
        )
        super.onCreate(savedInstanceState)

        // The own window hides the open app from the accessibility window list. The window shows after the app is known
        setVisible(false)
        observeLifecycleFlow(viewModel.state.map { it.step != null }.distinctUntilChanged().filter { it }) {
            setVisible(true)
        }

        observeLifecycleFlow(viewModel.events) { event ->
            when (event) {
                is QuickSplitViewModel.Event.Launch -> launchSplit(event)
                is QuickSplitViewModel.Event.Close -> {
                    event.message?.let { toast(getString(it)) }
                    doFinish()
                }
            }
        }

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                when (state.step) {
                    QuickSplitViewModel.Step.SIDE -> AppTheme(darkTheme = true) {
                        // The step draws in the own window, so it scales by the app setting as the app screens do
                        val density = LocalDensity.current
                        val scaledDensity = remember(density, state.uiScale) {
                            Density(density.density * state.uiScale, density.fontScale * state.uiScale)
                        }
                        CompositionLocalProvider(LocalDensity provides scaledDensity) {
                            QuickSplitSideStep(
                                ratio = state.ratio,
                                onRatioChange = viewModel::setRatio,
                                onRatioDragEnd = viewModel::roundRatio,
                                onSelectSide = viewModel::selectSide,
                                onClose = viewModel::close
                            )
                        }
                    }

                    QuickSplitViewModel.Step.APP -> AppTheme(darkTheme = state.darkTheme) {
                        QuickSplitAppStep(
                            setApps = state.setApps,
                            allApps = state.allApps,
                            showAllApps = state.showAllApps,
                            ratio = state.ratio,
                            insertFirst = state.insertFirst,
                            uiScale = state.uiScale,
                            onBack = viewModel::backToSide,
                            onShowAllApps = viewModel::showAllApps,
                            onSelect = viewModel::selectApp,
                            onCancel = viewModel::close
                        )
                    }

                    null -> Unit
                }
            }
        }
    }

    // A second launch from the shortcut closes the open steps of quick split
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        doFinish()
    }

    private fun launchSplit(event: QuickSplitViewModel.Event.Launch) {
        startActivity(
            Intent(this, PresetLauncherActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .putExtra(EXTRA_OPEN_PACKAGE, event.openPackage)
                .putExtra("first_package", event.firstPackage)
                .putExtra("second_package", event.secondPackage)
                .putExtra(EXTRA_RATIO, event.ratio)
                .putExtra("first_caption", if (event.firstCaption) 1 else 0)
                .putExtra("second_caption", if (event.secondCaption) 1 else 0)
                .putExtra("dark_background", if (event.darkBackground) 1 else 0)
        )
        doFinish()
    }

    private fun doFinish() {
        window.setWindowAnimations(0)
        finish()
    }

    companion object {
        const val EXTRA_OPEN_PACKAGE = "quick_split_open_package"
        const val EXTRA_RATIO = "ratio"
    }
}
