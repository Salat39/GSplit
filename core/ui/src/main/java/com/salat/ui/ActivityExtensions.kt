package com.salat.ui

import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun ComponentActivity.compatEdgeToEdge(statusBar: Color, navigationBar: Color, isDark: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (isDark) {
            this.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(statusBar.toArgb()),
                navigationBarStyle = SystemBarStyle.dark(navigationBar.toArgb())
            )
        } else {
            this.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(statusBar.toArgb(), Color.Transparent.toArgb()),
                navigationBarStyle = SystemBarStyle.light(navigationBar.toArgb(), Color.Transparent.toArgb())
            )
        }
    }
}

@Suppress("DEPRECATION")
fun ComponentActivity.compatAdaptiveEdgeToEdge(
    statusBar: Color,
    navigationBar: Color,
    compatStatusBar: Color = statusBar,
    compatNavigationBar: Color = navigationBar,
    isDark: Boolean = true
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (isDark) {
            this.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(statusBar.toArgb()),
                navigationBarStyle = SystemBarStyle.dark(navigationBar.toArgb())
            )
        } else {
            this.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(statusBar.toArgb(), Color.Transparent.toArgb()),
                navigationBarStyle = SystemBarStyle.light(navigationBar.toArgb(), Color.Transparent.toArgb())
            )
        }
    } else {
        try {
            if (compatStatusBar.alpha == 1f) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window?.statusBarColor = compatStatusBar.toArgb()
            } else {
                window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window?.statusBarColor = Color.Transparent.toArgb()
            }
            if (compatNavigationBar.alpha == 1f) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                window?.navigationBarColor = compatNavigationBar.toArgb()
            } else {
                window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                window?.navigationBarColor = Color.Transparent.toArgb()
            }
        } catch (e: Exception) {
            println(e)
        }
    }
}
