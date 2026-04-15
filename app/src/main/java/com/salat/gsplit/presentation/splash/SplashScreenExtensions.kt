package com.salat.gsplit.presentation.splash

import android.animation.ObjectAnimator
import android.os.Build
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import timber.log.Timber

fun SplashScreen.pulseAnimation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setOnExitAnimationListener { screen -> pulseAnimation(screen) }
    }
}

private fun pulseAnimation(screen: SplashScreenViewProvider) = try {
    val startScale = 1f
    val endScale = 1.10f
    val duration = 350L

    val zoomX = ObjectAnimator.ofFloat(
        screen.iconView,
        View.SCALE_X,
        startScale,
        endScale
    )
    zoomX.interpolator = OvershootInterpolator()
    zoomX.duration = duration
    zoomX.doOnEnd { screen.remove() }

    val zoomY = ObjectAnimator.ofFloat(
        screen.iconView,
        View.SCALE_Y,
        startScale,
        endScale
    )
    zoomY.interpolator = OvershootInterpolator()
    zoomY.duration = duration
    zoomY.doOnEnd { screen.remove() }

    zoomX.start()
    zoomY.start()
} catch (e: Exception) {
    Timber.e(e)
    screen.remove()
}
