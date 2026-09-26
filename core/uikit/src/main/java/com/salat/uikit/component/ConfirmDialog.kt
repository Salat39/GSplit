package com.salat.uikit.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme
import presentation.capitalizeFirstLetter

// private const val BASE_WIDTH = 320

@Composable
fun ConfirmDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    message: String = "",
    okButtonTitle: String = "",
    closeButtonTitle: String = "",
    negativeAction: Boolean = false,
    disableNegative: Boolean = false,
    isShort: Boolean = false,
    uiScaleState: State<Float>? = null,
    uiScale: Float? = null,
    onDismiss: () -> Unit = {},
    onCancel: () -> Unit = { onDismiss() },
    onClick: () -> Unit
) {
    val scale = uiScale ?: (uiScaleState?.value ?: 1f)
    BaseDialog(
        modifier = modifier,
        uiScaleState = scale,
        maxWidth = 560,
        onDismiss = onDismiss
    ) {
        Column(modifier = Modifier.padding(top = DialogTopPadding)) {
            // Title
            if (title.isNotEmpty()) {
                DialogTitle(title)
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Message
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = DialogTextPadding),
                    color = AppTheme.colors.contentPrimary,
                    style = AppTheme.typography.dialogListTitle
                )
                Spacer(modifier = Modifier.height(if (isShort) 4.dp else 12.dp))
            }

            DialogButtons {
                if (!disableNegative) {
                    DialogButton(
                        text = closeButtonTitle.ifEmpty {
                            stringResource(android.R.string.cancel).capitalizeFirstLetter()
                        },
                        onClick = onCancel
                    )
                }
                DialogButton(
                    text = okButtonTitle.ifEmpty { stringResource(android.R.string.ok) },
                    kind = if (negativeAction) DialogButtonKind.Danger else DialogButtonKind.Accent,
                    onClick = onClick
                )
            }
        }
    }
}
