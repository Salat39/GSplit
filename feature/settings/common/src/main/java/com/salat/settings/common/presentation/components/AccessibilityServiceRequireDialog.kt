package com.salat.settings.common.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.salat.resources.R
import com.salat.ui.clickableNoRipple
import com.salat.uikit.component.BaseDialog
import com.salat.uikit.component.DialogButton
import com.salat.uikit.component.DialogButtonKind
import com.salat.uikit.component.DialogButtons
import com.salat.uikit.component.DialogTextPadding
import com.salat.uikit.component.DialogTopPadding
import com.salat.uikit.theme.AppTheme
import presentation.isPackageInstalled
import presentation.openAccessibilitySettings
import presentation.openPackage

private const val MACRO_DROID_PACKAGE = "com.arlosoft.macrodroid"

@Composable
fun AccessibilityServiceRequireDialog(
    modifier: Modifier = Modifier,
    uiScaleState: Float = 1f,
    @StringRes titleRes: Int = R.string.accessibility_permission_prompt,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    BaseDialog(
        modifier = modifier.clickableNoRipple {},
        uiScaleState = uiScaleState,
        onDismiss = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = DialogTopPadding)
        ) {
            val context = LocalContext.current
            val isMacroDroidInstalled = context.isPackageInstalled(MACRO_DROID_PACKAGE)

            Text(
                modifier = Modifier.padding(horizontal = DialogTextPadding),
                text = stringResource(titleRes),
                color = AppTheme.colors.contentPrimary,
                style = AppTheme.typography.dialogListTitle
            )

            if (isMacroDroidInstalled) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    modifier = Modifier.padding(horizontal = DialogTextPadding),
                    text = stringResource(R.string.enable_AccessibilityService_step_two),
                    color = AppTheme.colors.contentPrimary,
                    style = AppTheme.typography.dialogListTitle
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            DialogButtons {
                DialogButton(
                    text = stringResource(R.string.settings),
                    onClick = { context.openAccessibilitySettings() }
                )

                if (isMacroDroidInstalled) {
                    DialogButton(
                        text = stringResource(R.string.macro_droid),
                        onClick = { context.openPackage(MACRO_DROID_PACKAGE) }
                    )
                }

                DialogButton(
                    text = stringResource(R.string.enable),
                    kind = DialogButtonKind.Accent,
                    onClick = onConfirm
                )
            }
        }
    }
}
