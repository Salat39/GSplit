package com.salat.settings.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.salat.uikit.component.SettingsDefaults
import com.salat.uikit.theme.AppTheme

@Composable
fun RenderSliderSetting(title: String, value: String, enabled: Boolean = true, slider: @Composable () -> Unit) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .alpha(if (enabled) 1f else SettingsDefaults.DISABLED_ALPHA)
        .padding(
            start = SettingsDefaults.RowHorizontalPadding,
            end = SettingsDefaults.RowHorizontalPadding,
            top = 16.dp,
            bottom = 4.dp
        )
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = AppTheme.typography.screenTitle,
            color = AppTheme.colors.contentPrimary
        )

        Spacer(Modifier.width(12.dp))

        RenderValueChip(value)
    }

    Spacer(Modifier.height(4.dp))

    slider()
}

@Composable
private fun RenderValueChip(value: String) {
    Text(
        modifier = Modifier
            .clip(CircleShape)
            .background(AppTheme.colors.contentAccent.copy(.18f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
        text = value,
        style = AppTheme.typography.cardTitle.copy(fontFeatureSettings = "tnum"),
        color = AppTheme.colors.settingsTitleAccent,
        maxLines = 1
    )
}
