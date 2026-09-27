package com.salat.settings.add.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.salat.resources.R
import com.salat.settings.add.presentation.components.AppSelectDialog
import com.salat.settings.add.presentation.components.DrawableImage
import com.salat.settings.add.presentation.components.FreeModeEditor
import com.salat.settings.add.presentation.components.ManualRatioHandle
import com.salat.settings.add.presentation.components.RenderToolbar
import com.salat.settings.add.presentation.components.WindowTypeSwitch
import com.salat.settings.add.presentation.components.WindowTypeSwitchVariant
import com.salat.settings.add.presentation.entity.DeviceAppInfo
import com.salat.settings.add.presentation.entity.SizeFormat
import com.salat.settings.add.presentation.entity.presetFormatOf
import com.salat.ui.appTextScale
import com.salat.ui.clickableNoRipple
import com.salat.ui.rememberIsLandscape
import com.salat.ui.rememberPainterResource
import com.salat.ui.scaledWithLayout
import com.salat.uikit.component.RatioGlyph
import com.salat.uikit.component.SettingsDefaults
import com.salat.uikit.component.TopShadow
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme
import presentation.toast

private val optionsBottom = listOf(
    SizeFormat.THREE_TO_FOUR,
    SizeFormat.FOUR_TO_THREE,
    SizeFormat.THREE_TO_TWO
)

private val optionsTop = listOf(
    SizeFormat.ONE_TO_THREE,
    SizeFormat.HALF,
    SizeFormat.TWO_TO_THREE
)

private val WindowCorner = 24.dp
private val WindowsGap = 8.dp
private val PaneContentPadding = 20.dp
private val PaneBadgeInset = 56.dp
private val PaneLabelMargin = 14.dp
private val NarrowPaneWidth = 280.dp
private val NarrowPaneContentPadding = 12.dp
private val ChipShape = RoundedCornerShape(14.dp)
private val OptionsCardShape = RoundedCornerShape(20.dp)
private val OptionsSpacing = 6.dp
private val SwapButtonSize = 40.dp
private const val PANE_BADGE_ALPHA = .24f
private const val PANE_BADGE_TEXT_ALPHA = .85f

@Composable
internal fun AddScreen(
    state: AddViewModel.ViewState,
    sendAction: (AddViewModel.Action) -> Unit = {},
    uiScaleState: State<Float>? = null,
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->

    val showFirstSelectDialog = remember { mutableStateOf(false) }
    val showSecondSelectDialog = remember { mutableStateOf(false) }

    if (showFirstSelectDialog.value) {
        AppSelectDialog(
            selected = state.topApp,
            list = state.deviceApps,
            uiScaleState = uiScaleState,
            onDismiss = { showFirstSelectDialog.value = false },
            onCancel = { showFirstSelectDialog.value = false },
            onSelect = { sendAction(AddViewModel.Action.SetTopApp(it)) }
        )
    }

    if (showSecondSelectDialog.value) {
        AppSelectDialog(
            selected = state.bottomApp,
            list = state.deviceApps,
            uiScaleState = uiScaleState,
            onDismiss = { showSecondSelectDialog.value = false },
            onCancel = { showSecondSelectDialog.value = false },
            onSelect = { sendAction(AddViewModel.Action.SetBottomApp(it)) }
        )
    }

    LaunchedEffect(state.closeScreenSingleEvent) {
        state.closeScreenSingleEvent?.let {
            onNavigateBack()
            sendAction(AddViewModel.Action.SetCloseScreenSingleEvent(null))
        }
    }

    if (state.freeMode) {
        val onFreeModeBack = {
            if (state.isFreePresetEdit) onNavigateBack() else sendAction(AddViewModel.Action.SetFreeMode(false))
        }
        BackHandler(onBack = onFreeModeBack)

        FreeModeEditor(
            windows = state.freeWindows,
            deviceApps = state.deviceApps,
            showWindowType = state.isWindowTypeEnabled,
            isWindowTypeLocked = !state.isWindowTypeAvailable,
            uiScaleState = uiScaleState,
            sendAction = sendAction,
            onBack = onFreeModeBack,
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.surfaceBackground)
                .padding(innerPadding)
        )
        return@Scaffold
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        val showApply = remember(state.topApp, state.bottomApp) {
            derivedStateOf { state.topApp != null && state.bottomApp != null }
        }

        RenderToolbar(state.editId != null, onNavigateBack, showApply = showApply) {
            sendAction(AddViewModel.Action.CommitPreset)
        }
        var panesSize by remember { mutableStateOf(IntSize.Zero) }
        var bandSize by remember { mutableStateOf(IntSize.Zero) }
        var isRatioDragging by remember { mutableStateOf(false) }

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTheme.colors.surfaceLayer1)
        ) {
            TopShadow()

            val windowRatio = animateFloatAsState(
                targetValue = state.windowRatio,
                animationSpec = if (isRatioDragging) snap() else tween(durationMillis = 300)
            )

            val isLandscape = rememberIsLandscape()
            val windowsLength = if (isLandscape) {
                panesSize.width - bandSize.width
            } else {
                panesSize.height - bandSize.height
            }
            val ratioPerPx = 1f / windowsLength.coerceAtLeast(1)
            val matchedPreset = presetFormatOf(state.customRatio)?.label()

            val firstPane: @Composable (Modifier) -> Unit = { paneModifier ->
                AppPane(
                    modifier = paneModifier,
                    number = 1,
                    label = stringResource(if (isLandscape) R.string.left_window else R.string.top_window),
                    app = state.topApp,
                    isFirst = true,
                    isLandscape = isLandscape,
                    showWindowType = state.isWindowTypeEnabled && !state.isNativeSplitEnabled && state.topApp != null,
                    isWindowTypeLocked = !state.isWindowTypeAvailable,
                    withCaption = state.topApp?.withCaption ?: false,
                    onClick = { showFirstSelectDialog.value = true },
                    onToggleAutoPlay = { sendAction(AddViewModel.Action.ToggleTopAutoPlay) },
                    onWindowTypeChange = { sendAction(AddViewModel.Action.SetTopWithCaption(it)) }
                )
            }
            val secondPane: @Composable (Modifier) -> Unit = { paneModifier ->
                AppPane(
                    modifier = paneModifier,
                    number = 2,
                    label = stringResource(if (isLandscape) R.string.right_window else R.string.bottom_window),
                    app = state.bottomApp,
                    isFirst = false,
                    isLandscape = isLandscape,
                    showWindowType = state.isWindowTypeEnabled &&
                        !state.isNativeSplitEnabled && state.bottomApp != null,
                    isWindowTypeLocked = !state.isWindowTypeAvailable,
                    withCaption = state.bottomApp?.withCaption ?: false,
                    onClick = { showSecondSelectDialog.value = true },
                    onToggleAutoPlay = { sendAction(AddViewModel.Action.ToggleBottomAutoPlay) },
                    onWindowTypeChange = { sendAction(AddViewModel.Action.SetBottomWithCaption(it)) }
                )
            }
            val band: @Composable () -> Unit = {
                SplitControls(
                    isLandscape = isLandscape,
                    state = state,
                    ratioPerPx = ratioPerPx,
                    matchedPreset = matchedPreset,
                    sendAction = sendAction,
                    onDraggingChange = { isRatioDragging = it },
                    modifier = Modifier.onSizeChanged { bandSize = it }
                )
            }

            val canvasModifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .onSizeChanged { panesSize = it }

            if (isLandscape) {
                Row(modifier = canvasModifier) {
                    firstPane(
                        Modifier
                            .fillMaxHeight()
                            .weight(windowRatio.value)
                    )
                    band()
                    secondPane(
                        Modifier
                            .fillMaxHeight()
                            .weight(1f - windowRatio.value)
                    )
                }
            } else {
                Column(modifier = canvasModifier) {
                    firstPane(
                        Modifier
                            .fillMaxWidth()
                            .weight(windowRatio.value)
                    )
                    band()
                    secondPane(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f - windowRatio.value)
                    )
                }
            }
        }
    }
}

private fun outerEdgeShape(isFirst: Boolean, isLandscape: Boolean) = when {
    !isLandscape && isFirst -> RoundedCornerShape(topStart = WindowCorner, topEnd = WindowCorner)
    !isLandscape -> RoundedCornerShape(bottomStart = WindowCorner, bottomEnd = WindowCorner)
    isFirst -> RoundedCornerShape(topStart = WindowCorner, bottomStart = WindowCorner)
    else -> RoundedCornerShape(topEnd = WindowCorner, bottomEnd = WindowCorner)
}

@Composable
private fun windowColor(isFirst: Boolean) =
    if (isFirst) AppTheme.colors.addWindowFirst else AppTheme.colors.addWindowSecond

@Composable
private fun windowAccent(isFirst: Boolean) =
    if (isFirst) AppTheme.colors.addWindowFirstAccent else AppTheme.colors.addWindowSecondAccent

@Composable
private fun AppPane(
    modifier: Modifier,
    number: Int,
    label: String,
    app: DeviceAppInfo?,
    isFirst: Boolean,
    isLandscape: Boolean,
    showWindowType: Boolean,
    isWindowTypeLocked: Boolean,
    withCaption: Boolean,
    onClick: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    onWindowTypeChange: (Boolean) -> Unit
) {
    val accent = windowAccent(isFirst)
    // The label stays in the outer corner because the options card covers the seam side
    val isLabelAtBottom = !isLandscape && !isFirst
    val labelAtEnd = isLandscape && !isFirst
    val isSwitchInLabelRow = showWindowType && !isLandscape
    val isSwitchInBottomCorner = showWindowType && isLandscape
    val context = LocalContext.current
    val captionToast = app?.let { stringResource(R.string.window_type_caption_toast, it.appName) }.orEmpty()
    val noCaptionToast = app?.let { stringResource(R.string.window_type_no_caption_toast, it.appName) }.orEmpty()
    val adbHint = stringResource(R.string.window_type_adb_hint, stringResource(R.string.adb_features))

    BoxWithConstraints(
        modifier = modifier
            .clip(outerEdgeShape(isFirst, isLandscape))
            .background(windowColor(isFirst))
            .clickableNoRipple(onClick = onClick)
    ) {
        val isNarrow = maxWidth < NarrowPaneWidth
        val badgeTextStyle = AppTheme.typography.radioTitle.let { if (isNarrow) it.scaledWithLayout() else it }
        val sidePadding = if (isNarrow) NarrowPaneContentPadding else PaneContentPadding
        val switchWithLabel: @Composable () -> Unit = {
            WindowTypeSwitch(
                withCaption = withCaption,
                color = accent,
                variant = WindowTypeSwitchVariant.PANE,
                showLabel = true,
                textStyle = badgeTextStyle,
                enabled = !isWindowTypeLocked,
                onChange = onWindowTypeChange,
                onLockedClick = { context.toast(adbHint) }
            )
        }
        val switchWithoutLabel: @Composable () -> Unit = {
            WindowTypeSwitch(
                withCaption = withCaption,
                color = accent,
                variant = WindowTypeSwitchVariant.PANE,
                enabled = !isWindowTypeLocked,
                onChange = { value ->
                    context.toast(if (value) captionToast else noCaptionToast)
                    onWindowTypeChange(value)
                },
                onLockedClick = { context.toast(adbHint) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = sidePadding,
                    end = sidePadding,
                    top = if (isLabelAtBottom) PaneContentPadding else PaneBadgeInset,
                    bottom = if (isLabelAtBottom || isSwitchInBottomCorner) PaneBadgeInset else PaneContentPadding
                ),
            contentAlignment = Alignment.Center
        ) {
            if (app == null) {
                AdaptivePaneContent(
                    regular = { EmptyPaneContent(isCompact = false) },
                    compact = { EmptyPaneContent(isCompact = true) }
                )
            } else {
                AdaptivePaneContent(
                    regular = {
                        AppPaneContent(app, accent, isCompact = false, badgeTextStyle, onToggleAutoPlay)
                    },
                    compact = {
                        AppPaneContent(app, accent, isCompact = true, badgeTextStyle, onToggleAutoPlay)
                    }
                )
            }
        }

        AdaptivePaneRow(
            isSwitchAtStart = labelAtEnd,
            modifier = Modifier
                .align(if (isLabelAtBottom) Alignment.BottomCenter else Alignment.TopCenter)
                .fillMaxWidth()
                .padding(PaneLabelMargin),
            label = {
                PaneLabel(number = number, label = label, accent = accent, textStyle = badgeTextStyle)
            },
            switchWithLabel = { if (isSwitchInLabelRow) switchWithLabel() },
            switchWithoutLabel = { if (isSwitchInLabelRow) switchWithoutLabel() }
        )

        if (isSwitchInBottomCorner) {
            AdaptivePaneRow(
                isSwitchAtStart = isFirst,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(PaneLabelMargin),
                switchWithLabel = switchWithLabel,
                switchWithoutLabel = switchWithoutLabel
            )
        }
    }
}

// Shows the switch with the text when it fits next to the label. Otherwise it shows the switch without the text
@Composable
private fun AdaptivePaneRow(
    isSwitchAtStart: Boolean,
    modifier: Modifier,
    switchWithLabel: @Composable () -> Unit,
    switchWithoutLabel: @Composable () -> Unit,
    label: @Composable () -> Unit = {}
) = Layout(
    modifier = modifier,
    contents = listOf(label, switchWithLabel, switchWithoutLabel)
) { (labelMeasurables, labeledSwitchMeasurables, compactSwitchMeasurables), constraints ->
    val itemConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val labelMeasurable = labelMeasurables.firstOrNull()
    val labeledSwitch = labeledSwitchMeasurables.firstOrNull()?.measure(itemConstraints)
    val compactSwitch = compactSwitchMeasurables.firstOrNull()?.measure(itemConstraints)
    val gap = if (labelMeasurable != null && compactSwitch != null) 8.dp.roundToPx() else 0
    val labelIntrinsicWidth = labelMeasurable?.maxIntrinsicWidth(Constraints.Infinity) ?: 0
    val windowTypeSwitch = labeledSwitch
        ?.takeIf { labelIntrinsicWidth + gap + it.width <= constraints.maxWidth }
        ?: compactSwitch
    val labelMaxWidth = (constraints.maxWidth - (windowTypeSwitch?.width ?: 0) - gap).coerceAtLeast(0)
    val labelPlaceable = labelMeasurable?.measure(
        itemConstraints.copy(maxWidth = labelMaxWidth, minHeight = windowTypeSwitch?.height ?: 0)
    )
    val height = maxOf(labelPlaceable?.height ?: 0, windowTypeSwitch?.height ?: 0)

    layout(constraints.maxWidth, height) {
        labelPlaceable?.let {
            val labelX = if (isSwitchAtStart) constraints.maxWidth - it.width else 0
            it.place(labelX, (height - it.height) / 2)
        }
        windowTypeSwitch?.let {
            val switchX = if (isSwitchAtStart) 0 else constraints.maxWidth - it.width
            it.place(switchX, (height - it.height) / 2)
        }
    }
}

// Shows the regular content when it fits the pane height. Otherwise it shows the compact row
@Composable
private fun AdaptivePaneContent(regular: @Composable () -> Unit, compact: @Composable () -> Unit) = Layout(
    contents = listOf(regular, compact)
) { (regularMeasurables, compactMeasurables), constraints ->
    val contentConstraints = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
    val regularPlaceables = regularMeasurables.map { it.measure(contentConstraints) }
    val regularHeight = regularPlaceables.maxOfOrNull { it.height } ?: 0
    val placeables = if (regularHeight <= constraints.maxHeight) {
        regularPlaceables
    } else {
        compactMeasurables.map { it.measure(contentConstraints) }
    }
    val contentWidth = placeables.maxOfOrNull { it.width } ?: 0
    val contentHeight = placeables.maxOfOrNull { it.height } ?: 0
    val width = contentWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
    val height = contentHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
    layout(width, height) {
        placeables.forEach { it.place((width - it.width) / 2, (height - it.height) / 2) }
    }
}

@Composable
private fun PaneLabel(number: Int, label: String, accent: Color, textStyle: TextStyle, modifier: Modifier = Modifier) =
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(PANE_BADGE_ALPHA))
            .padding(start = 5.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(accent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = AppTheme.typography.toggleChip,
                color = AppTheme.colors.surfaceBackground
            )
        }
        Text(
            text = label,
            style = textStyle,
            color = AppTheme.colors.contentPrimary.copy(PANE_BADGE_TEXT_ALPHA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

@Composable
private fun EmptyPaneContent(isCompact: Boolean) {
    if (isCompact) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashedAddCircle(size = 56.dp)
            EmptyPaneTexts(alignment = Alignment.Start, textAlign = TextAlign.Start)
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DashedAddCircle(size = 76.dp)
            Spacer(Modifier.height(14.dp))
            EmptyPaneTexts(alignment = Alignment.CenterHorizontally, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun EmptyPaneTexts(alignment: Alignment.Horizontal, textAlign: TextAlign) = Column(
    horizontalAlignment = alignment
) {
    Text(
        text = stringResource(R.string.choose_app),
        style = AppTheme.typography.buttonTitle,
        color = AppTheme.colors.contentPrimary,
        textAlign = textAlign
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = stringResource(R.string.tap_to_choose),
        style = AppTheme.typography.aboutText,
        color = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA),
        textAlign = textAlign
    )
}

@Composable
private fun DashedAddCircle(size: Dp) {
    val strokeColor = AppTheme.colors.contentPrimary.copy(.4f)
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawCircle(
                    color = strokeColor,
                    radius = (this.size.minDimension - strokeWidth) / 2,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = AppTheme.colors.contentPrimary.copy(PANE_BADGE_TEXT_ALPHA),
            modifier = Modifier.size(size * .42f)
        )
    }
}

@Composable
private fun AppPaneContent(
    app: DeviceAppInfo,
    accent: Color,
    isCompact: Boolean,
    autoPlayTextStyle: TextStyle,
    onToggleAutoPlay: () -> Unit
) {
    val autoPlay = app.autoPlay.takeIf { app.isMediaApp }
    if (isCompact) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PaneAppIcon(app = app, size = 56.dp)
            Column(modifier = Modifier.weight(1f, fill = false)) {
                PaneAppTexts(app = app, textAlign = TextAlign.Start)
                autoPlay?.let { checked ->
                    Spacer(Modifier.height(8.dp))
                    AutoPlayToggle(
                        checked = checked,
                        accent = accent,
                        textStyle = autoPlayTextStyle,
                        onToggle = onToggleAutoPlay
                    )
                }
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PaneAppIcon(app = app, size = 72.dp)
            Spacer(Modifier.height(14.dp))
            PaneAppTexts(app = app, textAlign = TextAlign.Center)
            autoPlay?.let { checked ->
                Spacer(Modifier.height(16.dp))
                AutoPlayToggle(
                    checked = checked,
                    accent = accent,
                    textStyle = autoPlayTextStyle,
                    onToggle = onToggleAutoPlay
                )
            }
        }
    }
}

@Composable
private fun PaneAppIcon(app: DeviceAppInfo, size: Dp) = DrawableImage(
    drawable = app.icon,
    modifier = Modifier
        .size(size)
        .clip(RoundedCornerShape(size * .22f))
)

@Composable
private fun PaneAppTexts(app: DeviceAppInfo, textAlign: TextAlign) {
    Text(
        text = app.appName,
        style = AppTheme.typography.statusTitle,
        color = AppTheme.colors.contentPrimary,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
    Spacer(Modifier.height(2.dp))
    Text(
        text = app.packageName,
        style = AppTheme.typography.dialogSubtitle,
        color = AppTheme.colors.contentPrimary.copy(.45f),
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}

@Composable
private fun AutoPlayToggle(checked: Boolean, accent: Color, textStyle: TextStyle, onToggle: () -> Unit) {
    val contentColor = if (checked) accent else AppTheme.colors.contentPrimary.copy(PANE_BADGE_TEXT_ALPHA)
    Row(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (checked) accent.copy(.22f) else Color.Black.copy(PANE_BADGE_ALPHA))
            .clickable(onClick = onToggle)
            .padding(start = 12.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (checked) Icons.Filled.Check else Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = stringResource(R.string.autoplay),
            style = textStyle,
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SplitControls(
    isLandscape: Boolean,
    state: AddViewModel.ViewState,
    ratioPerPx: Float,
    matchedPreset: String?,
    sendAction: (AddViewModel.Action) -> Unit,
    onDraggingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) = SplitBand(
    isLandscape = isLandscape,
    canSwap = state.topApp != null || state.bottomApp != null,
    onSwap = { sendAction(AddViewModel.Action.SwapApps) },
    modifier = modifier
) {
    if (state.splitForm == SizeFormat.CUSTOM) {
        ManualRatioHandle(
            isLandscape = isLandscape,
            ratio = state.customRatio,
            ratioPerPx = ratioPerPx,
            matchedPreset = matchedPreset,
            onRatioChange = { sendAction(AddViewModel.Action.SetCustomRatio(it)) },
            onDraggingChange = { isDragging ->
                onDraggingChange(isDragging)
                if (!isDragging) sendAction(AddViewModel.Action.RoundCustomRatio)
            },
            onShowPresets = { sendAction(AddViewModel.Action.ShowPresets) }
        )
    } else {
        val context = LocalContext.current
        val manualHint = stringResource(R.string.manual_ratio_hint)
        SplitOptionsCard(
            isLandscape = isLandscape,
            selected = state.splitForm,
            onSelect = { sendAction(AddViewModel.Action.SetSplitForm(it)) },
            onManualClick = {
                context.toast(manualHint)
                sendAction(AddViewModel.Action.SetSplitForm(SizeFormat.CUSTOM))
            },
            onFreeModeClick = { sendAction(AddViewModel.Action.SetFreeMode(true)) }
        )
    }
}

// The band is above the windows so that the handle shadow is visible
@Composable
private fun SplitBand(
    isLandscape: Boolean,
    canSwap: Boolean,
    onSwap: () -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit
) = Box(
    modifier = modifier
        .zIndex(1f)
        .then(if (isLandscape) Modifier.width(IntrinsicSize.Min) else Modifier.height(IntrinsicSize.Min)),
    contentAlignment = Alignment.Center
) {
    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(WindowsGap)
        ) {
            BandHalf(
                isFirst = true,
                isLandscape = true,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
            BandHalf(
                isFirst = false,
                isLandscape = true,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(WindowsGap)
        ) {
            BandHalf(
                isFirst = true,
                isLandscape = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            BandHalf(
                isFirst = false,
                isLandscape = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    val swapSlot: @Composable (Modifier) -> Unit = { slotModifier ->
        Box(modifier = slotModifier, contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = canSwap,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                SwapWindowsButton(isLandscape = isLandscape, onClick = onSwap)
            }
        }
    }
    if (isLandscape) {
        // The landscape card is tall. The swap button stays in a fixed slot under it
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
            swapSlot(Modifier.size(SwapButtonSize))
        }
    } else {
        // Equal weights keep the controls in the center. The swap button takes the middle of the free side
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            content()
            swapSlot(Modifier.weight(1f))
        }
    }
}

// The band half is the seam end of the window. Its corners match the outer edge of the other window
@Composable
private fun BandHalf(isFirst: Boolean, isLandscape: Boolean, modifier: Modifier) = Spacer(
    modifier
        .clip(outerEdgeShape(isFirst = !isFirst, isLandscape = isLandscape))
        .background(windowColor(isFirst))
)

@Composable
private fun SplitOptionsCard(
    isLandscape: Boolean,
    selected: SizeFormat,
    onSelect: (SizeFormat) -> Unit,
    onManualClick: () -> Unit,
    onFreeModeClick: () -> Unit
) = Column(
    modifier = Modifier
        .width(IntrinsicSize.Max)
        .shadow(10.dp, OptionsCardShape)
        .clip(OptionsCardShape)
        .background(AppTheme.colors.surfaceBackground)
        .padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OptionsSpacing)
        ) {
            listOf(optionsTop, optionsBottom).forEach { options ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(OptionsSpacing)
                ) {
                    options.forEach { option ->
                        SplitOptionItem(
                            option = option,
                            selected = selected,
                            isLandscape = true,
                            onSelect = onSelect,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    } else {
        listOf(optionsTop, optionsBottom).forEach { options ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OptionsSpacing)
            ) {
                options.forEach { option ->
                    SplitOptionItem(
                        option = option,
                        selected = selected,
                        isLandscape = false,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    OptionsDivider()

    if (isLandscape) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(OptionsSpacing)
        ) {
            ModeButton(
                iconRes = R.drawable.ic_manual_ratio,
                titleRes = R.string.manual_ratio,
                isVertical = true,
                onClick = onManualClick,
                modifier = Modifier.fillMaxWidth(),
                iconRotation = 90f
            )
            ModeButton(
                iconRes = R.drawable.ic_free_mode,
                titleRes = R.string.free_mode_short,
                isVertical = true,
                onClick = onFreeModeClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(OptionsSpacing)
        ) {
            ModeButton(
                iconRes = R.drawable.ic_manual_ratio,
                titleRes = R.string.manual_ratio,
                isVertical = false,
                onClick = onManualClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModeButton(
                iconRes = R.drawable.ic_free_mode,
                titleRes = R.string.free_mode_short,
                isVertical = false,
                onClick = onFreeModeClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun SwapWindowsButton(isLandscape: Boolean, onClick: () -> Unit) = Box(
    modifier = Modifier
        .size(SwapButtonSize)
        .shadow(6.dp, CircleShape)
        .clip(CircleShape)
        .background(AppTheme.colors.surfaceBackground)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
) {
    Icon(
        painter = rememberPainterResource(R.drawable.ic_swap_windows),
        contentDescription = stringResource(R.string.swap_windows),
        tint = AppTheme.colors.contentPrimary,
        modifier = Modifier
            .size(20.dp)
            .rotate(if (isLandscape) 90f else 0f)
    )
}

@Composable
private fun OptionsDivider() = Spacer(
    Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(AppTheme.colors.contentPrimary.copy(.08f))
)

@Composable
private fun ModeButton(
    iconRes: Int,
    titleRes: Int,
    isVertical: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    iconRotation: Float = 0f
) {
    val content: @Composable () -> Unit = {
        Icon(
            painter = rememberPainterResource(iconRes),
            contentDescription = null,
            tint = AppTheme.colors.contentPrimary,
            modifier = Modifier
                .size(20.dp)
                .rotate(iconRotation)
        )
        Text(
            text = stringResource(titleRes),
            color = AppTheme.colors.contentPrimary,
            style = AppTheme.typography.radioTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    val paddingScale = appTextScale
    val buttonModifier = modifier
        .clip(ChipShape)
        .background(AppTheme.colors.contentPrimary.copy(.06f))
        .clickable(onClick = onClick)

    if (isVertical) {
        Column(
            modifier = buttonModifier.padding(horizontal = 8.dp * paddingScale, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) { content() }
    } else {
        Row(
            modifier = buttonModifier
                .heightIn(min = 44.dp)
                .padding(horizontal = 14.dp * paddingScale, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) { content() }
    }
}

@Composable
private fun SplitOptionItem(
    option: SizeFormat,
    selected: SizeFormat,
    isLandscape: Boolean,
    onSelect: (SizeFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = option == selected
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(ChipShape)
            .background(if (isSelected) AppTheme.colors.contentAccent.copy(.22f) else Color.Transparent)
            .clickable { onSelect(option) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
    ) {
        RatioGlyph(
            firstShare = option.presetRatio ?: .5f,
            isLandscape = isLandscape,
            modifier = Modifier.alpha(if (isSelected) 1f else .55f)
        )
        Text(
            text = option.label(),
            color = if (isSelected) AppTheme.colors.settingsTitleAccent else AppTheme.colors.contentPrimary,
            style = AppTheme.typography.dialogListTitle.copy(fontFeatureSettings = "tnum"),
            maxLines = 1
        )
    }
}

private fun SizeFormat.label(): String = when (this) {
    SizeFormat.HALF -> "1x1"
    SizeFormat.ONE_TO_THREE -> "1x2"
    SizeFormat.TWO_TO_THREE -> "2x1"
    SizeFormat.THREE_TO_TWO -> "3x2"
    SizeFormat.THREE_TO_FOUR -> "3x4"
    SizeFormat.FOUR_TO_THREE -> "4x3"
    SizeFormat.CUSTOM -> ""
}

@Preview
@Composable
private fun AddScreenPreview() {
    PreviewScreen {
        AddScreen(
            state = AddViewModel.ViewState()
        )
    }
}
