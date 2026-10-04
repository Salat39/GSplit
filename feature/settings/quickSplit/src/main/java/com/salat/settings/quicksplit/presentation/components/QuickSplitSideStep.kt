package com.salat.settings.quicksplit.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.ui.clickableNoRipple
import com.salat.ui.rememberIsLandscape
import com.salat.uikit.component.AdaptivePaneContent
import com.salat.uikit.component.EmptyPaneContent
import com.salat.uikit.component.ManualRatioHandle
import com.salat.uikit.theme.AppTheme
import kotlin.math.roundToInt

private val ZoneShape = RoundedCornerShape(24.dp)
private val ZonesGap = 8.dp
private const val DIM_ALPHA = .35f
private const val ZONE_ALPHA = .9f
private const val ZONE_PRESSED_LIGHT_ALPHA = .1f

// Quick split step 1 over the open app. Each zone is a full size button, so a driver can hit it easily
// A guard margin around the handle and the close button stops a near tap from a zone selection
@Composable
fun QuickSplitSideStep(
    ratio: Float,
    onRatioChange: (Float) -> Unit,
    onRatioDragEnd: () -> Unit,
    onSelectSide: (insertFirst: Boolean) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    val isLandscape = rememberIsLandscape()
    var showPresets by remember { mutableStateOf(false) }
    var areaSize by remember { mutableStateOf(IntSize.Zero) }
    val gapPx = with(LocalDensity.current) { ZonesGap.toPx() }
    val windowsLength = (if (isLandscape) areaSize.width else areaSize.height) - gapPx
    val ratioPerPx = 1f / windowsLength.coerceAtLeast(1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(DIM_ALPHA))
            .safeDrawingPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .onSizeChanged { areaSize = it }
        ) {
            val zone: @Composable (Boolean, Modifier) -> Unit = { isFirst, zoneModifier ->
                InsertZone(
                    isFirst = isFirst,
                    isLandscape = isLandscape,
                    onClick = { onSelectSide(isFirst) },
                    modifier = zoneModifier
                )
            }
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(ZonesGap)
                ) {
                    zone(true, Modifier.fillMaxHeight().weight(ratio))
                    zone(false, Modifier.fillMaxHeight().weight(1f - ratio))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(ZonesGap)
                ) {
                    zone(true, Modifier.fillMaxWidth().weight(ratio))
                    zone(false, Modifier.fillMaxWidth().weight(1f - ratio))
                }
            }

            Box(
                modifier = Modifier
                    .atSeam(ratio, isLandscape, gapPx)
                    .clickableNoRipple {}
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                if (showPresets) {
                    RatioPresetsCard(
                        ratio = ratio,
                        isLandscape = isLandscape,
                        onSelect = { share ->
                            onRatioChange(share)
                            showPresets = false
                        },
                        onManualClick = { showPresets = false }
                    )
                } else {
                    ManualRatioHandle(
                        isLandscape = isLandscape,
                        ratio = ratio,
                        ratioPerPx = ratioPerPx,
                        matchedPreset = matchedRatioPreset(ratio),
                        onRatioChange = onRatioChange,
                        onDraggingChange = { isDragging -> if (!isDragging) onRatioDragEnd() },
                        onShowPresets = { showPresets = true }
                    )
                }
            }
        }

        // The title does not take the taps. The zone under it gets them
        Text(
            text = stringResource(R.string.quick_split_where),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 30.dp, start = 92.dp, end = 92.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(AppTheme.colors.surfaceBackground)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            style = AppTheme.typography.buttonTitle,
            color = AppTheme.colors.contentPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp)
                .clickableNoRipple {}
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceBackground)
                    .clickable(onClick = onClose, role = Role.Button),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = AppTheme.colors.contentPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Places the content on the seam between the zones and keeps it on the screen near an edge
// The rest of the area stays free for the zones
private fun Modifier.atSeam(ratio: Float, isLandscape: Boolean, gapPx: Float) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    val width = constraints.maxWidth
    val height = constraints.maxHeight
    layout(width, height) {
        val windowsLength = (if (isLandscape) width else height) - gapPx
        val seam = (windowsLength * ratio + gapPx / 2).roundToInt()
        if (isLandscape) {
            val x = (seam - placeable.width / 2).coerceIn(0, (width - placeable.width).coerceAtLeast(0))
            placeable.place(x, (height - placeable.height) / 2)
        } else {
            val y = (seam - placeable.height / 2).coerceIn(0, (height - placeable.height).coerceAtLeast(0))
            placeable.place((width - placeable.width) / 2, y)
        }
    }
}

@Composable
private fun InsertZone(isFirst: Boolean, isLandscape: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val windowColor = if (isFirst) AppTheme.colors.addWindowFirst else AppTheme.colors.addWindowSecond
    val zoneColor = if (isPressed) {
        Color.White.copy(ZONE_PRESSED_LIGHT_ALPHA).compositeOver(windowColor)
    } else windowColor
    val title = stringResource(
        when {
            isFirst && isLandscape -> R.string.quick_split_insert_left
            isFirst -> R.string.quick_split_insert_top
            isLandscape -> R.string.quick_split_insert_right
            else -> R.string.quick_split_insert_bottom
        }
    )
    val subtitle = stringResource(
        when {
            isFirst && isLandscape -> R.string.quick_split_open_app_moves_right
            isFirst -> R.string.quick_split_open_app_moves_down
            isLandscape -> R.string.quick_split_open_app_moves_left
            else -> R.string.quick_split_open_app_moves_up
        }
    )

    Box(
        modifier = modifier
            .clip(ZoneShape)
            .background(zoneColor.copy(ZONE_ALPHA))
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        AdaptivePaneContent(
            regular = { EmptyPaneContent(isCompact = false, title = title, subtitle = subtitle) },
            compact = { EmptyPaneContent(isCompact = true, title = title, subtitle = subtitle) }
        )
    }
}
