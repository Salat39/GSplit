package com.salat.settings.common.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salat.uikit.component.SettingsDefaults
import com.salat.uikit.theme.AppTheme

@Composable
fun RenderGroupCaption(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsDefaults.RowHorizontalPadding, vertical = 6.dp),
        text = text,
        style = AppTheme.typography.aboutText,
        color = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA)
    )
}
