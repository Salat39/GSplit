package com.salat.settings.add.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.add.presentation.components.AppSelectDialog
import com.salat.settings.add.presentation.components.DrawableImage
import com.salat.settings.add.presentation.components.RenderToolbar
import com.salat.settings.add.presentation.entity.DeviceAppInfo
import com.salat.settings.add.presentation.entity.SizeFormat
import com.salat.ui.clickableNoRipple
import com.salat.ui.rememberIsLandscape
import com.salat.uikit.component.CheckboxWithTitle
import com.salat.uikit.component.TopShadow
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme

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
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTheme.colors.surfaceLayer1)
        ) {
            TopShadow()

            val topWeight = animateFloatAsState(
                targetValue = state.splitForm.topWeight(),
                animationSpec = tween(durationMillis = 300)
            )

            val bottomWeight = animateFloatAsState(
                targetValue = state.splitForm.bottomWeight(),
                animationSpec = tween(durationMillis = 300)
            )

            val isLandscape = rememberIsLandscape()

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AppPane(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(topWeight.value),
                        title = "1.",
                        placeholder = stringResource(R.string.add_first_app),
                        app = state.topApp,
                        backgroundColor = AppTheme.colors.addSplitTop,
                        onClick = { showFirstSelectDialog.value = true },
                        onToggleAutoPlay = { sendAction(AddViewModel.Action.ToggleTopAutoPlay) }
                    )

                    SplitOptionsPane(
                        isLandscape = true,
                        selected = state.splitForm,
                        onSelect = { sendAction(AddViewModel.Action.SetSplitForm(it)) }
                    )

                    AppPane(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(bottomWeight.value),
                        title = "2.",
                        placeholder = stringResource(R.string.add_second_app),
                        app = state.bottomApp,
                        backgroundColor = AppTheme.colors.addSplitBottom,
                        onClick = { showSecondSelectDialog.value = true },
                        onToggleAutoPlay = { sendAction(AddViewModel.Action.ToggleBottomAutoPlay) }
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppPane(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(topWeight.value),
                        title = "1.",
                        placeholder = stringResource(R.string.add_first_app),
                        app = state.topApp,
                        backgroundColor = AppTheme.colors.addSplitTop,
                        onClick = { showFirstSelectDialog.value = true },
                        onToggleAutoPlay = { sendAction(AddViewModel.Action.ToggleTopAutoPlay) },
                        contentModifier = Modifier.fillMaxWidth()
                    )

                    SplitOptionsPane(
                        isLandscape = false,
                        selected = state.splitForm,
                        onSelect = { sendAction(AddViewModel.Action.SetSplitForm(it)) }
                    )

                    AppPane(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(bottomWeight.value),
                        title = "2.",
                        placeholder = stringResource(R.string.add_second_app),
                        app = state.bottomApp,
                        backgroundColor = AppTheme.colors.addSplitBottom,
                        onClick = { showSecondSelectDialog.value = true },
                        onToggleAutoPlay = { sendAction(AddViewModel.Action.ToggleBottomAutoPlay) },
                        contentModifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun AppPane(
    modifier: Modifier,
    title: String,
    placeholder: String,
    app: DeviceAppInfo?,
    backgroundColor: Color,
    onClick: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    contentModifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickableNoRipple(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = contentModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = AppTheme.typography.addingSectionTitle,
                color = AppTheme.colors.contentPrimary
            )
            Spacer(Modifier.width(16.dp))
            app?.let { item ->
                Row(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .padding(end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.icon?.let { icon ->
                        DrawableImage(
                            drawable = icon,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(Modifier.width(10.dp))
                    }

                    Column {
                        Text(
                            text = item.appName,
                            style = AppTheme.typography.screenTitle,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = AppTheme.colors.contentPrimary
                        )
                        Text(
                            text = item.packageName,
                            style = AppTheme.typography.dialogSubtitle,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = AppTheme.colors.contentPrimary.copy(.5f)
                        )
                    }
                }
            } ?: run {
                Text(
                    text = placeholder,
                    style = AppTheme.typography.screenTitle,
                    color = AppTheme.colors.contentPrimary
                )
            }
        }

        app?.let { item ->
            if (!item.isMediaApp) return@let
            item.autoPlay?.let { autoPlay ->
                Spacer(Modifier.height(12.dp))

                CheckboxWithTitle(
                    modifier = Modifier.background(Color.Black.copy(.15f)),
                    title = stringResource(R.string.autoplay),
                    checked = autoPlay,
                    onCheckedChange = { onToggleAutoPlay() }
                )
            }
        }
    }
}

@Composable
private fun SplitOptionsPane(isLandscape: Boolean, selected: SizeFormat, onSelect: (SizeFormat) -> Unit) {
    Box(
        modifier = if (isLandscape) {
            Modifier.width(IntrinsicSize.Min)
        } else {
            Modifier.height(IntrinsicSize.Min)
        },
        contentAlignment = Alignment.Center
    ) {
        if (isLandscape) {
            Row(Modifier.fillMaxSize()) {
                Spacer(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(AppTheme.colors.addSplitTop)
                )
                Spacer(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(AppTheme.colors.addSplitBottom)
                )
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(AppTheme.colors.addSplitTop)
                )
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(AppTheme.colors.addSplitBottom)
                )
            }
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surfaceBackground)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SplitOptionsGroup(
                    options = optionsTop,
                    selected = selected,
                    onSelect = onSelect,
                    isLandscape = true
                )
                SplitOptionsGroup(
                    options = optionsBottom,
                    selected = selected,
                    onSelect = onSelect,
                    isLandscape = true
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surfaceBackground)
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SplitOptionsGroup(
                    options = optionsTop,
                    selected = selected,
                    onSelect = onSelect,
                    isLandscape = false
                )
                SplitOptionsGroup(
                    options = optionsBottom,
                    selected = selected,
                    onSelect = onSelect,
                    isLandscape = false
                )
            }
        }
    }
}

@Composable
private fun SplitOptionsGroup(
    options: List<SizeFormat>,
    selected: SizeFormat,
    onSelect: (SizeFormat) -> Unit,
    isLandscape: Boolean
) {
    if (isLandscape) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            options.forEach { option ->
                SplitOptionItem(
                    option = option,
                    selected = selected,
                    onSelect = onSelect
                )
            }
        }
    } else {
        Row(horizontalArrangement = Arrangement.Center) {
            options.forEach { option ->
                SplitOptionItem(
                    option = option,
                    selected = selected,
                    onSelect = onSelect
                )
            }
        }
    }
}

@Composable
private fun SplitOptionItem(option: SizeFormat, selected: SizeFormat, onSelect: (SizeFormat) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickableNoRipple {
                onSelect(option)
            }
            .padding(end = 12.dp)
    ) {
        RadioButton(
            selected = option == selected,
            onClick = { onSelect(option) },
            colors = RadioButtonColors(
                selectedColor = AppTheme.colors.contentPrimary.copy(.8f),
                unselectedColor = AppTheme.colors.contentPrimary.copy(.3f),
                disabledSelectedColor = AppTheme.colors.contentPrimary.copy(.3f),
                disabledUnselectedColor = AppTheme.colors.contentPrimary.copy(.3f)
            )
        )
        Text(
            text = option.label(),
            color = AppTheme.colors.contentPrimary,
            style = AppTheme.typography.radioTitle,
            modifier = Modifier
        )
    }
}

private fun SizeFormat.topWeight(): Float = when (this) {
    SizeFormat.ONE_TO_THREE -> 1f
    SizeFormat.THREE_TO_FOUR -> 3f
    SizeFormat.HALF -> 1f
    SizeFormat.THREE_TO_TWO -> 3f
    SizeFormat.TWO_TO_THREE -> 2f
    SizeFormat.FOUR_TO_THREE -> 4f
}

private fun SizeFormat.bottomWeight(): Float = when (this) {
    SizeFormat.ONE_TO_THREE -> 2f
    SizeFormat.THREE_TO_FOUR -> 4f
    SizeFormat.HALF -> 1f
    SizeFormat.THREE_TO_TWO -> 2f
    SizeFormat.TWO_TO_THREE -> 1f
    SizeFormat.FOUR_TO_THREE -> 3f
}

private fun SizeFormat.label(): String = when (this) {
    SizeFormat.HALF -> "1x1"
    SizeFormat.ONE_TO_THREE -> "1x2"
    SizeFormat.TWO_TO_THREE -> "2x1"
    SizeFormat.THREE_TO_TWO -> "3x2"
    SizeFormat.THREE_TO_FOUR -> "3x4"
    SizeFormat.FOUR_TO_THREE -> "4x3"
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
