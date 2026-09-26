package com.salat.settings.presets.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.uikit.component.SettingsDefaults
import com.salat.uikit.theme.AppTheme

internal const val ADVANCED_SETTINGS_ANIMATION_MS = 200

@Composable
internal fun AdvancedSettingsRow(onClick: () -> Unit) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = SettingsDefaults.RowHorizontalPadding)
    ) {
        Text(
            text = stringResource(R.string.advanced_settings),
            style = AppTheme.typography.screenTitle,
            color = AppTheme.colors.contentPrimary
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = stringResource(R.string.advanced_settings_summary),
            style = AppTheme.typography.dialogSubtitle,
            color = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA)
        )
    }

    Icon(
        modifier = Modifier.size(24.dp),
        imageVector = Icons.Filled.KeyboardArrowDown,
        contentDescription = null,
        tint = AppTheme.colors.contentPrimary.copy(.6f)
    )
    Spacer(Modifier.width(16.dp))
}
