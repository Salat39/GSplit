package com.salat.gsplit.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.salat.gsplit.presentation.components.NativeSplitModeUtil
import com.salat.gsplit.presentation.components.startFreeformHack
import com.salat.gsplit.presentation.entity.LocalBroadcastEvent
import com.salat.gsplit.presentation.splash.pulseAnimation
import com.salat.gsplit.presentation.util.BootPrefsUtil
import com.salat.navigation.routs.SplitNavGraph
import com.salat.navigation.splitGraph
import com.salat.navigation.transitions.routedEnterTransition
import com.salat.navigation.transitions.routedExitTransition
import com.salat.navigation.transitions.routedPopEnterTransition
import com.salat.navigation.transitions.routedPopExitTransition
import com.salat.stub.presentation.navigateToStub
import com.salat.ui.observeLifecycleFlow
import com.salat.uikit.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

private const val INTENT_TASK_STATE_KEY = "INTENT_HANDLED"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.splashScreenState.value
            }
            pulseAnimation()
        }
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.Transparent.toArgb()),
            SystemBarStyle.dark(Color.Transparent.toArgb())
        )
        writeBootTime()
        super.onCreate(savedInstanceState)
        observeLifecycleFlow(viewModel.launchFreedomHackState) {
            startFreeformHack()
        }
        observeLifecycleFlow(viewModel.minimizeApp) {
            this.moveTaskToBack(true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            observeLifecycleFlow(viewModel.nativeSplitLauncherState) {
                NativeSplitModeUtil().launchSplitScreenMode(this, it)
            }
        }
        setContent {
            val uiScale by viewModel.uiScaleState.collectAsStateWithLifecycle()
            val density = LocalDensity.current
            val scaledDensity = remember(density, uiScale) {
                Density(
                    density.density * uiScale,
                    density.fontScale * uiScale
                )
            }

            val navController = rememberNavController()
            val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()
            val autoRunFiller by viewModel.autoRunFiller.collectAsStateWithLifecycle()

            AppTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(
                    LocalDensity provides scaledDensity,
                    LocalLayoutDirection provides LayoutDirection.Ltr
                ) {
                    if (!autoRunFiller) {
                        InitNavHost(viewModel, navController)
                    }
                }
            }

            // Tracking screen changes
            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow
                    .collect { backStackEntry ->
                        backStackEntry.destination.route?.let { viewModel.onScreenChanged(it) }
                    }
            }
        }

        // Check if intent was already handled to avoid re-processing
        val intentHandled = savedInstanceState?.getBoolean(INTENT_TASK_STATE_KEY, false) ?: false
        if (!intentHandled && intent?.flags?.and(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            handleIntent(intent)
        }

        // Send global app launched event
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(LocalBroadcastEvent.APP_LAUNCHED))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(INTENT_TASK_STATE_KEY, true)
    }

    private fun handleIntent(intent: Intent) {
        // Clear the intent to avoid re-processing on resume or relaunch
        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_CLEAR_TASK
        setIntent(null)
    }

    private fun ComponentActivity.writeBootTime() = lifecycleScope.launch(Dispatchers.IO) {
        try {
            BootPrefsUtil.setCodeExecuted(applicationContext, true)
            BootPrefsUtil.setBootTime(applicationContext)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

@Composable
private fun InitNavHost(viewModel: MainViewModel, navController: NavHostController) {
    // Background that is visible during screens transition animations
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
    )

    val toolbarExtraSize by viewModel.toolbarExtraSpace.collectAsStateWithLifecycle()
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = SplitNavGraph,
        enterTransition = {
            routedEnterTransition(initialState, targetState)
        },
        exitTransition = {
            routedExitTransition(initialState, targetState)
        },
        popEnterTransition = {
            routedPopEnterTransition(initialState, targetState)
        },
        popExitTransition = {
            routedPopExitTransition(initialState, targetState)
        }
    ) {
        splitGraph(
            toolbarExtraSize = toolbarExtraSize,
            navController = navController
        )
    }

    val launchDarkScreen by viewModel.launchDarkScreenState.collectAsStateWithLifecycle()
    LaunchedEffect(launchDarkScreen) {
        launchDarkScreen?.let { minimizeAfterCloseScreen ->
            try {
                navController.navigateToStub(minimizeAfterCloseScreen, toolbarExtraSize)
            } catch (e: Exception) {
                Timber.e(e)
            }

            viewModel.clearLaunchDarkScreenState()
        }
    }
}
