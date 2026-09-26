package com.salat.uikit.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.salat.ui.clickableNoRipple
import com.salat.ui.rememberAvailableScreenSize
import com.salat.ui.rememberIsLandscape
import com.salat.uikit.theme.AppTheme

private const val BASE_DIALOG_WIDTH = 640

@Composable
fun BaseDialog(
    modifier: Modifier = Modifier,
    uiScaleState: Float? = null,
    properties: DialogProperties = DialogProperties(),
    onDismiss: () -> Unit = {},
    maxWidth: Int = BASE_DIALOG_WIDTH,
    shadowElevation: Dp = 0.dp,
    dimAmount: Float? = null,
    content: @Composable () -> Unit
) {
    val isLandscape = rememberIsLandscape()
    val dialogFillerSize = if (isLandscape) .2f else .1f
    val dialogHeightFiller = rememberAvailableScreenSize().height * dialogFillerSize

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = false,
            securePolicy = properties.securePolicy,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        if (dimAmount != null) {
            val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            SideEffect { dialogWindow?.setDimAmount(dimAmount) }
        }

        val density = LocalDensity.current
        val scaledDensity = remember(density, uiScaleState ?: 1f) {
            Density(
                density.density * (uiScaleState ?: 1f),
                density.fontScale * (uiScaleState ?: 1f)
            )
        }

        val isImeVisible = WindowInsets.ime.getBottom(density) > 0
        val topFiller = if (isImeVisible || isLandscape) 4.dp else dialogHeightFiller
        val bottomFiller = if (isImeVisible) 4.dp else dialogHeightFiller

        CompositionLocalProvider(LocalDensity provides scaledDensity) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickableNoRipple(enabled = properties.dismissOnClickOutside) { onDismiss() }
                    .safeDrawingPadding(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = modifier
                        .widthIn(max = maxWidth.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = topFiller, bottom = bottomFiller)
                        .padding(horizontal = 32.dp)
                        .clickableNoRipple {},
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.surfaceBackground,
                    shadowElevation = shadowElevation,
                    content = content
                )
            }
        }
    }
}
