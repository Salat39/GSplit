package com.salat.uikit.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current

    val typography: AppTypography
        @Composable @ReadOnlyComposable get() = LocalAppTypography.current
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    typography: AppTypography = AppTheme.typography,
    content: @Composable () -> Unit
) {
    val colors = remember(darkTheme) {
        if (darkTheme) DarkAppColors else LightAppColors
    }

    val textSelectionColors = TextSelectionColors(
        handleColor = colors.contentAccent,
        backgroundColor = colors.surfaceLayerAccentPale
    )
    val rippleIndication = rememberRipple()
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
        LocalContentColor provides colors.contentPrimary,
        LocalTextSelectionColors provides textSelectionColors,
        LocalIndication provides rippleIndication,
        LocalRippleTheme provides AppRippleTheme,
        content = content
    )
}

private val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No colors provided")
}
