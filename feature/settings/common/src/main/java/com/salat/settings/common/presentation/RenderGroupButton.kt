package com.salat.settings.common.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

@Composable
fun RenderGroupButton(
    title: String,
    subtitle: String = "",
    icon: Painter = rememberVectorPainter(image = Icons.Filled.Settings),
    iconSize: Int = 24,
    onClick: () -> Unit = {}
) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
    verticalAlignment = Alignment.CenterVertically
) {
    Box(
        modifier = Modifier
            .width(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier
                .size(iconSize.dp),
            painter = icon,
            tint = AppTheme.colors.contentPrimary,
            contentDescription = null
        )
    }

    Spacer(Modifier.width(2.dp))

    Column(
        Modifier
            .weight(1f)
            .padding(vertical = if (subtitle.isEmpty()) 16.dp else 12.dp)
    ) {
        Text(
            text = title,
            style = AppTheme.typography.screenTitle,
            color = AppTheme.colors.contentPrimary
        )

        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(5.dp))

            Text(
                text = subtitle,
                color = AppTheme.colors.contentPrimary.copy(.4f),
                style = AppTheme.typography.dialogSubtitle
            )
        }
    }

    Spacer(Modifier.width(24.dp))
}
