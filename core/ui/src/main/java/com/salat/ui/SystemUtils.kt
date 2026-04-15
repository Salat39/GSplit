package com.salat.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun ChangeStatusBarIcons(isLightIcons: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current

    SideEffect {
        val window = (context as? Activity)?.window ?: return@SideEffect
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = isLightIcons
    }
}

@Composable
fun ChangeNavigationBarIcons(isLightIcons: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current

    SideEffect {
        val window = (context as? Activity)?.window ?: return@SideEffect
        WindowInsetsControllerCompat(window, view).isAppearanceLightNavigationBars = isLightIcons
    }
}

@Composable
fun DisposableChangeStatusBarIcons(isLightIcons: Boolean, onDispose: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current

    DisposableEffect(isLightIcons) {
        (context as? Activity)?.window?.let { window ->
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = isLightIcons
        }
        onDispose {
            (context as? Activity)?.window?.let { window ->
                WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = onDispose
            }
        }
    }
}

@Composable
fun DisposableChangeNavigationBarIcons(isLightIcons: Boolean, onDispose: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current

    DisposableEffect(isLightIcons) {
        (context as? Activity)?.window?.let { window ->
            WindowInsetsControllerCompat(window, view).isAppearanceLightNavigationBars = isLightIcons
        }
        onDispose {
            (context as? Activity)?.window?.let { window ->
                WindowInsetsControllerCompat(window, view).isAppearanceLightNavigationBars = onDispose
            }
        }
    }
}
