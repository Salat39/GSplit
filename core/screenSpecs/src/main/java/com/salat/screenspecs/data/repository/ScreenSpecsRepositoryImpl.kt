package com.salat.screenspecs.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.Surface
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.salat.screenspecs.data.entity.SpecRotation
import com.salat.screenspecs.domain.repository.ScreenSpecsRepository
import timber.log.Timber

class ScreenSpecsRepositoryImpl(private val context: Context) : ScreenSpecsRepository {

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    override fun getStatusBarHeight(legacyMode: Boolean): Int {
        if (legacyMode) {
            val statusBarResourceId =
                context.resources.getIdentifier("status_bar_height", "dimen", "android")
            val statusBarHeight = context.resources.getDimensionPixelSize(statusBarResourceId)
            return statusBarHeight
        }

        // Calculate status bar height
        var statusBarHeight = 0
        val statusBarResId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (statusBarResId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(statusBarResId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val windowManager = context.getSystemService(WindowManager::class.java)
                val windowMetrics = windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())
                statusBarHeight = insets.top
            } catch (e: Exception) {
                Timber.e(e, "Error obtaining status bar insets")
            }
        }
        return statusBarHeight
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    override fun getNavBarHeight(legacyMode: Boolean): Int {
        if (legacyMode) {
            val navBarResourceId =
                context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            val navBarHeight =
                if (navBarResourceId > 0) context.resources.getDimensionPixelSize(navBarResourceId) else 0
            return navBarHeight
        }

        // Calculate navigation bar height
        var navBarHeight = 0
        val navBarResId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (navBarResId > 0) {
            navBarHeight = context.resources.getDimensionPixelSize(navBarResId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val windowManager = context.getSystemService(WindowManager::class.java)
                val windowMetrics = windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
                // Usually navigation bar is at the bottom in portrait mode
                navBarHeight = insets.bottom
            } catch (e: Exception) {
                Timber.e(e, "Error obtaining navigation bar insets")
            }
        }
        return navBarHeight
    }

    @Suppress("DEPRECATION")
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    override fun getFreeScreenHeight(legacyMode: Boolean): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !legacyMode) {
            try {
                val windowManager = context.getSystemService(WindowManager::class.java)
                val windowMetrics = windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                val bounds = windowMetrics.bounds

                // If the device is in landscape mode, the navigation bar is likely on the side,
                // meaning insets.bottom will be 0. In any case, the free height is:
                bounds.height() - insets.top - insets.bottom
            } catch (e: Exception) {
                Timber.e(e, "Error while retrieving free screen height")
                0
            }
        } else {
            // For legacy devices, get the real screen dimensions
            val metrics = DisplayMetrics()
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay
                .getRealMetrics(metrics)
            val totalHeight = metrics.heightPixels

            // Determine device orientation: if width exceeds height, it's in landscape mode
            val isLandscape = metrics.widthPixels > metrics.heightPixels

            val statusBarHeight = getStatusBarHeight(legacyMode = true)
            // In portrait mode, subtract the navigation bar height as well,
            // but in landscape mode, assume the navigation bar is on the side and doesn't affect height.
            val navBarHeight = if (!isLandscape) getNavBarHeight(legacyMode = true) else 0

            totalHeight - statusBarHeight - navBarHeight
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    override fun getFreeScreenWidth(legacyMode: Boolean): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !legacyMode) {
            try {
                val windowManager = context.getSystemService(WindowManager::class.java)
                val windowMetrics = windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                val bounds = windowMetrics.bounds
                // Subtract system insets from the left and right.
                bounds.width() - insets.left - insets.right
            } catch (e: Exception) {
                Timber.e(e, "Error while retrieving free screen width")
                0
            }
        } else {
            val metrics = DisplayMetrics()
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay
                .getRealMetrics(metrics)
            val totalWidth = metrics.widthPixels

            // Determine orientation: if width exceeds height, the device is in landscape mode
            val isLandscape = metrics.widthPixels > metrics.heightPixels

            // In portrait mode, the navigation bar is typically at the bottom and doesn't affect width,
            // but in landscape mode, if the navigation bar is on the left or right, its width is subtracted.
            val navBarWidth = if (isLandscape) {
                val navBarResourceId = context.resources
                    .getIdentifier("navigation_bar_width", "dimen", "android")
                if (navBarResourceId > 0) context.resources.getDimensionPixelSize(navBarResourceId) else 0
            } else 0

            totalWidth - navBarWidth
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    override fun getScreenHorizontalInsets(legacyMode: Boolean): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !legacyMode) {
            try {
                val windowManager = context.getSystemService(WindowManager::class.java)
                val windowMetrics = windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                // Return left and right insets obtained through WindowInsets.
                Pair(insets.left, insets.right)
            } catch (e: Exception) {
                Timber.e(e, "Error while retrieving screen horizontal insets")
                Pair(0, 0)
            }
        } else {
            // Legacy mode: get the real screen dimensions.
            val metrics = DisplayMetrics()
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay
                .getRealMetrics(metrics)
            val isLandscape = metrics.widthPixels > metrics.heightPixels

            if (!isLandscape) {
                // In portrait mode, system bars are typically located at the top and bottom,
                // so horizontal insets are 0.
                Pair(0, 0)
            } else {
                // In landscape mode, the navigation bar may be positioned on the left or right.
                val navBarResId = context.resources
                    .getIdentifier("navigation_bar_width", "dimen", "android")
                val navBarWidth = if (navBarResId > 0) {
                    context.resources.getDimensionPixelSize(navBarResId)
                } else 0

                // Determine layout direction: for RTL, the inset is on the left side,
                // for LTR – on the right.
                val isRtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
                if (isRtl) {
                    Pair(navBarWidth, 0)
                } else {
                    Pair(0, navBarWidth)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun Context.getScreenRotation(): SpecRotation {
        val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        return when (display.rotation) {
            Surface.ROTATION_0 -> SpecRotation.SYSTEM_BAR_IN_TOP
            Surface.ROTATION_90 -> SpecRotation.SYSTEM_BAR_IN_RIGHT
            Surface.ROTATION_180 -> SpecRotation.SYSTEM_BAR_IN_BOTTOM
            Surface.ROTATION_270 -> SpecRotation.SYSTEM_BAR_IN_LEFT
            else -> SpecRotation.SYSTEM_BAR_IN_TOP
        }
    }
}
