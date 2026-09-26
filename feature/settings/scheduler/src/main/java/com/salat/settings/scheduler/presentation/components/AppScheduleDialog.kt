package com.salat.settings.scheduler.presentation.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.scheduler.presentation.entity.DeviceAppInfo
import com.salat.settings.scheduler.presentation.entity.ScheduleTaskType
import com.salat.settings.scheduler.presentation.entity.ScheduledApp
import com.salat.uikit.component.BaseDialog
import com.salat.uikit.component.DialogAppIconShape
import com.salat.uikit.component.DialogAppList
import com.salat.uikit.component.DialogButton
import com.salat.uikit.component.DialogButtonKind
import com.salat.uikit.component.DialogButtons
import com.salat.uikit.component.DialogContainerPadding
import com.salat.uikit.component.DialogInsetShape
import com.salat.uikit.component.DialogTextPadding
import com.salat.uikit.component.DialogTitle
import com.salat.uikit.component.DialogTopPadding
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.SettingsDefaults
import com.salat.uikit.theme.AppTheme
import kotlinx.coroutines.delay
import presentation.capitalizeFirstLetter

@Composable
fun AppScheduleDialog(
    modifier: Modifier = Modifier,
    list: List<DeviceAppInfo> = emptyList(),
    uiScaleState: State<Float>?,
    onDismiss: () -> Unit = {},
    onCancel: () -> Unit = { onDismiss() },
    onSelect: (ScheduledApp?) -> Unit
) {
    BaseDialog(
        modifier = modifier,
        uiScaleState = uiScaleState?.value,
        onDismiss = onDismiss
    ) {
        var seconds by remember { mutableIntStateOf(0) }
        var task by remember { mutableStateOf(ScheduleTaskType.BEFORE) }
        var autoPlay by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(top = DialogTopPadding)) {
            DialogTitle(stringResource(R.string.schedule_a_launch))
            Spacer(modifier = Modifier.height(16.dp))

            if (list.isEmpty()) {
                RenderScan()
            } else {
                var preSelected by remember { mutableStateOf<DeviceAppInfo?>(null) }

                DialogAppList(
                    items = list,
                    title = { it.appName },
                    subtitle = { it.packageName },
                    isSelected = { it.packageName == preSelected?.packageName },
                    onClick = { preSelected = it },
                    icon = { app ->
                        DrawableImage(
                            drawable = app.icon,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(DialogAppIconShape)
                        )
                    }
                )

                Spacer(Modifier.height(16.dp))

                TaskTypeToggler(task = task, onSelect = { task = it })

                val showTimePicker by remember { derivedStateOf { task != ScheduleTaskType.INSTEAD } }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showTimePicker,
                    enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(200)),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(200))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DialogContainerPadding)
                            .padding(top = 8.dp)
                            .clip(DialogInsetShape)
                            .background(AppTheme.colors.surfaceLayer1)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RepeatableButton(
                            text = "−",
                            onClick = remember {
                                {
                                    seconds = if (seconds > 0) seconds - 1 else 0
                                }
                            }
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = buildString {
                                append(
                                    when (task) {
                                        ScheduleTaskType.BEFORE -> stringResource(R.string.time_before_autostart)
                                        else -> stringResource(R.string.time_after_autostart)
                                    }
                                )
                                append(" ")
                                append(seconds)
                                append(" ")
                                append(stringResource(R.string.sec))
                            },
                            style = AppTheme.typography.settingsTitle,
                            color = AppTheme.colors.contentPrimary,
                            textAlign = TextAlign.Center
                        )
                        RepeatableButton(text = "+", onClick = remember { { seconds++ } })
                    }
                }

                val showAutoPlay by remember { derivedStateOf { preSelected?.isMediaApp == true } }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showAutoPlay,
                    enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(200)),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(200))
                ) {
                    RenderSwitcher(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .padding(horizontal = DialogTextPadding - SettingsDefaults.RowHorizontalPadding),
                        title = stringResource(R.string.autoplay_s),
                        value = autoPlay,
                        groupDivider = false
                    ) {
                        autoPlay = it
                    }
                }

                Spacer(Modifier.height(4.dp))

                DialogButtons {
                    DialogButton(
                        text = stringResource(android.R.string.cancel).capitalizeFirstLetter(),
                        onClick = onCancel
                    )
                    val enableOk by remember { derivedStateOf { preSelected != null } }
                    DialogButton(
                        text = stringResource(android.R.string.ok),
                        kind = DialogButtonKind.Accent,
                        enabled = enableOk,
                        onClick = {
                            preSelected?.let { app ->
                                onSelect(
                                    ScheduledApp(
                                        app = app,
                                        time = if (task == ScheduleTaskType.INSTEAD) 0 else seconds,
                                        isPreTask = task == ScheduleTaskType.BEFORE,
                                        isAutoPlay = autoPlay
                                    )
                                )
                            } ?: run { onSelect(null) }
                            onCancel()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskTypeToggler(task: ScheduleTaskType, onSelect: (ScheduleTaskType) -> Unit) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
        .padding(horizontal = DialogContainerPadding)
        .clip(DialogInsetShape)
        .background(AppTheme.colors.surfaceLayer1)
        .padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
) {
    ScheduleTaskType.entries.forEach { type ->
        val selected = type == task
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(TaskSegmentShape)
                .background(if (selected) AppTheme.colors.contentAccent.copy(.18f) else Color.Transparent)
                .clickable { onSelect(type) }
                .padding(horizontal = 6.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(type.titleRes).lowercase(),
                color = if (selected) AppTheme.colors.settingsTitleAccent else AppTheme.colors.contentPrimary,
                style = AppTheme.typography.radioTitle,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

private val TaskSegmentShape = RoundedCornerShape(12.dp)

private val ScheduleTaskType.titleRes
    get() = when (this) {
        ScheduleTaskType.BEFORE -> R.string.before_split
        ScheduleTaskType.INSTEAD -> R.string.instead_of_split
        ScheduleTaskType.AFTER -> R.string.after_split
    }

@Suppress("KotlinConstantConditions")
@Composable
fun RepeatableButton(text: String, initialDelay: Long = 500L, repeatDelay: Long = 60L, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(initialDelay)
            while (isPressed) {
                onClick()
                delay(repeatDelay)
            }
        }
    }

    Box(
        modifier = Modifier
            .sizeIn(minWidth = 56.dp, minHeight = 44.dp)
            .clip(TaskSegmentShape)
            .background(AppTheme.colors.contentPrimary.copy(.08f))
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTheme.typography.dialogTitle,
            color = AppTheme.colors.contentPrimary
        )
    }
}

@Composable
private fun RenderScan() = Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    CircularProgressIndicator(
        modifier = Modifier.size(36.dp),
        color = AppTheme.colors.contentPrimary
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.scanning_installed_apps),
        color = AppTheme.colors.contentPrimary,
        textAlign = TextAlign.Center
    )
}
