package com.salat.settings.add.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.add.presentation.entity.DeviceAppInfo
import com.salat.uikit.component.BaseDialog
import com.salat.uikit.component.DialogAppIconShape
import com.salat.uikit.component.DialogAppList
import com.salat.uikit.component.DialogButton
import com.salat.uikit.component.DialogButtons
import com.salat.uikit.component.DialogTitle
import com.salat.uikit.component.DialogTopPadding
import com.salat.uikit.theme.AppTheme
import presentation.capitalizeFirstLetter

@Composable
fun AppSelectDialog(
    modifier: Modifier = Modifier,
    selected: DeviceAppInfo? = null,
    list: List<DeviceAppInfo> = emptyList(),
    uiScaleState: State<Float>?,
    onDismiss: () -> Unit = {},
    onCancel: () -> Unit = { onDismiss() },
    onSelect: (DeviceAppInfo?) -> Unit
) {
    BaseDialog(
        modifier = modifier,
        uiScaleState = uiScaleState?.value,
        onDismiss = onDismiss
    ) {
        Column(modifier = Modifier.padding(top = DialogTopPadding)) {
            DialogTitle(stringResource(R.string.choosing_an_app))
            Spacer(modifier = Modifier.height(16.dp))

            if (list.isEmpty()) {
                RenderScan()
            } else {
                DialogAppList(
                    items = list,
                    title = { it.appName },
                    subtitle = { it.packageName },
                    isSelected = { it.packageName == selected?.packageName },
                    onClick = { app ->
                        onSelect(app)
                        onCancel()
                    },
                    icon = { app ->
                        DrawableImage(
                            drawable = app.icon,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(DialogAppIconShape)
                        )
                    }
                )

                Spacer(Modifier.height(4.dp))

                DialogButtons {
                    DialogButton(
                        text = stringResource(android.R.string.cancel).capitalizeFirstLetter(),
                        onClick = onCancel
                    )
                }
            }

            // TODO
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
