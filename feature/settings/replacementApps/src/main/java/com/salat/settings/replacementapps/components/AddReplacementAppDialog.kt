package com.salat.settings.replacementapps.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.salat.settings.replacementapps.entity.DeviceAppInfo
import com.salat.settings.replacementapps.entity.SelectedDialogApp
import com.salat.ui.clickableNoRipple
import com.salat.ui.rememberIsLandscape
import com.salat.uikit.component.BaseDialog
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.theme.AppTheme

@Composable
fun AddReplacementAppDialog(
    modifier: Modifier = Modifier,
    list: List<DeviceAppInfo> = emptyList(),
    uiScaleState: State<Float>?,
    onDismiss: () -> Unit = {},
    onCancel: () -> Unit = { onDismiss() },
    onSelect: (SelectedDialogApp?) -> Unit
) {
    val isLandscape = rememberIsLandscape()

    BaseDialog(
        modifier = modifier,
        uiScaleState = uiScaleState?.value,
        onDismiss = onDismiss
    ) {
        var firstWindow by remember { mutableStateOf(false) }
        var secondWindow by remember { mutableStateOf(false) }
        var autoPlay by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .padding(top = 22.dp)
        ) {
            Text(
                text = stringResource(R.string.adding_app),
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

                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(.1f))
                )

                val firstTitle = stringResource(
                    if (isLandscape) {
                        R.string.left_window
                    } else {
                        R.string.top_window
                    }
                )
                val secondTitle =
                    stringResource(if (isLandscape) R.string.right_window else R.string.bottom_window)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clickableNoRipple {
                                firstWindow = !firstWindow
                            }
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 18.dp)
                    ) {
                        Checkbox(
                            checked = firstWindow,
                            onCheckedChange = null,
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppTheme.colors.contentAccent.copy(alpha = .8f),
                                uncheckedColor = AppTheme.colors.contentPrimary.copy(alpha = .3f),
                                disabledCheckedColor = AppTheme.colors.contentPrimary.copy(alpha = .3f),
                                disabledUncheckedColor = AppTheme.colors.contentPrimary.copy(alpha = .3f),
                                checkmarkColor = AppTheme.colors.contentPrimary,
                                disabledIndeterminateColor = AppTheme.colors.contentPrimary
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = firstTitle.lowercase(),
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
                                secondWindow = !secondWindow
                            }
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Checkbox(
                            checked = secondWindow,
                            onCheckedChange = null,
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppTheme.colors.contentAccent.copy(alpha = .8f),
                                uncheckedColor = AppTheme.colors.contentPrimary.copy(alpha = .3f),
                                disabledCheckedColor = AppTheme.colors.contentPrimary.copy(alpha = .3f),
                                disabledUncheckedColor = AppTheme.colors.contentPrimary.copy(alpha = .3f),
                                checkmarkColor = AppTheme.colors.contentPrimary,
                                disabledIndeterminateColor = AppTheme.colors.contentPrimary
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = secondTitle.lowercase(),
                            color = AppTheme.colors.contentPrimary,
                            style = AppTheme.typography.radioTitle,
                            maxLines = 2
                        )
                    }
                }

                val showAutoPlay by remember { derivedStateOf { preSelected?.isMediaApp == true } }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showAutoPlay,
                    enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(200)),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(200))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(.1f))
                        )

                        RenderSwitcher(
                            title = stringResource(R.string.autoplay_s),
                            value = autoPlay,
                            groupDivider = false
                        ) {
                            autoPlay = it
                        }
                    }
                }

                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(.1f))
                )

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
                    val enableOk by remember { derivedStateOf { (firstWindow || secondWindow) && preSelected != null } }
                    Text(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(enabled = enableOk) {
                                preSelected?.let {
                                    // No position
                                    if (!firstWindow && !secondWindow) {
                                        onSelect(null)
                                        return@let
                                    }

                                    onSelect(
                                        SelectedDialogApp(
                                            app = it,
                                            first = firstWindow,
                                            second = secondWindow,
                                            autoPlay = if (it.isMediaApp) autoPlay else false
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
