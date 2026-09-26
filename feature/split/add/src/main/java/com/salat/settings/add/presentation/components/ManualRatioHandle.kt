package com.salat.settings.add.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.ui.rememberPainterResource
import com.salat.ui.splitRatioLabel
import com.salat.uikit.theme.AppTheme

private val handleShape = RoundedCornerShape(percent = 50)

@Composable
internal fun ManualRatioHandle(
    isLandscape: Boolean,
    ratio: Float,
    ratioPerPx: Float,
    matchedPreset: String?,
    onRatioChange: (Float) -> Unit,
    onDraggingChange: (Boolean) -> Unit,
    onShowPresets: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val currentRatio by rememberUpdatedState(ratio)
    val currentRatioPerPx by rememberUpdatedState(ratioPerPx)
    val currentOnRatioChange by rememberUpdatedState(onRatioChange)
    val currentOnDraggingChange by rememberUpdatedState(onDraggingChange)
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(matchedPreset) {
        if (isDragging && matchedPreset != null) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val modifier = Modifier
        .shadow(6.dp, handleShape)
        .clip(handleShape)
        .background(AppTheme.colors.surfaceBackground)
        .pointerInput(isLandscape) {
            awaitEachGesture {
                val down = awaitFirstDown()
                val startRatio = currentRatio
                val slopChange = awaitTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                    ?: return@awaitEachGesture

                isDragging = true
                currentOnDraggingChange(true)
                try {
                    var total = slopChange.position - down.position
                    val axisDistance = { offset: Offset -> if (isLandscape) offset.x else offset.y }
                    currentOnRatioChange(startRatio + axisDistance(total) * currentRatioPerPx)
                    drag(slopChange.id) { change ->
                        total += change.positionChange()
                        change.consume()
                        currentOnRatioChange(startRatio + axisDistance(total) * currentRatioPerPx)
                    }
                } finally {
                    isDragging = false
                    currentOnDraggingChange(false)
                }
            }
        }

    val content: @Composable () -> Unit = {
        Icon(
            painter = rememberPainterResource(R.drawable.ic_drag_grip),
            contentDescription = null,
            tint = AppTheme.colors.contentPrimary.copy(.6f),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = splitRatioLabel(ratio),
            style = AppTheme.typography.radioTitle.copy(fontFeatureSettings = "tnum"),
            color = AppTheme.colors.contentPrimary
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.contentPrimary.copy(.1f))
                .clickable(onClick = onShowPresets),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = rememberPainterResource(R.drawable.ic_presets_grid),
                contentDescription = null,
                tint = AppTheme.colors.contentPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    if (isLandscape) {
        Column(
            modifier = modifier
                .width(IntrinsicSize.Max)
                .padding(start = 6.dp, end = 6.dp, top = 12.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) { content() }
    } else {
        Row(
            modifier = modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) { content() }
    }
}
