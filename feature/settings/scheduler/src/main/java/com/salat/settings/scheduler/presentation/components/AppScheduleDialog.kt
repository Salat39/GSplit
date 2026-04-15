package com.salat.settings.scheduler.presentation.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.scheduler.presentation.entity.DeviceAppInfo
import com.salat.settings.scheduler.presentation.entity.ScheduleTaskType
import com.salat.settings.scheduler.presentation.entity.ScheduledApp
import com.salat.ui.clickableNoRipple
import com.salat.uikit.component.BaseDialog
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.theme.AppTheme
import kotlinx.coroutines.delay

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

        Column(
            modifier = Modifier
                .padding(top = 22.dp)
        ) {
            Text(
                text = stringResource(R.string.schedule_a_launch),
                modifier = Modifier.padding(horizontal = 24.dp),
                color = AppTheme.colors.contentPrimary,
                style = AppTheme.typography.dialogTitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (list.isEmpty()) {
                RenderScan()
            } else {
                var preSelected by remember { mutableStateOf<DeviceAppInfo?>(null) }

                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(.1f))
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    item(key = -1) {
                        Spacer(
                            Modifier
                                .height(.8.dp)
                        )
                    }
                    itemsIndexed(
                        items = list,
                        key = { index, _ -> index }
                    ) { _, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { preSelected = item }
                                .padding(vertical = 2.dp)
                                .padding(end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (preSelected?.packageName == item.packageName),
                                onClick = { preSelected = item },
                                colors = RadioButtonColors(
                                    selectedColor = AppTheme.colors.contentPrimary.copy(.8f),
                                    unselectedColor = AppTheme.colors.contentPrimary.copy(.3f),
                                    disabledSelectedColor = AppTheme.colors.contentPrimary.copy(.3f),
                                    disabledUnselectedColor = AppTheme.colors.contentPrimary.copy(.3f)
                                )
                            )

                            item.icon?.let { icon ->
                                DrawableImage(
                                    drawable = icon,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(Modifier.width(10.dp))
                            }

                            Column {
                                Text(
                                    text = item.appName,
                                    style = AppTheme.typography.dialogListTitle,
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
                    }
                }

                // TimeControl
                val showTimePicker by remember { derivedStateOf { task != ScheduleTaskType.INSTEAD } }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showTimePicker,
                    enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(200)),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(200))
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Spacer(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(.1f))
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RepeatableButton(
                                text = "-",
                                onClick = remember {
                                    {
                                        seconds = if (seconds > 0) seconds - 1 else 0
                                    }
                                }
                            )
                            Text(
                                modifier = Modifier.weight(1f),
                                text = buildString {
                                    if (ScheduleTaskType.INSTEAD == task) {
                                        append("-")
                                    } else {
                                        append(
                                            when (task) {
                                                ScheduleTaskType.BEFORE ->
                                                    stringResource(R.string.time_before_autostart)

                                                ScheduleTaskType.AFTER ->
                                                    stringResource(R.string.time_after_autostart)

                                                else -> ""
                                            }
                                        )
                                        append(" ")
                                        append(seconds)
                                        append(" ")
                                        append(stringResource(R.string.sec))
                                    }
                                },
                                style = AppTheme.typography.settingsTitle,
                                color = AppTheme.colors.contentPrimary,
                                textAlign = TextAlign.Center
                            )
                            RepeatableButton(text = "+", onClick = remember { { seconds++ } })
                        }
                    }
                }

                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(.1f))
                )

                /*Text(
                    modifier = Modifier.padding(start = 24.dp, end = 20.dp, top = 18.dp),
                    text = stringResource(R.string.launch_direction),
                    style = AppTheme.typography.settingsTitle,
                    color = AppTheme.colors.contentPrimary
                )*/

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clickableNoRipple {
                                task = ScheduleTaskType.BEFORE
                            }
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = task == ScheduleTaskType.BEFORE,
                            onClick = null,
                            colors = RadioButtonColors(
                                selectedColor = AppTheme.colors.contentAccent.copy(.8f),
                                unselectedColor = AppTheme.colors.contentPrimary.copy(.3f),
                                disabledSelectedColor = AppTheme.colors.contentPrimary.copy(.3f),
                                disabledUnselectedColor = AppTheme.colors.contentPrimary.copy(.3f)
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.before_split).lowercase(),
                            color = AppTheme.colors.contentPrimary,
                            style = AppTheme.typography.radioTitle,
                            maxLines = 2
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clickableNoRipple {
                                task = ScheduleTaskType.INSTEAD
                            }
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = task == ScheduleTaskType.INSTEAD,
                            onClick = null,
                            colors = RadioButtonColors(
                                selectedColor = AppTheme.colors.contentAccent.copy(.8f),
                                unselectedColor = AppTheme.colors.contentPrimary.copy(.3f),
                                disabledSelectedColor = AppTheme.colors.contentPrimary.copy(.3f),
                                disabledUnselectedColor = AppTheme.colors.contentPrimary.copy(.3f)
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.instead_of_split).lowercase(),
                            color = AppTheme.colors.contentPrimary,
                            style = AppTheme.typography.radioTitle,
                            maxLines = 2
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clickableNoRipple {
                                task = ScheduleTaskType.AFTER
                            }
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = task == ScheduleTaskType.AFTER,
                            onClick = null,
                            colors = RadioButtonColors(
                                selectedColor = AppTheme.colors.contentAccent.copy(.8f),
                                unselectedColor = AppTheme.colors.contentPrimary.copy(.3f),
                                disabledSelectedColor = AppTheme.colors.contentPrimary.copy(.3f),
                                disabledUnselectedColor = AppTheme.colors.contentPrimary.copy(.3f)
                            )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.after_split).lowercase(),
                            color = AppTheme.colors.contentPrimary,
                            style = AppTheme.typography.radioTitle,
                            maxLines = 2
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(.1f))
                )

                val showAutoPlay by remember { derivedStateOf { preSelected?.isMediaApp == true } }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showAutoPlay,
                    enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(200)),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(200))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        RenderSwitcher(
                            title = stringResource(R.string.autoplay_s),
                            value = autoPlay,
                            groupDivider = false
                        ) {
                            autoPlay = it
                        }

                        Spacer(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(.1f))
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    Text(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onCancel)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        text = stringResource(android.R.string.cancel).uppercase(),
                        style = AppTheme.typography.dialogButton,
                        color = AppTheme.colors.contentAccent
                    )
                    val enableOk by remember { derivedStateOf { preSelected != null } }
                    Text(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(enabled = enableOk) {
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
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        text = stringResource(android.R.string.ok).uppercase(),
                        style = AppTheme.typography.dialogButton,
                        color = if (enableOk) {
                            AppTheme.colors.contentAccent
                        } else AppTheme.colors.contentPrimary.copy(.3f)
                    )
                }
            }
        }
    }
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

    Button(
        onClick = onClick,
        colors = ButtonColors(
            containerColor = AppTheme.colors.autoStart,
            contentColor = AppTheme.colors.contentPrimary,
            disabledContainerColor = Color.Black.copy(.4f),
            disabledContentColor = AppTheme.colors.contentPrimary
        ),
        shape = RoundedCornerShape(20.dp),
        interactionSource = interactionSource
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
