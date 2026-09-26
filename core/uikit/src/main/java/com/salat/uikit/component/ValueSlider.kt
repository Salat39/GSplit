package com.salat.uikit.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNCHECKED_CAST")
@Composable
fun <T> ValueSlider(
    modifier: Modifier = Modifier,
    value: T,
    valueRange: ClosedRange<T>,
    onValueChange: (T) -> Unit,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    defaultMark: T? = null,
    step: T? = null
) where T : Number, T : Comparable<T> {
    // Colors for rendering (will be used in Canvas as well)
    val colors = SliderDefaults.colors(
        thumbColor = if (enabled) AppTheme.colors.contentAccent else AppTheme.colors.sliderPassive,
        activeTrackColor = if (enabled) AppTheme.colors.contentAccent else AppTheme.colors.sliderPassive,
        activeTickColor = if (enabled) AppTheme.colors.contentAccent else AppTheme.colors.sliderPassive,
        inactiveTickColor = AppTheme.colors.sliderPassive,
        inactiveTrackColor = AppTheme.colors.sliderPassive,
        disabledThumbColor = AppTheme.colors.sliderPassive,
        disabledActiveTrackColor = AppTheme.colors.sliderPassive,
        disabledInactiveTrackColor = AppTheme.colors.sliderPassive
    )
    val thumbColor = if (enabled) AppTheme.colors.contentAccent else AppTheme.colors.sliderPassive

    val minValue = valueRange.start.toFloat()
    val maxValue = valueRange.endInclusive.toFloat()

    val discreteSteps = if (step != null && step.toFloat() > 0) {
        val stepsCalculated = ((maxValue - minValue) / step.toFloat()).toInt() - 1
        if (stepsCalculated < 0) 0 else stepsCalculated
    } else 0

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .align(Alignment.Center)
        ) {
            val sliderWidth = size.width
            val thumbRadius = 10.dp.toPx()
            // Define track area considering padding for thumb
            val trackLeft = thumbRadius
            val trackRight = sliderWidth - thumbRadius
            val trackWidth = trackRight - trackLeft
            // Track thickness and its vertical position
            val trackHeight = 4.dp.toPx()
            val trackY = (size.height - trackHeight) / 2f

            // 1. Draw defaultMark marker if specified
            if (defaultMark != null) {
                val fraction = (defaultMark.toFloat() - minValue) / (maxValue - minValue)
                val xPosition = trackLeft + trackWidth * fraction
                drawLine(
                    color = colors.inactiveTrackColor,
                    start = Offset(xPosition, 0f),
                    end = Offset(xPosition, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 2. Draw passive (base) track with rounded edges
            drawRoundRect(
                color = colors.inactiveTrackColor,
                topLeft = Offset(trackLeft, trackY),
                size = Size(trackWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            // 3. Draw active track
            if (minValue < 0 && maxValue > 0) {
                // If range crosses 0 – calculate zero position
                val zeroFraction = (-minValue) / (maxValue - minValue)
                val zeroX = trackLeft + trackWidth * zeroFraction
                val currentFraction = (value.toFloat() - minValue) / (maxValue - minValue)
                val thumbX = trackLeft + trackWidth * currentFraction
                val (activeStart, activeEnd) = if (value.toFloat() >= 0f) {
                    zeroX to thumbX
                } else {
                    thumbX to zeroX
                }
                val activeWidth = activeEnd - activeStart
                if (activeWidth > 0) {
                    drawRoundRect(
                        color = colors.activeTrackColor,
                        topLeft = Offset(activeStart, trackY),
                        size = Size(activeWidth, trackHeight),
                        cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                    )
                }
            } else {
                // If range doesn't cross 0 – active track from start of track area to thumb
                val currentFraction = (value.toFloat() - minValue) / (maxValue - minValue)
                val thumbX = trackLeft + trackWidth * currentFraction
                val activeWidth = thumbX - trackLeft
                if (activeWidth > 0) {
                    drawRoundRect(
                        color = colors.activeTrackColor,
                        topLeft = Offset(trackLeft, trackY),
                        size = Size(activeWidth, trackHeight),
                        cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                    )
                }
            }
        }

        Slider(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            value = value.toFloat(),
            valueRange = minValue..maxValue,
            onValueChange = { newValue ->
                val snappedValue = if (step != null && step.toFloat() > 0) {
                    val ratio = ((newValue - minValue) / step.toFloat()).roundToInt()
                    (ratio * step.toFloat() + minValue).coerceIn(minValue, maxValue)
                } else newValue

                val convertedValue = when (value) {
                    is Int -> snappedValue.toInt() as T
                    is Double -> snappedValue.toDouble() as T
                    else -> snappedValue as T
                }
                onValueChange(convertedValue)
            },
            enabled = enabled,
            interactionSource = interactionSource,
            steps = discreteSteps,
            thumb = {
                SliderThumb(
                    interactionSource = interactionSource,
                    color = thumbColor,
                    enabled = enabled
                )
            },
            track = { Spacer(Modifier.fillMaxWidth()) }
        )
    }
}

@Composable
private fun SliderThumb(interactionSource: MutableInteractionSource, color: Color, enabled: Boolean) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val elevation = when {
        !enabled -> 0.dp
        isPressed || isDragged -> 6.dp
        else -> 1.dp
    }
    Spacer(
        Modifier
            .size(20.dp)
            .indication(interactionSource, ripple(bounded = false, radius = 20.dp))
            .hoverable(interactionSource)
            .shadow(elevation, CircleShape, clip = false)
            .background(color, CircleShape)
    )
}
