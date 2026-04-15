package com.salat.settings.list.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.salat.resources.R
import com.salat.settings.list.presentation.entity.DisplayPresetType
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.settings.list.presentation.entity.RenderListType
import com.salat.ui.rememberPainterResource
import com.salat.ui.rememberTimeLockedBoolean
import com.salat.uikit.theme.AppTheme

private const val AUTO_START_BORDER = 2
private const val ICON_SIZE = 28

@Composable
internal fun RenderListItem(
    modifier: Modifier,
    preset: DisplaySplitPreset,
    type: RenderListType,
    onClick: () -> Unit,
    onLongClick: (item: DisplaySplitPreset, offset: Offset) -> Unit
) {
    var rootOffsetX by remember { mutableFloatStateOf(0f) }
    var rootOffsetY by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current
    var clickLock by rememberTimeLockedBoolean(1000L)
    val interactionSource = remember { MutableInteractionSource() }
    val rippleIndication = rememberRipple()
    val borderColor = when (type) {
        RenderListType.PRESET -> AppTheme.colors.autoStart
        RenderListType.HISTORY -> AppTheme.colors.historyBorder
        RenderListType.HISTORY_CONTRAST -> AppTheme.colors.historyAccentBorder
    }
    val showBorder = when (type) {
        RenderListType.PRESET -> preset.autoStart
        RenderListType.HISTORY, RenderListType.HISTORY_CONTRAST -> true
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (showBorder) {
                borderColor
            } else {
                AppTheme.colors.cardItemBackground
            }
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (showBorder) {
                when (type) {
                    RenderListType.PRESET -> Spacer(Modifier.height(AUTO_START_BORDER.dp))

                    RenderListType.HISTORY, RenderListType.HISTORY_CONTRAST -> {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(borderColor),
                            text = stringResource(R.string.last_launched_split),
                            style = AppTheme.typography.dialogSubtitle,
                            color = AppTheme.colors.contentPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (showBorder) {
                            Modifier.padding(horizontal = AUTO_START_BORDER.dp)
                        } else Modifier
                    )
                    .heightIn(min = 64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.cardItemBackground)
                    .onGloballyPositioned { coordinates ->
                        rootOffsetX = coordinates.positionInRoot().x
                        rootOffsetY = coordinates.positionInRoot().y
                    }
                    .indication(interactionSource, rippleIndication)
                    .pointerInput(
                        preset.firstApp.packageName,
                        preset.secondApp.packageName,
                        preset.autoStart,
                        preset.darkBackground,
                        preset.bottomWindowShift,
                        preset.quickAccess
                    ) {
                        detectTapGestures(
                            onPress = { offset ->
                                val press = PressInteraction.Press(offset)
                                interactionSource.emit(press)
                                try {
                                    awaitRelease()
                                } finally {
                                    interactionSource.emit(PressInteraction.Release(press))
                                }
                            },
                            onLongPress = {
                                onLongClick(
                                    preset,
                                    Offset(
                                        x = it.x + rootOffsetX,
                                        y = it.y + rootOffsetY
                                    )
                                )
                            },
                            onTap = {
                                if (!clickLock) {
                                    onClick()
                                }
                                clickLock = true
                            }
                        )
                    }
                    .then(
                        if (showBorder) {
                            Modifier.padding(horizontal = (16 - AUTO_START_BORDER).dp, vertical = 16.dp)
                        } else Modifier.padding(16.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(ICON_SIZE.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    preset.firstApp.icon?.let {
                        AsyncImage(
                            modifier = Modifier
                                .size(ICON_SIZE.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            model = remember(preset.firstApp.packageName) {
                                ImageRequest.Builder(context)
                                    .data(it)
                                    .build()
                            },
                            contentDescription = "firstAppIcon",
                            contentScale = ContentScale.Fit
                        )
                    }

                    if (preset.firstApp.autoPlay == true) {
                        Icon(
                            modifier = Modifier
                                .offset(x = 3.dp, y = 3.dp)
                                .alpha(.9f)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.contentAccent)
                                .padding(3.5.dp),
                            painter = rememberPainterResource(R.drawable.ic_play),
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = preset.firstApp.title,
                        style = AppTheme.typography.cardTitle,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = if (preset.firstApp.autoPlay == true) {
                            AppTheme.colors.contentLightAccent
                        } else AppTheme.colors.contentPrimary
                    )
                    Text(
                        text = preset.firstApp.packageName,
                        style = AppTheme.typography.dialogSubtitle,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = AppTheme.colors.contentPrimary.copy(.5f)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (preset.type) {
                            DisplayPresetType.HALF -> "1x1"
                            DisplayPresetType.ONE_TO_THREE -> "1x2"
                            DisplayPresetType.TWO_TO_THREE -> "2x1"
                            DisplayPresetType.THREE_TO_FOUR -> "3x4"
                            DisplayPresetType.THREE_TO_TWO -> "3x2"
                            DisplayPresetType.FOUR_TO_THREE -> "4x3"
                        },
                        textAlign = TextAlign.Center,
                        style = AppTheme.typography.cardFormatTitle,
                        color = AppTheme.colors.contentPrimary
                    )
                    if (preset.darkBackground || preset.bottomWindowShift || preset.quickAccess) {
                        Spacer(Modifier.height(3.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            if (preset.darkBackground) {
                                Icon(
                                    modifier = Modifier
                                        .size(12.dp),
                                    painter = rememberPainterResource(R.drawable.ic_moon),
                                    contentDescription = null,
                                    tint = AppTheme.colors.contentPrimary
                                )
                            }

                            if (preset.bottomWindowShift) {
                                Icon(
                                    modifier = Modifier
                                        .size(12.5.dp),
                                    painter = rememberPainterResource(R.drawable.ic_lift),
                                    contentDescription = null,
                                    tint = AppTheme.colors.contentPrimary
                                )
                            }

                            if (preset.quickAccess) {
                                Icon(
                                    modifier = Modifier
                                        .size(12.5.dp),
                                    painter = rememberPainterResource(R.drawable.ic_star),
                                    contentDescription = null,
                                    tint = AppTheme.colors.contentPrimary
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = preset.secondApp.title,
                        style = AppTheme.typography.cardTitle,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        color = if (preset.secondApp.autoPlay == true) {
                            AppTheme.colors.contentLightAccent
                        } else AppTheme.colors.contentPrimary
                    )
                    Text(
                        text = preset.secondApp.packageName,
                        style = AppTheme.typography.dialogSubtitle,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = AppTheme.colors.contentPrimary.copy(.5f)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(ICON_SIZE.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    preset.secondApp.icon?.let {
                        AsyncImage(
                            modifier = Modifier
                                .size(ICON_SIZE.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            model = remember(preset.secondApp.packageName) {
                                ImageRequest.Builder(context)
                                    .data(it)
                                    .build()
                            },
                            contentDescription = "secondAppIcon",
                            contentScale = ContentScale.Fit
                        )
                    }

                    if (preset.secondApp.autoPlay == true) {
                        Icon(
                            modifier = Modifier
                                .offset(x = (-3).dp, y = 3.dp)
                                .alpha(.9f)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.contentAccent)
                                .padding(3.5.dp),
                            painter = rememberPainterResource(R.drawable.ic_play),
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                }
            }

            if (showBorder) {
                when (type) {
                    RenderListType.PRESET -> {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(borderColor),
                            text = stringResource(R.string.runs_on_app_startup),
                            style = AppTheme.typography.dialogSubtitle,
                            color = AppTheme.colors.contentPrimary,
                            textAlign = TextAlign.Center
                        )
                    }

                    RenderListType.HISTORY, RenderListType.HISTORY_CONTRAST ->
                        Spacer(Modifier.height(AUTO_START_BORDER.dp))
                }
            }
        }
    }
}
