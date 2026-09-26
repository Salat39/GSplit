package com.salat.settings.common.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.salat.uikit.component.RenderChevron
import com.salat.uikit.component.SettingsDefaults
import com.salat.uikit.theme.AppTheme

private val TileStartPadding = 16.dp
private val TileSize = 40.dp
private val TileTextGap = 18.dp
val GroupButtonTextStart = TileStartPadding + TileSize + TileTextGap

private val IconTileShape = RoundedCornerShape(12.dp)
private const val TILE_ICON_SCALE = .8f

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
        .clickable(onClick = onClick)
        .padding(horizontal = TileStartPadding),
    verticalAlignment = Alignment.CenterVertically
) {
    Box(
        modifier = Modifier
            .size(TileSize)
            .clip(IconTileShape)
            .background(AppTheme.colors.contentAccent.copy(.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size((iconSize * TILE_ICON_SCALE).dp),
            painter = icon,
            tint = AppTheme.colors.settingsTitleAccent,
            contentDescription = null
        )
    }

    Spacer(Modifier.width(TileTextGap))

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
                color = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA),
                style = AppTheme.typography.dialogSubtitle
            )
        }
    }

    Spacer(Modifier.width(12.dp))

    RenderChevron()
}
