package com.salat.settings.common.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.salat.resources.R
import com.salat.ui.clickableNoRipple
import com.salat.uikit.component.BaseButton
import com.salat.uikit.component.BaseDialog
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
    @StringRes confirmTitleRes: Int = R.string.enable,
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
                .padding(top = 24.dp)
        ) {
            val context = LocalContext.current

            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(titleRes),
                color = AppTheme.colors.contentPrimary,
                style = AppTheme.typography.dialogListTitle
            )
            Spacer(modifier = Modifier.height(12.dp))
            BaseButton(
                modifier = Modifier.padding(horizontal = 24.dp),
                title = stringResource(R.string.settings),
                onClick = { context.openAccessibilitySettings() }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (context.isPackageInstalled(MACRO_DROID_PACKAGE)) {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(R.string.enable_AccessibilityService_step_two),
                    color = AppTheme.colors.contentPrimary,
                    style = AppTheme.typography.dialogListTitle
                )
                Spacer(modifier = Modifier.height(12.dp))
                BaseButton(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    title = stringResource(R.string.macro_droid),
                    onClick = {
                        context.openPackage(MACRO_DROID_PACKAGE)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppTheme.colors.contentPrimary.copy(.1f))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = stringResource(confirmTitleRes),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onConfirm)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    color = AppTheme.colors.contentAccent.copy(alpha = .9f),
                    style = AppTheme.typography.alertDialogButton
                )
            }
        }
    }
}
