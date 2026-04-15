package com.salat.gsplit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.salat.gsplit.presentation.components.startFreeformHack
import com.salat.navigation.noUiGraph
import com.salat.navigation.routs.NoUiNavGraph
import com.salat.ui.observeLifecycleFlow
import com.salat.uikit.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * PresetLauncherActivity — launch preset by id
 */
@AndroidEntryPoint
class PresetLauncherActivity : ComponentActivity() {
    private val viewModel: PresetLauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.Transparent.toArgb()),
            SystemBarStyle.dark(Color.Transparent.toArgb())
        )
        super.onCreate(savedInstanceState)
        setContent {
            val darkScreen by viewModel.launchDarkScreenState.collectAsStateWithLifecycle()
            val toolbarExtraSize by viewModel.toolbarExtraSizeState.collectAsStateWithLifecycle()

            if (darkScreen) {
                val uiScale by viewModel.uiScaleState.collectAsStateWithLifecycle()
                val density = LocalDensity.current
                val scaledDensity = remember(density, uiScale) {
                    Density(
                        density.density * uiScale,
                        density.fontScale * uiScale
                    )
                }

                val navController = rememberNavController()

                AppTheme(darkTheme = true) {
                    CompositionLocalProvider(
                        LocalDensity provides scaledDensity,
                        LocalLayoutDirection provides LayoutDirection.Ltr
                    ) {
                        NavHost(
                            modifier = Modifier.fillMaxSize(),
                            navController = navController,
                            startDestination = NoUiNavGraph,
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None },
                            popEnterTransition = { EnterTransition.None },
                            popExitTransition = { ExitTransition.None }
                        ) {
                            noUiGraph(toolbarExtraSize = toolbarExtraSize) {
                                doFinish()
                            }
                        }
                    }
                }
            }
        }

        observeLifecycleFlow(viewModel.launchFreedomHackState) {
            startFreeformHack()
        }

        observeLifecycleFlow(viewModel.finishState) {
            doFinish()
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val id: Long = intent.getLongExtra("id", -1L)

        val launchLast = intent.getBooleanExtra("launch_last", false)

        val firstPackage = intent.getStringExtra("first_package") ?: ""
        val secondPackage = intent.getStringExtra("second_package") ?: ""

        if (launchLast) {
            viewModel.launchLastSplit()
        } else if (firstPackage.isNotEmpty() && secondPackage.isNotEmpty()) {
            val firstAutoPlay = intent.getIntExtra("first_auto_play", 0)
            val secondAutoPlay = intent.getIntExtra("second_auto_play", 0)
            val type = intent.getStringExtra("type") ?: ""
            val darkBackground = intent.getIntExtra("dark_background", 0)
            val windowShift = intent.getIntExtra("window_shift", 0)

            viewModel.launchCustomSplit(
                firstPackage = firstPackage,
                firstAutoPlay = firstAutoPlay,
                secondPackage = secondPackage,
                secondAutoPlay = secondAutoPlay,
                type = type,
                darkBackground = darkBackground,
                windowShift = windowShift
            )
        } else if (id != -1L) {
            viewModel.findAndLaunchPresetById(id)
        } else {
            doFinish()
        }

        // Clear the intent to avoid re-processing on resume or relaunch
        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_CLEAR_TASK
        setIntent(null)
    }

    private fun doFinish() {
        try {
            window.setWindowAnimations(0)
            finish()
        } catch (_: Exception) {
        }
    }
}
