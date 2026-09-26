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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.replacementapps.entity.DeviceAppInfo
import com.salat.settings.replacementapps.entity.SelectedDialogApp
import com.salat.ui.rememberIsLandscape
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
import presentation.capitalizeFirstLetter

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

        Column(modifier = Modifier.padding(top = DialogTopPadding)) {
            DialogTitle(stringResource(R.string.adding_app))
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

                val firstTitle = stringResource(if (isLandscape) R.string.left_window else R.string.top_window)
                val secondTitle = stringResource(if (isLandscape) R.string.right_window else R.string.bottom_window)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DialogContainerPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WindowChip(
                        modifier = Modifier.weight(1f),
                        title = firstTitle.lowercase(),
                        checked = firstWindow,
                        onClick = { firstWindow = !firstWindow }
                    )
                    WindowChip(
                        modifier = Modifier.weight(1f),
                        title = secondTitle.lowercase(),
                        checked = secondWindow,
                        onClick = { secondWindow = !secondWindow }
                    )
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
                    val enableOk by remember { derivedStateOf { (firstWindow || secondWindow) && preSelected != null } }
                    DialogButton(
                        text = stringResource(android.R.string.ok),
                        kind = DialogButtonKind.Accent,
                        enabled = enableOk,
                        onClick = {
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
                    )
                }
            }
        }
    }
}

@Composable
private fun WindowChip(modifier: Modifier, title: String, checked: Boolean, onClick: () -> Unit) = Row(
    modifier = modifier
        .clip(DialogInsetShape)
        .background(
            if (checked) {
                AppTheme.colors.contentAccent.copy(.18f)
            } else {
                AppTheme.colors.surfaceLayer1
            }
        )
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 14.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
) {
    Checkbox(
        checked = checked,
        onCheckedChange = null,
        modifier = Modifier.size(20.dp),
        colors = CheckboxDefaults.colors(
            checkedColor = AppTheme.colors.settingsTitleAccent,
            uncheckedColor = AppTheme.colors.contentPrimary.copy(alpha = .3f),
            checkmarkColor = AppTheme.colors.surfaceLayer1
        )
    )
    Spacer(Modifier.width(10.dp))
    Text(
        text = title,
        color = if (checked) AppTheme.colors.settingsTitleAccent else AppTheme.colors.contentPrimary,
        style = AppTheme.typography.radioTitle,
        maxLines = 2
    )
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
