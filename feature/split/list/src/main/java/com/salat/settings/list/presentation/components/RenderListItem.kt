package com.salat.settings.list.presentation.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.salat.resources.R
import com.salat.settings.list.presentation.entity.DisplayAppPreset
import com.salat.settings.list.presentation.entity.DisplayPresetType
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.settings.list.presentation.entity.RenderListType
import com.salat.ui.rememberIsLandscape
import com.salat.ui.rememberPainterResource
import com.salat.ui.rememberTimeLockedBoolean
import com.salat.ui.splitRatioLabel
import com.salat.uikit.component.RatioGlyph
import com.salat.uikit.theme.AppTheme
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(20.dp)
private val CardHorizontalPadding = 16.dp
private val CardVerticalPadding = 14.dp
private val ItemGap = 5.dp
private val FrameWidth = 2.dp
private val RegularIconSize = 40.dp
private val CompactIconSize = 32.dp
private val CompactRowWidth = 480.dp
private val IconTextGap = 12.dp
private val RatioColumnPadding = 12.dp
private val LiftElevation = 12.dp
private val LiftBorderWidth = 1.5.dp
private val NudgeAmplitude = 4.dp
private const val NUDGE_DURATION_MILLIS = 450

private const val CAPTION_GLYPH_GAP_EM = .36f

private data class CardFrame(
    val color: Color,
    @StringRes val titleRes: Int,
    val titleAtTop: Boolean
)

@Composable
internal fun RenderListItem(
    modifier: Modifier,
    preset: DisplaySplitPreset,
    type: RenderListType,
    showWindowShift: Boolean = true,
    showWindowType: Boolean = false,
    dragHandle: Modifier? = null,
    lifted: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (item: DisplaySplitPreset, offset: Offset) -> Unit
) {
    var rootOffsetX by remember { mutableFloatStateOf(0f) }
    var rootOffsetY by remember { mutableFloatStateOf(0f) }

    var clickLock by rememberTimeLockedBoolean(1000L)
    val interactionSource = remember { MutableInteractionSource() }
    val rippleIndication = LocalIndication.current
    val frame = cardFrame(type, preset.autoStart)
    val reorderMode = dragHandle != null
    val scope = rememberCoroutineScope()
    val handleNudge = remember { Animatable(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CardHorizontalPadding, vertical = ItemGap)
            .then(
                if (lifted) {
                    Modifier
                        .shadow(LiftElevation, CardShape)
                        .border(LiftBorderWidth, AppTheme.colors.contentAccent, CardShape)
                } else Modifier
            )
            .then(frame?.let { Modifier.clip(CardShape).background(it.color) } ?: Modifier)
    ) {
        frame?.let { CardFrameEdge(frame = it, isTop = true) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (frame != null) Modifier.padding(horizontal = FrameWidth) else Modifier)
                .heightIn(min = 64.dp)
                .clip(CardShape)
                .background(AppTheme.colors.surfaceSettingsLayer1)
                .onGloballyPositioned { coordinates ->
                    rootOffsetX = coordinates.positionInRoot().x
                    rootOffsetY = coordinates.positionInRoot().y
                }
                .indication(interactionSource, rippleIndication)
                .pointerInput(preset, reorderMode) {
                    if (reorderMode) {
                        detectTapGestures(onTap = { scope.launch { handleNudge.nudge() } })
                    } else {
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
                                onLongClick(preset, Offset(x = it.x + rootOffsetX, y = it.y + rootOffsetY))
                            },
                            onTap = {
                                if (!clickLock) {
                                    onClick()
                                }
                                clickLock = true
                            }
                        )
                    }
                }
                .padding(
                    horizontal = if (frame != null) CardHorizontalPadding - FrameWidth else CardHorizontalPadding,
                    vertical = CardVerticalPadding
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (preset.type == DisplayPresetType.FREE) {
                FreePresetContent(preset = preset, showWindowType = showWindowType, modifier = Modifier.weight(1f))
            } else {
                SplitPresetContent(preset = preset, showWindowShift = showWindowShift, showWindowType = showWindowType)
            }

            dragHandle?.let { DragHandle(modifier = it, nudge = handleNudge, lifted = lifted) }
        }

        frame?.let { CardFrameEdge(frame = it, isTop = false) }
    }
}

@Composable
private fun DragHandle(modifier: Modifier, nudge: Animatable<Float, *>, lifted: Boolean) = Box(
    modifier = modifier
        .padding(start = 8.dp)
        .size(width = 40.dp, height = 48.dp)
        .graphicsLayer { translationX = nudgeShift(nudge.value) * NudgeAmplitude.toPx() },
    contentAlignment = Alignment.Center
) {
    Icon(
        painter = rememberPainterResource(R.drawable.ic_drag_grip),
        contentDescription = stringResource(R.string.reorder_presets),
        tint = AppTheme.colors.contentPrimary.copy(if (lifted) 1f else .55f),
        modifier = Modifier.size(24.dp)
    )
}

private suspend fun Animatable<Float, *>.nudge() {
    snapTo(0f)
    animateTo(1f, tween(NUDGE_DURATION_MILLIS))
    snapTo(0f)
}

// Decaying wave for a short side shake
private fun nudgeShift(progress: Float) = sin(progress * 3 * PI.toFloat()) * (1 - progress)

@Composable
private fun cardFrame(type: RenderListType, autoStart: Boolean) = when (type) {
    RenderListType.PRESET -> if (autoStart) {
        CardFrame(
            color = AppTheme.colors.autoStart,
            titleRes = R.string.runs_on_app_startup,
            titleAtTop = false
        )
    } else {
        null
    }

    RenderListType.HISTORY -> CardFrame(
        color = AppTheme.colors.historyBorder,
        titleRes = R.string.last_launched_split,
        titleAtTop = true
    )

    RenderListType.HISTORY_CONTRAST -> CardFrame(
        color = AppTheme.colors.historyAccentBorder,
        titleRes = R.string.last_launched_split,
        titleAtTop = true
    )
}

@Composable
private fun CardFrameEdge(frame: CardFrame, isTop: Boolean) {
    if (frame.titleAtTop == isTop) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            text = stringResource(frame.titleRes),
            style = AppTheme.typography.dialogSubtitle,
            color = AppTheme.colors.contentPrimary,
            textAlign = TextAlign.Center
        )
    } else {
        Spacer(Modifier.height(FrameWidth))
    }
}

@Composable
private fun RowScope.SplitPresetContent(preset: DisplaySplitPreset, showWindowShift: Boolean, showWindowType: Boolean) =
    BoxWithConstraints(
        modifier = Modifier.weight(1f)
    ) {
        val iconSize = if (maxWidth < CompactRowWidth) CompactIconSize else RegularIconSize
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(app = preset.firstApp, size = iconSize, playBadgeAtEnd = true)

            Spacer(Modifier.width(IconTextGap))

            AppTexts(
                app = preset.firstApp,
                alignEnd = false,
                showCaption = showWindowType && preset.firstApp.withCaption,
                modifier = Modifier.weight(1f)
            )

            RatioColumn(
                preset = preset,
                showWindowShift = showWindowShift,
                modifier = Modifier.padding(horizontal = RatioColumnPadding)
            )

            AppTexts(
                app = preset.secondApp,
                alignEnd = true,
                showCaption = showWindowType && preset.secondApp.withCaption,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(IconTextGap))

            AppIcon(app = preset.secondApp, size = iconSize, playBadgeAtEnd = false)
        }
    }

@Composable
private fun AppIcon(app: DisplayAppPreset, size: Dp, playBadgeAtEnd: Boolean) = Box(
    modifier = Modifier.size(size),
    contentAlignment = if (playBadgeAtEnd) Alignment.BottomEnd else Alignment.BottomStart
) {
    val context = LocalContext.current
    app.icon?.let {
        AsyncImage(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size * .22f)),
            model = remember(app.packageName) {
                ImageRequest.Builder(context)
                    .data(it)
                    .build()
            },
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
    }

    if (app.autoPlay == true) {
        Icon(
            modifier = Modifier
                .offset(x = if (playBadgeAtEnd) 4.dp else (-4).dp, y = 4.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.surfaceSettingsLayer1)
                .padding(2.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.contentAccent)
                .padding(3.dp),
            painter = rememberPainterResource(R.drawable.ic_play),
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun AppTexts(app: DisplayAppPreset, alignEnd: Boolean, showCaption: Boolean, modifier: Modifier) = Column(
    modifier = modifier,
    horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
) {
    Text(
        text = app.title,
        style = AppTheme.typography.cardTitle,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        color = AppTheme.colors.contentPrimary
    )
    val packageStyle = AppTheme.typography.dialogSubtitle
    Row(
        horizontalArrangement = Arrangement.spacedBy(
            packageStyle.emToDp(CAPTION_GLYPH_GAP_EM),
            if (alignEnd) Alignment.End else Alignment.Start
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val glyphTint = AppTheme.colors.contentPrimary.copy(.5f)
        if (showCaption && !alignEnd) WindowCaptionGlyph(textStyle = packageStyle, tint = glyphTint)
        Text(
            text = app.packageName,
            style = packageStyle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = AppTheme.colors.contentPrimary.copy(.4f),
            modifier = Modifier.weight(1f, fill = false)
        )
        if (showCaption && alignEnd) WindowCaptionGlyph(textStyle = packageStyle, tint = glyphTint)
    }
}

@Composable
private fun RatioColumn(preset: DisplaySplitPreset, showWindowShift: Boolean, modifier: Modifier) = Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        RatioGlyph(
            firstShare = preset.firstShare(),
            isLandscape = rememberIsLandscape(),
            length = 16.dp,
            modifier = Modifier.alpha(.75f)
        )
        Text(
            text = preset.ratioLabel(),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.cardFormatTitle.copy(fontFeatureSettings = "tnum"),
            color = AppTheme.colors.contentPrimary,
            maxLines = 1
        )
    }

    val windowShift = preset.bottomWindowShift && showWindowShift
    if (preset.darkBackground || windowShift || preset.quickAccess) {
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (preset.darkBackground) PresetStatusBadge(R.drawable.ic_moon)
            if (windowShift) PresetStatusBadge(R.drawable.ic_lift)
            if (preset.quickAccess) PresetStatusBadge(R.drawable.ic_star)
        }
    }
}

private fun DisplaySplitPreset.firstShare() = when (type) {
    DisplayPresetType.HALF -> 1 / 2f
    DisplayPresetType.ONE_TO_THREE -> 1 / 3f
    DisplayPresetType.TWO_TO_THREE -> 2 / 3f
    DisplayPresetType.THREE_TO_FOUR -> 3 / 7f
    DisplayPresetType.THREE_TO_TWO -> 3 / 5f
    DisplayPresetType.FOUR_TO_THREE -> 4 / 7f
    DisplayPresetType.CUSTOM, DisplayPresetType.FREE -> ratio
}

@Composable
private fun DisplaySplitPreset.ratioLabel() = when (type) {
    DisplayPresetType.HALF -> "1x1"
    DisplayPresetType.ONE_TO_THREE -> "1x2"
    DisplayPresetType.TWO_TO_THREE -> "2x1"
    DisplayPresetType.THREE_TO_FOUR -> "3x4"
    DisplayPresetType.THREE_TO_TWO -> "3x2"
    DisplayPresetType.FOUR_TO_THREE -> "4x3"
    DisplayPresetType.FREE -> stringResource(R.string.free_mode_short)
    DisplayPresetType.CUSTOM -> splitRatioLabel(ratio)
}
