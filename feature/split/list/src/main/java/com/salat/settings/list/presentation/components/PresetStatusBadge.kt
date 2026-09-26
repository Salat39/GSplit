package com.salat.settings.list.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.salat.ui.rememberPainterResource
import com.salat.uikit.theme.AppTheme

internal val PresetStatusBadgeSize = 20.dp

@Composable
internal fun PresetStatusBadge(@DrawableRes iconRes: Int) = Box(
    modifier = Modifier
        .size(PresetStatusBadgeSize)
        .clip(CircleShape)
        .background(AppTheme.colors.contentPrimary.copy(.08f)),
    contentAlignment = Alignment.Center
) {
    Icon(
        painter = rememberPainterResource(iconRes),
        contentDescription = null,
        tint = AppTheme.colors.contentPrimary.copy(.8f),
        modifier = Modifier.size(11.dp)
    )
}
