package com.salat.uikit.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.createRippleModifierNode
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.RippleConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.unit.Dp

private val LightThemeHighContrastRippleAlpha = RippleAlpha(
    pressedAlpha = 0.24f,
    focusedAlpha = 0.24f,
    draggedAlpha = 0.16f,
    hoveredAlpha = 0.08f
)

private val LightThemeLowContrastRippleAlpha = RippleAlpha(
    pressedAlpha = 0.12f,
    focusedAlpha = 0.12f,
    draggedAlpha = 0.08f,
    hoveredAlpha = 0.04f
)

private val DarkThemeRippleAlpha = RippleAlpha(
    pressedAlpha = 0.10f,
    focusedAlpha = 0.12f,
    draggedAlpha = 0.08f,
    hoveredAlpha = 0.04f
)

private fun appRippleColor(contentColor: Color, isDark: Boolean) =
    if (isDark && contentColor.luminance() < 0.5f) Color.White else contentColor

private fun appRippleAlpha(contentColor: Color, isDark: Boolean) = when {
    isDark -> DarkThemeRippleAlpha
    contentColor.luminance() > 0.5f -> LightThemeHighContrastRippleAlpha
    else -> LightThemeLowContrastRippleAlpha
}

// Material components get dark content colors from the default light color scheme
internal fun appMaterialRippleConfiguration(colors: AppColors) = RippleConfiguration(
    color = if (colors.isDark) Color.White else Color.Unspecified,
    rippleAlpha = appRippleAlpha(colors.contentPrimary, colors.isDark)
)

internal class AppRipple(private val isDark: Boolean) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        AppRippleNode(interactionSource, isDark)

    override fun equals(other: Any?) = other is AppRipple && other.isDark == isDark

    override fun hashCode() = isDark.hashCode()
}

private class AppRippleNode(
    interactionSource: InteractionSource,
    isDark: Boolean
) : DelegatingNode(), CompositionLocalConsumerModifierNode {
    init {
        delegate(
            createRippleModifierNode(
                interactionSource = interactionSource,
                bounded = true,
                radius = Dp.Unspecified,
                color = { appRippleColor(currentValueOf(LocalContentColor), isDark) },
                rippleAlpha = { appRippleAlpha(currentValueOf(LocalContentColor), isDark) }
            )
        )
    }
}
