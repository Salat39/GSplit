package com.salat.uikit.component

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

@Composable
fun RenderSettingsButton(
    title: String,
    subtitle: String? = null,
    enable: Boolean = true,
    showChevron: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enable) {
                onClick()
            }
            .alpha(if (enable) 1f else SettingsDefaults.DISABLED_ALPHA)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = SettingsDefaults.RowHorizontalPadding)
        ) {
            Text(
                text = title,
                style = AppTheme.typography.screenTitle,
                color = AppTheme.colors.contentPrimary
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = subtitle ?: "",
                color = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA),
                style = AppTheme.typography.dialogSubtitle
            )
        }
        if (showChevron) {
            RenderChevron()
            Spacer(Modifier.width(16.dp))
        }
    }
}

@Composable
fun RenderChevron(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier.size(24.dp),
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        tint = AppTheme.colors.contentPrimary.copy(SettingsDefaults.CHEVRON_ALPHA),
        contentDescription = null
    )
}
